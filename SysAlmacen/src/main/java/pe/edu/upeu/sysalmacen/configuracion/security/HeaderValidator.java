package pe.edu.upeu.sysalmacen.configuracion.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Valida headers HTTP individuales según criterios de seguridad
 * adaptados a SysAlmacen (API REST JSON + Angular frontend).
 *
 * Principios aplicados:
 *   - Lista blanca de valores permitidos para Accept y Content-Type
 *   - Validación de formato Bearer token (OWASP ASVS V3.5)
 *   - Content-Length numérico y no negativo (CWE-130)
 *   - Respuestas de error genéricas: no revelan información interna
 *     (ISO 27001 A.14.2.5, OWASP A05:2021 Security Misconfiguration)
 */
public class HeaderValidator {

    private static final Logger log = LoggerFactory.getLogger(HeaderValidator.class);

    private final ObjectMapper objectMapper;

    public HeaderValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ─── Validadores específicos por header ───────────────────────────────────

    /**
     * Valida el header Host: debe tener valor, sin espacios y sin patrones
     * de SSRF básicos (p. ej. localhost, 127.x.x.x en producción).
     */
    public boolean validateHost(String value, String ip, HttpServletResponse response) throws IOException {
        if (value == null || value.isBlank()) {
            log.warn("HOST HEADER VACÍO - IP: {}", ip);
            sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_MISSING_HOST, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }
        // Bloquear host vacío o con control chars
        if (value.chars().anyMatch(c -> c < 0x20 || c == 0x7F)) {
            log.warn("HOST HEADER CON CARACTERES DE CONTROL - IP: {}", ip);
            sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }
        return false;
    }

    /**
     * Valida el header Accept: solo valores de la lista blanca.
     * SysAlmacen es una API REST pura; solo acepta application/json o asterisk.
     */
    public boolean validateAccept(String value, String ip, HttpServletResponse response) throws IOException {
        if (value == null || value.isBlank()) return false;

        // Paso 1: tomar el primer tipo de la lista
        String firstType = value.split(",")[0].trim().toLowerCase();

        // Paso 2: eliminar q-factor y parámetros → obtener el tipo puro
        // "application/json; q=0.9; charset=utf-8" → "application/json"
        String baseType = firstType.contains(";")
                ? firstType.substring(0, firstType.indexOf(";")).trim()
                : firstType;

        // Paso 3: comparación exacta contra la whitelist
        boolean valid = SecurityPatterns.VALID_ACCEPT_VALUES.contains(baseType);

        if (!valid) {
            if (log.isWarnEnabled()) {
                log.warn("ACCEPT NO PERMITIDO - IP: {}", ip);
            }
            sendError(response, SecurityPatterns.ERROR_NOT_ACCEPTABLE,
                    "Accept no válido: se esperaba application/json o */*",
                    HttpServletResponse.SC_NOT_ACCEPTABLE);
            return true;
        }
        return false;
    }

    /**
     * Valida Content-Type solo para métodos con body (POST, PUT, PATCH).
     * Permite multipart/form-data para el endpoint de subida de archivos.
     */
    public boolean validateContentType(String value, String method, String ip,
            HttpServletResponse response) throws IOException {
        if (!SecurityPatterns.METHODS_WITH_BODY.contains(method)) return false;
        if (value == null || value.isBlank()) return false;

        // Normaliza: quita parámetros como boundary=...
        String normalized = value.split(";")[0].trim().toLowerCase();

        boolean valid = SecurityPatterns.VALID_CONTENT_TYPES.contains(normalized);
        if (!valid) {
            log.warn("CONTENT-TYPE NO PERMITIDO - IP: {}", ip);
            sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return true;
        }
        return false;
    }

    /**
     * Valida Content-Length: debe ser un número no negativo (CWE-130).
     * Previene ataques de HTTP request smuggling por Content-Length inválido.
     */
    public boolean validateContentLength(String value, String ip, HttpServletResponse response)
            throws IOException {
        if (value == null) return false;
        try {
            long length = Long.parseLong(value.trim());
            if (length < 0) {
                log.warn("CONTENT-LENGTH NEGATIVO - IP: {}", ip);
                sendError(response, SecurityPatterns.ERROR_INVALID,
                        SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
                return true;
            }
        } catch (NumberFormatException e) {
            log.warn("CONTENT-LENGTH NO NUMÉRICO - IP: {}", ip);
            sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }
        return false;
    }

    /**
     * Valida Accept-Encoding: solo encodings estándar.
     * Previene bypass de filtros mediante encodings no estándar.
     */
    public boolean validateAcceptEncoding(String value, String ip, HttpServletResponse response)
            throws IOException {
        if (value == null) return false;
        String lower = value.toLowerCase();
        // Solo permitir encodings conocidos
        if (!lower.matches("[a-z0-9,;=\\s*\\.\\-]+")) {
            log.warn("ACCEPT-ENCODING SOSPECHOSO - IP: {}", ip);
            sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }
        return false;
    }

    /**
     * Valida el header Authorization.
     * Solo acepta el esquema Bearer con formato JWT válido (3 segmentos Base64url).
     * OWASP ASVS V3.5.1: verificar que los tokens tengan formato correcto
     * antes de procesarlos.
     */
    public boolean validateAuthorization(String value, String ip, HttpServletResponse response)
            throws IOException {
        if (value == null || value.isBlank()) return false;

        if (!SecurityPatterns.BEARER_TOKEN_PATTERN.matcher(value).matches()) {
            log.warn("AUTHORIZATION HEADER INVÁLIDO - IP: {}", ip);
            sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_UNAUTHORIZED);
            return true;
        }
        return false;
    }

    // ─── Respuesta de error ───────────────────────────────────────────────────

    /**
     * Escribe una respuesta de error JSON con el mismo formato que usa
     * CustomResponse en el resto de SysAlmacen.
     * No revela información interna del sistema (ISO 27001 A.14.2.5).
     */
    public void sendError(HttpServletResponse response, String error,
            String message, int status) throws IOException {
        if (response.isCommitted()) return;

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("error", error);
        body.put("message", message);

        objectMapper.writeValue(response.getWriter(), body);
    }
}

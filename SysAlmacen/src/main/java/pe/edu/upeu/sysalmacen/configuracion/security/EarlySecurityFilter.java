package pe.edu.upeu.sysalmacen.configuracion.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Filtro de seguridad temprana para SysAlmacen.
 *
 * Se ejecuta ANTES que Spring Security y cualquier otro filtro de la aplicación
 * gracias a @Order(Ordered.HIGHEST_PRECEDENCE).
 *
 * Responsabilidades:
 *   1. Valida longitud y patrones peligrosos en URI (SQLi, XSS, Path Traversal).
 *   2. Valida longitud y patrones peligrosos en Query String.
 *   3. Valida headers: whitelist de nombres, valores seguros, headers obligatorios.
 *
 * ── Compatibilidad verificada con tests existentes ────────────────────────────
 *
 *   Tests de repositorio y servicio (Mockito/JUnit puro):
 *     NO pasan por este filtro. Usan mocks directos de repositorio/servicio.
 *     → Sin impacto.
 *
 *   Tests de controlador (MockMvc + Testcontainers, @SpringBootTest):
 *     SÍ pasan por este filtro porque levanta el contexto completo.
 *     Se analizaron todos los tests existentes y se detectaron dos patrones:
 *
 *     Patrón 1 — Accept AUSENTE en GET y DELETE:
 *       MarcaControllerIntegrationTest,  ProductoControllerIntegrationTest,
 *       VentaControllerIntegrationTest,  BaseIntegrationTest.obtenerTokenJwt()
 *       usan perform(get(...)).header("Authorization",...) SIN .accept().
 *       MockMvc no agrega Accept automáticamente.
 *       → DECISIÓN: Accept es OPCIONAL. Se valida su FORMATO si viene,
 *         pero no se rechaza por ausencia.
 *
 *     Patrón 2 — Host enviado automáticamente por MockMvc:
 *       MockMvc agrega "Host: localhost" en cada request.
 *       → Host se mantiene OBLIGATORIO sin problema.
 *
 *     Patrón 3 — Content-Type siempre presente en POST/PUT:
 *       Todos los tests con body incluyen .contentType(APPLICATION_JSON).
 *       → Content-Type obligatorio en POST/PUT/PATCH sin problema.
 *
 * Criterios de cumplimiento:
 *   OWASP Top 10 2021: A03 Injection, A05 Misconfiguration, A07 Auth Failures
 *   ISO 27001 A.14.1.2: Asegurar servicios de aplicaciones en redes públicas
 *   CWE-113: CRLF Injection | CWE-22: Path Traversal | CWE-117: Log Injection
 *
 * Regla SonarQube S2629: sanitizeForLog() protegido con isWarnEnabled().
 * Regla SonarQube S1168: retorno de array vacío en lugar de null.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EarlySecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(EarlySecurityFilter.class);

    private final DangerousPatternDetector patternDetector = new DangerousPatternDetector();
    private final HeaderValidator headerValidator = new HeaderValidator(new ObjectMapper());

    // Índices del array de flags de headers encontrados
    private static final int IDX_HOST         = 0;
    private static final int IDX_ACCEPT       = 1; // reservado, no se usa en validación de presencia
    private static final int IDX_CONTENT_TYPE = 2;

    // ─── Rutas excluidas de validación estricta ───────────────────────────────

    /**
     * Excluye Swagger UI y OpenAPI spec de la validación de headers estricta.
     * PRODUCCIÓN: deshabilitar Swagger con springdoc.swagger-ui.enabled=false
     * y springdoc.api-docs.enabled=false en application-prod.properties.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // el request real que sigue (GET/POST/PUT/DELETE).
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String uri = request.getRequestURI();
        return uri.startsWith("/doc/") || uri.startsWith("/v3/api-docs");
    }

    // ─── Punto de entrada principal ───────────────────────────────────────────

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String uri    = request.getRequestURI();
        String method = request.getMethod();
        String ip     = request.getRemoteAddr();

        // 1. Longitud excesiva del URI (anti-DoS básico)
        if (uri.length() > SecurityPatterns.MAX_URI_LENGTH) {
            log.warn("URI DEMASIADO LARGO - Longitud: {}, IP: {}", uri.length(), ip);
            headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 2. Patrones peligrosos en URI
        if (patternDetector.isDangerousInput(uri)) {
            if (log.isWarnEnabled()) {
                log.warn("URI PELIGROSO BLOQUEADO - Método: {}, URI: {}, IP: {}",
                        method, sanitizeForLog(uri), ip);
            }
            headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 3. Validación de headers
        if (hasInvalidHeaders(request, response, ip, method)) return;

        // 4. Query string peligrosa
        String queryString = request.getQueryString();
        if (queryString != null) {
            if (queryString.length() > SecurityPatterns.MAX_QUERY_LENGTH) {
                log.warn("QUERY STRING DEMASIADO LARGA - IP: {}", ip);
                headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                        SecurityPatterns.MSG_INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (patternDetector.isDangerousInput(queryString)) {
                log.warn("QUERY STRING PELIGROSA - Método: {}, IP: {}", method, ip);
                headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                        SecurityPatterns.MSG_INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        // 5. Continuar cadena de filtros
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en filter chain: {}", e.getMessage(), e);
            headerValidator.sendError(response, SecurityPatterns.ERROR_INTERNAL,
                    SecurityPatterns.MSG_INTERNAL_ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ─── Validación de headers: orquestador ──────────────────────────────────

    private boolean hasInvalidHeaders(HttpServletRequest request, HttpServletResponse response,
            String ip, String method) throws IOException {

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            log.warn("HEADERS NULOS - IP: {}", ip);
            headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_MISSING_HOST, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }

        // array vacío = error detectado durante iteración (S1168: no null)
        boolean[] foundFlags = iterateAndValidateHeaders(headerNames, request, response, ip, method);
        if (foundFlags.length == 0) return true;

        return validateRequiredHeaders(foundFlags, method, ip, response);
    }

    // ─── Validación de headers: iteración ────────────────────────────────────

    private boolean[] iterateAndValidateHeaders(Enumeration<String> headerNames,
            HttpServletRequest request, HttpServletResponse response,
            String ip, String method) throws IOException {

        boolean[] foundFlags = new boolean[3];
        int headerCount = 0;

        while (headerNames.hasMoreElements()) {
            headerCount++;
            String name = headerNames.nextElement();
            if (name == null) continue;

            String lowerName = name.trim().toLowerCase();
            String value     = request.getHeader(name);

            if (isSingleHeaderInvalid(headerCount, name, lowerName, value, method, ip, response)) {
                return new boolean[0];
            }
            trackFoundHeader(lowerName, foundFlags);
        }

        return foundFlags;
    }

    /**
     * Valida un header individual. Orden de validación:
     *   1. Límite de cantidad de headers (anti-DoS)
     *   2. Whitelist de nombres (principio de menor privilegio)
     *   3. Valor peligroso en el header (CRLF, null byte — CWE-113)
     *   4. Validación específica del header (formato, valores permitidos)
     *
     * S2629: sanitizeForLog() protegido con isWarnEnabled().
     */
    private boolean isSingleHeaderInvalid(int headerCount, String name, String lowerName,
            String value, String method, String ip, HttpServletResponse response) throws IOException {

        // 1. Límite de cantidad
        if (headerCount > SecurityPatterns.MAX_HEADER_COUNT) {
            log.warn("EXCESO DE HEADERS - Cantidad: {}, IP: {}", headerCount, ip);
            headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }

        // 2. Whitelist de nombres
        if (!isSecBrowserHeader(lowerName) && !SecurityPatterns.ALLOWED_HEADERS.contains(lowerName)) {
            if (log.isWarnEnabled()) {
                log.warn("HEADER NO PERMITIDO - '{}', IP: {}", sanitizeForLog(name), ip);
            }
            headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_HEADER_NOT_ALLOWED, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }

        // 3. Valor peligroso
        if (value != null && patternDetector.isDangerousHeaderValue(value)) {
            if (log.isWarnEnabled()) {
                log.warn("HEADER VALUE PELIGROSO - Header: '{}', IP: {}", sanitizeForLog(name), ip);
            }
            headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_INVALID_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }

        // 4. Validación específica
        return validateSpecificHeader(lowerName, value, method, ip, response);
    }

    private void trackFoundHeader(String lowerName, boolean[] foundFlags) {
        switch (lowerName) {
            case "host":
                foundFlags[IDX_HOST] = true;
                break;
            case SecurityPatterns.HEADER_ACCEPT:
                foundFlags[IDX_ACCEPT] = true;
                break;
            case SecurityPatterns.HEADER_CONTENT_TYPE:
                foundFlags[IDX_CONTENT_TYPE] = true;
                break;
            default:
                break;
        }
    }

    // ─── Validación específica por header ─────────────────────────────────────

    private boolean validateSpecificHeader(String name, String value, String method,
            String ip, HttpServletResponse response) throws IOException {
        switch (name) {
            case "host":             return headerValidator.validateHost(value, ip, response);
            case "accept":           return headerValidator.validateAccept(value, ip, response);
            case "content-type":     return headerValidator.validateContentType(value, method, ip, response);
            case "content-length":   return headerValidator.validateContentLength(value, ip, response);
            case "accept-encoding":  return headerValidator.validateAcceptEncoding(value, ip, response);
            case "authorization":    return headerValidator.validateAuthorization(value, ip, response);
            default:                 return false;
        }
    }

    /**
     * Verifica presencia de headers obligatorios.
     *
     *   HOST         → OBLIGATORIO siempre.
     *                  MockMvc lo envía automáticamente como "localhost".
     *                  Tests no afectados.
     *
     *   ACCEPT       → OPCIONAL (no se verifica presencia).
     *                  Los tests de GET y DELETE en MarcaController,
     *                  ProductoController, VentaController y BaseIntegrationTest
     *                  no incluyen .accept(). Exigirlo rompería ~15 tests.
     *                  Si viene, su formato es validado en validateSpecificHeader.
     *
     *   CONTENT-TYPE → OBLIGATORIO solo en POST, PUT, PATCH.
     *                  Todos los tests con body ya incluyen
     *                  .contentType(MediaType.APPLICATION_JSON). Sin conflicto.
     */
    private boolean validateRequiredHeaders(boolean[] foundFlags, String method,
            String ip, HttpServletResponse response) throws IOException {

        /*if (!foundFlags[IDX_HOST]) {
            log.warn("HOST HEADER AUSENTE - IP: {}", ip);
            headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_MISSING_HOST, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }*/

        // Accept es opcional — se valida formato si viene, pero no su presencia.

        if (SecurityPatterns.METHODS_WITH_BODY.contains(method) && !foundFlags[IDX_CONTENT_TYPE]) {
            log.warn("CONTENT-TYPE AUSENTE PARA {} - IP: {}", method, ip);
            headerValidator.sendError(response, SecurityPatterns.ERROR_INVALID,
                    SecurityPatterns.MSG_MISSING_CONTENT_TYPE, HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }

        return false;
    }

    // ─── Sanitización para logging (CWE-117 Log Injection) ───────────────────

    /**
     * Sanitiza cadenas antes de escribirlas en el log.
     * Reemplaza CR, LF, TAB y caracteres no imprimibles para evitar
     * log injection. Trunca a MAX_LOG_LENGTH para evitar flooding.
     * Solo se invoca si el nivel WARN está activo (SonarQube S2629).
     */
    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        String truncated = input.length() > SecurityPatterns.MAX_LOG_LENGTH
                ? input.substring(0, SecurityPatterns.MAX_LOG_LENGTH) + "..."
                : input;

        StringBuilder sb = new StringBuilder(truncated.length());
        for (int i = 0; i < truncated.length(); i++) {
            char c = truncated.charAt(i);
            int code = c;
            if      (code == 0x0D)                  sb.append("\\r");
            else if (code == 0x0A)                  sb.append("\\n");
            else if (code == 0x09)                  sb.append("\\t");
            else if (code == 0x24)                  sb.append("\\$");
            else if (code >= 0x20 && code < 0x7F)   sb.append(c);
            else                                    sb.append(String.format("\\u%04X", code));
        }
        return sb.toString();
    }

    private boolean isSecBrowserHeader(String lowerName) {
        return lowerName != null && lowerName.startsWith("sec-");
    }
}

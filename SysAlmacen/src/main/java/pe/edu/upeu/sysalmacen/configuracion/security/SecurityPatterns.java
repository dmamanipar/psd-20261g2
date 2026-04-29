package pe.edu.upeu.sysalmacen.configuracion.security;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Constantes y patrones de seguridad centralizados para SysAlmacen.
 *
 * Criterios aplicados:
 *   - OWASP Top 10 2021: A03 Injection, A07 Auth Failures
 *   - ISO 27001 A.14.1.2: Asegurar servicios de aplicaciones en redes públicas
 *   - CWE-113: Improper Neutralization of CRLF Sequences in HTTP Headers
 *   - CWE-116: Improper Encoding or Escaping of Output (log injection)
 */
public final class SecurityPatterns {

    private SecurityPatterns() { }

    // ─── Límites de tamaño ────────────────────────────────────────────────────

    /** Máximo de headers HTTP permitidos por request (OWASP). */
    public static final int MAX_HEADER_COUNT = 30;

    /** Longitud máxima de un valor de header. */
    public static final int MAX_HEADER_VALUE_LENGTH = 2048;

    /** Longitud máxima del URI completo. */
    public static final int MAX_URI_LENGTH = 512;

    /** Longitud máxima de la query string. */
    public static final int MAX_QUERY_LENGTH = 1024;

    /** Longitud máxima a preservar en logs (anti log-flooding). */
    public static final int MAX_LOG_LENGTH = 200;

    // ─── Headers ─────────────────────────────────────────────────────────────

    public static final String HEADER_ACCEPT       = "accept";
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String HEADER_AUTHORIZATION = "authorization";

    /**
     * Whitelist de headers HTTP aceptados.
     * Cualquier header fuera de esta lista es rechazado (principio de lista blanca).
     * Ajusta según lo que realmente use tu frontend Angular.
     */
    /**
     * Whitelist de headers HTTP aceptados.
     *
     * Incluye los headers estándar de Angular HttpClient más los headers
     * que Chrome y Firefox agregan automáticamente en TODAS las peticiones
     * (sec-fetch-*, sec-ch-ua-*). Sin estos, cualquier request desde un
     * navegador moderno sería bloqueado con 400.
     *
     * Referencias:
     *   - Fetch Metadata Request Headers (W3C): sec-fetch-site, sec-fetch-mode,
     *     sec-fetch-dest, sec-fetch-user
     *   - Client Hints (RFC 8942): sec-ch-ua, sec-ch-ua-mobile, sec-ch-ua-platform
     */
    public static final Set<String> ALLOWED_HEADERS = Set.of(
            // ── Obligatorios / comunes ────────────────────────────────────────
            "host",
            "accept",
            "accept-encoding",
            "accept-language",
            "content-type",
            "content-length",
            "authorization",
            "origin",
            "referer",
            "user-agent",
            "x-requested-with",
            "cache-control",
            "connection",
            "pragma",
            "keep-alive",
            // ── Fetch Metadata: Chrome/Firefox los envían en TODA petición ────
            /*"sec-fetch-site",
            "sec-fetch-mode",
            "sec-fetch-dest",
            "sec-fetch-user",*/
            // ── Client Hints: Chrome los envía desde v89+ ─────────────────────
            /*"sec-ch-ua",
            "sec-ch-ua-mobile",
            "sec-ch-ua-platform",*/
            // ── CORS preflight (OPTIONS desde Angular) ────────────────────────
            "access-control-request-method",
            "access-control-request-headers"
    );

    /**
     * Métodos HTTP que deben incluir Content-Type en el body.
     */
    public static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");

    /**
     * Valores de Accept aceptados.
     *
     * Incluye application/json (API REST), asterisk (cliente genérico),
     * application/octet-stream para /reporte/generateReport y /reporte/readFile,
     * y text/plain por compatibilidad con clientes HTTP básicos.
     */
    public static final Set<String> VALID_ACCEPT_VALUES = Set.of(
            "application/json",
            "application/json;charset=utf-8",
            "application/json; charset=utf-8",
            "application/octet-stream",
            "text/plain",
            "text/plain;charset=utf-8",
            "*/*"
    );

    /**
     * Valores de Content-Type aceptados.
     * Incluye multipart para el endpoint de subida de imágenes (/reporte/saveFile).
     */
    public static final Set<String> VALID_CONTENT_TYPES = Set.of(
            "application/json",
            "application/json;charset=utf-8",
            "application/json; charset=utf-8",
            "multipart/form-data",
            "application/octet-stream"
    );

    // ─── Patrones peligrosos ──────────────────────────────────────────────────

    /**
     * Patrones que detectan intentos de inyección en URI y query string.
     * OWASP A03:2021 - Injection (SQLi, XSS, Path Traversal, Command Injection).
     */
    public static final Pattern DANGEROUS_URI_PATTERN = Pattern.compile(
            "(?i)(" +
            // SQL Injection básico
            "('|--|;|\\bunion\\b|\\bselect\\b|\\bdrop\\b|\\binsert\\b|\\bdelete\\b|\\bupdate\\b|\\bexec\\b)|" +
            // XSS
            "(<script|javascript:|vbscript:|on\\w+=|<iframe|<object|<embed)|" +
            // Path traversal
            "(\\.\\./|\\.\\./\\./|%2e%2e|%252e)|" +
            // Command injection
            "(\\$\\{|\\$\\(|`|\\|\\||&&)|" +
            // SSRF / header injection
            "(%0[aAdD]|\\r|\\n)" +
            ")"
    );

    /**
     * Patrones peligrosos en valores de headers.
     * Detecta CRLF injection (CWE-113) y header splitting.
     */
    public static final Pattern DANGEROUS_HEADER_PATTERN = Pattern.compile(
            "(?i)(" +
            // CRLF injection
            "(%0[aAdD]|\\r\\n|\\r|\\n)|" +
            // Null byte
            "%00|\\u0000|" +
            // Script injection en headers
            "(<script|javascript:)" +
            ")"
    );

    /**
     * Formato esperado del header Authorization: "Bearer <token_jwt>".
     * El token JWT tiene 3 partes en Base64url separadas por puntos.
     */
    public static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "^Bearer\\s+[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+$"
    );

    /**
     * Segmentos de URI que indican acceso a recursos del sistema operativo
     * o archivos de configuración sensibles.
     */
    public static final Set<String> BLOCKED_URI_SEGMENTS = Set.of(
            "/etc/", "/proc/", "/sys/", "/.env",
            "/wp-admin", "/wp-login", "/.git",
            "/actuator", "/admin", "/console",
            "/.htaccess", "/web.config"
    );

    // ─── Mensajes de error (genéricos, sin revelar detalles internos) ─────────

    public static final String ERROR_INVALID  = "INVALID_REQUEST";
    public static final String ERROR_INTERNAL = "INTERNAL_ERROR";
    public static final String ERROR_NOT_ACCEPTABLE = "NOT_ACCEPTABLE";

    public static final String MSG_INVALID_REQUEST    = "Solicitud no válida";
    public static final String MSG_INVALID_PARAMS     = "Parámetros no válidos";
    public static final String MSG_MISSING_HOST       = "Header Host requerido";
    public static final String MSG_MISSING_ACCEPT     = "Header Accept requerido";
    public static final String MSG_MISSING_CONTENT_TYPE = "Header Content-Type requerido";
    public static final String MSG_HEADER_NOT_ALLOWED = "Header no permitido";
    public static final String MSG_INTERNAL_ERROR     = "Error interno del servidor";
}

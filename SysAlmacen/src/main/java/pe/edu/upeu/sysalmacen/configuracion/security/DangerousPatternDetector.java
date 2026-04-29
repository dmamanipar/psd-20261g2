package pe.edu.upeu.sysalmacen.configuracion.security;

/**
 * Detecta patrones peligrosos en valores de entrada (URI, query string, headers).
 *
 * Esta clase es stateless e instanciable directamente (sin Spring context)
 * para facilitar su uso en filtros de alta precedencia como EarlySecurityFilter.
 *
 * Referencias:
 *   - OWASP Testing Guide v4.2: OTG-INPVAL-001 (Reflected XSS)
 *   - OWASP Testing Guide v4.2: OTG-INPVAL-005 (SQL Injection)
 *   - CWE-113: CRLF Injection
 *   - CWE-22: Path Traversal
 */
public class DangerousPatternDetector {

    /**
     * Evalúa si un valor de URI contiene patrones de ataque conocidos.
     * Aplica decodificación previa del porcentaje para capturar intentos
     * de evasión con doble encoding (p. ej. %252e%252e).
     *
     * @param input valor a evaluar (puede ser URI o query string)
     * @return true si se detecta un patrón peligroso
     */
    public boolean isDangerousInput(String input) {
        if (input == null || input.isBlank()) return false;

        String decoded = partialUrlDecode(input);

        // Verifica contra el patrón centralizado en SecurityPatterns
        if (SecurityPatterns.DANGEROUS_URI_PATTERN.matcher(decoded).find()) return true;

        // Verifica segmentos de URI bloqueados explícitamente
        String lower = decoded.toLowerCase();
        for (String blocked : SecurityPatterns.BLOCKED_URI_SEGMENTS) {
            if (lower.contains(blocked)) return true;
        }

        return false;
    }

    /**
     * Evalúa si un valor de header contiene patrones de inyección
     * (principalmente CRLF injection y null-byte injection).
     *
     * @param value valor del header a evaluar
     * @return true si se detecta un patrón peligroso
     */
    public boolean isDangerousHeaderValue(String value) {
        if (value == null || value.isBlank()) return false;
        if (value.length() > SecurityPatterns.MAX_HEADER_VALUE_LENGTH) return true;
        return SecurityPatterns.DANGEROUS_HEADER_PATTERN.matcher(value).find();
    }

    /**
     * Decodificación parcial de porcentaje: convierte %XX a su carácter real
     * para detectar evasiones por encoding, sin lanzar excepciones en cadenas
     * malformadas. Se aplica una sola pasada (no recursiva) para mantener
     * rendimiento en el filtro de alta precedencia.
     */
    private String partialUrlDecode(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '%' && i + 2 < input.length()) {
                char h1 = input.charAt(i + 1);
                char h2 = input.charAt(i + 2);
                if (isHexDigit(h1) && isHexDigit(h2)) {
                    int decoded = (Character.digit(h1, 16) << 4) | Character.digit(h2, 16);
                    sb.append((char) decoded);
                    i += 3;
                    continue;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}

package cl.duoc.barberia.service;

/** Validacion del RUT chileno con digito verificador (modulo 11). */
public final class RutUtil {

    private RutUtil() {
    }

    public static boolean esValido(String rut) {
        if (rut == null || !rut.matches("^\\d{7,8}-[\\dkK]$")) {
            return false;
        }
        String[] partes = rut.split("-");
        String cuerpo = partes[0];
        char dv = Character.toUpperCase(partes[1].charAt(0));
        return calcularDv(cuerpo) == dv;
    }

    static char calcularDv(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return '0';
        if (resto == 10) return 'K';
        return (char) ('0' + resto);
    }

    public static String normalizar(String rut) {
        return rut == null ? null : rut.trim().toUpperCase();
    }
}

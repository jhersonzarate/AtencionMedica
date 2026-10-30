package CentroMedico.MicroVistaHistorialClinico;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// Pase firmado de corta vida: lleva al médico de una vista a otra (cada vista es una app con su propia sesión).
// Formato: base64url(datos) + "." + base64url(HMAC-SHA256(datos)). Es idéntico en todas las vistas.
public final class TokenMedico {

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder D64 = Base64.getUrlDecoder();

    private TokenMedico() { }

    public static String crear(Medico m, String secreto, long segundosDeVida) {
        long vence = Instant.now().getEpochSecond() + segundosDeVida;
        String datos = m.idMedico() + "\n" + m.nom() + "\n" + m.especialidad() + "\n" + vence;
        byte[] cuerpo = datos.getBytes(StandardCharsets.UTF_8);
        return B64.encodeToString(cuerpo) + "." + B64.encodeToString(firmar(cuerpo, secreto));
    }

    // Devuelve el médico si la firma es válida y no venció; null en cualquier otro caso.
    public static Medico leer(String token, String secreto) {
        try {
            if (token == null) return null;
            int punto = token.indexOf('.');
            if (punto < 1) return null;
            byte[] cuerpo = D64.decode(token.substring(0, punto));
            byte[] firma = D64.decode(token.substring(punto + 1));
            if (!MessageDigest.isEqual(firma, firmar(cuerpo, secreto))) return null;
            String[] p = new String(cuerpo, StandardCharsets.UTF_8).split("\n", -1);
            if (p.length != 4) return null;
            if (Instant.now().getEpochSecond() > Long.parseLong(p[3])) return null;
            return new Medico(Long.valueOf(p[0]), p[1], p[2]);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static byte[] firmar(byte[] cuerpo, String secreto) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(cuerpo);
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
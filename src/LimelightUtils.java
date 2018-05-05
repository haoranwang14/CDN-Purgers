import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpMessage;

public class LimelightUtils {
    private static String HMAC_ALGORITHM = "HmacSHA256";

    public static void addSecurityHeaders(final HttpMessage httpRequest, final String url, final String queryString, final String postBody,
            final Long timestamp) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException,
            UnsupportedEncodingException, DecoderException {
        final String meth = httpRequest.toString().split("\\s")[0];
        final byte[] rawHmac = generateMac(ACCESS_TOKEN, meth, url, queryString, postBody, timestamp);
        final String hmac = new String(Hex.encodeHex(rawHmac));

        httpRequest.setHeader("X-LLNW-Security-Principal", SECURITY_PRINCIPAL);
        httpRequest.setHeader("X-LLNW-Security-Timestamp", String.valueOf(timestamp));
        httpRequest.setHeader("X-LLNW-Security-Token", hmac);
    }

    private static byte[] generateMac(String sharedKey, String httpMethod, String url, String queryString,
            String postBody, Long timestamp) throws NoSuchAlgorithmException, InvalidKeyException,
            IllegalStateException, UnsupportedEncodingException, DecoderException {

        byte[] decodedSharedKey = Hex.decodeHex(sharedKey.toCharArray());
        String dataString;
        if (queryString == null) {
            dataString = httpMethod.toUpperCase() + url + timestamp;
        } else {
            dataString = httpMethod.toUpperCase() + url + queryString + timestamp;
        }
        if (postBody != null) {
            dataString = dataString + postBody;
        }
        SecretKeySpec keySpec = new SecretKeySpec(decodedSharedKey, HMAC_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.reset();
        mac.init(keySpec);
        return mac.doFinal(dataString.getBytes("UTF-8"));
    }
}

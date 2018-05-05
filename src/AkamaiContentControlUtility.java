import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.ClientCredential.ClientCredentialBuilder;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridInterceptor;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridRoutePlanner;
import com.amazonaws.services.devicefarm.model.ArgumentException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.roblox.webcore.akamai.CcuPurgePayload;


public class AkamaiContentControlUtility {

    private static final Gson GSON = new GsonBuilder().create();

    public static void main(String[] args) throws ClientProtocolException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // https://api.ccu.akamai.com/ccu/v2/docs/index.html
        final List<String> newArgs = new ArrayList<String>();
        
        if (args.length == 0) {
            throw new ArgumentException("must have url's to purge");
        }

        for (String s : args) {
            final Triple<Integer, String, String> cdnInfo = CdnUtils.parseRobloxCdnResource(s);

            if (cdnInfo != null) {
                newArgs.add(String.format("https://c%dak.rbxcdn.com/%s", cdnInfo.getLeft(), cdnInfo.getRight()));
            }
        }
        
        final ClientCredentialBuilder builder = new ClientCredentialBuilder();
        final ClientCredential clientCredential = builder.accessToken(ACCESS_TOKEN).clientToken(CLIENT_TOKEN)
                .clientSecret(CLIENT_SECRET).host(BASE_URL).build();

        final HttpClient client = HttpClientBuilder.create()    //.setSSLSocketFactory(sslsf)
                .addInterceptorFirst(new ApacheHttpClientEdgeGridInterceptor(clientCredential))
                .setRoutePlanner(new ApacheHttpClientEdgeGridRoutePlanner(clientCredential)).build();

        final HttpPost postRequest = new HttpPost(String.format("https://%s/ccu/v2/queues/default", BASE_URL));
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-Type", "application/json");
        
        final CcuPurgePayload purgeData = new CcuPurgePayload(newArgs.toArray(new String[newArgs.size()]));
        String json = GSON.toJson(purgeData);
        final StringEntity entity = new StringEntity(json);
        postRequest.setEntity(entity);
        
        final HttpResponse response = client.execute(postRequest);
        json = EntityUtils.toString(response.getEntity());
        System.out.println(json);
    }
}

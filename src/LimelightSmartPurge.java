import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.amazonaws.services.devicefarm.model.ArgumentException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class LimelightSmartPurge {
    public static final String BASE_URL = "purge.llnw.com";

    private static final Gson GSON = new GsonBuilder().create();

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException,
            DecoderException, ClientProtocolException, IOException {
        final List<String> newArgs = new ArrayList<String>();

        if (args.length == 0) {
            throw new ArgumentException("must have url's to purge");
        }

        for (String s : args) {
            final Triple<Integer, String, String> cdnInfo = CdnUtils.parseRobloxCdnResource(s);

            if (cdnInfo != null) {
                newArgs.add(String.format("https://c%dll.rbxcdn.com/%s", cdnInfo.getLeft(), cdnInfo.getRight()));
            }
        }

        PurgeRequest pr = new PurgeRequest();
        pr.setUrls(newArgs.toArray(new String[newArgs.size()]));
        pr.setEmail("email", "purging data");

        final HttpClient client = HttpClientBuilder.create().build();
        final String url = String.format("https://%s/purge/v1/account/%s/requests", BASE_URL, "...");
        final Long timestamp = System.currentTimeMillis();

        final HttpPost purgeRequest = new HttpPost(url);
        String json = GSON.toJson(pr);
        final StringEntity entity = new StringEntity(json);
        purgeRequest.setEntity(entity);

        LimelightUtils.addSecurityHeaders(purgeRequest, url, null, json, timestamp);
        purgeRequest.setHeader("Accept", "application/json");
        purgeRequest.setHeader("Content-Type", "application/json");

        final HttpResponse response = client.execute(purgeRequest);
        json = EntityUtils.toString(response.getEntity());
        final TrafficMeasureResultList trafficResult = GSON.fromJson(json, TrafficMeasureResultList.class);
    }
}

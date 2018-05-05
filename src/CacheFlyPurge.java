
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amazonaws.services.devicefarm.model.ArgumentException;


public class CacheFlyPurge {
    // cachefly has an API key for each bucket, which is considered a subaccount

    private static final String BASE_URL = "api.cachefly.com/1.0/purge.purge.file";

    public static void main(String[] args) throws ParseException, IOException {
        // https://cachefly.zendesk.com/hc/en-us/articles/215068646-Reverse-Proxy-Purge-API
        
    	if (args.length == 0) {
            throw new ArgumentException("must have url's to purge");
        }
        
        for (String s : args) {
            final Triple<Integer, String, String> cdnInfo = CdnUtils.parseRobloxCdnResource(URL);
            if (cdnInfo != null) {
                final Integer bucket = cdnInfo.getLeft();
                
                String rsrc = "/" + cdnInfo.getRight();
                rsrc = Base64.getEncoder().encodeToString(rsrc.getBytes(StandardCharsets.UTF_8));
                System.out.println(rsrc);
                final HttpClient client = HttpClientBuilder.create().build();
                final HttpPost postRequest = new HttpPost(String.format("https://%s@%s", APIKEY, BASE_URL));
                final List<NameValuePair> parameters = Arrays.asList(new BasicNameValuePair("files", rsrc));
                System.out.println(parameters);
                postRequest.setEntity(new UrlEncodedFormEntity(parameters));
                System.out.println(new UrlEncodedFormEntity(parameters));
                final HttpResponse response = client.execute(postRequest);
                final String json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
            }
        }
    }
}
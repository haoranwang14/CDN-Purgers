import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.amazonaws.services.devicefarm.model.ArgumentException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class HighwindsCdnPurge {
    private static final Gson GSON = new GsonBuilder().create();

    public static void main(String[] args) throws ParseException, IOException {
        // https://support.highwinds.com/customer/en/portal/articles/2559141-purge-via-api?b_id=15425
        // https://confluence.roblox.com/display/PLATFORM/CDN#CDN-CDNendpointsandOrigin
        final List<String> newArgs = new ArrayList<String>();
        
        
        if (args.length == 0) {
            throw new ArgumentException("must have url's to purge");
        }

        for (String s : args) {
            final Triple<Integer, String, String> cdnInfo = CdnUtils.parseRobloxCdnResource(URL);

            if (cdnInfo != null) {
                newArgs.add(String.format("https://c%dhw.rbxcdn.com/%s", cdnInfo.getLeft(), cdnInfo.getRight()));
            }
        }

        final OAuthAuthorizationResponse oauth = HighwindsAuthentication.authenticate();
        final HighwindsSimpleUser user = HighwindsAuthentication.getUser(oauth);
        final HttpClient client = HttpClientBuilder.create().build();

        final HttpPost cdnPurgePost = new HttpPost(
                String.format("https://striketracker.highwinds.com/api/accounts/%s/purge", user.accountHash));
        System.out.println(user.accountHash);
        cdnPurgePost.setHeader("Authorization", String.format("Bearer apikey"));
        cdnPurgePost.setHeader("X-Application-Id:", "...");
        cdnPurgePost.setHeader("Accept", "application/json");
        cdnPurgePost.setHeader("Content-Type", "application/json");
        cdnPurgePost.setHeader("Expect", "100-continue");

        final List<PurgePayloadEntry> entries = newArgs.stream().map(PurgePayloadEntry::new)
                .collect(Collectors.toList());
        final PurgePayload purgeData = new PurgePayload(entries);
        String json = GSON.toJson(purgeData);
        final StringEntity entity = new StringEntity(json);

        cdnPurgePost.setEntity(entity);

        final HttpResponse response = client.execute(cdnPurgePost);
        json = EntityUtils.toString(response.getEntity());
        System.out.println(json);
    }
}

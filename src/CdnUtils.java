import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.common.base.Strings;

public class CdnUtils {
    private static final Pattern CDN_FULL_RSRC_PATTERN = Pattern
            .compile("https://c(?<bucket>[01234567])(?<cdn>(hw|ll|ak|cfly)*)\\.rbxcdn\\.com/(?<rsrc>[\\da-f]{32})");

    private static final Pattern CDN_RSRC_PATTERN = Pattern.compile("/*(?<rsrc>[\\da-f]{32})");

    public static Triple<Integer, String, String> parseRobloxCdnResource(final String url) {
        Matcher m1 = CDN_FULL_RSRC_PATTERN.matcher(url);
        Matcher m2 = CDN_RSRC_PATTERN.matcher(url);

        if (m1.matches()) {
            Integer bucket = Integer.valueOf(m1.group("bucket"));
            String provider = !Strings.isNullOrEmpty(m1.group("cdn")) ? m1.group("cdn") : "";
            String rsrc = m1.group("rsrc");

            return ImmutableTriple.of(bucket, provider, rsrc);
        } else if (m2.matches()) {
            // some CDN's are ok with just passing in the resource, but if not, we need to figure out which bucket
            final HttpClient client = HttpClientBuilder.create().build();

            for (int bucket = 0; bucket < 8; bucket++) {
                try {
                    final HttpHead headRequest = new HttpHead(String.format("https://c%d.rbxcdn.com/%s", bucket, url));
                    final HttpResponse httpResp = client.execute(headRequest);
                    final Integer status = httpResp.getStatusLine().getStatusCode(); 

                    if (status == 200) {
                        return ImmutableTriple.of(bucket, null, m2.group("rsrc"));
                    }
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            // can't find
            return null;
        }

        return null;
    }
}
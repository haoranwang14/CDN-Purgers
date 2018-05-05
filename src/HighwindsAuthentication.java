import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HighwindsAuthentication {
    private static final Gson GSON = new GsonBuilder().create();

    public static OAuthAuthorizationResponse authenticate() throws ParseException, IOException {
        // https://striketracker.highwinds.com/accounts/apikey/documentation/api
        final HttpClient client = HttpClientBuilder.create().build();
        final HttpPost authPost = new HttpPost("https://striketracker.highwinds.com/auth/token");
        final List<NameValuePair> authParams = new ArrayList<NameValuePair>();

        authParams.add(new BasicNameValuePair("grant_type", "password"));
        authParams.add(new BasicNameValuePair("username", "email"));
        authParams.add(new BasicNameValuePair("password", "{D-vdSjXXi\\\\'G:}'d6nv"));

        authPost.setEntity(new UrlEncodedFormEntity(authParams));

        HttpResponse response = client.execute(authPost);
        
        String json = EntityUtils.toString(response.getEntity());
        
        final OAuthAuthorizationResponse oauth = GSON.fromJson(json, OAuthAuthorizationResponse.class);
        
        return oauth;
    }
    
    public static HighwindsSimpleUser getUser(final OAuthAuthorizationResponse oauth) throws ParseException, IOException {
        final HttpClient client = HttpClientBuilder.create().build();
        final HttpGet meGet = new HttpGet("https://striketracker.highwinds.com/api/users/me");
        
        meGet.setHeader("Authorization", String.format("Bearer apikey"));
        meGet.setHeader("X-Application-Id:", "roblox.com");
        
        HttpResponse response = client.execute(meGet);
        String json = EntityUtils.toString(response.getEntity());
        
        final HighwindsSimpleUser user = GSON.fromJson(json, HighwindsSimpleUser.class);

        return user;
    }
}

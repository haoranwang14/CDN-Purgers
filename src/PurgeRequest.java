import java.util.Arrays;
import java.util.stream.Collectors;

public class PurgeRequest {
    public String username;
    public String shortname;
    public PurgeRequestPattern[] patterns;
    public PurgeRequestEmail email;

    public void setUrls(String[] urls) {
        patterns = new PurgeRequestPattern[urls.length];
        
        for (int i = 0; i < urls.length; i++) {
            String pat = urls[i];
            
            patterns[i] = new PurgeRequestPattern();
            patterns[i].pattern = pat;
            patterns[i].evict = true;
            patterns[i].exact = true;
            patterns[i].incqs = true;
        }
    }

    public void setEmail(String t, String s) {
        email = new PurgeRequestEmail();
        email.subject = s;
        email.to = t;
    }
}

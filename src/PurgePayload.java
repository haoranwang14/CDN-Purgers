import java.util.List;

public class PurgePayload {
    public PurgePayloadEntry[] list;

    public PurgePayload(PurgePayloadEntry... resources) {
        list = resources;
    }

    public PurgePayload(List<PurgePayloadEntry> resources) {
        list = resources.stream().toArray(PurgePayloadEntry[]::new);
    }
}
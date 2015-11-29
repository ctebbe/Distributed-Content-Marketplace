package cs555.tebbe.data;

/**
 * Created by ctebbe
 */
public class ContentCache {
    public final String ID;
    public final String channel;
    public final byte[] content;
    public final double price;

    public ContentCache(String id, String channel, byte[] content, double price) {
        ID = id;
        this.channel = channel;
        this.content = content;
        this.price = price;
    }

    public ContentCache() {
        ID = "";
        this.channel = "";
        this.content = null;
        this.price = 0.0;
    }
}

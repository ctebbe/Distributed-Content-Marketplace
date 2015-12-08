package cs555.tebbe.data;

import cs555.tebbe.wireformats.PublishContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ct.
 */
public class ContentFeed {

    Map<String, List<ContentStoreData>> contentMap = new HashMap<>();

    public void update(PublishContent event) {
        String channel = event.header.getChannel();
        if(!contentMap.containsKey(channel))
            contentMap.put(channel, new ArrayList<ContentStoreData>());

        contentMap.get(channel).add(new ContentStoreData(event.contentID, event.address, event.publisher, event.price));
    }

    public String getFeed(String channel) {
        if(!contentMap.containsKey(channel))
            contentMap.put(channel, new ArrayList<ContentStoreData>());

        StringBuilder sb = new StringBuilder();
        List<ContentStoreData> feed = contentMap.get(channel);
        for(int i=0; i < feed.size(); i++) {
            sb.append(i + ". " + feed.get(i) +"\n");
        }
        return sb.toString();
    }

    public ContentStoreData getContent(String channel, int index) {
        return contentMap.get(channel).get(index);
    }

    public ContentStoreData getContent(String channel, String id) {
        List<ContentStoreData> lst = contentMap.get(channel);
        for(ContentStoreData data : lst) {
            if(data.ID.equals(id))
                return data;
        }
        return null;
    }
}

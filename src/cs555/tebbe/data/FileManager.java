package cs555.tebbe.data;

import cs555.tebbe.node.PubSubNode;
import cs555.tebbe.wireformats.StoreFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by ct.
 */
public class FileManager {
    public Map<String, List<String>> fMap = new HashMap<>();

    public void store(StoreFile event) throws IOException {
        String channel = event.getHeader().getChannel();
        if(!fMap.containsKey(channel))
            fMap.put(channel, new ArrayList<String>());

        fMap.get(channel).add(event.ID);
        File file = new File(PubSubNode.BASE_SAVE_DIR + event.ID);
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(event.bytes);
        fos.close();
    }

    public Iterator<String> getIterator(String channel) {
        return fMap.get(channel).iterator();
    }
}

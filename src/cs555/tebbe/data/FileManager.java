package cs555.tebbe.data;

import cs555.tebbe.node.PubSubNode;
import cs555.tebbe.wireformats.StoreFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        File file = new File(PubSubNode.BASE_SAVE_DIR + event.ID +"."+ channel);
        file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(event.bytes);
        fos.close();
    }

    public Iterator<String> getIterator(String channel) {
        if(!fMap.containsKey(channel))
            fMap.put(channel, new ArrayList<String>());
        return fMap.get(channel).iterator();
    }

    public String getFilesString(String channel) {
        if(!fMap.containsKey(channel))
            fMap.put(channel, new ArrayList<String>());
        StringBuilder sb = new StringBuilder();
        for(String f : fMap.get(channel)) {
            sb.append(f+";");
        }
        return sb.toString();
    }

    public byte[] get(String channel, String lookupID) {
        Path path = Paths.get(PubSubNode.BASE_SAVE_DIR + lookupID +"."+ channel);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}

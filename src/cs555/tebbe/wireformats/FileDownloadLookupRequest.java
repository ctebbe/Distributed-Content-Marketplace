package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ctebbe
 */
public class FileDownloadLookupRequest extends LookupRequest implements Event {

    protected FileDownloadLookupRequest(int protocol, NodeConnection connection, String channel, String toLookup, String myId) {
        super(protocol, connection, channel, toLookup, myId);
    }

    protected FileDownloadLookupRequest(int protocol, NodeConnection connection, String channel, String toLookup, String[] route, String id) {
        super(protocol, connection, channel, toLookup, route, id);
    }

    protected FileDownloadLookupRequest(byte[] marshalledBytes) throws IOException {
        super();
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        super.parseStream(din);

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        super.writeBytes(dout);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }
}

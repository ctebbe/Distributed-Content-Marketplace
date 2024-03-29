package cs555.tebbe.wireformats;
import cs555.tebbe.transport.NodeConnection;

import java.io.*;
public class SubscribeRequest implements Event {

    private final Header header;
    private final String nodeIdentifierRequest;

    public String getNodeIDRequest() {
        return nodeIdentifierRequest;
    }

    protected SubscribeRequest(int protocol, NodeConnection connection, String id, String channel) {
        header = new Header(protocol, connection, channel);
        if(id==null) nodeIdentifierRequest = "";
        else  nodeIdentifierRequest = id;
    }

    protected SubscribeRequest(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // targetNodeID
        int idLen = din.readInt();
        byte[] idBytes = new byte[idLen];
        din.readFully(idBytes);
        nodeIdentifierRequest = new String(idBytes);

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(header.getBytes());

        // targetNodeID
        byte[] idBytes = nodeIdentifierRequest.getBytes();
        dout.writeInt(idBytes.length);
        dout.write(idBytes);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public int getType() {
        return this.header.getType();
    }

    public Header getHeader() {
        return this.header;
    }
}

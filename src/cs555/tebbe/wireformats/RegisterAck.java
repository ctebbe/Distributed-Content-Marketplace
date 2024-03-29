package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ctebbe
 */
public class RegisterAck implements Event {

    private final Header header;
    public final String assignedID;
    public final boolean success;
    public final String randomNodeIP;

    protected RegisterAck(int protocol, NodeConnection connection, String id, boolean success, String nodeIP, String channel) {
        header = new Header(protocol, connection, channel);
        this.success = success;
        if(success) {
            this.assignedID = id;
            this.randomNodeIP = nodeIP;
        } else {
            this.assignedID = "";
            this.randomNodeIP = "";
        }
    }

    protected RegisterAck(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // targetNodeID
        int idLen = din.readInt();
        byte[] idBytes = new byte[idLen];
        din.readFully(idBytes);
        assignedID = new String(idBytes);

        // success
        success = din.readBoolean();

        // node payload
        int ipLen = din.readInt();
        byte[] ipBytes = new byte[ipLen];
        din.readFully(ipBytes);
        randomNodeIP = new String(ipBytes);

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
        byte[] idBytes = assignedID.getBytes();
        dout.writeInt(idBytes.length);
        dout.write(idBytes);

        // success
        dout.writeBoolean(success);

        // payload
        byte[] ipBytes = randomNodeIP.getBytes();
        dout.writeInt(ipBytes.length);
        dout.write(ipBytes);

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

package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;

import java.io.*;

/**
 * Created by ctebbe
 */
public class DecryptRequest implements Event {

    private final Header header;
    public String txHash;
    public final byte[] content;

    protected DecryptRequest(int protocol, NodeConnection connection, String channel, String txHash, byte[] bytes) {
        header = new Header(protocol, connection, channel);
        this.txHash = txHash;
        this.content = bytes;
    }

    protected DecryptRequest(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // ID
        int nameLen = din.readInt();
        byte[] ipBytes = new byte[nameLen];
        din.readFully(ipBytes);
        txHash = new String(ipBytes);

        // bytes
        int bLen = din.readInt();
        this.content = new byte[bLen];
        din.readFully(this.content);

        bais.close();
        din.close();
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // header
        dout.write(header.getBytes());

        // ID
        byte[] fBytes = txHash.getBytes();
        dout.writeInt(fBytes.length);
        dout.write(fBytes);

        // bytes
        dout.writeInt(content.length);
        dout.write(content);

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

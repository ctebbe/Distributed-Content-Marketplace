package cs555.tebbe.wireformats;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.util.Util;

import java.io.*;
public class Header implements Event {

    private int protocol;
    private String senderKey;
    private String receiverKey;
    private String channel;

    public Header(int protocol, NodeConnection connection) {
        this.protocol = protocol;
        this.senderKey = connection.getLocalKey();
        this.receiverKey = connection.getRemoteKey();
        channel = "default";
    }

    public Header(int protocol, NodeConnection connection, String channel) {
        this.protocol = protocol;
        this.senderKey = connection.getLocalKey();
        this.receiverKey = connection.getRemoteKey();
        this.channel = channel;
    }

    public Header(int protocol, String senderKey, String receiverKey) {
        this.protocol = protocol;
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
        channel = "default";
    }

    public Header(int protocol, String senderKey, String receiverKey, String channel) {
        this.protocol = protocol;
        this.senderKey = senderKey;
        this.receiverKey = receiverKey;
        this.channel = channel;
    }

    // strips a header out of the input stream and returns a new header
    public static Header parseHeader(DataInputStream din) throws IOException {
        // type
        int type = din.readInt();

        // sender
        int senderLen = din.readInt();
        byte[] senderBytes = new byte[senderLen];
        din.readFully(senderBytes);
        String sender = new String(senderBytes);

        // receiver
        int receiverLen = din.readInt();
        byte[] receiverBytes = new byte[receiverLen];
        din.readFully(receiverBytes);
        String receiver = new String(receiverBytes);

        String channel = Util.readString(din);

        return new Header(type, sender, receiver, channel);
    }

    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        // type
        dout.writeInt(getType());
        // sender
        byte[] senderBytes = getSenderKey().getBytes();
        dout.writeInt(senderBytes.length);
        dout.write(senderBytes);
        // receiver
        byte[] receiverBytes = getReceiverKey().getBytes();
        dout.writeInt(receiverBytes.length);
        dout.write(receiverBytes);

        Util.writeString(channel, dout);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    public int getType() {
        return this.protocol;
    }

    public String getSenderKey() {
        return this.senderKey;
    }

    public String getReceiverKey() {
        return this.receiverKey;
    }

    public String getChannel() {
        return this.channel;
    }

    @Override public String toString() {
        return "["+Protocol.getProtocolString(getType()) + " SenderKey:"+getSenderKey() + " RecKey:"+getReceiverKey() +"]";
    }
}

package cs555.tebbe.wireformats;

import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.util.Util;

import java.io.*;

/**
 * Created by ctebbe
 */
public class PublishContent implements Event {

    public Header header;
    public String contentID;
    public double price;
    public String address;
    public String publisher;

    protected PublishContent(int protocol, NodeConnection nc, String channel, double price, String contentID, String address, String publisher) {
        header = new Header(protocol, nc, channel);
        this.contentID = contentID;
        this.price = price;
        this.address = address;
        this.publisher = publisher;
    }

    protected PublishContent(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        header = Header.parseHeader(din);
        contentID = Util.readString(din);
        price = din.readDouble();
        address = Util.readString(din);
        publisher = Util.readString(din);

        bais.close();
        din.close();
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baos));

        dout.write(header.getBytes());
        Util.writeString(contentID, dout);
        dout.writeDouble(price);
        Util.writeString(address, dout);
        Util.writeString(publisher, dout);

        // clean up
        dout.flush();
        marshalledBytes = baos.toByteArray();
        baos.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public int getType() {
        return header.getType();
    }
}

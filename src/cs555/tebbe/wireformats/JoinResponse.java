package cs555.tebbe.wireformats;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ctebbe
 */
public class JoinResponse implements Event {

    private final Header header;

    public final String targetNodeID;

    public final String lowLeafIP;
    public final String lowLeafIdentifier;

    public final String highLeafIP;
    public final String highLeafIdentifier;

    public final String[] route;
    public final List<List<PeerNodeData>> table;

    protected JoinResponse(int protocol, NodeConnection connection, String channel, String ID, PeerNodeData lowLeaf, PeerNodeData highLeaf,
                           String[] prevRoute, List<List<PeerNodeData>> routingTable) {
        header = new Header(protocol, connection, channel);

        this.targetNodeID = ID;

        if(lowLeaf == null) {
            this.lowLeafIP = "";
            this.lowLeafIdentifier = "";
        } else {
            this.lowLeafIP = Util.removePort(lowLeaf.host_port);
            this.lowLeafIdentifier = lowLeaf.identifier;
        }

        if(highLeaf == null) {
            this.highLeafIP = "";
            this.highLeafIdentifier = "";
        } else {
            this.highLeafIP = Util.removePort(highLeaf.host_port);
            this.highLeafIdentifier = highLeaf.identifier;
        }

        route = new String[prevRoute.length+1];
        for(int i=0; i < prevRoute.length; i++)
            route[i] = prevRoute[i];
        route[route.length-1] = Util.removePort(connection.getLocalKey())+"\t"+ID;

        table = routingTable;
    }

    protected JoinResponse(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(marshalledBytes);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bais));

        // header
        this.header = Header.parseHeader(din);

        // targetNodeID
        int idLen = din.readInt();
        byte[] idBytes = new byte[idLen];
        din.readFully(idBytes);
        targetNodeID = new String(idBytes);

        // lowLeafIP payload
        int lowLen = din.readInt();
        byte[] lowBytes = new byte[lowLen];
        din.readFully(lowBytes);
        lowLeafIP = new String(lowBytes);

        // lowLeafIP targetNodeID
        int lowIDLen = din.readInt();
        byte[] lowIDBytes = new byte[lowIDLen];
        din.readFully(lowIDBytes);
        lowLeafIdentifier = new String(lowIDBytes);

        // highLeafIP payload
        int highLen = din.readInt();
        byte[] highBytes = new byte[highLen];
        din.readFully(highBytes);
        highLeafIP = new String(highBytes);

        // highLeafIP targetNodeID
        int highIDLen = din.readInt();
        byte[] highIDBytes = new byte[highIDLen];
        din.readFully(highIDBytes);
        highLeafIdentifier = new String(highIDBytes);

        // route
        route = new String[din.readInt()];
        for(int i=0; i < route.length; i++) {
            int routeLen = din.readInt();
            byte[] routeBytes = new byte[routeLen];
            din.readFully(routeBytes);
            route[i] = new String(routeBytes);
        }

        // table
        int numRows = din.readInt(); // table size
        table = new ArrayList<>();
        for(int i=0; i < numRows; i++) {
            int rowSize = din.readInt(); // row size
            List<PeerNodeData> row = new ArrayList<>();
            for(int j=0; j < rowSize; j++) {
                // host port
                int hLen = din.readInt();
                byte[] hBytes = new byte[hLen];
                din.readFully(hBytes);
                String hostport = new String(hBytes);

                // id
                int iLen = din.readInt();
                byte[] iBytes = new byte[iLen];
                din.readFully(iBytes);
                String id = new String(iBytes);

                row.add(new PeerNodeData(hostport, id));
            }
            table.add(row);
        }

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
        byte[] idBytes = targetNodeID.getBytes();
        dout.writeInt(idBytes.length);
        dout.write(idBytes);

        // lowLeafIP
        byte[] lowBytes = lowLeafIP.getBytes();
        dout.writeInt(lowBytes.length);
        dout.write(lowBytes);

        // lowLeafID
        byte[] lowIDBytes = lowLeafIdentifier.getBytes();
        dout.writeInt(lowIDBytes.length);
        dout.write(lowIDBytes);

        // highLeafIP
        byte[] highBytes = highLeafIP.getBytes();
        dout.writeInt(highBytes.length);
        dout.write(highBytes);

        // highLeafID
        byte[] highIDBytes = highLeafIdentifier.getBytes();
        dout.writeInt(highIDBytes.length);
        dout.write(highIDBytes);

        // route
        dout.writeInt(route.length);
        for(int i=0; i < route.length; i++) {
            byte[] routeBytes = route[i].getBytes();
            dout.writeInt(routeBytes.length);
            dout.write(routeBytes);
        }

        // table
        dout.writeInt(table.size()); // table size
        for(List<PeerNodeData> row : table) {
            dout.writeInt(row.size()); // row size
            for(PeerNodeData entry : row) {
                // host_port
                byte[] hBytes = entry.host_port.getBytes();
                dout.writeInt(hBytes.length);
                dout.write(hBytes);

                // identifier
                byte[] iBytes = entry.identifier.getBytes();
                dout.writeInt(iBytes.length);
                dout.write(iBytes);
            }
        }

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
        return header;
    }

    public void printRouteTrace() {
        for(int i=0; i < route.length; i++) {
            System.out.println("\t" + route[i]);
        }
    }
}

package cs555.tebbe.node;

import cs555.tebbe.bitcoin.BitcoinManager;
import cs555.tebbe.data.ContentCache;
import cs555.tebbe.data.FileManager;
import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.diagnostics.Log;
import cs555.tebbe.encryption.EncryptionManager;
import cs555.tebbe.routing.PeerNodeRouteHandler;
import cs555.tebbe.transport.ConnectionFactory;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.transport.TCPServerThread;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PubSubNode implements Node {

    public static final int DEFAULT_SERVER_PORT = 18081;
    public static final String BASE_SAVE_DIR = "/tmp/ctebbe/";

    private NodeConnection _DiscoveryNode = null;
    private String id;
    private TCPServerThread serverThread = null;                                        // listens for incoming nodes
    private Map<String, NodeConnection> connectionsMap = new ConcurrentHashMap<>();     // buffers all current connections for reuse
    private Map<String, PeerNodeRouteHandler> routerMap = new ConcurrentHashMap<>();
    private FileManager fileManager = new FileManager();
    private Log logger = new Log();                                                     // logs events and prints diagnostic messages
    private EncryptionManager encryptionManager;
    private BitcoinManager bitcoinManager;

    public PubSubNode(String host, int port, boolean isCustomID, String channel) {
        try {
            serverThread = new TCPServerThread(this, new ServerSocket(DEFAULT_SERVER_PORT));
            serverThread.start();
        } catch(IOException ioe) {
            System.out.println("IOException thrown opening server thread:" + ioe.getMessage());
            System.exit(0);
        }

        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            encryptionManager = new EncryptionManager(hostname);
            bitcoinManager = new BitcoinManager(hostname);
        } catch (IOException e) { e.printStackTrace();
        } catch (ClassNotFoundException e) { e.printStackTrace(); }

        try {
            _DiscoveryNode = ConnectionFactory.getInstance().buildConnection(this, new Socket(host, port));
            //String id;
            if(!isCustomID)
                id = Util.getTimestampHexID();
            else {
                Scanner keyboard = new Scanner(System.in);
                System.out.println("Peer Node ID?");
                id = keyboard.nextLine();
            }
            _DiscoveryNode.sendEvent(EventFactory.buildRegisterEvent(_DiscoveryNode, id, channel));
        } catch(IOException ioe) {
            System.out.println("IOException thrown contacting DiscoveryNode:" + ioe.getMessage());
            ioe.printStackTrace();
            System.exit(0);
        }

        run();
    }

    private void run() {
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        while(input != null) {
            if(input.contains("state")) {
                printState();
            } else if(input.contains("files")) {
                System.out.println("Stored files:");
                /*for(String fname : fileManager.fMap.values()) {
                    System.out.println("\t" + fname + "\t" + Util.getDataHexID(fname.getBytes()));
                }*/
            } else if(input.contains("exit")) {
                try {
                    exitOverlay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(input.contains("sub")) {
                String channel = input.split(" ")[1];
                try {
                    _DiscoveryNode.sendEvent(EventFactory.buildRegisterEvent(_DiscoveryNode, id, channel));
                } catch (IOException e) { e.printStackTrace(); }
            } else if(input.contains("pub")) {
                String channel = input.split(" ")[1];
                System.out.println("Enter content:");
                String content = keyboard.nextLine();
                System.out.println("Enter contentID:");
                String id = keyboard.nextLine();
                System.out.println("Enter price:");
                String price = keyboard.nextLine();
                try {
                    initPublishContent(channel, content.getBytes(), id, Double.parseDouble(price));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(input.contains("add")) {
                System.out.println(bitcoinManager.getFreshAddress());
            }
            input = keyboard.nextLine();
        }
    }

    private ContentCache cache = new ContentCache();
    private void initPublishContent(String channel, byte[] contentBytes, String contentID, double price) throws IOException {
        cache = new ContentCache(contentID, channel, contentBytes, price);
        NodeConnection toSend = getNodeConnection(routerMap.get(channel).getClosestNodeIP(cache.ID));
        toSend.sendEvent(EventFactory.buildFileStoreRequestEvent(toSend, channel, cache.ID, routerMap.get(channel)._Identifier));
    }

    private void publishCachedContent() {
        NodeConnection toSend = getNodeConnection(routerMap.get(cache.channel).getHighLeaf().host_port);
        toSend.sendEvent(EventFactory.buildPublishContentEvent(toSend, cache.channel, cache.ID, cache.price));
    }

    private void exitOverlay() throws IOException {
        /*
        _DiscoveryNode.sendEvent(EventFactory.buildExitOverlayEvent(_DiscoveryNode, router._Identifier));

        // swap leafs
        NodeConnection lowLeaf = getNodeConnection(router.getLowLeaf().host_port);
        lowLeaf.sendEvent(EventFactory.buildLeafsetUpdateEvent(lowLeaf, router.getHighLeaf(), false));

        NodeConnection highLeaf = getNodeConnection(router.getHighLeaf().host_port);
        highLeaf.sendEvent(EventFactory.buildLeafsetUpdateEvent(highLeaf, router.getLowLeaf(), true));

        // migrate files
        for(String fname : files) {
            String fId = Util.getDataHexID(fname.getBytes());
            int distLow = Util.getAbsoluteHexDifference(router.getLowLeaf().identifier, fId);
            int distHigh = Util.getAbsoluteHexDifference(router.getHighLeaf().identifier, fId);

            File fToMigrate = new File(BASE_SAVE_DIR + fname);
            NodeConnection connection;
            if(distLow < distHigh) {
                connection = getNodeConnection(router.getLowLeaf().host_port);
            } else if(distHigh < distLow) {
                connection = getNodeConnection(router.getHighLeaf().host_port);
            } else {
                if(Util.getHexDifference(router.getLowLeaf().identifier, router.getHighLeaf().identifier) > 0)
                   connection = getNodeConnection(router.getLowLeaf().host_port);
                else
                   connection = getNodeConnection(router.getHighLeaf().host_port);
            }
            connection.sendEvent(EventFactory.buildFileStoreEvent(connection, fToMigrate.getName(), Files.readAllBytes(fToMigrate.toPath())));
            logger.printDiagnostic(fToMigrate);
        }
        */
    }

    private void printState() {

        /*System.out.println("ID:");
        System.out.println(router._Identifier);
        System.out.println("Low leaf:");
        System.out.println(router.getLowLeaf().toString());
        System.out.println("High leaf:");
        System.out.println(router.getHighLeaf().toString());
        System.out.println(router.printTable());
        */
    }

    public synchronized void onEvent(Event event){
        switch(event.getType()) {
            case Protocol.REGISTER_ACK:
                try {
                    processRegisterResponse((RegisterAck) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.JOIN_REQ:
                try {
                    processJoinRequest((JoinLookupRequest) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.JOIN_RESP:
                try {
                    processJoinResponse((JoinResponse) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.LEAFSET_UPDATE:
                try {
                    processLeafsetUpdate((NodeIDEvent) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.FILE_STORE_REQ:
                try {
                    System.out.println("file store request");
                    processFileStoreRequest((FileStoreLookupRequest) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.FILE_STORE_RESP:
                try {
                    System.out.println("file store response");
                    processFileStoreResponse((FileStoreLookupResponse) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.FILE_STORE:
                try {
                    System.out.println("storing file");
                    processFileStore((StoreFile) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.FILE_STORE_COMP:
                publishCachedContent();
                break;
            case Protocol.TABLE_UPDATE:
                processRoutingTableUpdateEvent((NodeIDEvent) event);
                break;
            case Protocol.PUBLISH:
                try {
                    processPublishContentEvent((PublishContent) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Unknown event type");
        }
    }

    private void processFileStoreResponse(FileStoreLookupResponse event) throws IOException {
        NodeConnection toSend = getNodeConnection(event.getHeader().getSenderKey());
        toSend.sendEvent(EventFactory.buildFileStoreEvent(toSend, cache.channel, cache.ID, encryptionManager.encrypt(cache.content)));
    }

    private void processPublishContentEvent(PublishContent event) throws IOException {
        System.out.println("Publish content event");
        System.out.println(event.header.getChannel());
        System.out.println(event.contentID);
        System.out.println(event.price);

        if(!event.contentID.equals(cache.ID)) {
            NodeConnection toSend = getNodeConnection(Util.removePort(routerMap.get(event.header.getChannel()).getHighLeaf().host_port));
            toSend.sendEvent(EventFactory.buildPublishContentEvent(toSend, event.header.getChannel(), event.contentID, event.price));
        } else
            cache = new ContentCache();
    }

    private void processRoutingTableUpdateEvent(NodeIDEvent event) {
        PeerNodeRouteHandler router = routerMap.get(event.payload);
        router.updateTable(event.getHeader().getSenderKey(), event.nodeID);
        logger.printDiagnostic(router);
    }

    private void processFileStore(StoreFile event) throws IOException {
        fileManager.store(event);
        // send completion
        NodeConnection connection = getNodeConnection(event.getHeader().getSenderKey());
        connection.sendEvent(EventFactory.buildFileStoreCompleteEvent(connection,"",""));
    }

    private void processFileStoreRequest(FileStoreLookupRequest event) throws IOException {
        PeerNodeRouteHandler router = routerMap.get(event.getHeader().getChannel());
        String lookupID = router.lookup(event.getLookupID());
        if(!lookupID.equals(router._Identifier)) {                                          // re-route request to closer node
            NodeConnection forwardNode = getNodeConnection(router.queryIPFromNodeID(lookupID));
            Event fEvent = EventFactory.buildFileStoreRequestEvent(forwardNode, event, router._Identifier);
            logger.printDiagnostic((FileStoreLookupRequest) fEvent);
            forwardNode.sendEvent(fEvent);
        } else { // file is stored here
            NodeConnection respNode = getNodeConnection(event.getQueryNodeIP());
            respNode.sendEvent(EventFactory.buildFileStoreResponseEvent(respNode, event, router._Identifier));
        }
    }

    private void processLeafsetUpdate(NodeIDEvent event) throws IOException {
        PeerNodeRouteHandler router = routerMap.get(event.getHeader().getChannel());
        String ip = event.payload.isEmpty() ? event.getHeader().getSenderKey() : event.payload;
        if(event.lowLeaf) {
            router.setLowLeaf(new PeerNodeData(ip, event.nodeID));
        } else {
            router.setHighLeaf(new PeerNodeData(ip, event.nodeID));
        }

        // migrate any suitable files to new leaf
        Iterator<String> fIterator = fileManager.getIterator(event.getHeader().getChannel());
        while(fIterator.hasNext()) {
            String fname = fIterator.next();
            String fId = Util.getDataHexID(fname.getBytes());
            int dist = Util.getAbsoluteHexDifference(router._Identifier,fId);
            int distLeaf = Util.getAbsoluteHexDifference(event.nodeID,fId);

            if(distLeaf < dist || (distLeaf == dist && Util.getHexDifference(event.nodeID,router._Identifier) > 0)) { // leaf closer or same with bigger ID
                File fToMigrate = new File(BASE_SAVE_DIR + fname);
                NodeConnection connection = getNodeConnection(event.getHeader().getSenderKey());

                connection.sendEvent(EventFactory.buildFileStoreEvent(connection, event.getHeader().getChannel(), fToMigrate.getName(), Files.readAllBytes(fToMigrate.toPath())));
                logger.printDiagnostic(fToMigrate);
                fIterator.remove();
            }
        }
    }

    private void processJoinResponse(JoinResponse event) throws IOException {
        String channel = event.getHeader().getChannel();
        PeerNodeRouteHandler router = routerMap.get(channel);
        if(event.lowLeafIP.isEmpty() && event.highLeafIP.isEmpty()) { // no leafset, set each other as leaf set
            NodeConnection leafSetConnection = getNodeConnection(event.getHeader().getSenderKey());
            router.setLowLeaf(new PeerNodeData(leafSetConnection.getRemoteKey(), event.targetNodeID));
            router.setHighLeaf(new PeerNodeData(leafSetConnection.getRemoteKey(), event.targetNodeID));
            leafSetConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(leafSetConnection, channel, router._Identifier, false));
            leafSetConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(leafSetConnection, channel, router._Identifier, true));

        } else if(event.lowLeafIP.equals(event.highLeafIP)) { // 3rd node in, must position between them
            NodeConnection senderLeafConnection = getNodeConnection(event.getHeader().getSenderKey());
            NodeConnection otherLeafConnection = getNodeConnection(event.highLeafIP);
            String id_1 = event.targetNodeID;
            String id_2 = event.highLeafIdentifier;

            boolean isNotMiddleNode = (Util.getHexDifference(router._Identifier, id_1) > 0 && Util.getHexDifference(router._Identifier, id_2) > 0) ||
                    (Util.getHexDifference(router._Identifier, id_1) < 0 && Util.getHexDifference(router._Identifier, id_2) < 0);
            boolean isSenderLowLeaf = Util.getHexDifference(id_1, id_2) < 0; // id1 < id2
            if(!isNotMiddleNode)
                isSenderLowLeaf = !isSenderLowLeaf; // if this is the middle node, make it sender's low leaf instead

            if(isSenderLowLeaf) {
                router.setHighLeaf(new PeerNodeData(senderLeafConnection.getRemoteKey(), id_1));
                router.setLowLeaf(new PeerNodeData(otherLeafConnection.getRemoteKey(), id_2));
            } else {
                router.setLowLeaf(new PeerNodeData(senderLeafConnection.getRemoteKey(), id_1));
                router.setHighLeaf(new PeerNodeData(otherLeafConnection.getRemoteKey(), id_2));
            }
            senderLeafConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(senderLeafConnection, channel, router._Identifier, isSenderLowLeaf));
            otherLeafConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(otherLeafConnection, channel, router._Identifier, !isSenderLowLeaf));

        } else { // join lookup result
            boolean isSenderLowLeaf = Util.getHexDifference(event.targetNodeID, router._Identifier) > 0;
            NodeConnection senderLeafConnection = getNodeConnection(event.getHeader().getSenderKey());
            NodeConnection otherLeafConnection;
            if(isSenderLowLeaf) {
                otherLeafConnection = getNodeConnection(event.lowLeafIP);
                router.setHighLeaf(new PeerNodeData(senderLeafConnection.getRemoteKey(), event.targetNodeID));
                router.setLowLeaf(new PeerNodeData(otherLeafConnection.getRemoteKey(), event.lowLeafIdentifier));
            } else {
                otherLeafConnection = getNodeConnection(event.highLeafIP);
                router.setLowLeaf(new PeerNodeData(senderLeafConnection.getRemoteKey(), event.targetNodeID));
                router.setHighLeaf(new PeerNodeData(otherLeafConnection.getRemoteKey(), event.highLeafIdentifier));
            }

            senderLeafConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(senderLeafConnection, channel, router._Identifier, isSenderLowLeaf));
            senderLeafConnection.sendEvent(EventFactory.buildRouteTableUpdateEvent(senderLeafConnection, channel, router._Identifier));

            otherLeafConnection.sendEvent(EventFactory.buildLeafsetUpdateEvent(otherLeafConnection, channel, router._Identifier, !isSenderLowLeaf));
            otherLeafConnection.sendEvent(EventFactory.buildRouteTableUpdateEvent(otherLeafConnection, channel, router._Identifier));

            router.updateTableEntries(event.table);
            for(PeerNodeData entry : router.getAllEntries()) { // update all nodes in the routing table
                NodeConnection connection = getNodeConnection(entry.host_port);
                if(!(connection.getRemoteKey().equals(senderLeafConnection.getRemoteKey()) ||
                        connection.getRemoteKey().equals(otherLeafConnection.getRemoteKey()))) { // avoid double-sending updates to the leafset

                    connection.sendEvent(EventFactory.buildRouteTableUpdateEvent(connection, channel, router._Identifier));
                }
            }
            logger.printDiagnostic(router);
        }
        logger.printDiagnostic(event.route);
        _DiscoveryNode.sendEvent(EventFactory.buildJoinCompleteEvent(_DiscoveryNode, router._Identifier, event.getHeader().getChannel()));
    }

    private NodeConnection getNodeConnection(String key) {
        String IP = Util.removePort(key);
        try {
            if(connectionsMap.containsKey(IP))
                return connectionsMap.get(IP);
            return ConnectionFactory.buildConnection(this, IP, DEFAULT_SERVER_PORT);

        } catch (IOException e) {
            e.printStackTrace();
        } return null;
    }

    private void processJoinRequest(JoinLookupRequest event) throws IOException {
        PeerNodeRouteHandler router = routerMap.get(event.getHeader().getChannel());
        boolean sendJoinResponse = false;
        if(router.getLowLeaf() == null && router.getHighLeaf() == null) {                       // first connection of the overlay, no neighbors yet
            sendJoinResponse = true;
        } else if(router.getLowLeaf().identifier.equals(router.getHighLeaf().identifier)) {   // currently two nodes in overlay, joining node is in this leafset
            sendJoinResponse = true;
        } else {                                                                                // decide if node is in this leafset or should be re-routed
            String closestID = router.lookup(event.getLookupID());
            if(!closestID.equals(router._Identifier)) {                                          // re-route request to closer node
                NodeConnection forwardNode = getNodeConnection(router.queryIPFromNodeID(closestID));
                Event fEvent = EventFactory.buildJoinRequestEvent(forwardNode, event, router._Identifier, router.findRow(event.getLookupID()));
                logger.printDiagnostic((JoinLookupRequest) fEvent);
                forwardNode.sendEvent(fEvent);
            } else
                sendJoinResponse = true;
        }

        if(sendJoinResponse) {                                                                  // send leafset to the query node
            NodeConnection newNode = getNodeConnection(event.getQueryNodeIP());
            newNode.sendEvent(EventFactory.buildJoinResponseEvent(newNode, event.getHeader().getChannel(), router._Identifier,
                    router.getLowLeaf(), router.getHighLeaf(), event.getRoute(), event.routingTable));
        }
    }

    private void processRegisterResponse(RegisterAck event) throws IOException {
        if(event.success) { // claimed and received ID
            logger.printDiagnostic(event);
            PeerNodeRouteHandler router = new PeerNodeRouteHandler(event.assignedID);
            if(!event.randomNodeIP.isEmpty()) { // if first node in no node to contact
                NodeConnection entryConnection = getNodeConnection(event.randomNodeIP);
                entryConnection.sendEvent(EventFactory.buildJoinRequestEvent(entryConnection, event.getHeader().getChannel(), router._Identifier));
            } else {
                _DiscoveryNode.sendEvent(EventFactory.buildJoinCompleteEvent(_DiscoveryNode, event.assignedID, event.getHeader().getChannel()));
            }

            this.id = event.assignedID;
            routerMap.put(event.getHeader().getChannel(), router);
        } else {
            _DiscoveryNode.sendEvent(EventFactory.buildRegisterEvent(_DiscoveryNode, Util.getTimestampHexID(), event.getHeader().getChannel()));
        }
    }

    public void newConnectionMade(NodeConnection connection) {
        connectionsMap.put(Util.removePort(connection.getRemoteKey()), connection);
    }

    public void lostConnection(String disconnectedIP) {
        System.out.println("Lost connection to:" + disconnectedIP);
    }

    public static void main(String args[]) {
        new PubSubNode(args[0], DiscoveryNode.DEFAULT_SERVER_PORT, false, args[1]);
    }
}

package cs555.tebbe.node;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.diagnostics.Log;
import cs555.tebbe.transport.NodeConnection;
import cs555.tebbe.transport.TCPServerThread;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DiscoveryNode implements Node {

    public static final int DEFAULT_SERVER_PORT = 18080;

    private TCPServerThread serverThread = null;                            // listens for incoming connections
    private ConcurrentHashMap<String, NodeConnection> bufferMap = null;  // buffers incoming unregistered connections
    private Map<String, Map<String, PeerNodeData>> channelMap = null;    // registered peer nodes
    private Map<String, String> identifierMap = new HashMap<>();

    public DiscoveryNode(int port) {
        try {
            bufferMap = new ConcurrentHashMap<>();
            channelMap = new ConcurrentHashMap<>();

            serverThread = new TCPServerThread(this, new ServerSocket(port));
            serverThread.start();

            run();
        } catch(IOException ioe) {
            display("IOException on DiscoveryNode:"+ioe.toString());
        }
    }

    private void run() {
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        while(input != null) {
            if(input.contains("nodes")) {
                printListNodes();
            }
            input = keyboard.nextLine();
        }
    }

    private void printListNodes() {
        for(Map.Entry<String, Map<String, PeerNodeData>> channel : channelMap.entrySet()) {
            System.out.println("Channel: " + channel.getKey());
            for(Map.Entry<String, PeerNodeData> entry : channel.getValue().entrySet()) {
                System.out.println("\t"+ entry.getValue());
            }
        }
    }

    public synchronized void onEvent(Event event) {
        switch(event.getType()) {
            case Protocol.REGISTER_REQ:
                try {
                    processRegisterRequest((SubscribeRequest) event);
                } catch (IOException e) {
                    System.out.println("IOE throws processing register event.");
                    e.printStackTrace();
                }
                break;
            case Protocol.RANDOM_PEER_REQ:
                try {
                    processRandomPeerRequest((RandomPeerNodeRequest) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.JOIN_COMP:
                try {
                    processJoinComplete((NodeIDEvent) event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Protocol.EXIT:
                processExit((NodeIDEvent) event);
                break;
            default:
                display("unknown event type:"+event.getType());
        }
    }

    private void processExit(NodeIDEvent event) {
        System.out.println("Node exiting overlay:\t" + event.getHeader().getSenderKey() + "\t" + event.nodeID);
        //peerMap.remove(event.getHeader().getSenderKey());
        //identifierSet.remove(event.nodeID);
    }

    private void processJoinComplete(NodeIDEvent event) throws IOException {
        String key = event.getHeader().getSenderKey();
        //peerMap.put(key, new PeerNodeData(event.getHeader().getSenderKey(), event.nodeID));
        channelMap.get(event.payload).put(key, new PeerNodeData(key, event.nodeID));
        Log.printDiagnostic(event);

        isNodeJoining = false;
        if(reqQueue.size() > 0) {
            SubscribeRequest req = reqQueue.remove(0);
            processRegisterRequest(req);
        }
    }

    private void processRandomPeerRequest(RandomPeerNodeRequest event) throws IOException {
        NodeConnection connection = bufferMap.get(event.getHeader().getSenderKey());
        connection.sendEvent(EventFactory.buildRandomPeerResponseEvent(connection, getRandomPeerNode("default")));
    }

    private List<SubscribeRequest> reqQueue = new ArrayList<>();
    private boolean isNodeJoining = false;
    private String joiningNodeKey;
    private void processRegisterRequest(SubscribeRequest event) throws IOException {
        if(isNodeJoining && !event.getHeader().getSenderKey().equals(joiningNodeKey)) {
            reqQueue.add(event);
            return;
        }

        boolean success = !identifierMap.containsKey(event.getNodeIDRequest())
                || identifierMap.get(event.getNodeIDRequest()).equals(Util.removePort(event.getHeader().getSenderKey()));
        if(success) {
            identifierMap.put(event.getNodeIDRequest(), Util.removePort(event.getHeader().getSenderKey()));
            isNodeJoining = true;
        } else {
            joiningNodeKey = event.getHeader().getSenderKey();
        }

        NodeConnection connection = bufferMap.get(event.getHeader().getSenderKey());
        connection.sendEvent(EventFactory.buildRegisterResponseEvent(connection, event.getNodeIDRequest(), success, getRandomPeerNode(event.channel)));
    }

    private String getRandomPeerNode(String channel) {
        if(channelMap.containsKey(channel)) {
            if(channelMap.get(channel).size() > 0) {
                List<String> keys = new ArrayList(channelMap.get(channel).keySet());
                String randKey = keys.get(Util.generateRandomNumber(0, keys.size()));
                return Util.removePort(channelMap.get(channel).get(randKey).host_port);
            }
            else return "";
        } else {
            channelMap.put(channel, new ConcurrentHashMap<String, PeerNodeData>());
            return "";
        }
    }

    public synchronized void newConnectionMade(NodeConnection connection) {
        bufferMap.put(connection.getRemoteKey(), connection);
    }

    public synchronized void lostConnection(String disconnectedConnectionKey) {
        System.out.println("Lost connection:" + disconnectedConnectionKey);
    }

    public void display(String str) {
        System.out.println(str);
    }

    public static void main(String args[]) {
        new DiscoveryNode(DEFAULT_SERVER_PORT);
    }
}

package cs555.tebbe.diagnostics;

import cs555.tebbe.data.PeerNodeData;
import cs555.tebbe.routing.PeerNodeRouteHandler;
import cs555.tebbe.util.Util;
import cs555.tebbe.wireformats.*;
import cs555.tebbe.wireformats.RegisterAck;

import java.io.File;
import java.util.logging.Logger;
/**
 * Created by ctebbe
 */
public class Log {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static int i=0;
    public static void printDiagnostic(NodeIDEvent event) {
        System.out.println();
        log((++i) + "* New PubSubNode joined");
        log(event.getHeader().getSenderKey());
        log(event.nodeID);
        System.out.println();
    }

    public static void log(String payload) {
        //LOGGER.log(Level.INFO, payload);
        System.out.println(payload);
    }

    public static void printDiagnostic(PeerNodeData oldLeaf, PeerNodeData newLeaf) {
        System.out.println();
        log("* Leafset update");
        if(oldLeaf != null)
            log("Old leaf:" + oldLeaf.toString());
        log("New leaf:" + newLeaf.toString());
    }

    public void printDiagnostic(RegisterAck event) {
        System.out.println();
        log("Assigned ID:" + event.assignedID);
        log("Random node to contact:" + event.randomNodeIP);
        System.out.println();
    }

    public void printDiagnostic(String[] route) {
        log("* Route:");
        for(String node : route)
            log("\t" + node);
        System.out.println();
    }

    public void printDiagnostic(FileStoreLookupResponse event) {
        System.out.println();
        log("Node to store file:"+Util.removePort(event.getHeader().getSenderKey()));
        printDiagnostic(event.getRoute());
        System.out.println();
    }

    public void printDiagnostic(LookupRequest event) {
        System.out.println();
        log("* Re-routing lookup request:");
        log("Type:"+ Protocol.getProtocolString(event.getType()));
        log("Lookup ID:"+ event.getLookupID());
        log("Hop count:"+ (event.getRoute().length-1));
        log("Next hop:"+ Util.removePort(event.getHeader().getReceiverKey()));
        System.out.println();
    }

    public void printDiagnostic(RandomPeerNodeResponse event, String dataID) {
        System.out.println();
        log("Random node to contact:"+event.nodeIP);
        log("Data ID:"+dataID);
        System.out.println();
    }

    public void printDiagnostic(StoreFile event) {
        System.out.println();
        log("Storing new file:" + event.ID);
        System.out.println();
    }

    public void printDiagnostic(PeerNodeRouteHandler router) {
        log("* Routing table update");
        log(router.printTable());
        System.out.println();
    }

    public void printDiagnostic(File file) {
        System.out.println();
        log("* Migrating file to leaf:" + file.getName());
        System.out.println();
    }
}

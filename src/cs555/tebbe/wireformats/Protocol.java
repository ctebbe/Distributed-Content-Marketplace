package cs555.tebbe.wireformats;
public class Protocol {

    // event types
    public static final int NOTYPE                  = -101;
    public static final int REGISTER_REQ = 100;
    public static final int REGISTER_ACK = 101;

    public static final int JOIN_REQ                = 102;
    public static final int JOIN_RESP               = 103;
    public static final int JOIN_COMP               = 113;

    public static final int RANDOM_PEER_REQ         = 104;
    public static final int RANDOM_PEER_RESP        = 105;

    public static final int LEAFSET_UPDATE          = 106;

    public static final int FILE_STORE_REQ          = 107;
    public static final int FILE_STORE_RESP         = 108;
    public static final int FILE_STORE              = 109;
    public static final int FILE_STORE_COMP         = 110;

    public static final int TABLE_UPDATE            = 111;

    public static final int EXIT                    = 112;

    public static final int PUBLISH                 = 120;

    public static final int DOWNLOAD_REQ            = 121;
    public static final int DOWNLOAD_RESP           = 122;

    public static final int DECRYPT_REQ                 = 123;
    public static final int DECRYPT_RESP                = 124;

    // status codes
    public static final byte NOSTATUS               = (byte) 0x00;
    public static final byte SUCCESS                = (byte) 0x01;
    public static final byte FAILURE                = (byte) 0x02;

    public static String getProtocolString(int protocol) {
        switch(protocol) {
            case NOTYPE: return "NOTYPE";
            case REGISTER_REQ: return "REGISTER_REQ";
            case JOIN_REQ: return "JOIN REQUEST LOOKUP";
            case JOIN_COMP: return "JOIN COMPLETE";
            case FILE_STORE_REQ: return "FILE REQUEST LOOKUP";
            case FILE_STORE_RESP: return "FILE RESPONSE";
            default: return "UNKNOWN";
        }
    }
}

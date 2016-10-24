package protocol;

import chat.Logging;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This class represent a general message of this protocol.
 * The protocol takes a lot of ideas from IRC protocol.
 *
 * @see /https://tools.ietf.org/html/rfc1459
 */
public class Message {

    private static Logger LOGGER = Logging.setup(Logger.getLogger(Message.class.getName()));

    // this pattern helps verify IPv4 format
    // http://stackoverflow.com/a/5667417
    private static final Pattern IP4_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    // this UID code is encoded into messages of this application
    // only message encoded with this secret code is processed
    private static final String UID = "27111991";

    // message header's values
    public static final String DELIMITER = " ";
    public static final String HELLO = "HELLO";
    public static final String HELLO_ACK = "HI";
    public static final String BYE = "BYE";
    public static final String CHAT = "MSG";
    public static final String CHAT_PRIV = "MSG_PRIV";
    public static final String FILE = "FILE";

    // message structure
    protected String header;
    protected String data;

    // additional information
    protected String srcIP;

    /**
     * Create an empty message.
     *
     * @param header    the message type
     */
    public Message(String header) {
        this(header, "", "");
    }

    /**
     * Create a message with populated data.
     *
     * @param header    the message type
     * @param data      the message data
     */
    public Message(String header, String data) {
        this(header, data, "");
    }

    /**
     * Create a message with full information.
     *
     * @param header    the message tpye
     * @param data      the message data
     * @param srcIP     IP address of the sender
     */
    public Message(String header, String data, String srcIP) {
        this.header = header;
        this.data = data;
        this.srcIP = srcIP;
    }

    /**
     * Get the header i.e. type of this message.
     *
     * @return  the message header
     */
    public String getHeader() {
        return this.header;
    }

    /**
     * Get IP of the source of this message.
     *
     * @return  the source IP
     */
    public String getSrcIP() {
        return this.srcIP;
    }

    /**
     * Set the source IP of this message.
     *
     * @param srcIP the IP of the sender
     */
    public void setSrcIP(String srcIP) {
        if (!IP4_PATTERN.matcher(srcIP).matches())
            LOGGER.warning("The IP address " + srcIP + " is malformed");
        this.srcIP = srcIP;
    }

    /**
     * Set the data that this message will carry.
     *
     * @param data  the message data
     */
    public void setData(String data) {
        if (data == null)
            data = "";
        this.data = data;
    }

    /**
     * Get the data of this message.
     *
     * @return  the message data
     */
    public String getData() {
        return this.data;
    }

    /**
     * Get the payload of this message that to be used for serialization.
     *
     * @return  the message payload
     */
    public String getPayload() {
        return UID + DELIMITER + this.header + DELIMITER + this.data;
    }

    /**
     * Parse a string and try to convert it to a specific message.
     * The result is an optional message i.e. can be null
     *
     * @param msg   the received string
     * @return      the parsed result
     */
    public static Optional<Message> parse(String msg) {
        LOGGER.info("Received message [" + msg + "]");
        // FIXME when user presses enter to send a message, there will be am extra newline character
        if (msg.charAt(msg.length() - 1) == '\n')
            msg = msg.substring(0, msg.length() - 1);
        String[] segments = msg.split(" ", 3);
        if (segments.length < 2)
            return Optional.empty();

        // verify the secret code matches with the protocol specification
        String uid = segments[0];
        if (!uid.equals(Message.UID))
            return Optional.empty();

        // parse header information and the message data
        String header = segments[1];
        String data = "";
        if (segments.length == 3)
            data = segments[2];

        return Optional.of(new Message(header, data));
    }

    /**
     * Print the message information to text.
     *
     * @return  the message as human-readable text
     */
    public String toString() {
        return this.header + DELIMITER + this.data;
    }
}

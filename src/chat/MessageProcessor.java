package chat;

import protocol.Message;
import protocol.TCPMessage;

import java.io.*;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * This class processes the messages it receives.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class MessageProcessor {

    private static MessageProcessor INSTANCE;

    private static final Logger LOGGER = Logging.setup(Logger.getLogger(MessageProcessor.class.getName()));

    /**
     * Get the singleton instance of this class.
     *
     * @return  the singleton instance
     */
    public static MessageProcessor getInstance() {
        if (INSTANCE == null) {
            synchronized (MessageProcessor.class) {
                INSTANCE = new MessageProcessor();
            }
        }
        return INSTANCE;
    }

    /**
     * Process the hello message.
     *
     * @param msg   the received message
     */
    private static void processHelloMsg(Message msg) {
        // filter out message from self
        if (msg.getSrcIP().equals(Application.getInstance().getIP()))
            return;

        Peer peer = new Peer(msg.getSrcIP(), Application.PORT);
        boolean isNewPeer = PeerManager.getInstance().add(peer);
        if (isNewPeer)
            UserInterface.display(peer.toString() + " joined.");

        // response to the greeting
        UDPMessageSender.getInstance().send(peer, new Message(Message.HELLO_ACK));
    }

    /**
     * Process the hello ack message.
     *
     * @param msg   the received message
     */
    private static void processHelloAckMsg(Message msg) {
        // filter out message from self
        if (msg.getSrcIP().equals(Application.getInstance().getIP()))
            return;

        Peer peer = new Peer(msg.getSrcIP(), Application.PORT);
        boolean isNewPeer = PeerManager.getInstance().add(peer);
        if (isNewPeer)
            UserInterface.display(peer.toString() + " joined.");
    }

    /**
     * Process the goodbye message.
     *
     * @param msg   the received message
     */
    private static void processByeMsg(Message msg) {
        Peer peer = new Peer(msg.getSrcIP(), Application.PORT);
        boolean removed = PeerManager.getInstance().remove(peer);
        if (removed)
            UserInterface.display(peer.toString() + " left.");
    }

    /**
     * Process a chat message.
     *
     * @param msg   the received message
     */
    private static void processChatMsg(Message msg) {
        // TODO for a sudden in-between chat message like this, do we want to skip them?
        Peer peer = new Peer(msg.getSrcIP(), Application.PORT);
        if (!PeerManager.getInstance().contains(peer)) {
            PeerManager.getInstance().add(peer);
            UserInterface.display(peer.toString() + " joined.");
        }

        UserInterface.display(peer.toString() + ": " + msg.getData());
    }

    /**
     * Process the private chat message.
     *
     * @param msg   the received message
     */
    private static void processPrivateChatMsg(Message msg) {
        // TODO for a sudden in-between chat message like this, do we want to skip them?
        Peer peer = new Peer(msg.getSrcIP(), Application.PORT);
        if (!PeerManager.getInstance().contains(peer)) {
            PeerManager.getInstance().add(peer);
            UserInterface.display(peer.toString() + " joined.");
        }

        UserInterface.display(peer.toString() + "[PRIV]: " + msg.getData());
    }

    /**
     * Process a file sharing message.
     *
     * @param msg   the received message
     */
    private static void processFileMsg(TCPMessage msg) throws Exception {
        DataInputStream in = new DataInputStream(msg.getSocket().getInputStream());
        String filename = in.readUTF();
        long filesize = in.readLong();

        // prepare to write received data to file using a buffer
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
        byte[] buffer = new byte[1024];
        int bytesRead;

        // keep reading from the input stream for the exact file size
        while (filesize > 0) {
            bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, filesize));
            if (bytesRead == -1)
                break;
            out.write(buffer, 0, bytesRead);
            filesize -= bytesRead;
        }

        // clean up
        out.flush();
        UserInterface.display("Received file " + filename + " from " + msg.getSrcIP());
        in.close();
        out.close();

        if (Security.isEncryptionEnabled())
            Security.decryptFile(filename);
    }

    /**
     * Process a received message.
     *
     * @param msg   the received message
     */
    public static void process(Optional<Message> msg) {
        try {
            switch (msg.map(Message::getHeader).orElse(null)) {
                case Message.HELLO:
                    processHelloMsg(msg.get());
                    break;
                case Message.HELLO_ACK:
                    processHelloAckMsg(msg.get());
                    break;
                case Message.BYE:
                    processByeMsg(msg.get());
                    break;
                case Message.CHAT:
                    processChatMsg(msg.get());
                    break;
                case Message.CHAT_PRIV:
                    processPrivateChatMsg(msg.get());
                    break;
                case Message.FILE:
                    processFileMsg((TCPMessage) msg.get());
                default:
                    break;
            }
        } catch (Exception e) {
            LOGGER.severe("Error while processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package chat;

import protocol.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is responsible for sending messages out.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class UDPMessageSender {

    private static UDPMessageSender INSTANCE;
    private static Logger LOGGER = Logging.setup(Logger.getLogger(UDPMessageSender.class.getName()));

    private DatagramSocket socket;

    /**
     * Construct an instance of this class.
     */
    private UDPMessageSender() {
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            LOGGER.severe("Unable to create datagram socket: " + e.getMessage());
        }
    }

    /**
     * Get the singleton instance of this class.
     *
     * @return  the singleton instance
     */
    public static UDPMessageSender getInstance() {
        if (INSTANCE == null) {
            synchronized (MessageProcessor.class) {
                INSTANCE = new UDPMessageSender();
            }
        }
        return INSTANCE;
    }

    /**
     * Send a message out.
     *
     * @param dst   the destination address
     * @param msg   the message to send
     */
    public void send(Peer dst, Message msg) {
        try {
            if (dst == null || msg == null)
                return;

            byte[] data = msg.getPayload().getBytes();
            DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    InetAddress.getByName(dst.getIPAddress()),
                    dst.getPort());
            socket.send(packet);
        } catch (IOException e) {
            LOGGER.severe("Unable to send msg: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Send a message to a list of peers.
     *
     * @param peers the peers to send message to
     * @param msg   the message to send
     */
    public void send(List<Peer> peers, Message msg) {
        peers.forEach(peer -> send(peer, msg));
    }
}

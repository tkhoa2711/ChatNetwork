package protocol;

import java.net.Socket;

/**
 * This message is specifically to be delivered over TCP.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class TCPMessage extends Message {

    private Socket socket;

    /**
     * Create a message received over TCP.
     *
     * @param header    the message header
     * @param socket    the socket where message is received
     */
    public TCPMessage(String header, Socket socket) {
        super(header);
        this.socket = socket;
        this.srcIP = socket.getRemoteSocketAddress().toString().replace("/", "").split(":")[0];
    }

    /**
     * Get the socket where the message is delivered to.
     *
     * @return  the socket
     */
    public Socket getSocket() {
        return this.socket;
    }
}

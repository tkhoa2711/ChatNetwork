package chat;

import protocol.Message;
import protocol.TCPMessage;

import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * This class is responsible for listening to incoming messages.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class MessageListener {

    private static final Logger LOGGER = Logging.setup(Logger.getLogger(MessageListener.class.getName()));

    private TCPMessageListener tcpMessageListener;
    private UDPMessageListener udpMessageListener;

    public MessageListener() {
        try {
            tcpMessageListener = new TCPMessageListener();
            new Thread(tcpMessageListener).start();

            udpMessageListener = new UDPMessageListener();
            new Thread(udpMessageListener).start();
        } catch (Exception e) {
            LOGGER.severe("Error creating message listeners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        tcpMessageListener.shutdown();
        udpMessageListener.shutdown();
    }
}

/**
 * This is the interface for all implementation of the message listener.
 */
interface IMessageListener extends Runnable {
    void shutdown();
}

/**
 * This class is responsible for listening to incoming TCP messages.
 *
 * @author Khoa Le
 * @version 1.0
 */
class TCPMessageListener implements IMessageListener {

    private static final Logger LOGGER = Logging.setup(Logger.getLogger(TCPMessageListener.class.getName()));

    private static final ExecutorService EXECUTOR = Executors.newScheduledThreadPool(10);

    private ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Application.PORT);
            LOGGER.info("Start listening to incoming TCP message");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = serverSocket.accept();
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String msgType = in.readUTF();

                    EXECUTOR.execute(() -> {
                        MessageProcessor.process(Optional.of(new TCPMessage(msgType, socket)));
                    });
                } catch (SocketException e) {
                    // expected when closing socket
                    LOGGER.warning(e.getMessage());
                    break;
                } catch (Exception e) {
                    LOGGER.severe("Error listening to the message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Unable to establish the socket: " + e.getMessage());
        } finally {
            if (serverSocket != null)
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    LOGGER.warning("Error closing socket: " + e.getMessage());
                }
        }
    }

    /**
     * Stop the listener.
     */
    @Override
    public void shutdown() {
        EXECUTOR.shutdown();
        Thread.currentThread().interrupt();
        if (serverSocket != null)
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.warning("Error closing socket: " + e.getMessage());
            }
    }
}

/**
 * This class is responsible for listening to incoming UDP messages.
 *
 * @author Khoa Le
 * @version 1.0
 */
class UDPMessageListener implements IMessageListener {

    private static final Logger LOGGER = Logging.setup(Logger.getLogger(UDPMessageListener.class.getName()));

    private static final ExecutorService EXECUTOR = Executors.newScheduledThreadPool(10);

    DatagramSocket socket;

    /**
     * Create a MessageListener object listening on a given socket.
     */
    public UDPMessageListener() throws IOException {
        this.socket = new DatagramSocket(Application.PORT);
    }

    @Override
    public void run() {
        LOGGER.info("Start listening to incoming UDP message");
        byte[] buffer = new byte[1024];
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                EXECUTOR.execute(() -> {
                    Optional<Message> message = Message.parse(new String(packet.getData(), 0, packet.getLength()));
                    message.ifPresent(msg -> msg.setSrcIP(packet.getAddress().getHostAddress()));
                    MessageProcessor.process(message);
                });
            } catch (SocketException e) {
                // expected when closing socket
                LOGGER.warning(e.getMessage());
                break;
            } catch (Exception e) {
                LOGGER.severe("Error listening to the message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop the listener
     */
    @Override
    public void shutdown() {
        EXECUTOR.shutdown();
        Thread.currentThread().interrupt();
        if (socket != null)
            socket.close();
    }
}

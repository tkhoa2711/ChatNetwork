package chat;

import protocol.Message;

import java.net.InetAddress;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * The main class for starting the application.
 *
 * @author  Khoa Le
 * @version 1.0
 */
public class Main {

    private static Logger LOGGER = Logging.setup(Logger.getLogger(Main.class.getName()));

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                Application.PORT = Integer.parseInt(args[0]);;
            } catch (Exception e) {
                UserInterface.display("Invalid port provided. Use default port " + Application.PORT);
            }
        }

        try {
            // initialise the application
            Application.getInstance().init();
            LOGGER.info("Started the application");

            UserInterface.display("IP Address: " + Application.getInstance().getSubnet().getInfo().getCidrSignature());
            UserInterface.display("Port: " + Application.PORT);
            UserInterface.display("Subnet Mask: " + Application.getInstance().getSubnet().getInfo().getNetmask());

            // start listening to messages (in a background thread)
            MessageListener messageListener = new MessageListener();

            Consumer<String> greet = (addr) -> {
                try {
                    InetAddress address = InetAddress.getByName(addr);
                    Peer peer = new Peer(addr, Application.PORT);
                    UDPMessageSender.getInstance().send(peer, new Message(Message.HELLO));
                } catch (Exception e) {
                    LOGGER.warning("Unable to say hello to " + addr + ": " + e.getMessage());
                }
            };

            LOGGER.info("Checking for online peers..");
            Application.getInstance().getAllAdresses().parallelStream().forEach(greet);

            new UserInterface().run();

            // clean-up before stopping the application
            messageListener.stop();
        } catch (Exception e) {
            LOGGER.severe("Unknown exception: " + e.toString());
            e.printStackTrace();
        } finally {
            UserInterface.print("Exited.");
            LOGGER.info("Exited.");
            Logging.stop();
        }
    }
}

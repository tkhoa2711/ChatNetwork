package chat;

import protocol.Message;

import java.util.logging.Logger;

/**
 * The user interface of the application.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class UserInterface {

    private static final Logger LOGGER = Logging.setup(Logger.getLogger(UserInterface.class.getName()));

    private static final String PROMPT = "> ";

    private static final String CMD_ENCRYPT     = "\\e";
    private static final String CMD_FILE        = "\\f";
    private static final String CMD_GENKEY      = "\\g";
    private static final String CMD_HELP        = "\\h";
    private static final String CMD_LIST        = "\\l";
    private static final String CMD_CHAT_PRIV   = "\\p";
    private static final String CMD_EXIT        = "\\x";

    /**
     * Print help message.
     */
    private void help() {
        display("\\e    Enable/disable encryption");
        display("\\f    Send a file [format: <ip> <filename>]");
        display("\\g    Generate a secret key for encryption");
        display("\\h    Help");
        display("\\l    List all connected peers");
        display("\\p    Chat private [format: <ip> <message>]");
        display("\\x    Exit");
    }

    /**
     * Start the user interface.
     */
    public void run() {
        String input;
        String[] args;
        display("Type \\h for help at anytime.");
        while (true) {
            try {
                input = Console.readLine("");
                switch (input.split(" ")[0].toLowerCase()) {
                    case CMD_HELP:
                        help();
                        break;
                    case CMD_LIST:
                        PeerManager.getInstance().getAllPeers().stream()
                                .map(Peer::toString)
                                .forEach(UserInterface::display);
                        break;
                    case CMD_EXIT:
                        UDPMessageSender.getInstance().send(
                                PeerManager.getInstance().getAllPeers(),
                                new Message(Message.BYE)
                        );
                        return;
                    case CMD_CHAT_PRIV:
                        args = input.split(" ", 3);
                        if (args.length != 3) {
                            display("Invalid input");
                            break;
                        }
                        String ipAddress = args[1];
                        String msg = "";
                        if (args.length > 1)
                            msg = args[2];
                        UDPMessageSender.getInstance().send(
                                PeerManager.getInstance().get(ipAddress),
                                new Message(Message.CHAT_PRIV, msg));
                        System.out.print(PROMPT);
                        break;
                    case CMD_FILE:
                        args = input.split(" ", 3);
                        if (args.length != 3) {
                            display("Invalid input");
                            break;
                        }

                        Peer peer = PeerManager.getInstance().get(args[1]);
                        String filename = args[2];
                        TCPMessageSender.sendFile(peer, filename);
                        break;
                    case CMD_GENKEY:
                        Security.generateSecretKey();
                        display("Generated a secret key saved at " + Security.SECRET_KEY_FILE);
                        break;
                    case CMD_ENCRYPT:
                        Security.toggleEncryption();
                        display("Encryption is enabled: " + String.valueOf(Security.isEncryptionEnabled()));
                        break;
                    default:
                        // default is a normal public chat message
                        if (input.trim().length() > 0)
                            UDPMessageSender.getInstance().send(
                                    PeerManager.getInstance().getAllPeers(),
                                    new Message(Message.CHAT, input));
                        System.out.print(PROMPT);
                        break;
                }
            } catch (Exception e) {
                LOGGER.warning("Got an exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Display a message to the screen.
     *
     * @param msg   the message to display
     */
    public static void display(String msg) {
        // TODO there will be a problem when user is typing and a message is displayed concurrently
        System.out.print("\b\b"); // clear previous prompt
        System.out.println(msg);
        System.out.print(PROMPT);
    }

    /**
     * Display a message without newline character at the end.
     *
     * @param msg   the message to display
     */
    public static void print(String msg) {
        System.out.print(msg);
    }
}

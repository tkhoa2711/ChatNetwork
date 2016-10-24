package chat;

import protocol.Message;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.Key;

/**
 * This class provides functionality to send TCP messages.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class TCPMessageSender {

    /**
     * Send a file over TCP.
     *
     * @param dst       the destination peer
     * @param filename  the file name to send
     */
    public static void sendFile(Peer dst, String filename) {
        try {
            if (dst == null || filename == null)
                throw new IOException("Either destination or file name is empty");

            // initialize a client socket to connect to the server
            Socket socket = new Socket(dst.getIPAddress(), dst.getPort());

            // read the file into the buffered input stream
            File file = new File(filename);
            byte[] data = new byte[(int) file.length()];

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            in.read(data, 0, data.length);

            // encrypt data
            if (Security.isEncryptionEnabled())
                data = Security.encrypt(data);

            // send over the file name, file size and file contents respectively
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(Message.FILE);
            out.writeUTF(file.getName());
            out.writeLong(data.length);
            out.write(data, 0, data.length);
            out.flush();
            UserInterface.display("Sent " + filename + " to " + dst);

            // cleanup
            in.close();
            out.close();
        } catch (Exception e) {
            UserInterface.display("Error while trying to send file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

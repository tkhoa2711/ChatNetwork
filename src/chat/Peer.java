package chat;

/**
 * A representation of a peer within the network.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class Peer {

    private String ipAddress;
    private int port;

    /**
     * Construct a peer instance.
     *
     * @param ipAddress the peer's IP address
     * @param port      the port
     */
    public Peer(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public int hashCode() {
        int result = 1;
        int factor = 10;
        for (char c : this.ipAddress.toCharArray()) {
            result += ((int) c) * factor;
            factor *= 10;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj != null && obj.getClass() == Peer.class) {
            if (hashCode() == obj.hashCode())
                return true;
        }

        return false;
    }

    /**
     * Get IP address of this peer.
     *
     * @return  the IP address as a string
     */
    public String getIPAddress() {
        return this.ipAddress;
    }

    /**
     * Get the port of the peer.
     *
     * @return  the port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * A string represents this object.
     *
     * @return  a representative string
     */
    public String toString() {
        return "[" + this.ipAddress + ":" + this.port + "]";
    }
}

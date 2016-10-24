package chat;

import org.apache.commons.net.util.SubnetUtils;

import java.net.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represent the running application.
 * It contains information about this application.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class Application {

    private static Logger LOGGER = Logging.setup(Logger.getLogger(Application.class.getName()));

    private static final Application INSTANCE = new Application();

    public static Application getInstance() {
        return INSTANCE;
    }

    public static int PORT = 4000;

    // information about this application
    private InetAddress localhost;
    private SubnetUtils subnet;
    private short subnetMask;

    /**
     * Initialize the application.
     *
     * @throws UnknownHostException if the hostname could not be resolved, most probably due to disconnection
     * @throws SocketException      if an IO error occurs
     */
    public void init() throws UnknownHostException, SocketException {
        // TODO redo this step after a disconnection
        // investigate the current state of the network
        // get IP address of the local host
        this.localhost = Inet4Address.getLocalHost();
        String localhostAddress = this.localhost.getHostAddress();

        // get the subnet info
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost);
        this.subnetMask = networkInterface.getInterfaceAddresses().stream()
                .filter(addr -> (addr.getAddress() instanceof Inet4Address))
                .map(InterfaceAddress::getNetworkPrefixLength)
                .findAny()
                .orElse(null);
        this.subnet = new SubnetUtils(localhostAddress + "/" + this.subnetMask);
    }

    /**
     * Get the localhost address.
     *
     * @return  the localhost object
     */
    public InetAddress getLocalhost() {
        return this.localhost;
    }

    /**
     * Get the IP of local host.
     *
     * @return  the local host IP address
     */
    public String getIP() {
        return this.localhost.getHostAddress();
    }

    /**
     * Get the subnet.
     *
     * @return  the subnet object
     */
    public SubnetUtils getSubnet() {
        return this.subnet;
    }

    /**
     * Get the subnet mask of the current subnet.
     *
     * @return  the subnet mask
     */
    public short getSubnetMask() {
        return this.subnetMask;
    }

    /**
     * Get all addresses in range of the current subnet, except self.
     *
     * @return  the list of all address range
     */
    public List<String> getAllAdresses() {
        return Stream.of(this.subnet.getInfo().getAllAddresses())
                .filter(i -> !i.equals(this.localhost.getHostAddress()))
                .collect(Collectors.toList());
    }
}

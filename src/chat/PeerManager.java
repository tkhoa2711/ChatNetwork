package chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A manager class that takes care of all the known peers within the network.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class PeerManager {

    private static final PeerManager INSTANCE = new PeerManager();

    private Set<Peer> peers;

    public static PeerManager getInstance() {
        return INSTANCE;
    }

    /**
     * Construct an object of this class.
     */
    private PeerManager() {
        this.peers = new HashSet<>();
    }

    /**
     * Add a new peer to the list.
     *
     * @param peer  the new peer
     * @return      true if the peer is added successfully
     */
    public boolean add(Peer peer) {
        return this.peers.add(peer);
    }

    /**
     * Add multiple peers to the list.
     *
     * @param peers the peers to add
     * @return      true if new peers are added successfully
     */
    public boolean add(List<Peer> peers) {
        return this.peers.addAll(peers);
    }

    /**
     * Remove a peer from the list.
     *
     * @param peer  the peer to remove
     * @return      true if the peer is removed
     */
    public boolean remove(Peer peer) {
        return this.peers.remove(peer);
    }

    /**
     * Find a peer given its IP address.
     *
     * @param ip    the IP address to find
     * @return      the peer instance if exists, else null
     */
    public Peer get(String ip) {
        return this.peers.stream()
                .filter(peer -> peer.getIPAddress().equals(ip))
                .findAny().orElse(null);
    }

    /**
     * Check whether the peer is already in the list.
     *
     * @param peer  the peer to check
     */
    public boolean contains(Peer peer) {
        return this.peers.contains(peer);
    }

    /**
     * Retrieve all peers.
     *
     * @return      all connected peers
     */
    public List<Peer> getAllPeers() {
        return new ArrayList<>(this.peers);
    }
}

// SPDX-License-Identifier: GPL-3.0
pragma solidity >=0.8.2 <0.9.0;

contract Rendezvous {
    struct SLA {
        string name; // The name of the SLA
        string md5Checksum; // MD5 checksum of the SLA file
    }

    struct Peer {
        bool available; // Indicates if the peer is available for SLAs
        string peerID; // IPFS ID, uniquely identifies the peer
        uint64 stdOutTopic; // Topic for publishing peer output
        uint64 stdInTopic; // Topic for receiving requests or messages
        uint64 stdErrTopic; // Topic for self error reporting
        uint8[] slaIDs; // Array of SLA IDs the peer subscribes/offers
        uint8[] prices; // Prices corresponding to each subscribed SLA
    }

    // Mapping from an address to a Peer struct
    mapping(address => Peer) public hederaAddressToPeer;

    // Array to store addresses of all peers
    address[] public peerList;

    // Array of allowed SLAs, indexed by SLA ID
    SLA[] public allowedSLAs;

    // Address of Neuron
    address public neuron;

    // Constructor to initialize the contract with Neuron's address and initial SLAs
    constructor() {
        neuron = 0x1234567890abcdef1234567890abcdef12345678; // Placeholder for Neuron's address

        // Initialize allowed SLAs
        allowedSLAs.push(SLA("ADSB001", "5d41402abc4b2a76b9719d911017c592")); // SLA ID 0
        allowedSLAs.push(SLA("ADSB002", "e99a18c428cb38d5f260853678922e03")); // SLA ID 1
    }

    /**
     * @dev Registers a peer as available and updates their SLA subscriptions and prices.
     *      Future Work: No payment/staking logic is implemented here yet.
     *      This function is a work in progress and may be extended in the future
     *      to handle payment, staking, and other related logic.
     * @param stdOutTopic The topic for publishing peer output.
     * @param stdInTopic The topic for receiving peer requests.
     * @param stdErrTopic The topic for peer error reporting.
     * @param peerID The unique identifier for the peer (e.g., IPFS ID).
     * @param slaIDs Array of SLA IDs the peer subscribes/offers.
     * @param prices Array of prices corresponding to each subscribed SLA.
     */
    function putPeerAvailableSelf(
        uint64 stdOutTopic,
        uint64 stdInTopic,
        uint64 stdErrTopic,
        string memory peerID,
        uint8[] memory slaIDs,
        uint8[] memory prices
    ) public {
        // Ensure that the number of SLAs matches the number of prices
        require(slaIDs.length == prices.length, "SLA IDs and prices must have the same length");

        // Ensure that each SLA ID is valid (i.e., exists in the allowedSLAs array)
        for (uint i = 0; i < slaIDs.length; i++) {
            require(slaIDs[i] >= 0 && slaIDs[i] < allowedSLAs.length, "An SLA ID was not allowed");
        }

        // Retrieve the peer's existing data or create a new entry
        Peer storage peer = hederaAddressToPeer[msg.sender];

        // If this is a new peer, add their address to the peer list
        if (!peer.available) {
            peerList.push(msg.sender);
        }

        // Update the peer's information
        peer.available = true;
        peer.peerID = peerID;
        peer.stdOutTopic = stdOutTopic;
        peer.stdInTopic = stdInTopic;
        peer.stdErrTopic = stdErrTopic;

        // Clear existing SLAs and prices, then update with new values
        delete peer.slaIDs;
        delete peer.prices;
        for (uint i = 0; i < slaIDs.length; i++) {
            peer.slaIDs.push(slaIDs[i]);
            peer.prices.push(prices[i]);
        }
    }

    /**
     * @dev Retrieves the total number of peers.
     * @return The size of the peer list.
     */
    function getPeerArraySize() public view returns (uint) {
        return peerList.length;
    }

    /**
     * @dev Retrieves the SLAs subscribed to by a specific peer.
     * @param peerAddress The address of the peer.
     * @return An array of SLA IDs the peer is subscribed to.
     */
    function getPeerSLAs(address peerAddress) public view returns (uint8[] memory) {
        require(hederaAddressToPeer[peerAddress].available, "Peer is not available");
        return hederaAddressToPeer[peerAddress].slaIDs;
    }

    /**
     * @dev Retrieves the price of a specific SLA for a peer.
     * @param peerAddress The address of the peer.
     * @param slaID The ID of the SLA.
     * @return The price associated with the specified SLA ID.
     */
    function getPriceForSLA(address peerAddress, uint8 slaID) public view returns (uint8) {
        require(hederaAddressToPeer[peerAddress].available, "Peer is not available");

        Peer storage peer = hederaAddressToPeer[peerAddress];

        // Match the SLA ID and return the corresponding price
        for (uint i = 0; i < peer.slaIDs.length; i++) {
            if (peer.slaIDs[i] == slaID) {
                return peer.prices[i];
            }
        }
        revert("SLA ID not found for this peer");
    }

    /**
     * @dev Retrieves the name and checksum of an SLA by its ID.
     * @param slaID The ID of the SLA.
     * @return name The name of the SLA.
     * @return md5Checksum The MD5 checksum of the SLA file.
     */
    function getSLADetails(uint8 slaID) public view returns (string memory name, string memory md5Checksum) {
        require(slaID >= 0 && slaID < allowedSLAs.length, "SLA ID is not valid");
        SLA storage sla = allowedSLAs[slaID];
        return (sla.name, sla.md5Checksum);
    }
}
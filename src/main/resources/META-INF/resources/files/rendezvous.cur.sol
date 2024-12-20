// SPDX-License-Identifier: GPL-3.0
pragma solidity >=0.8.2 <0.9.0;

contract Rendezvous {
    struct Peer {
        bool available;
        string peerID; // IPFS ID, can be derived from 'address'
        uint64 stdOutTopic; // Outputs from the peer, e.g., location or NAT status
        uint64 stdInTopic; // Incoming requests end here, e.g., service requests
        uint64 stdErrTopic; // Used for reporting errors, similar to stdOutTopic but for errors
        uint8[] serviceIDs; // Array of service IDs
        uint8[] prices; // Array of prices for the services
    }

    // Mapping from an address to a Peer struct
    mapping (address => Peer) public hederaAddressToPeer;

    // An array of addresses to keep track of peers
    address[] public peerList;

    // Array of allowed service names
    string[] public allowedServices;

    constructor() {
        // Initialize allowed services by their names
        allowedServices.push("ADSB001"); // 0
        allowedServices.push("ADSB002"); // 1
    }

    function putPeerAvailableSelf(
        uint64 stdOutTopic, 
        uint64 stdInTopic, 
        uint64 stdErrTopic, 
        string memory peerID, 
        uint8[] memory serviceIDs, 
        uint8[] memory prices
    ) public {
        require(serviceIDs.length == prices.length, "Service IDs and prices must have the same length");

        // Ensure each service is allowed
        for (uint i = 0; i < serviceIDs.length; i++) {
            require(serviceIDs[i] >= 0 && serviceIDs[i] < allowedServices.length, "A service ID was not allowed");
        }

        Peer storage peer = hederaAddressToPeer[msg.sender];
        
        // If this is the first time the peer is being added, push their address to the peerList
        if(!peer.available) {
            peerList.push(msg.sender);
        }

        // Update the peer information
        peer.available = true;
        peer.peerID = peerID;
        peer.stdOutTopic = stdOutTopic;
        peer.stdInTopic = stdInTopic;
        peer.stdErrTopic = stdErrTopic;
        
        // Clear existing services and prices and add new ones
        delete peer.serviceIDs;
        delete peer.prices;
        for (uint i = 0; i < serviceIDs.length; i++) {
            peer.serviceIDs.push(serviceIDs[i]);
            peer.prices.push(prices[i]);
        }
    }

    function getPeerArraySize() public view returns (uint) {
        return peerList.length;
    }
}

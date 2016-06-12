# Distributed-Content-Marketplace

This repo contains the source code for a proof-of-concept of a decentralized digital-content distribution network powered by the Bitcoin testnet. Participating peers have the ability to subscribe to channels which interest them and publish/price content to those channels to be purchased by other nodes.

## Logical Overlay
Pastry DHT routing tables are used to negotiate the entrance and exit of nodes as well as the routing of content O(lg N). Active nodes use gossiping protocols to propagate new content and knowledge of nodes, which enables the network to eventually achieve a zero-hop O(1) lookup architecture.
Nodes are grouped by publish-subscribe channels which form respective DHTs.

## Content storage
When a peer publishes content to a respective channel/DHT the following steps occur:

1. Publishing node sets a price for the content to be paid by subscribing nodes and assigns it a unique Bitcoin address
2. Raw content is encrypted by an RSA key the publishing node contains
3. The encrypted content is given a hash and routed to it's place within the DHT
4. Publishing node then begins propagating the existence of the content to subscribing nodes
5. When a subscribing node decides to purchase the content, it pays directly to the Bitcoin address and makes requests for both the encrypted content stored on the DHT and the decryption key from the publishing node

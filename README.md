P2PDistribute
=============
A file distribution tool for fast, reliable transfers over an internal network and the general internet.

Contents of Submission
----------------------
`Examples/` Contains Example JSON messages and example .p2pmeta files (initial swarm metadata - See Report 2.1)
`P2PDistribute` Contains source code for Swarm Manager, Peer & Meta Generator. Also contains `sm.jar` `peer.jar` and `metagen.jar` - the executables.
Results.xlsx - Predicted and Empirical data collected, also described in the Appendix.
Report.pdf - Report + Appendices.

How to distribute file(s)
-------------------------

1. Start Swarm Manager.
2. Generate a .p2pmeta file for the transfer. This file will contain the information about each file to be transferred. This is required by a peer to participate in the swarm.
3. Distribute this .p2pmeta file to all nodes which will participate in the stream.
4. Start the peer on all nodes. Ensure at least the initial node is started with --seed

The file(s) will now distribute to all nodes

Starting the Swarm Manager
--------------------------

Using the supplied .jar file:
`java -jar sm.jar`

Generating a .p2pmeta file
--------------------------

Using the supplied .jar file:
`java -jar metagen.jar SwarmManagerHostname File1 File2 ... FileN p2pMetaOutputFile`

For example:

``java -jar metagen.jar `hostname` Files/TheFastandtheFuriousJohnIreland1954goofyrip_512kb.mp4 Files/cawiki-20140129-stub-articles.xml 700mbtest.p2pmeta``

Starting a peer
---------------

`java -jar peer.jar 700mbtest.p2pmeta /tmp/output`

For the peer to not exit on completion, --seed can be supplied. This is recommended for at least the initial peer:

`java -jar peer.jar --seed 700mbtest.p2pmeta /tmp/output`

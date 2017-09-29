# ChordX
A Chord Protocol implementation for distributed key lookup with network locality awareness and bi-directional routing table

Chord is a protocol and algorithm used in distributed and peer to peer environment to locate node with m-bit identifier. Chord does not require any centralized server to locate node containing the resources. Nodes are arranged in a ring using consistent hashing and nodes can join and depart anytime.

ChordX is an improved version of chord which aims at improving handling certain drawbacks of the original protocol and provide reduced latency and decrease in the number of messages exchanges between nodes.

## Environment Requirements
The ChordX algorithm is implementation in JAVA language which offer cross platform support. Below is the recommended requirement to run a stable and efficient chordX implementation:
1. 1 x86-64 CPU core
2. 2 GB RAM 
3. 1 Mbps network interface
4. Operation System : Linux or Windows
5. JDK and JRE version 8

## Version
This git repo contains 2 version for chord implementation:
1. Original Chord Implementation
2. Improved ChordX Implementation

## Setup
ChordX can be run in 2 ways, Manually and Automated. 

### To run manually, follow the below steps:
1. Clone the project on local machine.
2. Enter chordx-original/src directory and compile source files using `javac -cp .:log4j-1.2.17.jar *.java`.
3. Run the RMI Registry to enable remote procedure calls:
* Linux : `rmiregistry &`
* Windows : `start rmiregistry`
4. Run the Bootstrap Node:
* Linux : `java -cp .:log4j-1.2.17.jar BootStrapNodeImpl`
* Windows : `java -cp .;log4j-1.2.17.jar BootStrapNodeImpl`
5. Run the original ChordX instance:
* Linux : `java -cp .:log4j-1.2.17.jar ChordNodeImpl <IP ADDRESS OF CURRENT NODE> <IP ADDRESS OF BOOTSTRAP NODE>`
* Windows : `java -cp .;log4j-1.2.17.jar ChordNodeImpl <IP ADDRESS OF CURRENT NODE> <IP ADDRESS OF BOOTSTRAP NODE>`

**Note:** To run improved ChordX node instances, change to chordx-improvements/src directory and follow the same procedure till step 4. Then run the below commands:
* Linux : `java -cp .:log4j-1.2.17.jar ChordNodeImpl <IP ADDRESS OF CURRENT NODE> <IP ADDRESS OF BOOTSTRAP NODE> <ZONE ID>`
* Windows : `java -cp .;log4j-1.2.17.jar ChordNodeImpl <IP ADDRESS OF CURRENT NODE> <IP ADDRESS OF BOOTSTRAP NODE> <ZONE ID>`

The additional Zone ID parameter is specified to make the new node join a paticular zone. Zone ID can range from 0 to m-1, where the identifier of Chord nodes is an m-bit number. [Default value of m is 5, but can be configure by modifying variables "m" and "maxNodes" in  "src/BootStrapNodeImpl.java" & "ftsize" and "maxNodes" in "src/ChordNodeImpl.java"]. If you don't want to use zone-based node joining, provide `-1` as the value

### To run the automation script for latency measurement, follow the below steps:
1. Clone the project on local machine.
2. Configure key based authentication on each node where chordX instance will run, and store the private key on the Bootstrap node. 
3. Configure the firewall on each node to allow machines to communicate on port 1099
4. Deployer should run on the Bootstrap server. In `deployer.sh` , configure the IP Address of bootstrap Node and all the machines where chordX will be running. Modify the number of keys to insert and number of keys to lookup. (Modify variables "testkeystoinsert" and "testkeystoquery" inside the deployer.sh)
5. Modify the permission of `deployer.sh` to run as executable and execute it.
6. All the chord nodes will be setup and performance metrics will be displayed on screen.

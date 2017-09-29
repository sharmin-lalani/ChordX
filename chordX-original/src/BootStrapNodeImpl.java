import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class BootStrapNodeImpl extends UnicastRemoteObject implements BootStrapNode{
	private static final long serialVersionUID = 1L;
	
	// m and maxNodes in BootStrapNodeImpl should be same as
	// ftsize and maxNodes in ChordNodeImpl.
	public static int maxNodes = 32; // Maximum number of permitted nodes in the Chord Ring
	public static int m = 5; // maxNodes = 2^m;
	public static int noOfNodes = 0;
	
	// For testing module
	public static int testNodeCount = 0;
	public static int testQueryKeyCount = 0;
	public static int testKeyCount = 0;
	public static long totalNodeJoinTime = 0;
	public static String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	public static ArrayList<String> TestKeysList = new ArrayList<String>();
	public static long[] operationStat= new long[3];
	public static ArrayList<Result> stats = new ArrayList<Result>();//insert
	public static ArrayList<Result> stats2 = new ArrayList<Result>();//get query
	public static boolean[] testOpsStat;
	public static boolean[] testOpsStat2;
	public static boolean testGetOps;
	public static long errcount_query = 0;
	
	public static HashMap<Integer, NodeInfo> nodes = new HashMap<Integer, NodeInfo>();
	public static ArrayList<NodeInfo> nodeList = new ArrayList<NodeInfo>();
	public static ArrayList<Integer> nodeIds = new ArrayList<Integer>();
	
	public BootStrapNodeImpl() throws RemoteException {
		System.out.println("Bootstrap Node created");
	}

	public ArrayList<NodeInfo> addNodeToRing(String ipaddress, String port) throws RemoteException{
		synchronized(this){
			ArrayList<NodeInfo> result = new ArrayList<NodeInfo>();
			noOfNodes++;
			int nodeID = -1;
			try{
				nodeID = generate_ID(ipaddress + port, maxNodes);
                System.out.println("Generated ID " + nodeID + " for new node.");
				
                if(nodeIds.indexOf(nodeID) == -1){
                	NodeInfo ni = new NodeInfo(ipaddress, port, nodeID);
                	nodes.put(nodeID, ni);
                	nodeIds.add(nodeID);
                	nodeList.add(ni);
                }else{  // ID collision, generate new node ID
                	NodeInfo ni = null;
                	while(nodeIds.indexOf(nodeID) >= 0){
                		String timeStamp = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS").format(new Date());
                		nodeID = generate_ID(nodeID + timeStamp, maxNodes);
                    	ni = new NodeInfo(ipaddress, port, nodeID);
                    	System.out.println("Generated ID already exists...New ID: " + nodeID);
                    }
                	nodeIds.add(nodeID);
                	nodeList.add(ni);
                	nodes.put(nodeID, ni);
                }
                System.out.println("New node added to ring with ID: " + nodeID);                
			}catch (Exception e) {
				System.out.println("Error in hashing function");
				e.printStackTrace();
				return null;
			}
			
			Collections.sort(nodeIds);
			int successor = nodeIds.get((nodeIds.indexOf(nodeID) + 1) % noOfNodes);
			System.out.println("Successor for new node: " + successor);
			int predecessor = nodeIds.get((nodeIds.indexOf(nodeID) - 1 + noOfNodes) % noOfNodes);
			System.out.println("Predecessor for new node: " + predecessor);
			
			result.add(nodes.get(nodeID));
			result.add(nodes.get(successor));
			result.add(nodes.get(predecessor));
			
			System.out.println("");
			if(noOfNodes == testNodeCount){
				// start testing
				testOpsStat = new boolean[3];
				testOpsStat[0]=false;
				testOpsStat[1]=false;
				testOpsStat[2]=false;
				testGetOps = false;
				testOpsStat2 = new boolean[3];
				testOpsStat2[0]=false;
				testOpsStat2[1]=false;
				testOpsStat2[2]=false;
				
				totalNodeJoinTime = 0;
				
				for(int i=0;i<noOfNodes;i++){
					try {
			    		Result insHops = new Result();
						ChordNode c = (ChordNode)Naming.lookup("rmi://" + nodeList.get(i).ipaddress + "/ChordNode_" + nodeList.get(i).port);
						totalNodeJoinTime += c.get_join_time();
					} catch (MalformedURLException | RemoteException | NotBoundException e) {
						e.printStackTrace();
						//log.error(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause() + "\n" +  e.getStackTrace().toString(),e);
					}
				}
				
				//create 3 threads
				Thread t1 = new Thread(new Runnable() {
			    public void run()
			    {
			    	// 
			    	try {
			    		Result insHops = new Result();
						ChordNode c = (ChordNode)Naming.lookup("rmi://" + nodeList.get(0).ipaddress + "/ChordNode_" + nodeList.get(0).port);
						int testOps=0;
						for(int i=0;i<testKeyCount/3;i++){
							System.out.println("THREAD 1 : Inserting Key : "+TestKeysList.get(i));
							c.insert_key(TestKeysList.get(i), TestKeysList.get(i), insHops);
							testOps++;
						}
						insHops.latency = c.get_insert_latency();
						insHops.hopCount = c.get_insert_hopcount();
						System.out.println("Got latency from thread 1 : "+insHops.latency);
						stats.add(insHops);
						testOpsStat[0]=true;
					} catch (MalformedURLException | RemoteException | NotBoundException e) {
						e.printStackTrace();
						//log.error(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause() + "\n" +  e.getStackTrace().toString(),e);
					}
			    }});  
				Thread t2 = new Thread(new Runnable() {
				    public void run()
				    {
				    	// 
				    	try {
				    		Result insHops = new Result();
							ChordNode c = (ChordNode)Naming.lookup("rmi://" + nodeList.get(1).ipaddress + "/ChordNode_" + nodeList.get(1).port);
							int testOps=0;
							for(int i=(testKeyCount/3);i<((2*testKeyCount)/3);i++){
								System.out.println("THREAD 2 : Inserting Key : "+TestKeysList.get(i));
								c.insert_key(TestKeysList.get(i), TestKeysList.get(i), insHops);
								testOps++;
							}
							insHops.latency = c.get_insert_latency();
							insHops.hopCount = c.get_insert_hopcount();
							System.out.println("Got latency from thread 2 : "+insHops.latency);
							stats.add(insHops);
							testOpsStat[1]=true;
						} catch (MalformedURLException | RemoteException | NotBoundException e) {
							e.printStackTrace();
							//log.error(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause() + "\n" +  e.getStackTrace().toString(),e);
						}
				    }});  
				Thread t3 = new Thread(new Runnable() {
				    public void run()
				    {
				    	// 
				    	try {
				    		Result insHops = new Result();
							ChordNode c = (ChordNode)Naming.lookup("rmi://" + nodeList.get(2).ipaddress + "/ChordNode_" + nodeList.get(2).port);
							int testOps=0;
							for(int i=((2*testKeyCount)/3);i<testKeyCount;i++){
								System.out.println("THREAD 3 : Inserting Key : "+TestKeysList.get(i));
								c.insert_key(TestKeysList.get(i), TestKeysList.get(i), insHops);
								testOps++;
							}
							insHops.latency = c.get_insert_latency();
							insHops.hopCount = c.get_insert_hopcount();
							System.out.println("Got latency from thread 2 : "+insHops.latency);
							stats.add(insHops);
							testOpsStat[2]=true;
						} catch (MalformedURLException | RemoteException | NotBoundException e) {
							e.printStackTrace();
							//log.error(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause() + "\n" +  e.getStackTrace().toString(),e);
						}
				    }}); 
				
				Thread t4 = new Thread(new Runnable() {
				    public void run()
				    {
				    	//
				    	try {
							while(!(testOpsStat[0] && testOpsStat[1] && testOpsStat[2])){	
								Thread.sleep(5000);
								System.out.println("Key Insertion Operation Still happening");
							}
							System.out.println("Key Operation completed");
							long totalTime= 0;
							long totalHops = 0;
							for(int i=0;i<stats.size();i++){
								totalTime += stats.get(i).latency;
								totalHops += stats.get(i).hopCount;
							}
							operationStat[0] = totalTime;
							operationStat[2] = totalHops;//insert hop count
							testOpsStat[0]=false;
							testOpsStat[1]=false;
							testOpsStat[2]=false;
							testGetOps = true;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }}); 
				
				Thread t5 = new Thread(new Runnable() {
				    public void run()
				    {
				    	// 
				    	try {
				    		while(!testGetOps){
								Thread.sleep(5000);
				    		}
				    		Result insHops = new Result();
							ChordNode c = (ChordNode)Naming.lookup("rmi://" + nodeList.get(0).ipaddress + "/ChordNode_" + nodeList.get(0).port);
							int testOps = 0;
							for(int i=0;i<testQueryKeyCount/3;i++){
								System.out.println("THREAD 1 : Get Key : "+TestKeysList.get(i));
								String returnVal = c.get_value(TestKeysList.get(i), insHops);
								//System.out.println("THREAD 1 : Get Key Return : "+returnVal);
								if(!TestKeysList.get(i).equals(returnVal))
									errcount_query++;
								testOps++;
							}
							insHops.latency = c.get_query_latency();
							insHops.hopCount = c.get_query_hopcount();
							System.out.println("Got Query latency from thread 1 : "+insHops.latency);
							stats2.add(insHops);
							testOpsStat2[0]=true;
						} catch (InterruptedException | MalformedURLException | RemoteException | NotBoundException e) {
							e.printStackTrace();
							//log.error(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause() + "\n" +  e.getStackTrace().toString(),e);
						}
				    }});
				
				Thread t6 = new Thread(new Runnable() {
				    public void run()
				    {
				    	// 
				    	try {
				    		while(!testGetOps){
								Thread.sleep(5000);
				    		}
				    		Result insHops = new Result();
							ChordNode c = (ChordNode)Naming.lookup("rmi://" + nodeList.get(1).ipaddress + "/ChordNode_" + nodeList.get(1).port);
							int testOps=0;
							for(int i=(testQueryKeyCount/3);i<((2*testQueryKeyCount)/3);i++){
								System.out.println("THREAD 2 : Get Key : "+TestKeysList.get(i));
								String returnVal = c.get_value(TestKeysList.get(i), insHops);
								//System.out.println("THREAD 2 : Get Key Return : "+returnVal);
								if(!TestKeysList.get(i).equals(returnVal))
									errcount_query++;
								testOps++;
							}
							insHops.latency = c.get_query_latency();
							insHops.hopCount = c.get_query_hopcount();
							System.out.println("Got Query latency from thread 2 : "+insHops.latency);
							stats2.add(insHops);
							testOpsStat2[1]=true;
						} catch (InterruptedException | MalformedURLException | RemoteException | NotBoundException e) {
							e.printStackTrace();
							//log.error(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause() + "\n" +  e.getStackTrace().toString(),e);
						}
				    }});
				
				Thread t7 = new Thread(new Runnable() {
				    public void run()
				    {
				    	// 
				    	try {
				    		while(!testGetOps){
								Thread.sleep(5000);
				    		}
				    		Result insHops = new Result();
							ChordNode c = (ChordNode)Naming.lookup("rmi://" + nodeList.get(2).ipaddress + "/ChordNode_" + nodeList.get(2).port);
							int testOps=0;
							for(int i=((2*testQueryKeyCount)/3);i<testQueryKeyCount;i++){
								System.out.println("THREAD 3 : Get Key : "+TestKeysList.get(i));
								String returnVal = c.get_value(TestKeysList.get(i), insHops);
								//System.out.println("THREAD 3 : Get Key Return : "+returnVal);
								if(!TestKeysList.get(i).equals(returnVal))
									errcount_query++;
								testOps++;
							}
							insHops.latency = c.get_query_latency();
							insHops.hopCount = c.get_query_hopcount();
							System.out.println("Got Query latency from thread 3 : "+insHops.latency);
							stats2.add(insHops);
							testOpsStat2[2]=true;
						} catch (InterruptedException | MalformedURLException | RemoteException | NotBoundException e) {
							e.printStackTrace();
							//log.error(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause() + "\n" +  e.getStackTrace().toString(),e);
						}
				    }});
				
				Thread t8 = new Thread(new Runnable() {
				    public void run()
				    {
				    	//
				    	try {
							while(!(testOpsStat2[0] && testOpsStat2[1] && testOpsStat2[2])){	
								Thread.sleep(5000);
								System.out.println("Key Query Operation Still happening");
							}
							System.out.println("Key Operation completed");
							long totalTime= 0;
							long totalHopsQuery = 0;
							for(int i=0;i<stats2.size();i++){
								totalTime += stats2.get(i).latency;
								totalHopsQuery += stats2.get(i).hopCount;
							}
							
							// join latency
							System.out.println("\n##########################");
							System.out.println("Total latency for "+noOfNodes+" node joins is : "+totalNodeJoinTime+"ms");
							System.out.println("Average latency per node join for "+noOfNodes+" is : "+(totalNodeJoinTime/noOfNodes)+"ms");
							System.out.println("##########################\n");
							
							// Insert Operations
							System.out.println("\n##########################");
							System.out.println("Total latency for "+testKeyCount+" keys insert is : "+operationStat[0]+"ms");
							System.out.println("Average latency per key insertion for "+testKeyCount+" keys is : "+(operationStat[0]/testKeyCount)+"ms");
							System.out.println("Average communication messages per key insertion for "+testKeyCount+" keys is : "+(operationStat[2]/testKeyCount));
							System.out.println("##########################\n");
							
							// Query latency
							operationStat[1] = totalTime;
							System.out.println("\n##########################");
							System.out.println("Total latency for "+testQueryKeyCount+" keys query is : "+operationStat[1]+"ms");
							System.out.println("Average latency per key query for "+testQueryKeyCount+" keys is : "+(operationStat[1]/testQueryKeyCount)+"ms");
							System.out.println("Average communication messages per key query for "+testQueryKeyCount+" keys is : "+(totalHopsQuery/testQueryKeyCount));
							System.out.println("No of key hits: " + (testQueryKeyCount - errcount_query));
							System.out.println("No of key misses: " + errcount_query);
							System.out.println("##########################\n");
							testOpsStat2[0]=false;
							testOpsStat2[1]=false;
							testOpsStat2[2]=false;
							testGetOps = true;
							stats = new ArrayList<Result>(); 
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }}); 
				
			    t1.start();
			    t2.start();
			    t3.start();
			    t4.start();
			    t5.start();
			    t6.start();
			    t7.start();
			    t8.start();
			}
			return result;
		}
	}
	
	
	public void startKeyDeletion(){
		// perform Get Operations
		
	}
	
	public void removeNodeFromRing(NodeInfo n) throws RemoteException{
		synchronized(this){
			if(n == null || nodes.get(n.nodeID) == null)
				return;
			nodeList.remove(nodes.get(n.nodeID));
			System.out.println("Updated node list");
			nodeIds.remove(new Integer(n.nodeID));
			System.out.println("Updated node ID list");
			nodes.remove(n.nodeID);
			noOfNodes--;
			System.out.println("Node " + n.nodeID + " left Chord Ring");
			System.out.println("Number of nodes in Chord Ring: " + noOfNodes);
			displayNodesInRing();
		}		
	}	
	
	public NodeInfo findNewSuccessor(NodeInfo n, NodeInfo dead_node) throws RemoteException {
		System.out.println("Received update from node " + n.nodeID + " that node " + dead_node.nodeID + " is dead.");
		try {
			removeNodeFromRing(dead_node);
		} catch(Exception e){
			System.out.println("There is some problem with Removing dead node " + dead_node.nodeID + ": " + e.getMessage());	
		}
		
		int successor = nodeIds.get((nodeIds.indexOf(n.nodeID) + 1) % noOfNodes);
		System.out.println("Assigning new successor " + successor + " to node " + n.nodeID);
		return nodes.get(successor);
	}
	
	public void acknowledgeNodeJoin(int nodeID) throws RemoteException {
		synchronized(this){
			System.out.println("Join acknowledge: New node joined Chord Ring with identifier " + nodeID);
			System.out.println("Number of nodes in Chord Ring: " + noOfNodes);
			displayNodesInRing();	
		}			
	}
	
	public void displayNodesInRing() throws RemoteException{
		Iterator<NodeInfo> i = nodeList.iterator();
		System.out.println("*********************List of nodes in the ring********************");
		while(i.hasNext()){
			NodeInfo ninfo = i.next();
			System.out.println("Node ID: " + ninfo.nodeID);
			System.out.println("Node IP: " + ninfo.ipaddress);
			System.out.println("Node Port: " + ninfo.port);
			System.out.println("******************\n");
		}
	}
	
	public static void main(String []args) throws Exception{
		if(args.length>0){
			// testing module params 
			testNodeCount = new Integer(args[0]).intValue();
			testKeyCount = new Integer(args[1]).intValue();
			testQueryKeyCount = new Integer(args[2]).intValue();
			for(int i=0;i<testKeyCount;i++){
				Random rand=new Random(System.currentTimeMillis());
			    StringBuilder res=new StringBuilder();
			    // generate 20 character keys and values
			    for (int j = 0; j < 20; j++) {
			       int randIndex=rand.nextInt(36); 
			       res.append(SALTCHARS.charAt(randIndex));            
			    }
			    TestKeysList.add(res.toString());
			}
		}
		
		
		try{
			BootStrapNodeImpl bnode = new BootStrapNodeImpl();
			//System.setProperty("java.rmi.server.hostname","127.0.0.1");
			Naming.rebind("ChordRing", bnode);
			noOfNodes = 0;
			System.out.println("Waiting for nodes to join or leave the Chord Ring");
			System.out.println("Number of nodes in Chord Ring: " + noOfNodes + "\n");
		} catch(Exception e){
			System.out.println("There is some problem with Bootstrap Node." + e.getMessage());			
		}	
	}

	public int getNodesInRing() throws RemoteException {
		return nodeList.size();
	}

	public ArrayList<Integer> getNodeIds() throws RemoteException {
		return nodeIds;
	}

	@Override
	public int getMaxNodesInRing() throws RemoteException {
		return maxNodes;
	}
	
	@Override
	public int generate_ID(String key, int maxNodes) throws RemoteException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.reset();		
		md.update((key).getBytes());
        byte[] hashBytes = md.digest();
        BigInteger hashValue = new BigInteger(1,hashBytes);
        return Math.abs(hashValue.intValue()) % maxNodes;
	}
}

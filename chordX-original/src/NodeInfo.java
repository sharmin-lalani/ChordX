import java.io.Serializable;

public class NodeInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String ipaddress;
	public String port;
	public int nodeID;
	
	public NodeInfo(String ipaddress, String port, int nodeID) {
		this.ipaddress = ipaddress;
		this.port = port;
		this.nodeID = nodeID;
	}
	
	/*
	public String getIpaddress() {
		return ipaddress;
	}
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public int getNodeID() {
		return nodeID;
	}
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}
	*/
}

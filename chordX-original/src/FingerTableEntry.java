public class FingerTableEntry {
	public int start;
	public NodeInfo successor;
	
	public FingerTableEntry(){
		this.start = 0;
		this.successor = null;
	}
	public FingerTableEntry(int start, NodeInfo n){
		this.start = start;
		this.successor = n;
	}
	
	/*
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public NodeInfo getSuccessor() {
		return successor;
	}
	public void setSuccessor(NodeInfo successor) {
		this.successor = successor;
	}
	*/
}

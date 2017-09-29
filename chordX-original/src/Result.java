import java.io.Serializable;

public class Result implements Serializable{
	public int hopCount;
	public long latency;

	public Result(){
		this.hopCount = 0;
		this.latency = 0;
	}

	public void setHopcount(int val){
		this.hopCount = val;
	}

	public void setLatency(long val){
		this.latency = val;
	}
}
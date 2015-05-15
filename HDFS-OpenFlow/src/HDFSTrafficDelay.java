
/**
 * Atomic class for managing delay.
 * 
 * @author AllenLiu
 */

public class HDFSTrafficDelay {
	private long readDelay;
	private long writeDelay;
	private long replicationDelay;
	
	
	public HDFSTrafficDelay(){
		new HDFSTrafficDelay(0L, 0L, 0L);
	}
	
	public HDFSTrafficDelay(long readDelay, long writeDelay, long replicationDelay){
		this.readDelay = readDelay;
		this.writeDelay = writeDelay;
		this.replicationDelay = replicationDelay;
	}
	
	public void setReadDelay(long readDelay){
		this.readDelay = readDelay;
	}
	
	public void setWriteDelay(long writeDelay){
		this.writeDelay = writeDelay;
	}
	
	public void setReplicationDelay(long replicationDelay){
		this.replicationDelay = replicationDelay;
	}
	
	
	public long getReadDelay(){
		return this.readDelay;
	}
	
	public long getWriteDelay(){
		return this.writeDelay;
	}
	
	public long getReplicationDelay(){
		return this.replicationDelay;
	}
	
	public String toString(){
		return "read delay: " + this.readDelay + " write delay: " + this.writeDelay + " replication delay: " + this.replicationDelay;
	}
}

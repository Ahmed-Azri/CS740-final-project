package utils;

public class RemoteEntity {
	private String ip;
	private int port;
	public RemoteEntity(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	public String getIp(){
		return this.ip;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public boolean equals(RemoteEntity other){
		return this.port == other.port && this.ip.equals(other.ip);
	}
	
	public int hashCode(){
		return (ip + port).hashCode();
	}
}

package p2pdistribute.message;


public class Message {
	public final String cmd;
	public final String metaHash;
	
	public Message(String cmd, String metaHash) {
		this.cmd = cmd;
		this.metaHash = metaHash;
	}
}

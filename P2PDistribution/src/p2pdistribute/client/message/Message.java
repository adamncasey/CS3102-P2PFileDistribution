package p2pdistribute.client.message;

public class Message {
	public final short version;
	public final MessageType type;
	public final int length;
	
	public Message(short version, MessageType type, int length) {
		this.version = version;
		this.type = type;
		this.length = length;
	}

}

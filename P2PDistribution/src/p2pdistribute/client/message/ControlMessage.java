package p2pdistribute.client.message;

import p2pdistribute.common.message.JSONMessage;

/**
 * Stores all properties of a P2P ControlMessage
 *
 */
public class ControlMessage extends Message {

	public final JSONMessage payload;
	
	public ControlMessage(short version, MessageType type, int length, JSONMessage payload) {
		super(version, type, length);
		
		this.payload = payload;
	}	
}

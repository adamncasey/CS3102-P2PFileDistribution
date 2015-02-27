/**
 * 
 */
package p2pdistribute.client.message;

/**
 * Stores all properties of a P2P Data Message
 */
public class DataMessage extends Message {
	
	public final byte[] metaHash;
	public final int fileid;
	public final int chunkid;
	public final byte[] data;

	public DataMessage(byte[] metaHash, int fileid, int chunkid, byte[] chunkData, short version, MessageType type, int length) {
		super(version, type, length);
		
		this.metaHash = metaHash;
		this.fileid = fileid;
		this.chunkid = chunkid;
		this.data = chunkData;
	}

}

package p2pdistribute.client.message;

import p2pdistribute.common.message.JSONMessage;

public class RequestChunkJSONMessage extends JSONMessage {

	public final int fileid;
	public final int chunkid;
	
	public RequestChunkJSONMessage(int fileid, int chunkid, String cmd, String metaHash) {
		super(cmd, metaHash);

		this.fileid = fileid;
		this.chunkid = chunkid;
	}

}

package p2pdistribute.client.message;

import java.util.Map;

import p2pdistribute.common.message.JSONMessage;

public class AdvertiseJSONMessage extends JSONMessage {
	
	// Map<FileID, ChunkID>.
	public final Map<Integer, Integer> chunksComplete;

	public AdvertiseJSONMessage(Map<Integer, Integer> chunksComplete, String cmd, String metaHash) {
		super(cmd, metaHash);
		
		this.chunksComplete = chunksComplete;
	}

}

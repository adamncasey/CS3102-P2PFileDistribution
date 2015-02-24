package p2pdistribute.client.message;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import p2pdistribute.common.message.JSONMessage;

public class RequestChunkJSONMessage extends JSONMessage {

	public final int fileid;
	public final int chunkid;
	
	public RequestChunkJSONMessage(int fileid, int chunkid, String cmd, String metaHash) {
		super(cmd, metaHash);

		this.fileid = fileid;
		this.chunkid = chunkid;
	}
	
	public RequestChunkJSONMessage(int fileid, int chunkid, byte[] metaHash) {
		this(fileid, chunkid, "request_chunk", new String(Hex.encodeHex(metaHash)));
	}
	
	@Override
	public Map<String, Object> getJSON() {
		Map<String, Object> map = super.getJSON();
		
		List<Integer> chunk = Arrays.asList(new Integer[] { this.fileid, this.chunkid });
		
		map.put("chunk", chunk);
		
		return map;
	}
}

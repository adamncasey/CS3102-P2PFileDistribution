package p2pdistribute.client.message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Hex;

import p2pdistribute.common.message.JSONMessage;

public class AdvertiseJSONMessage extends JSONMessage {
	
	// Map<FileID, ChunkID>.
	public final Map<Integer, Integer> chunksComplete;

	public AdvertiseJSONMessage(Map<Integer, Integer> chunksComplete, String cmd, String metaHash) {
		super(cmd, metaHash);
		
		this.chunksComplete = chunksComplete;
	}
	
	public AdvertiseJSONMessage(int[][] chunksComplete, byte[] metaHash) {
		super("advertise_chunks", new String(Hex.encodeHex(metaHash)));
		
		this.chunksComplete = convertIntsToMap(chunksComplete);
	}

	@Override
	public Map<String, Object> getJSON() {
		Map<String, Object> map = super.getJSON();
		
		List<List<Integer>> chunks = new LinkedList<>();
		
		for(Entry<Integer, Integer> entry : chunksComplete.entrySet()) {
			Integer[] filechunkid = new Integer[] { entry.getKey(), entry.getValue() }; 
			chunks.add(Arrays.asList(filechunkid));
		}
		
		map.put("chunks", chunks);
		
		return map;
	}
	
	/**
	 * Converts an array of arrays into a Map
	 * @note No bounds checking is performed. This should only be called using a value which must be correct
	 */
	private Map<Integer, Integer> convertIntsToMap(int[][] chunksComplete) {
		Map<Integer, Integer> map = new HashMap<>();
		
		for(int[] row : chunksComplete) {
			map.put(row[0], row[1]);
		}
		
		return map;
	}

}

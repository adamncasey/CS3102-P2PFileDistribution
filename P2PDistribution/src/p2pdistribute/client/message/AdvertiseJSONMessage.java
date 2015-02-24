package p2pdistribute.client.message;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import p2pdistribute.common.message.JSONMessage;

public class AdvertiseJSONMessage extends JSONMessage {
	
	// Map<FileID, ChunkID>.
	public final List<List<Integer>> chunksComplete;

	public AdvertiseJSONMessage(List<List<Integer>> chunksComplete, String cmd, String metaHash) {
		super(cmd, metaHash);
		
		this.chunksComplete = chunksComplete;
	}
	
	public AdvertiseJSONMessage(int[][] chunksComplete, byte[] metaHash) {
		super("advertise_chunks", new String(Hex.encodeHex(metaHash)));
		
		this.chunksComplete = convertIntsToLists(chunksComplete);
	}

	@Override
	public Map<String, Object> getJSON() {
		Map<String, Object> map = super.getJSON();
		
		map.put("chunks", chunksComplete);
		
		return map;
	}
	
	/**
	 * Converts an array of arrays into a Map
	 * @note No bounds checking is performed. This should only be called using a value which must be correct
	 */
	private List<List<Integer>> convertIntsToLists(int[][] chunksComplete) {
		List<List<Integer>> list = new LinkedList<>();
		
		for(int[] row : chunksComplete) {
			
			list.add(Arrays.asList(new Integer[] { row[0], row[1] }));
		}
		
		return list;
	}

}

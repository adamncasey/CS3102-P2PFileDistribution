package p2pdistribute.client.filemanager;

import p2pdistribute.common.p2pmeta.chunk.ChunkMetadata;

public class P2PChunk {
	public final ChunkMetadata meta;
	
	private Status chunkStatus;
	private final HashAlgorithm hashFunc;
	
	public P2PChunk(ChunkMetadata meta, HashAlgorithm hashFunc) {
		this.meta = meta;
		this.hashFunc = hashFunc;

		this.chunkStatus = Status.UNKNOWN;
	}
	
	public void verifyChunk(byte[] data) {

		chunkStatus = Status.INCOMPLETE;
		
		assert data.length == meta.size;
		
		if(hashFunc.verifyData(data, meta.hash)) {
			chunkStatus = Status.COMPLETE;
		}
	}
	
	public Status getStatus() {
		return chunkStatus;
	}
}

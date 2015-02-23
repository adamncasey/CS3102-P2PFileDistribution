package p2pdistribute.client.filemanager;

import p2pdistribute.common.p2pmeta.chunk.ChunkMetadata;

public class P2PChunk {
	public final ChunkMetadata meta;
	
	private final HashAlgorithm hashFunc;
	
	public P2PChunk(ChunkMetadata meta, HashAlgorithm hashFunc) {
		this.meta = meta;
		this.hashFunc = hashFunc;
	}
	
	public Status verifyChunk(byte[] data) {
		
		assert data.length == meta.size;
		
		if(hashFunc.verifyData(data, meta.hash)) {
			return Status.COMPLETE;
		}
		
		return Status.INCOMPLETE;
	}
}

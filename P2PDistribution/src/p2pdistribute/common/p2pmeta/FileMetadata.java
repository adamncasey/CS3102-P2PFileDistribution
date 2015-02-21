package p2pdistribute.common.p2pmeta;

import p2pdistribute.common.p2pmeta.chunk.ChunkMetadata;


public class FileMetadata {
	public final String filename;
	
	public final byte[] fileHash;
	
	public final ChunkMetadata[] chunks;
	
	
	public FileMetadata(String filename, byte[] fileHash, ChunkMetadata[] chunks) {
		assert chunks != null;
		
		this.filename = filename;
		this.fileHash = fileHash;
		this.chunks = chunks;
		
	}
	
	public int getFileSize() {
		int size = 0;
		for(ChunkMetadata chunk : chunks) {
			size += chunk.size;
		}
		
		return size;
	}
}

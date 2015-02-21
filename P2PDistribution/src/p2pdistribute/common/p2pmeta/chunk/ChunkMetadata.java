package p2pdistribute.common.p2pmeta.chunk;

public class ChunkMetadata {
	public final int size;
	public final byte[] hash;
	
	public ChunkMetadata(int size, byte[] hash) {
		this.size = size;
		this.hash = hash;
	}
}

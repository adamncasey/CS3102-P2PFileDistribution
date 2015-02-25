package p2pdistribute.client.filemanager;

public interface ChunkStatusChangeHandler {
	public void onChunkComplete(int fileid, int chunkid);
}

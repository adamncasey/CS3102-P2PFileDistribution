package p2pdistribute.client.filemanager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import p2pdistribute.common.p2pmeta.FileMetadata;
import p2pdistribute.common.p2pmeta.chunk.ChunkMetadata;

public class P2PFile {
	public final FileMetadata meta;
	public final long fileSize;
	
	private final Path destinationFolder;
	private P2PChunk[] chunks;
	private RandomAccessFile file;
	
	public P2PFile(Path destination, FileMetadata meta, HashAlgorithm hashFunc) {
		this.meta = meta;
		this.destinationFolder = destination;
		
		this.fileSize = calculateFileSize(meta.chunks);
		
		chunks = new P2PChunk[meta.chunks.length];
		
		for(int i=0; i<chunks.length; i++) {
			chunks[i] = new P2PChunk(meta.chunks[i], hashFunc);
		}
	}

	/**
	 * Prepares the file for downloading/uploading
	 * Will ensure files exist, and are of the correct size
	 * 
	 * If some chunks are already owned, this will be reflected in the return value
	 * 
	 * @return
	 * @throws P2PFilePreparationException - Unable to create/expand/read to files
	 */
	public Status[] prepare() throws P2PFilePreparationException {
		
		allocateFile();
		
		return verifyChunks();
	}
	
	/**
	 * Returns the number of chunks this file is made up of
	 * @return
	 */
	public int getTotalChunks() {
		return meta.chunks.length;
	}
	
	/**
	 * Writes the chunk data to the file, and returns the Status of the chunk after writing the data
	 * @param chunkid - Chunk ID to write data to
	 * @param data - The data to write into Chunk ID.
	 * @return
	 * @throws IOException
	 */
	public Status writeChunkData(int chunkid, byte[] data) throws IOException {
		assert chunkid < chunks.length;
	
		if(data.length != chunks[chunkid].meta.size) {
			System.err.println("Received unexpected chunk data length");
			return Status.INCOMPLETE;
		}
		long offset = getChunkOffset(chunkid);
		
		file.seek(offset);
		file.write(data, 0, data.length);
		
		return chunks[chunkid].verifyChunk(data);
	}

	/**
	 * Reads chunk data from disk
	 * @param chunkid
	 * @return
	 * @throws IOException
	 */
	public byte[] readChunkData(int chunkid) throws IOException {
		if(chunkid >= chunks.length) {
			
			throw new IOException("Cannot read invalid chunkid.");
		}
		long offset = getChunkOffset(chunkid);
		
		file.seek(offset);
		byte[] data = new byte[meta.chunks[chunkid].size];
		file.read(data, 0, data.length);

		return data;
	}
	
	private long getChunkOffset(int chunkid) {

		long offset = 0;
		for(int i=0; i<chunkid; i++) {
			offset += chunks[i].meta.size;
		}
		
		return offset;
	}
	
	private void allocateFile() throws P2PFilePreparationException {
		Path path = destinationFolder.resolve(meta.filename);
		
		if(!Files.exists(path)) {
			try {
				path = Files.createFile(path);
			} catch (IOException e) {
				throw new P2PFilePreparationException("Unable to create file " + meta.filename + ": " + e.getMessage());
			}
		}
		
		try {
			file = new RandomAccessFile(path.toFile(), "rw");
			
			if(file.length() != this.fileSize) {
				file.setLength(fileSize);
			}
		} catch(IOException e) {
			throw new P2PFilePreparationException("Unable to allocate file " + meta.filename + ": " + e.getMessage());
		}
	}
	
	private Status[] verifyChunks() throws P2PFilePreparationException {
		
		Status[] statuses = new Status[chunks.length];
		int i=0;		
		
		try {
			file.seek(0);
			for(P2PChunk chunk : chunks) {
				byte[] data = new byte[chunk.meta.size];// Read chunk data from file
				file.read(data, 0, data.length);
			
				statuses[i++] = chunk.verifyChunk(data);
			}
		} catch(IOException e) {
			throw new P2PFilePreparationException("Error occurred verifying file chunks: " + e.getMessage());
		}
		
		return statuses;
	}
	
	private long calculateFileSize(ChunkMetadata[] chunks) {
		long length = 0;
		
		for(ChunkMetadata chunk : chunks) {
			length += chunk.size;
		}
		
		return length;
	}
}

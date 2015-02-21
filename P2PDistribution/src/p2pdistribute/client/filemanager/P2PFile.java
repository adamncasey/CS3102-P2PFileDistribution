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

	public void prepare() throws P2PFilePreparationException {
		
		allocateFile();
		
		verifyChunks();
	}
	
	public int getTotalChunks() {
		return meta.chunks.length;
	}
	
	public int getCompleteChunks() {
		
		return chunkStatusCount(Status.COMPLETE);
	}
	
	public int getIncompleteChunks() {
		
		return chunkStatusCount(Status.INCOMPLETE);
	}
	
	private int chunkStatusCount(Status status) {
		int count = 0;
		for(P2PChunk chunk : chunks) {
			if(chunk.getStatus() == status) {
				count++;
			}
		}
		
		return count;
	}
	
	public void writeChunkData(int chunkid, int dataoffset, int data) {
		// TODO write writeChunkData
		throw new RuntimeException("not implemented");
		
		// Write data into file at correct location
		
		// verify chunk again.
	}
	
	public byte[] readChunkData(int chunkid, int dataoffset, int datalength) {
		// TODO Write readChunkData
		throw new RuntimeException("not implemented");
		
		// Read data from file at correct location
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
	
	private void verifyChunks() throws P2PFilePreparationException {
		
		try {
			file.seek(0);
			for(P2PChunk chunk : chunks) {
				byte[] data = new byte[chunk.meta.size];// Read chunk data from file
				file.read(data, 0, data.length);
			
				chunk.verifyChunk(data);
			}
		} catch(IOException e) {
			throw new P2PFilePreparationException("Error occurred verifying file chunks: " + e.getMessage());
		}
	}
	
	private long calculateFileSize(ChunkMetadata[] chunks) {
		long length = 0;
		
		for(ChunkMetadata chunk : chunks) {
			length += chunk.size;
		}
		
		return length;
	}
}

package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import p2pdistribute.p2pmeta.FileMetadata;
import p2pdistribute.p2pmeta.chunk.ChunkMetadata;

public class FileMetaDataTest {

	@Test
	public void testTotalSize() {		
		ArrayList<ChunkMetadata> list = new ArrayList<>();
		list.add(new ChunkMetadata(50, new byte[1]));
		list.add(new ChunkMetadata(150, new byte[1]));
		
		FileMetadata metadata = new FileMetadata("test", new byte[1], list.toArray(new ChunkMetadata[2]));
		
		assertEquals(list.get(0).size + list.get(1).size, metadata.getFileSize());
	}

}

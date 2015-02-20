package tests;

import static org.junit.Assert.*;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import p2pdistribute.message.MessageParser;
import p2pdistribute.p2pmeta.FileMetadata;
import p2pdistribute.p2pmeta.P2PMetadata;
import p2pdistribute.p2pmeta.FileParser;
import p2pdistribute.p2pmeta.ParserException;
import p2pdistribute.p2pmeta.chunk.ChunkMetadata;

public class P2PMetadataParserTests {

	@Test
	public void testChunkMetadataParse() throws ParseException, DecoderException, ParserException {
		String chunk = "{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"			\"hash\": \"de04d58dc5ccc4b9671c3627fb8d626fe4a15810bc1fe3e724feea761965fb71\"\r\n" + 
				"		}";
		JSONObject obj = MessageParser.parseJSON(chunk);
		
		ChunkMetadata meta = FileParser.parseChunkMetadata(obj);
		
		assertEquals(128812, meta.size);
		byte[] hash = Hex.decodeHex("de04d58dc5ccc4b9671c3627fb8d626fe4a15810bc1fe3e724feea761965fb71".toCharArray());
		assertArrayEquals(hash, meta.hash);
	}
	@Test(expected=ParserException.class)
	public void testChunkMetadataParseInvalidStructure() throws ParseException, ParserException {
		String chunk = "{\r\n" + 
				"			\"size\": [128812], \r\n" + 
				"			\"hash\": \"de04d58dc5ccc4b9671c3627fb8d626fe4a15810bc1fe3e724feea761965fb71\"\r\n" + 
				"		}";
		JSONObject obj = MessageParser.parseJSON(chunk);
		
		FileParser.parseChunkMetadata(obj);
	}
	@Test(expected=ParserException.class)
	public void testChunkMetadataParseNoHash() throws ParseException, ParserException {
		String chunk = "{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"		}";
		JSONObject obj = MessageParser.parseJSON(chunk);
		
		FileParser.parseChunkMetadata(obj);
	}
	@Test(expected=ParserException.class)
	public void testChunkMetadataParseNoSize() throws ParseException, ParserException {
		String chunk = "{\r\n" + 
				"			\"hash\": \"de04d58dc5ccc4b9671c3627fb8d626fe4a15810bc1fe3e724feea761965fb71\"\r\n" + 
				"		}";
		JSONObject obj = MessageParser.parseJSON(chunk);
		
		FileParser.parseChunkMetadata(obj);
	}
	
	
	@Test
	public void testFileMetadataParse() throws DecoderException, ParserException, ParseException {
		String file = "{\r\n" + 
				"		\"name\": \"pg44823.txt\",\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\r\n" + 
				"		\"chunks\": [{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"			\"hash\": \"de04d58dc5ccc4b9671c3627fb8d626fe4a15810bc1fe3e724feea761965fb71\"\r\n" + 
				"		}]" + 
				"}";
		JSONObject obj = MessageParser.parseJSON(file);
		
		FileMetadata meta = FileParser.parseFileMetadata(obj);
		
		assertEquals("pg44823.txt", meta.filename);
		byte[] hash = Hex.decodeHex("d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db".toCharArray());
		assertArrayEquals(hash, meta.fileHash);
		
		
		assertEquals(1, meta.chunks.length);
		

		assertEquals(128812, meta.chunks[0].size);
		byte[] hash2 = Hex.decodeHex("de04d58dc5ccc4b9671c3627fb8d626fe4a15810bc1fe3e724feea761965fb71".toCharArray());
		assertArrayEquals(hash2, meta.chunks[0].hash);
	}
	@Test(expected=ParserException.class)
	public void testFileMetadataParseNoName() throws ParseException, ParserException {
		String file = "{\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\r\n" + 
				"		\"chunks\": [{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"			\"hash\": \"de04d58dc5ccc4b9671c3627fb8d626fe4a15810bc1fe3e724feea761965fb71\"\r\n" + 
				"		}]" + 
				"}";
		JSONObject obj = MessageParser.parseJSON(file);
		
		FileParser.parseFileMetadata(obj);
	}
	@Test(expected=ParserException.class)
	public void testFileMetadataParseNoHash() throws ParseException, ParserException {		
		String file = "{\r\n" + 
			"		\"name\": \"pg44823.txt\",\r\n" + 
			"		\r\n" + 
			"		\"chunks\": [{\r\n" + 
			"			\"size\": 128812, \r\n" + 
			"			\"hash\": \"de04d58dc5ccc4b9671c3627fb8d626fe4a15810bc1fe3e724feea761965fb71\"\r\n" + 
			"		}]" + 
			"}";
		JSONObject obj = MessageParser.parseJSON(file);
		
		FileParser.parseFileMetadata(obj);
	}
	@Test(expected=ParserException.class)
	public void testFileMetadataParseNoChunks() throws ParseException, ParserException {
		String file = "{\r\n" + 
				"		\"name\": \"pg44823.txt\",\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\r\n" + 
				"}";
		JSONObject obj = MessageParser.parseJSON(file);
		
		FileParser.parseFileMetadata(obj);
	}
	@Test(expected=ParserException.class)
	public void testFileMetadataParseZeroChunks() throws ParseException, ParserException {
		String file = "{\r\n" + 
				"		\"name\": \"pg44823.txt\",\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\"chunks\":[]\r\n" + 
				"}";
		JSONObject obj = MessageParser.parseJSON(file);
		
		FileParser.parseFileMetadata(obj);
	}
	
	
	@Test
	public void testEntireMetadataParse() throws ParserException, DecoderException {
		String p2pmeta = "{\r\n" + 
				"	\"hash_type\": \"sha-256\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"files\": [{\r\n" + 
				"		\"name\": \"pg44823.txt\",\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\r\n" + 
				"		\"chunks\": [{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"			\"hash\": \"\"\r\n" + 
				"		}]\r\n" + 
				"	}]\r\n" + 
				"}";
		
		P2PMetadata meta = FileParser.parseP2PMetaFileContents(p2pmeta);
		
		assertEquals("sha-256", meta.hashType);
		byte[] hash = Hex.decodeHex("2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43".toCharArray());
		assertArrayEquals(hash, meta.metaHash);
		
		
		assertEquals(1, meta.files.length);
		
		assertEquals("pg44823.txt", meta.files[0].filename);
		// If we get this far it can't be messed up too badly. We test actual fileMetadata parsing above
	}
	@Test(expected=ParserException.class)
	public void testEntireMetadataParseNoHashType() throws ParserException {
		String p2pmeta = "{\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"files\": [{\r\n" + 
				"		\"name\": \"pg44823.txt\",\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\r\n" + 
				"		\"chunks\": [{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"			\"hash\": \"\"\r\n" + 
				"		}]\r\n" + 
				"	}]\r\n" + 
				"}";
		
		FileParser.parseP2PMetaFileContents(p2pmeta);
	}
	@Test(expected=ParserException.class)
	public void testEntireMetadataParseNoMetaHash() throws ParserException {
		String p2pmeta = "{\r\n" + 
				"	\"hash_type\": \"sha-256\",\r\n" + 
				"	\"files\": [{\r\n" + 
				"		\"name\": \"pg44823.txt\",\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\r\n" + 
				"		\"chunks\": [{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"			\"hash\": \"\"\r\n" + 
				"		}]\r\n" + 
				"	}]\r\n" + 
				"}";
		
		FileParser.parseP2PMetaFileContents(p2pmeta);
	}
	@Test(expected=ParserException.class)
	public void testEntireMetadataParseNoFiles() throws ParserException {
		String p2pmeta = "{\r\n" + 
				"	\"hash_type\": \"sha-256\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"}";
		
		FileParser.parseP2PMetaFileContents(p2pmeta);
	}
	@Test(expected=ParserException.class)
	public void testEntireMetadataParseZeroFiles() throws ParserException {
		String p2pmeta = "{\r\n" + 
				"	\"hash_type\": \"sha-256\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"files\": []\r\n" + 
				"}";
		
		FileParser.parseP2PMetaFileContents(p2pmeta);
	}
	@Test(expected=ParserException.class)
	public void testEntireMetadataParseUnknownHashAlgorithm() throws ParserException {
		String p2pmeta = "{\r\n" + 
				"	\"hash_type\": \"sha-onemillion\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"files\": [{\r\n" + 
				"		\"name\": \"pg44823.txt\",\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\r\n" + 
				"		\"chunks\": [{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"			\"hash\": \"\"\r\n" + 
				"		}]\r\n" + 
				"	}]\r\n" + 
				"}";
		
		FileParser.parseP2PMetaFileContents(p2pmeta);
	}
	
	/**
	 * The p2pmeta file's contents must hash to the same value as "meta_hash"
	 * Hashing algorithm is effectively: Remove hash value from message, then hash the remaining string using hash_type.
	 * @throws ParserException
	 */
	@Ignore @Test(expected=ParserException.class)
	public void testEntireMetadataParseInvalidMetaHash() throws ParserException {
		String p2pmeta = "{\r\n" + 
				"	\"hash_type\": \"sha-256\",\r\n" + 
				"	\"meta_hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"	\"files\": [{\r\n" + 
				"		\"name\": \"pg44823.txt\",\r\n" + 
				"		\"hash\": \"d48ff4b2f68a10fd7c86f185a6ccede0dc0f2c48538d697cb33b6ada3f1e85db\",\r\n" + 
				"		\r\n" + 
				"		\"chunks\": [{\r\n" + 
				"			\"size\": 128812, \r\n" + 
				"			\"hash\": \"\"\r\n" + 
				"		}]\r\n" + 
				"	}]\r\n" + 
				"}";
		
		FileParser.parseP2PMetaFileContents(p2pmeta);
	}
}

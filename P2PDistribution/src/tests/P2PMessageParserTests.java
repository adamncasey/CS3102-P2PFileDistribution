package tests;

import static org.junit.Assert.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;

import p2pdistribute.client.message.AdvertiseJSONMessage;
import p2pdistribute.client.message.ControlMessage;
import p2pdistribute.client.message.DataMessage;
import p2pdistribute.client.message.Message;
import p2pdistribute.client.message.MessageType;
import p2pdistribute.client.message.P2PMessageParser;
import p2pdistribute.client.message.RequestChunkJSONMessage;
import p2pdistribute.common.p2pmeta.ParserException;

public class P2PMessageParserTests {

	private PipedOutputStream pipeOut;
	
	private DataOutputStream out;
	private InputStream pipeSink;
	
	@Before
	public void setup() throws IOException {
		pipeOut = new PipedOutputStream();
		out = new DataOutputStream(pipeOut);
		pipeSink = new PipedInputStream(pipeOut);
	}
	
	// Test control message
	@Test
	public void testControlMessage() throws IOException, ParserException {
		String advertiseMsg = "{\r\n" + 
				"	\"cmd\": \"advertise_chunks\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"chunks\": [[0, 0], [0, 1]]\r\n" + 
				"}";
		
		byte[] header = ByteBuffer.allocate(4).putInt(advertiseMsg.length()).array();
		// 4 bits zero (version)
		// 4 bits zero (Control Message)
		header[0] = 0;
		
		out.write(header);
		
		out.writeBytes(advertiseMsg);
	    
	    Message msg = P2PMessageParser.readMessage(pipeSink);

	    assertEquals(0, msg.version);
	    assertEquals(MessageType.CONTROL, msg.type);
	    assertEquals(advertiseMsg.length(), msg.length);
	}
	
	// Test data message
	
	// Test advertise control message
	@Test
	public void testAdvertiseMessage() throws IOException, ParserException {
		String advertiseMsg = "{\r\n" + 
				"	\"cmd\": \"advertise_chunks\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"chunks\": [[4, 0], [0, 2]]\r\n" + 
				"}";
		
		byte[] header = ByteBuffer.allocate(4).putInt(advertiseMsg.length()).array();
		// 4 bits zero (version)
		// 4 bits zero (Control Message)
		header[0] = 0;
		
		out.write(header);
		out.writeBytes(advertiseMsg);		
	    
	    Message message = P2PMessageParser.readMessage(pipeSink);

	    assertTrue(message instanceof ControlMessage);
	    
	    ControlMessage msg = (ControlMessage) message;
	    
	    assertNotNull(msg.payload);

	    assertEquals("2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43", msg.payload.metaHash);
	    assertEquals("advertise_chunks", msg.payload.cmd);
	    
	    assertTrue(msg.payload instanceof AdvertiseJSONMessage);
	    List<List<Integer>> chunks = new LinkedList<>();
	    chunks.add(Arrays.asList(new Integer[] {4, 0}));
	    chunks.add(Arrays.asList(new Integer[] {0, 2}));
	    assertEquals(chunks, ((AdvertiseJSONMessage)msg.payload).chunksComplete);
	}
	
	// Test request chunk control message
	@Test
	public void testRequestChunkMessage() throws IOException, ParserException {
		String advertiseMsg = "{\r\n" + 
				"	\"cmd\": \"request_chunk\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"chunk\": [0, 0]\r\n" + 
				"}";
		
		byte[] header = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(advertiseMsg.length()).array();
		// 4 bits zero (version)
		// 4 bits zero (Control Message)
		header[0] = 0;
		
		out.write(header);
		out.writeBytes(advertiseMsg);		
	    
	    Message message = P2PMessageParser.readMessage(pipeSink);

	    assertTrue(message instanceof ControlMessage);
	    
	    ControlMessage msg = (ControlMessage) message;
	    
	    assertNotNull(msg.payload);

	    assertEquals("2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43", msg.payload.metaHash);
	    assertEquals("request_chunk", msg.payload.cmd);
	    
	    assertTrue(msg.payload instanceof RequestChunkJSONMessage);
	    assertEquals(0, ((RequestChunkJSONMessage)msg.payload).fileid);
	    assertEquals(0, ((RequestChunkJSONMessage)msg.payload).chunkid);
	}

	// Test control message
	@Test
	public void testDataMessage() throws IOException, DecoderException, ParserException {
		byte[] metaHash = Hex.decodeHex("2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43".toCharArray());
		
		byte[] data = new byte[] { 0x55, 0x00, 0x55, 0x00 };
		ByteBuffer dataHeader = ByteBuffer.allocate(41).order(ByteOrder.BIG_ENDIAN);
		dataHeader.put((byte) 32); // 1 byte hash length
		dataHeader.put(metaHash); // 16 bytes metaHash
		
		dataHeader.putInt(0); // 4 bytes fileid (0)
		dataHeader.putInt(0); // 4 bytes chunk id
		byte[] dataHeaderBytes = dataHeader.array();
		
		int length = data.length + dataHeaderBytes.length;
		
		byte[] header = ByteBuffer.allocate(4).putInt(length).array();
		// 4 bits zero (version)
		// 4 bits zero (Control Message)
		header[0] = 1;
		
		out.write(header);
		out.write(dataHeaderBytes);
		out.write(data);
	    
	    Message message = P2PMessageParser.readMessage(pipeSink);

	    assertEquals(0, message.version);
	    assertEquals(MessageType.DATA, message.type);
	    assertEquals(length, message.length);
	    assertTrue(message instanceof DataMessage);
	    
	    DataMessage msg = (DataMessage)message;

	    assertArrayEquals(metaHash, msg.metaHash);
	    assertArrayEquals(data, msg.data);
	    assertEquals(0, msg.fileid);
	    assertEquals(0, msg.chunkid);
		
	}
	
}

package tests;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.codec.DecoderException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import p2pdistribute.message.MessageParser;
import p2pdistribute.p2pmeta.ParserException;
import p2pdistribute.swarmmanager.message.SwarmManagerMessage;

public class MessageParserTests {

	@Test
	public void testSwarmManagerRegisterMessage() throws ParserException {
		String line = "{\r\n" + 
				"	\"cmd\": \"register\",\r\n" + 
				"	\"port\": 8844,\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"\r\n" + 
				"}";
		
		SwarmManagerMessage msg = MessageParser.parseSwarmManageMessage(line);
		
		assertEquals("register", msg.cmd);
		assertEquals(8844, msg.getPort());
		assertEquals("2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43", msg.metaHash);
	}
	@Test(expected=RuntimeException.class)
	public void testSwarmManagerRegisterMessageExceptionGetPeers() throws ParseException, DecoderException, ParserException {
		String line = "{\r\n" + 
				"	\"cmd\": \"register\",\r\n" + 
				"	\"port\": 8844,\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"\r\n" + 
				"}";
		
		SwarmManagerMessage msg = MessageParser.parseSwarmManageMessage(line);
		
		msg.getPeers();
	}
	
	@Test(expected=ParserException.class)
	public void testSwarmManagerRegisterNoPort() throws ParserException {
		String line = "{\r\n" + 
				"	\"cmd\": \"register\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"\r\n" + 
				"}";
		
		MessageParser.parseSwarmManageMessage(line);
	}	
	
	@Test(expected=ParserException.class)
	public void testSwarmManagerMessageNoCmd() throws ParserException {
		String line = "{\r\n" + 
				"	\"port\": 8844,\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"\r\n" + 
				"}";
		
		MessageParser.parseSwarmManageMessage(line);
	}
	
	@Test(expected=ParserException.class)
	public void testSwarmManagerMessageNoHash() throws ParserException {
		String line = "{\r\n" + 
				"	\"cmd\": \"register\",\r\n" + 
				"	\"port\": 8844,\r\n" + 
				"}";
		
		MessageParser.parseSwarmManageMessage(line);
	}

	@Test
	public void testSwarmManagerRequestPeersMessage() throws ParserException {
		String line = "{\r\n" + 
				"	\"cmd\": \"request_peers\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"\r\n" + 
				"}";
		
		SwarmManagerMessage msg = MessageParser.parseSwarmManageMessage(line);
		
		assertEquals("request_peers", msg.cmd);
		assertEquals("2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43", msg.metaHash);
	}
	
	@Test(expected=ParserException.class)
	public void testSwarmManagerRequestPeersGetPortException() throws ParserException {
		String line = "{\r\n" + 
				"	\"cmd\": \"request_peers\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"\r\n" + 
				"}";
		
		SwarmManagerMessage msg = MessageParser.parseSwarmManageMessage(line);
		
		msg.getPort();
	}
	
	@Test(expected=ParserException.class)
	public void testSwarmManagerRequestPeersGetPeersException() throws ParserException {
		String line = "{\r\n" + 
				"	\"cmd\": \"request_peers\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"\r\n" + 
				"}";
		
		SwarmManagerMessage msg = MessageParser.parseSwarmManageMessage(line);
		
		msg.getPeers();
	}

	@Test
	public void testSwarmManagerPeersMessage() throws ParserException, UnknownHostException {
		String line = "{\r\n" + 
				"	\"cmd\": \"peers\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"peers\": [[\"138.251.204.45\", 4499], [\"138.251.204.35\", 8622]]\r\n" + 
				"}";
		
		SwarmManagerMessage msg = MessageParser.parseSwarmManageMessage(line);
		
		assertEquals("peers", msg.cmd);
		assertEquals("2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43", msg.metaHash);

		assertEquals(2, msg.getPeers().size());
		assertEquals(4499, msg.getPeers().get(1).port);
		assertEquals(8622, msg.getPeers().get(2).port);
		

		InetAddress addr = InetAddress.getByName("138.251.204.45");
		assertEquals(addr, msg.getPeers().get(1).address);
		
		addr = InetAddress.getByName("138.251.204.35");
		assertEquals(addr, msg.getPeers().get(2).address);
	}

	@Test(expected=ParserException.class)
	public void testSwarmManagerPeersMessageNoPeers() throws ParserException, UnknownHostException {
		String line = "{\r\n" + 
				"	\"cmd\": \"peers\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"}";
		
		MessageParser.parseSwarmManageMessage(line);
	}
	

	@Test(expected=ParserException.class)
	public void testSwarmManagerPeersMessagePeersInvalidStructure() throws ParserException, UnknownHostException {
		String line = "{\r\n" + 
				"	\"cmd\": \"peers\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"peers\": [{\"ip\": \"138.251.204.45\", \"port\": 4499], [\"138.251.204.35\", 8622]]\r\n" + 
				"}";
		
		MessageParser.parseSwarmManageMessage(line);
	}


	@Test(expected=ParserException.class)
	public void testSwarmManagerPeersMessagePeersIpNotString() throws ParserException, UnknownHostException {
		String line = "{\r\n" + 
				"	\"cmd\": \"peers\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"peers\": [[1234321, 4499], [\"138.251.204.35\", 8622]]\r\n" + 
				"}";
		
		MessageParser.parseSwarmManageMessage(line);
	}

	@Test(expected=ParserException.class)
	public void testSwarmManagerPeersMessagePeersPortNotNumber() throws ParserException, UnknownHostException {
		String line = "{\r\n" + 
				"	\"cmd\": \"peers\",\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\",\r\n" + 
				"	\"peers\": [[\"138.251.204.45\", []], [\"138.251.204.35\", 8622]]\r\n" + 
				"}";
		
		MessageParser.parseSwarmManageMessage(line);
	}
}

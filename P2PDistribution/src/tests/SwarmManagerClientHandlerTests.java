package tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import p2pdistribute.common.Peer;
import p2pdistribute.common.message.SwarmManagerMessage;
import p2pdistribute.common.p2pmeta.ParserException;
import p2pdistribute.message.MessageParser;
import p2pdistribute.swarmmanager.ClientHandler;
import p2pdistribute.swarmmanager.SwarmIndex;

public class SwarmManagerClientHandlerTests {
	
	@Test
	public void testRegisterHandling() throws IOException, InterruptedException {
		SwarmIndex index = mock(SwarmIndex.class);
		Socket client = mock(Socket.class);
		
		// Put message into socket.read
		String message = "{" + 
				"	\"cmd\": \"register\"," + 
				"	\"port\": 8844," + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"" + 
				"}\n";
		InputStream stream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
		// Put addr into socket.getAddress
		InetAddress addr = InetAddress.getByName("138.251.204.3");
		
	    PipedInputStream pipeInput = new PipedInputStream();	    
		
		// Only run through one loop of thread
		when(client.isClosed()).thenReturn(false).thenReturn(true);
		when(client.getInputStream()).thenReturn(stream);
		when(client.getOutputStream()).thenReturn(new PipedOutputStream(pipeInput));
		
		when(client.getInetAddress()).thenReturn(addr);
		
		ClientHandler handler = new ClientHandler(client, index);
		handler.readMessage();
		
		verify(index).registerClient(addr, 8844, "2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43");
	}
	
	@Test
	public void testRequestPeers() throws IOException, InterruptedException, ParserException {
		SwarmIndex index = mock(SwarmIndex.class);
		Socket client = mock(Socket.class);
		
		// Put message into socket input stream
		String message = "{" + 
				"	\"cmd\": \"request_peers\"," + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"" + 
				"}\n";
		InputStream stream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
		
	    PipedInputStream pipeInput = new PipedInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(pipeInput));
	    
	    OutputStream out = new PipedOutputStream(pipeInput);
		
		// Only run through one loop of thread
		when(client.isClosed()).thenReturn(false).thenReturn(true);
		when(client.getInputStream()).thenReturn(stream);
		when(client.getOutputStream()).thenReturn(out);
		
		Peer peer = new Peer(InetAddress.getByName("138.251.204.3"), 63728);
		List<Peer> peers = new LinkedList<>();
		peers.add(peer);
		
		when(index.get("2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43"))
			.thenReturn(peers);
		
		ClientHandler handler = new ClientHandler(client, index);
		handler.readMessage();
		
		String expectedMessage = "{" + 
				"	\"cmd\": \"peers\"," + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"," + 
				"	\"peers\": [[\"138.251.204.3\", 63728]]" + 
				"}\n";
		System.out.println(expectedMessage);
		
		String msg = reader.readLine();
		
		System.out.println(msg);
		
		SwarmManagerMessage expected = MessageParser.parseSwarmManageMessage(expectedMessage);
		SwarmManagerMessage actual = MessageParser.parseSwarmManageMessage(msg);
		
		assertEquals(expected, actual);
	}
}

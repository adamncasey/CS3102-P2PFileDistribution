package tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import p2pdistribute.swarmmanager.ClientHandler;
import p2pdistribute.swarmmanager.SwarmIndex;

public class SwarmManagerClientHandlerTests {

	// TODO Mochitest?
	
	// TODO if "register" sent, test Swarmindex updated correctly.
	@Test
	public void testRegisterHandling() throws IOException, InterruptedException {
		SwarmIndex index = mock(SwarmIndex.class);
		Socket client = mock(Socket.class);
		
		// Put message into socket.read
		String message = "{\r\n" + 
				"	\"cmd\": \"register\",\r\n" + 
				"	\"port\": 8844,\r\n" + 
				"	\"meta_hash\": \"2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43\"\r\n" + 
				"}";
		InputStream stream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
		// Put addr into socket.getAddress
		InetAddress addr = InetAddress.getByName("138.251.204.3");
		
		// Only run through one loop of thread
		when(client.isClosed()).thenReturn(false).thenReturn(true);
		when(client.getInputStream()).thenReturn(stream);
		
		when(client.getInetAddress()).thenReturn(addr);
		
		ClientHandler handler = new ClientHandler(client, index);
		Thread thread = new Thread(handler);
		thread.start();
		thread.join();
		
		verify(index).registerClient(addr, 8844, "2a8593d74a066ec1f3902e72ae468489bbda8b0444758a19fd6b8bf29ed1bf43");
	}
	
	// TODO if "request_peers" is sent, test "peers" is sent back correctly
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}

package p2pdistribute.client.message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import p2pdistribute.common.message.JSONMessage;
import p2pdistribute.common.message.MessageParserUtils;
import p2pdistribute.common.message.SwarmManagerMessageParser;
import p2pdistribute.common.p2pmeta.ParserException;

/**
 * Parses all messages for P2P connections
 *
 */
public class P2PMessageParser {
	
	/**
	 * Reads a message from the given InputStream, returning the parsed Message or throwing an Exception
	 * @param stream
	 * @return Message on success
	 * @throws ParserException - Invalid data read off socket
	 * @throws IOException - Socket error occurred
	 */
	public static Message readMessage(InputStream stream) throws ParserException, IOException {
		
		byte[] header = readBytes(stream, 4);
		
		byte version = (byte) (header[0] & (byte)0xf0);
		byte typeValue = (byte) (header[0] & (byte)0x0f);
		
		if(version != 0) {
			throw new ParserException("Encountered unsupported version: " + version);
		}
		
		header[0] = 0;
		int length = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN).getInt();
		
		if(typeValue < 0 || typeValue >= MessageType.values().length) {
			throw new ParserException("Unsupported Message type: " + typeValue);
		}
		
		MessageType type = MessageType.values()[typeValue];
		
		switch(type) {
		case CONTROL:
			return readControlMessage(stream, version, length);
		case DATA:
			return readDataMessage(stream, version, length);
		default:
			throw new ParserException("Unsupported Message type: " + typeValue);
		}
	}

	/**
	 * Serialises a JSON Payload into a Message suitable for sending
	 * @param payload
	 * @return Message encoded and stored in a byte array
	 */
	public static byte[] serialiseJSONMessage(JSONMessage payload) {
		byte[] json = MessageParserUtils.serialiseMessageAsJSON(payload).getBytes();
		
		int length = json.length;
		
		// Bounds check (< 2^24 max message data length)
		if(length > 16777215) {
			// Invalid length. Message will be clipped
			length = 16777216;
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(4 + length).order(ByteOrder.BIG_ENDIAN);
		buffer.putInt(length);
		
		buffer.put(json, 0, length);
		
		byte[] data = buffer.array();
		data[0] = 0; // Set version and MessageType to zero.
		
		return data;
	}

	/**
	 * 
	 * Serialises a Data message into a byte array which can be sent over the network
	 * @param data - The data to send
	 * @param metaHash - The metahash this data relates to
	 * @param fileid - The file ID the data relates to
	 * @param chunkid - The chunk ID of the data
	 * @return byte array containing the encoded message
	 */
	public static byte[] serialiseData(byte[] data, byte[] metaHash, int fileid, int chunkid) {
		
		int length = 1 + metaHash.length + 4 + 4 + data.length; 
		
		ByteBuffer dataBuffer = ByteBuffer.allocate(4 + length).order(ByteOrder.BIG_ENDIAN);
		dataBuffer.putInt(length);

		dataBuffer.put((byte)metaHash.length);
		dataBuffer.put(metaHash);
		dataBuffer.putInt(fileid);
		dataBuffer.putInt(chunkid);
		dataBuffer.put(data);
		
		byte[] serialised = dataBuffer.array();
		
		// Set first two bytes to 0x01 (version zero, MessageType 1)
		serialised[0] = 0x01;
		
		return serialised;
	}
	
	private static DataMessage readDataMessage(InputStream stream, byte version, int length) throws IOException {
		
		// read 1 byte (hash length): N
		short hashLength = getShortFromByte(readBytes(stream, 1)[0]);
		
		// Read N bytes (hash)
		byte[] metaHash = readBytes(stream, hashLength);
		
		byte[] ids = readBytes(stream, 8);
		
		ByteBuffer buffer = ByteBuffer.wrap(ids).order(ByteOrder.BIG_ENDIAN);
		
		// Read 4 bytes (file ID)
		int fileid = buffer.getInt();
		// Read 4 bytes (chunk ID)
		int chunkid = buffer.getInt();
		
				// sizeof(hashLength) - sizeof(int) - sizeof(int) - hashLength
		byte[] data = readBytes(stream, length - 1 - 4 - 4 -hashLength);
		
		return new DataMessage(metaHash, fileid, chunkid, data, version, MessageType.DATA, length);
	}
	
	private static ControlMessage readControlMessage(InputStream stream, byte version, int length) throws IOException, ParserException {
		
		byte[] data = readBytes(stream, length);
		
		JSONObject obj = SwarmManagerMessageParser.validateMessage(new String(data));

		String cmd = (String)obj.get("cmd");
		String metaHash = (String)obj.get("meta_hash");
		
		JSONMessage payload = parseControlJSONMessage(obj, cmd, metaHash);
		
		return new ControlMessage(version, MessageType.CONTROL, length, payload);
	}
	
	private static JSONMessage parseControlJSONMessage(JSONObject obj, String cmd, String metaHash) throws ParserException {
		switch(cmd) {
		case "advertise_chunks":
			return parseAdvertiseMessage(obj, cmd, metaHash);
		case "request_chunk":
			return parseRequestMessage(obj, cmd, metaHash);
		
		default:
			throw new ParserException("RFeceived invalid message command: " + cmd);
		}
	}

	private static JSONMessage parseAdvertiseMessage(JSONObject json, String cmd, String metaHash) throws ParserException {
		MessageParserUtils.validateFieldType(json, "chunks", JSONArray.class);

		List<List<Integer>> chunksComplete = new LinkedList<>();
		JSONArray chunks = (JSONArray)json.get("chunks");
		for(Object obj : chunks) {
			int[] values = parseFileChunkIDs(obj);
			chunksComplete.add(Arrays.asList(new Integer[] { values[0], values[1] }));
		}
		
		return new AdvertiseJSONMessage(chunksComplete, cmd, metaHash);
	}

	private static JSONMessage parseRequestMessage(JSONObject json, String cmd, String metaHash) throws ParserException {
		MessageParserUtils.validateFieldType(json, "chunk", JSONArray.class);
		

		int[] chunk = parseFileChunkIDs(json.get("chunk"));
		
		
		return new RequestChunkJSONMessage(chunk[0], chunk[1], cmd, metaHash);
	}
	
	
	private static int[] parseFileChunkIDs(Object obj) throws ParserException {
		if(!MessageParserUtils.validateType(obj, JSONArray.class)) {
			throw new ParserException("Malformed message: Could not parse list of file/chunk IDs.");
		}
		
		JSONArray ids = (JSONArray)obj;
		
		if(ids.size() != 2) {
			throw new ParserException("Badly formed file/chunk details. Should be [fileid integer, chunkid integer]");
		}
		
		boolean typeCheck = MessageParserUtils.validateType(ids.get(0), Long.class);
		typeCheck &= MessageParserUtils.validateType(ids.get(1), Long.class);
		
		if(!typeCheck) {
			throw new ParserException("Badly formed file/chunk details. Should be [fileid integer, chunkid integer]");
		}
		
		return new int[] { ((Long)ids.get(0)).intValue(), ((Long)ids.get(1)).intValue() };
	}

	/**
	 * Attempts to read numBytes from stream. Throws IOException if it was not possible to read exactly numBytes. (EOF etc)
	 * Blocks until numBytes has been read (or socket error has occured)
	 * @param stream
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	private static byte[] readBytes(InputStream stream, int numBytes) throws IOException {
		int count = 0;
		
		byte[] result = new byte[numBytes];
		
		while(count < numBytes) {
			int read = stream.read(result, count, numBytes - count);
			if(read == -1) {
				throw new IOException("Reached EOF");
			}
			count += read;
		}
		
		return result;
	}
	
	private static short getShortFromByte(byte value) {
		// Used to avoid any issues with java's signed byte.
		byte[] bytes = new byte[] { 0x00, value };
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
	}
}

package p2pdistribute.common.message;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import p2pdistribute.common.Peer;
import p2pdistribute.common.p2pmeta.ParserException;


public class SwarmManagerMessageParser {

	/**
	 * Ensures json passed in is well-formed, and conforms to the basic requirement of every network message
	 * @param json
	 * @return
	 * @throws ParserException 
	 */
	public static JSONObject validateMessage(String json) throws ParserException {
		JSONObject obj = MessageParserUtils.parseJSON(json);

		MessageParserUtils.validateFieldType(obj, "cmd", String.class);
		MessageParserUtils.validateFieldType(obj, "meta_hash", String.class);
		
		return obj;
	}
	
	public static SwarmManagerMessage parseSwarmManageMessage(String line) throws ParserException {
		JSONObject obj = validateMessage(line);

		String cmd = (String)obj.get("cmd");
		String metaHash = (String)obj.get("meta_hash");
		
		return parseSwarmMessage(cmd, metaHash, obj);
	}
	
	private static SwarmManagerMessage parseSwarmMessage(String cmd, String metaHash, JSONObject obj) throws ParserException {
		if(cmd.equals("register")) {
			MessageParserUtils.validateFieldType(obj, "port", Long.class);
			
			return new SwarmManagerMessage(cmd, metaHash, ((Long)obj.get("port")).intValue());
		} else if(cmd.equals("request_peers")) {
			
			return new SwarmManagerMessage(cmd, metaHash);
		} else if(cmd.equals("peers")) {
			MessageParserUtils.validateFieldType(obj, "peers", JSONArray.class);
			
			Peer[] peers = convertJSONArrayToPeerArray((JSONArray)obj.get("peers"));
			
			return new SwarmManagerMessage(cmd, metaHash, peers);
		}
		
		throw new ParserException("Received unknown command: " + cmd);
	}

	private static Peer[] convertJSONArrayToPeerArray(JSONArray jsonArray) throws ParserException {
		Peer[] peers = new Peer[jsonArray.size()];
		int i=0;
		for(Object obj : jsonArray) {
			if(!MessageParserUtils.validateType(obj, JSONArray.class)) {
				throw new ParserException("Could not parse list of peers into Peer array. Peer array index of invalid type");
			}
			JSONArray peer = (JSONArray)obj;
			
			if(peer.size() != 2) {
				throw new ParserException("Badly formed peer. Should be [String, Integer]");
			}
			
			boolean typeCheck = MessageParserUtils.validateType(peer.get(0), String.class);
			typeCheck &= MessageParserUtils.validateType(peer.get(1), Long.class);
			
			if(!typeCheck) {
				throw new ParserException("Badly formed peer. Should be [String, Integer]");
			}
			
			try {
				peers[i++] = new Peer(InetAddress.getByName((String) peer.get(0)), ((Long)peer.get(1)).intValue());
				
			} catch (UnknownHostException e) {
				throw new ParserException("Badly formed IP Address encountered whilst parsing peer");
			}
		}
		
		return peers;
	}
}

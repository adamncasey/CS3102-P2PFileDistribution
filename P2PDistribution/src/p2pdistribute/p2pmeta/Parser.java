package p2pdistribute.p2pmeta;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import p2pdistribute.p2pmeta.chunk.ChunkMetadata;

public class Parser {
	public static P2PMetadata parseP2PMetaFileContents(String contents) throws ParserException {
		Object obj;
		try {
			obj = JSONValue.parseWithException(contents);
			
		} catch(ParseException e) {
			throw new ParserException("Could not parse contents as JSON");
		}
		validateType(obj, JSONObject.class);
		
		return parseP2PMetadata((JSONObject)obj);
	}
	
	private static P2PMetadata parseP2PMetadata(JSONObject obj) throws ParserException {
		
		validateFieldType(obj, "hash_type", String.class);
		validateFieldType(obj, "meta_hash", String.class);
		validateFieldType(obj, "files", JSONArray.class);
		
		// Only support "sha-256", but useful to be able to change in future
		String hashType = (String)obj.get("hash_type");
		if(!hashType.equals("sha-256")) {
			throw new ParserException("Hash Type other than sha-256 detected. Unsupported in this program");
		}
		
		byte[] metaHash = convertHexStringToByteArray(((String)obj.get("meta_hash")));
		
		FileMetadata[] files = convertJSONArrayToFileArray((JSONArray)obj.get("files"));
		
		if(files.length == 0) {
			throw new ParserException("p2pmeta file must specify at least one file");
		}
		
		P2PMetadata file;
		
		try {
			file = new P2PMetadata(hashType, metaHash, files);
		} catch (IncorrectHashException e) {
			throw new ParserException("Failed checksum on p2pmeta file");
		}
		
		return file;
	}
	
	public static FileMetadata parseFileMetadata(JSONObject file) throws ParserException {
		validateFieldType(file, "name", String.class);
		validateFieldType(file, "hash", String.class);
		validateFieldType(file, "chunks", JSONArray.class);
		
		String name = (String)file.get("name");
		byte[] hash = convertHexStringToByteArray(((String)file.get("hash")));
		
		ChunkMetadata[] chunks = convertJSONArrayToChunkArray((JSONArray)file.get("chunks"));
		
		if(chunks.length == 0) {
			throw new ParserException("File to download in P2PMetaFile must have at least 1 chunk");
		}
		
		return new FileMetadata(name, hash, chunks);
	}

	private static ChunkMetadata[] convertJSONArrayToChunkArray(JSONArray jsonArray) throws ParserException {
		ChunkMetadata[] chunks = new ChunkMetadata[jsonArray.size()];
		
		int i = 0;
		
		for(JSONObject obj : convertJSONArrayToJSONObjectArray(jsonArray)) {
			
			chunks[i] = parseChunkMetadata(obj);
			i++;
		}
		
		return chunks;
	}
	
	private static FileMetadata[] convertJSONArrayToFileArray(JSONArray jsonArray) throws ParserException {
		FileMetadata[] chunks = new FileMetadata[jsonArray.size()];
		
		int i = 0;
		
		for(JSONObject obj : convertJSONArrayToJSONObjectArray(jsonArray)) {
			
			chunks[i] = parseFileMetadata(obj);
			i++;
		}
		
		return chunks;
	}
	
	private static JSONObject[] convertJSONArrayToJSONObjectArray(JSONArray jsonArray) throws ParserException {
		
		JSONObject[] objects = new JSONObject[jsonArray.size()];
		
		for(int i=0; i<jsonArray.size(); i++) {
			if(!validateType(jsonArray.get(i), JSONObject.class)) {
				throw new ParserException("Array index " + i + " is not of type JSONObject as required");
			}
			
			objects[i] = (JSONObject)jsonArray.get(i);
		}
		
		return objects;
	}

	public static ChunkMetadata parseChunkMetadata(JSONObject chunk) throws ParserException {
		validateFieldType(chunk, "size", Number.class);
		validateFieldType(chunk, "hash", String.class);
		
		int size = ((Number)chunk.get("size")).intValue();
		byte[] hash = convertHexStringToByteArray(((String)chunk.get("hash")));
		
		return new ChunkMetadata(size, hash);
	}
	
	private static byte[] convertHexStringToByteArray(String hex) throws ParserException {
		byte[] hash;
		
		try {
			hash = Hex.decodeHex(hex.toCharArray());
		} catch(DecoderException e) {
			throw new ParserException("Unable to parse hash hex-string. " + e.getMessage());
		}
		
		return hash;
	}
	
	private static void validateFieldType(JSONObject object, String key, Class<?> class1) throws ParserException {
		if(object.get(key) != null && validateType(object.get(key), class1)) {
			return;
		}
		
		throw new ParserException("Unable to parse. " + key + " must be present and of correct type (" + class1.toString() + ").");
	}
	private static boolean validateType(Object obj, Class<?> class1) {
        if(class1.isAssignableFrom(obj.getClass())) {
            return true;
        }

        return false;
    }
}

package p2pdistribute.message;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import p2pdistribute.p2pmeta.ParserException;
import p2pdistribute.swarmmanager.message.SwarmManagerMessage;

public class MessageParser {

	public static SwarmManagerMessage parseSwarmManageMessage(String line) throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static JSONObject parseJSON(String json) throws ParserException {
		Object obj;
		try {
			obj = JSONValue.parseWithException(json);
			
		} catch(ParseException e) {
			throw new ParserException("Could not parse string as JSON");
		}
		validateType(obj, JSONObject.class);
		
		return (JSONObject)obj;
	}
	
	public static void validateFieldType(JSONObject object, String key, Class<?> class1) throws ParserException {
		if(object.get(key) != null && validateType(object.get(key), class1)) {
			return;
		}
		
		throw new ParserException("Unable to parse. " + key + " must be present and of correct type (" + class1.toString() + ").");
	}
	public static boolean validateType(Object obj, Class<?> class1) {
        if(class1.isAssignableFrom(obj.getClass())) {
            return true;
        }

        return false;
    }

}

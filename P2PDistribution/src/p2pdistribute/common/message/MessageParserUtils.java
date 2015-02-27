package p2pdistribute.common.message;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import p2pdistribute.client.message.P2PMessageParser;
import p2pdistribute.common.p2pmeta.ParserException;

/**
 * Some utility methods used by both the {@link SwarmManagerMessageParser} and {@link P2PMessageParser}
 */
public class MessageParserUtils {

	/**
	 * Attempts to parse a string into a JSON Object
	 */
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
	
	/**
	 * Validates the type of a field of a JSON Object
	 * @param object The JSON Object to work on.
	 * @param key - The key being validated
	 * @param class1 - The class of the type expected of the value associated with Key
	 * @throws ParserException - Thrown if the validation fails.
	 */
	public static void validateFieldType(JSONObject object, String key, Class<?> class1) throws ParserException {
		if(object.get(key) != null && validateType(object.get(key), class1)) {
			return;
		}
		
		throw new ParserException("Unable to parse. " + key + " must be present and of correct type (" + class1.toString() + ").");
	}
	/**
	 * Checks whether obj is of class1
	 * @param obj - The object to check
	 * @param class1 - The class to compare against
	 * @return True on success, False on failure
	 */
	public static boolean validateType(Object obj, Class<?> class1) {
        if(class1.isAssignableFrom(obj.getClass())) {
            return true;
        }

        return false;
    }

	/**
	 * Serialises a JSONMessage to String
	 * @param msg - The JSON Message to serialise
	 * @return String representing this object
	 */
	public static String serialiseMessageAsJSON(JSONMessage msg) {
		JSONObject obj = new JSONObject(msg.getJSON());
		
		String line = obj.toJSONString() + "\n";
		
		return line;
	}
}

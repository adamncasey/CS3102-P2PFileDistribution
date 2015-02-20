package p2pdistribute.message;

import java.util.HashMap;
import java.util.Map;


public class Message {
	public final String cmd;
	public final String metaHash;
	
	public Message(String cmd, String metaHash) {
		this.cmd = cmd;
		this.metaHash = metaHash;
	}
	
	
	public Map<String, Object> getJSON() {
		HashMap<String, Object> map = new HashMap<>();
		
		map.put("cmd", cmd);
		map.put("meta_hash", metaHash);
		
		return map;
	}

	/** 
	 * Eclipse generated hashCode and equals
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
		result = prime * result
				+ ((metaHash == null) ? 0 : metaHash.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (cmd == null) {
			if (other.cmd != null)
				return false;
		} else if (!cmd.equals(other.cmd))
			return false;
		if (metaHash == null) {
			if (other.metaHash != null)
				return false;
		} else if (!metaHash.equals(other.metaHash))
			return false;
		return true;
	}
}

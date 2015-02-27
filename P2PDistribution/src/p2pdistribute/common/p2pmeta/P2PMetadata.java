package p2pdistribute.common.p2pmeta;

/**
 * Represents the data stored in a .p2pmeta file
 *
 */
public class P2PMetadata {
	public final String hashType;
	public final byte[] metaHash;
	
	public final FileMetadata[] files;
	public final String swarmManagerHostname;
	
	public P2PMetadata(String hashType, byte[] hash, String smHostname, FileMetadata[] files) throws IncorrectHashException {
		this.metaHash = hash;
		this.hashType = hashType;
		this.files = files;
		this.swarmManagerHostname = smHostname;
		
		// TODO: Verify contents using metaHash
	}
}

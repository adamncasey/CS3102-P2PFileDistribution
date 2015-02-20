package p2pdistribute.p2pmeta;

public class P2PMetadata {
	public final String hashType;
	public final byte[] metaHash;
	
	public final FileMetadata[] files;
	
	public P2PMetadata(String hashType, byte[] hash, FileMetadata[] files) throws IncorrectHashException {
		this.metaHash = hash;
		this.hashType = hashType;
		this.files = files;
		
		// TODO: Verify contents using metaHash
	}
}

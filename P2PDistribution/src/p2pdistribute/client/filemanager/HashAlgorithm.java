package p2pdistribute.client.filemanager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class HashAlgorithm {
	private MessageDigest digestFunction;
	
	public HashAlgorithm(String hashType) throws NoSuchAlgorithmException {
		digestFunction = MessageDigest.getInstance(hashType);
	}
	
	public boolean verifyData(byte[] data, byte[] expectedHash) {
		
		digestFunction.update(data);
		byte[] digest = digestFunction.digest();
		
		digestFunction.reset();
				
		return Arrays.equals(expectedHash, digest);
	}
}

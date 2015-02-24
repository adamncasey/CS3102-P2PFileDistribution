package p2pdistribute.p2pmetagenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;

public class P2PMetaGenerator {

	public static String HASH_TYPE = "sha-256";
	public static int CHUNK_SIZE = 256*1024;

	public static void main(String[] args) {
		verifyArgs(args);
		// Pass swarm manager hostname + list of files + output file
		String smHostname = args[0];
		String outputFile = args[args.length - 1];
		int numInputFiles = args.length - 2;
		
		String[] files = new String[numInputFiles];

		System.out.println("Swarm Manager Hostname: " + smHostname);
		for(int i=0; i<numInputFiles; i++) {
			files[i] = args[1 + i];
			System.out.println("Input File Name       : " + files[i]);
		}
		System.out.println("P2PMeta Output File   : " + outputFile);

		try {
			String contents = generateP2PMetaFile(smHostname, HASH_TYPE, CHUNK_SIZE, files);
			
			Files.write(Paths.get(outputFile), contents.getBytes());
			
		} catch (NoSuchAlgorithmException | IOException e) {
			System.err.println("Error occured creating p2pmeta file: " + e.getMessage());
			return;
		}
		
		System.out.println(outputFile + " created successfully.");
	}
	
	private static void verifyArgs(String[] args) {
		if(args.length < 3) {
			System.out.println("Usage: <SwarmManagerHostname> <FileToSend1> ... <FileToSendN> <P2PMetaOutputFile>");
		}
	}
	
	public static String generateP2PMetaFile(String smHostname, String algorithm, long chunkSize, String[] files) throws NoSuchAlgorithmException, IOException {
		Map<String, Object> map = new LinkedHashMap<>();
		
		map.put("hash_type", algorithm);
		map.put("meta_hash", "");
		map.put("swarm_manager", smHostname);
		
		MessageDigest metaDigest = MessageDigest.getInstance(algorithm);
		metaDigest.update(algorithm.getBytes());
		metaDigest.update(smHostname.getBytes());
		
		map.put("files", processFiles(files, algorithm, chunkSize, metaDigest));
		
		map.put("meta_hash", new String(Hex.encodeHex(metaDigest.digest())));
		
		JSONObject obj = new JSONObject(map);
		
		return obj.toJSONString() + "\n";
	}

	private static List<Map<String, Object>> processFiles(String[] files, String algorithm, long chunkSize, MessageDigest metaDigest) throws NoSuchAlgorithmException, IOException {
		
		List<Map<String, Object>> processed = new LinkedList<>();
		for(String file : files) {
			
			System.out.println("Processing file: " + file);
			processed.add(processFile(file, algorithm, chunkSize, metaDigest));
		}
		
		return processed;
	}

	private static Map<String, Object> processFile(String filePath, String algorithm, long chunkSize, MessageDigest metaDigest) throws NoSuchAlgorithmException, IOException {
		Map<String, Object> output = new LinkedHashMap<>();
		Path path = Paths.get(filePath);
		
		String filename = path.getFileName().toString();
		output.put("name", filename);
		metaDigest.update(filename.getBytes());
		
		File file = path.toFile();
		FileInputStream stream = new FileInputStream(file);
		
		// hash contents
		MessageDigest fileDigester = MessageDigest.getInstance(algorithm);
		MessageDigest chunkDigester = MessageDigest.getInstance(algorithm);
		
		long pointer = 0;
		long fileSize = file.length();
		List<Map<String, Object>> chunksInfo = new LinkedList<>();
		
		// split into 256kb chunks:
		while(pointer < fileSize) {
			long readAmount = Math.min(chunkSize, fileSize - pointer);
			
			byte[] chunk = new byte[(int) readAmount];
			stream.read(chunk);
			
			pointer += readAmount;
			
			fileDigester.update(chunk);
			
			byte[] chunkHash = chunkDigester.digest(chunk);
			metaDigest.update(chunkHash);
			
			Map<String, Object> chunkInfo = new LinkedHashMap<>();
			chunkInfo.put("size", readAmount);
			chunkInfo.put("hash", new String(Hex.encodeHex(chunkHash)));
			
			chunksInfo.add(chunkInfo);
		}
		
		stream.close();
		
		byte[] fileDigest = fileDigester.digest();
		metaDigest.update(fileDigest);
		
		output.put("hash", new String(Hex.encodeHex(fileDigest)));
		output.put("chunks", chunksInfo);
		
		return output;
	}

}

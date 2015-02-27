package p2pdistribute.client.filemanager;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AcquisitionStatus {

	private Status[][] status;
	private List<ChunkStatusChangeHandler> handlers;
	
	private Random random; // Used to select a random chunk
	
	public AcquisitionStatus(int numFiles) {
		status = new Status[numFiles][];
		
		handlers = new LinkedList<>();
		
		random = new Random();
	}
	
	public AcquisitionStatus(AcquisitionStatus copyStatus) {
		status = new Status[copyStatus.status.length][];
		
		for(int i=0; i<copyStatus.status.length; i++) {
			status[i] = new Status[copyStatus.status[i].length];
			
			for(int j=0; j<status[i].length; j++) {
				status[i][j] = Status.UNKNOWN;
			}
		}
		
		handlers = new LinkedList<>();
		random = new Random();
	}
	
	public synchronized void registerHandler(ChunkStatusChangeHandler handler) {
		handlers.add(handler);
	}
	
	public void setStatus(int fileid, int chunkid, Status status) {
		Status old;
		synchronized(this) {
			ensureSize(fileid, chunkid);
			
			old = this.status[fileid][chunkid];
			
			
			this.status[fileid][chunkid] = status;
		}

		if(status == Status.COMPLETE && old != Status.UNKNOWN) {
			chunkComplete(fileid, chunkid);
		}
	}
	private synchronized void chunkComplete(int fileid, int chunkid) {
		for(ChunkStatusChangeHandler handler : handlers) {
			
			handler.onChunkComplete(fileid, chunkid);
		}
	}

	private void ensureSize(int fileid, int chunkid) {
		
		if(this.status[fileid] == null || this.status[fileid].length <= chunkid) {
			Status[] old = this.status[fileid];
			
			this.status[fileid] = new Status[chunkid + 1];
			
			if(old != null) {
				int i=0;
				for(Status stat : old) {
					this.status[fileid][i++] = stat;
				}
			}
		}
	}

	public synchronized void setStatus(int fileid, Status[] chunkStatuses) {
		this.status[fileid] = chunkStatuses;
	}
	
	public synchronized int numChunksIncomplete() {
		int total = 0;
		
		for(Status[] chunkStatuses : status) {
			for(Status status : chunkStatuses) {
				if(status.equals(Status.INCOMPLETE)) {
					total++;
				}
			}
		}
		
		return total;
	}

	public synchronized int numChunksComplete() {
		int total = 0;
		
		for(Status[] chunkStatuses : status) {
			for(Status status : chunkStatuses) {
				if(status.equals(Status.COMPLETE)) {
					total++;
				}
			}
		}
		
		return total;
	}
	
	public synchronized boolean complete() {
		for(Status[] chunkStatuses : status) {
			if(chunkStatuses == null) {
				return false;
			}
			for(Status stat : chunkStatuses) {
				if(stat == null || (!stat.equals(Status.COMPLETE))) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Selects a chunk that peer possesses which we do not.
	 * The chunk picked is set to be INPROGRESS and no other peer can request this chunk.
	 * @param peer
	 * @return [fileid, chunkid] if a chunk is found.
	 * @return null if peer status is not compatible with ours (incompatible number of files / chunks)
	 * @return null if no useful chunk is found
	 */
	public synchronized int[] pickUsefulChunk(AcquisitionStatus peer) {
		
		if(peer.status.length != status.length) {
			return null;
		}
		
		// We want to gather a list of all possible IDs, then pick a random one from that list.
		
		// LinkedList poor for the random access we do at the end, but we need it for the fast insertion.
		// TreeList could be a good route, but requires another library (apache commons collections).
		List<int[]> chunks = new LinkedList<>();
		
		for(int i=0; i<status.length; i++) {
			Status[] ourRow = status[i];
			Status[] theirRow = peer.status[i];
			
			if(theirRow == null || (ourRow.length != theirRow.length)) {
				return null;
			}
			
			for(int j=0; j<ourRow.length; j++) {
				if(theirRow[j] == Status.COMPLETE) {
					if(ourRow[j] != Status.COMPLETE && ourRow[j] != Status.INPROGRESS) {
						ourRow[j] = Status.INPROGRESS;
						chunks.add(new int[] { i, j });
					}
				}
			}
		}
		
		if(chunks.size() == 0) {
			return null;
		}
		
		int index = random.nextInt(chunks.size());
		
		return chunks.get(index);
	}
	
	public synchronized int[][] getCompleteFileChunkIDs() {
		return getStatusFileChunkIDs(Status.COMPLETE);
	}
	
	public synchronized int[][] getIncompleteFileChunkIDs() {
		return getStatusFileChunkIDs(Status.INCOMPLETE);
	}
	
	private int[][] getStatusFileChunkIDs(Status state) {
		LinkedList<int[]> list = new LinkedList<>();
		
		for(int i=0; i<status.length; i++) {
			for(int j=0; j<status[i].length; j++) {
				if(status[i][j] == state) {
					list.add(new int[] { i, j});
				}
			}
		}
		
		return list.toArray(new int[list.size()][]);
	}
}

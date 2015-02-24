package p2pdistribute.client.filemanager;

import java.util.LinkedList;

public class AcquisitionStatus {

	private Status[][] status;
	
	public AcquisitionStatus(int numFiles) {
		status = new Status[numFiles][];
	}
	
	public synchronized void setStatus(int fileid, int chunkid, Status status) {
		ensureSize(fileid, chunkid);
		
		this.status[fileid][chunkid] = status;
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
			for(Status status : chunkStatuses) {
				if(!status.equals(Status.COMPLETE)) {
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
						return new int[] { i, j };
					}
				}
			}
		}
		return null;
	}
	
	public synchronized int[][] getCompleteFileChunkIDs() {
		LinkedList<int[]> list = new LinkedList<>();
		
		for(int i=0; i<status.length; i++) {
			for(int j=0; j<status[i].length; j++) {
				if(status[i][j] == Status.COMPLETE) {
					list.add(new int[] { i, j});
				}
			}
		}
		
		return list.toArray(new int[list.size()][]);
	}
}

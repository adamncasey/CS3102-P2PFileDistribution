package p2pdistribute.client.filemanager;

import java.util.Arrays;
import java.util.LinkedList;

public class AcquisitionStatus {

	private Status[][] status;
	
	public AcquisitionStatus(int numFiles, int numChunks) {
		status = new Status[numFiles][numChunks];
		
		for(Status[] row : status) {
			Arrays.fill(row, Status.UNKNOWN);
		}
	}
	
	public void setStatus(int fileid, int chunkid, Status status) {
		this.status[fileid][chunkid] = status;
	}
	public void setStatus(int fileid, Status[] chunkStatuses) {
		this.status[fileid] = chunkStatuses;
	}
	
	public int numChunksIncomplete() {
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

	public int numChunksComplete() {
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
	
	public boolean complete() {
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
	 * @param peer
	 * @return [fileid, chunkid] if a chunk is found.
	 * @return null if peer status is not compatible with ours (incompatible number of files / chunks)
	 * @return null if no useful chunk is found
	 */
	public int[] pickUsefulChunk(AcquisitionStatus peer) {
		
		if(peer.status.length != status.length) {
			return null;
		}
		
		for(int i=0; i<status.length; i++) {
			Status[] ourRow = status[i];
			Status[] theirRow = peer.status[i];
			
			if(ourRow.length != theirRow.length) {
				return null;
			}
			
			for(int j=0; j<status.length; j++) {
				if(theirRow[j] == Status.COMPLETE) {
					if(ourRow[j] != Status.COMPLETE) {
						return new int[] { i, j };
					}
				}
			}
		}
		return null;
	}
	
	public int[][] getCompleteFileChunkIDs() {
		LinkedList<int[]> list = new LinkedList<>();
		
		for(int i=0; i<status.length; i++) {
			for(int j=0; j<status.length; j++) {
				if(status[i][j] == Status.COMPLETE) {
					list.add(new int[] { i, j});
				}
			}
		}
		
		return list.toArray(new int[list.size()][]);
	}
}

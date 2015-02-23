package p2pdistribute.client.filemanager;

import java.util.Arrays;

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
}

package aec;

import java.util.ArrayList;
import java.util.List;

/**
 * One Quorum. When the number of receivedOriginalRequestIDs equals the quorumSize, 
 * the Qourum was successfull.
 *
 */
public class Quorum {

	//All request IDs I send to other nodes of this Quorum
	private List<String> sendRequestIDs = new ArrayList<String>();
	
	//For each answer I get, I put the the original sendRequestID in here
	private List<String> receivedOriginalRequestIDs = new ArrayList<String>();
	
	//Number of answers to be received, so that this Quorum is successfull
	private int quorumSize = 0;
	
	public Quorum(int quorumSize) {
		this.quorumSize = quorumSize;
	}
	
	public void addSendRequestID(String id) {
		this.sendRequestIDs.add(id);
	}
	
	/**
	 * If an id is already present in the sendRequestIDs list, it is put it into 
	 * the receivedOriginalRequestIDs list. Returns true in this case.
	 * @param id
	 * @return true, if id was in sendRequestIDs
	 */
	public boolean addReceivedOriginalRequestID(String id) {
		if (this.sendRequestIDs.contains(id)) {
			this.receivedOriginalRequestIDs.add(id);
			return true;
		}
		return false;
	}
	
	public boolean quorumSuccessfull() {
		if (this.receivedOriginalRequestIDs.size() >= quorumSize) {
			return true;
		}
		return false;
	}
}

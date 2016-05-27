package aec;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class to collect Quorums for one update in the Memory.
 * When all Quorums are successful, the value can be written for the specified key.
 *
 */
public class QuorumCollection {
	
	// Key to be written in Memory
	private Integer key;
	
	// Value to be written in Memory
	private String value;
	
	// True, when the QuorumCollection wrote the key -> value to memory
	private boolean written = false;

	List<Quorum> quorums = new ArrayList<Quorum>();
	
	public QuorumCollection(Integer key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public synchronized void addQuorum(Quorum q) {
		quorums.add(q);
	}
	
	/**
	 * Adds a receivedRequestID (see Quorum class) to a Quroum that has this ID as sendRequestIDs
	 * Returns true if one exists, otherwise false.
	 * @return boolean (see above)
	 */
	public synchronized boolean addReceivedRequestIDToItsQuorum(String id) {
		for (Quorum q: quorums) {
			if (q.addReceivedOriginalRequestID(id)) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized boolean checkAllQuorumsSuccessful() {
		boolean successfull = true;
		for (Quorum q: quorums) {
			if (!q.quorumSuccessfull()) {
				successfull = false;
			}
		}
		return successfull;
	}
	
	public Callable<Boolean> checkAllQuorumsSuccessfulCallable() {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return checkAllQuorumsSuccessful();
			}
			
		};
	}
	
	/**
	 * Writes a value to Memory, if all related Quorums are successful and did not write before.
	 * Otherwise returns false.
	 * @return true, if writing was successful.
	 */
	public synchronized boolean writeValueToMemory() {
		if (checkAllQuorumsSuccessful() && written == false) {
			Memory.put(key, value);
			written = true;
			return true;
		}
		return false;
	}

}

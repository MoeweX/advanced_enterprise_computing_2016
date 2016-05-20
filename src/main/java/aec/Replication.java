package aec;

import java.util.ArrayList;
import java.util.List;

public class Replication {

	private int qsize;
	private List<String> targetNodes = new ArrayList<String>();

	public Replication(int qsize, List<String> targetNodes) {
		this.qsize = qsize;
		this.targetNodes = targetNodes;
	}
	
	@Override
	public String toString() {
		return "Quoroum with size " + qsize + " to nodes " + targetNodes;
	}
}

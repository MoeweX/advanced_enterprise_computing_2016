package aec;

import java.util.ArrayList;
import java.util.List;

public class Quorum {

	private int qsize;
	private List<String> targetNodes = new ArrayList<String>();

	public Quorum(int qsize, List<String> targetNodes) {
		this.qsize = qsize;
		this.targetNodes = targetNodes;
	}
	
	@Override
	public String toString() {
		return "Quoroum with size " + qsize + " to nodes " + targetNodes;
	}
}

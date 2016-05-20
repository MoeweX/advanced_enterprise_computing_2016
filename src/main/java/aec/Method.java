package aec;

import java.util.ArrayList;
import java.util.List;

public class Method {
	public enum methods {
		sync, async, quorum
	}

	methods type;
	int qsize;
	List<String> zielKnoten = new ArrayList<String>();

	public Method(String type, int qsize, List<String> zielKnoten) {
		if (type.equals("async")) {
			this.type = methods.async;
		} else if (type.equals("sync")) {
			this.type = methods.sync;
		} else if (type.equals("quorum")) {
			this.type = methods.quorum;
		}

		this.qsize = qsize;
		this.zielKnoten = zielKnoten;
	}
	
	@Override
	public String toString() {
		if (type == methods.quorum) {
			return "Quoroum with size " + qsize + " to nodes " + zielKnoten;
		} else if (type == methods.async) {
			return "Async to node " + zielKnoten.get(0);
		} else if (type == methods.sync) {
			return "Sync to node " + zielKnoten.get(0);
		} else {
			return "Invalid method";
		}
	}
}

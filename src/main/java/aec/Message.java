package aec;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 1886301432166925753L;
	
	private String startNode;
	private Integer key;
	private String value;
	
	public Message(String startNode, Integer key, String value) {
		this.startNode = startNode;
		this.key = key;
		this.value = value;
	}

	public String getStartNode() {
		return startNode;
	}

	public Integer getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "Message(startNode="+startNode+",key="+key+",value="+value+")";
	}
	
}

package aec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Configuration {
	
	private String myNode;
	private String hostsURI;
	private HashMap<String, String> hosts = new HashMap<String, String>(); //node -> ip:port
	private String replicationPathsURI;
	private HashMap<String, List<Quorum>> replicationPaths = new HashMap<String, List<Quorum>>(); // startnode -> List<Quorum>
	private int receivePort = 8086;
	
	public Configuration(String myNode, String replicationPathsURI, String hostsURI) {
		super();
		this.myNode = myNode;
		this.replicationPathsURI = replicationPathsURI;
		this.hostsURI = hostsURI;
	}

	public String getMyNode() {
		return myNode;
	}

	public String getHostIPForNode(String node) {
		return hosts.get(node).split(":")[0];
	}
	
	public int getHostPortForNode(String node) {
		return Integer.parseInt(hosts.get(node).split(":")[1]);
	}
	
	public String getHostStringForNode(String node) {
		return node + " = " + hosts.get(node);
	}

	public List<Quorum> getReplicationPathsForStartNode(String node) {
		return replicationPaths.get(node);
	}
	
	public String getReplicationPathsStringForStartNode(String node) {
		return "Startnode = " + node + " -> " + replicationPaths.get(node);
	}

	public int getReceivePort() {
		return receivePort;
	}
	
	public void setReceivePort(int port) {
		this.receivePort = port;
	}

	public void parseHosts() throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, String> h = new HashMap<String, String>();
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(hostsURI);
		doc.getDocumentElement().normalize();
		
		NodeList nodeList = doc.getElementsByTagName("host");
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			h.put(n.getAttributes().getNamedItem("node").getNodeValue(), 
					n.getAttributes().getNamedItem("ip").getNodeValue());
		}
		hosts = h;

	}

	public void parseReplicationPaths() throws ParserConfigurationException, SAXException, IOException {
		//TODO Funktioniert nicht für nodeC
		String startNode;
		String type;
		List <String> target = new ArrayList<String>();;
		String trgNode;
		String srcNode;
		int qsize;
	
		HashMap<String, Quorum> h = new HashMap<String, Quorum>();
	
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(replicationPathsURI);
		doc.getDocumentElement().normalize();
	
		NodeList pathList = doc.getElementsByTagName("path");
		for (int temp1 = 0; temp1 < pathList.getLength(); temp1++) {
			Node n1 = pathList.item(temp1);
			startNode = n1.getAttributes().getNamedItem("start").getNodeValue();
	
			if (startNode.equals(myNode)) {
	
				NodeList L = doc.getElementsByTagName("link");
				for (int a = 0; a < L.getLength(); a++) {
					Node m = L.item(a);
					if (m.getParentNode() == n1) {
						type = m.getAttributes().getNamedItem("type").getNodeValue();
						if (type.equals("async")) {
							srcNode = m.getAttributes().getNamedItem("src").getNodeValue();
							trgNode =  m.getAttributes().getNamedItem("target").getNodeValue();
							target.add(trgNode);
							qsize = 0;
							Quorum m1 = new Quorum (type, qsize ,target);
							h.put(srcNode, m1);
	
						} else if (type.equals("sync")) {
							srcNode = m.getAttributes().getNamedItem("src").getNodeValue();
							trgNode = m.getAttributes().getNamedItem("target").getNodeValue();
							target.add(trgNode);
							qsize = 0;
							Quorum m2 = new Quorum (type, qsize ,target);
							h.put(srcNode, m2);
	
						} else if (type.equals("quorum")) {
							srcNode = m.getAttributes().getNamedItem("src").getNodeValue();
							qsize = Integer.parseInt( m.getAttributes().getNamedItem("qsize").getNodeValue());
							NodeList M = doc.getElementsByTagName("qparticipant");
							for (int b = 0; b < M.getLength(); b++) {
								Node g = M.item(b);
								if (g.getParentNode() == m) {
									trgNode = g.getAttributes().getNamedItem("name").getNodeValue();
									target.add(trgNode);
								}
							}
							Quorum m3 = new Quorum (type, qsize ,target);
							h.put(srcNode, m3);
						}
					}
				}
			}
		}
		replicationPaths = h;
	}

	/**
	 * Tests, whether all keys of replicationPaths are present in hosts.
	 * @return true, if keys are present
	 */
	public boolean testAllNodeInformationProvided() {
		Set<String> repKeys = replicationPaths.keySet();
		Set<String> hostKeys = hosts.keySet();
		for (String repKey: repKeys) {
			if (!hostKeys.contains(repKey)) {
				return false;
			}
		}
		return true;
	}
	
}

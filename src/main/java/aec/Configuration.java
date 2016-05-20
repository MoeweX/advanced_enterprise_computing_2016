package aec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Configuration {

	
	private HashMap<String, String> hosts = new HashMap<String, String>(); //node -> IP
	private HashMap<String, Method> replicationPaths = new HashMap<String, Method>(); // startnode -> Method
	//TODO replicationPaths muss wahrscheinlich String -> List<Method> enthalten
	private String myNode; 
	private String replicationPathsURI;
	private String hostsURI;
	
	public Configuration(String myNode, String replicationPathsURI, String hostsURI) {
		super();
		this.myNode = myNode;
		this.replicationPathsURI = replicationPathsURI;
		this.hostsURI = hostsURI;
	}

	public Method getReplicationPathsForNode(String node) {
		return replicationPaths.get(node);
	}
	
	/**
	 * Tests, whether all keys of replicationPaths are present in hosts.
	 * @return true, if keys are present
	 */
	public boolean testAllNodeInformationProvided() {
		//TODO 
		return false;
	}
	
	/**
	 * Returns the IP for a given node, if present. Otherwise null.
	 * @param node
	 * @return IP of node
	 */
	public String getHostIPForNode(String node) {
		return hosts.get(node);
	}
	
	public void parseHostIPs() {
		//TODO
	}
	
	public void parseReplicationPaths() throws ParserConfigurationException, SAXException, IOException {

		String startNode;
		String type;
		List <String> target = new ArrayList<String>();;
		String trgNode;
		String srcNode;
		int qsize;

		HashMap<String, Method> h = new HashMap<String, Method>();

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
							Method m1 = new Method (type, qsize ,target);
							h.put(srcNode, m1);

						} else if (type.equals("sync")) {
							srcNode = m.getAttributes().getNamedItem("src").getNodeValue();
							trgNode = m.getAttributes().getNamedItem("target").getNodeValue();
							target.add(trgNode);
							qsize = 0;
							Method m2 = new Method (type, qsize ,target);
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
							Method m3 = new Method (type, qsize ,target);
							h.put(srcNode, m3);
						}
					}
				}
			}
		}
		replicationPaths = h;
	}
}

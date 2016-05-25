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
	private HashMap<String, List<Replication>> replicationPaths = new HashMap<String, List<Replication>>(); // startnode -> List
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
	
	public List<String[]> getAllHosts() {
		List<String[]> list = new ArrayList<>();
		for (String host: this.hosts.values()) {
			list.add(new String[] {host.split(":")[0], host.split(":")[1]});
		}
		return list;
	}

	public List<Replication> getReplicationPathsForStartNode(String node) {
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

		int qsize;
		String startNode;
		String trgNode;
		String srcNode;
		String type;

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(replicationPathsURI);
		doc.getDocumentElement().normalize();

		NodeList pathList = doc.getElementsByTagName("path");
		for (int temp1 = 0; temp1 < pathList.getLength(); temp1++) {
			Node n1 = pathList.item(temp1);
			startNode = n1.getAttributes().getNamedItem("start").getNodeValue();
			List<Replication> repList = new ArrayList<Replication>();

			NodeList L = doc.getElementsByTagName("link");
			for (int a = 0; a < L.getLength(); a++) {
				Node m = L.item(a);
				if (m.getParentNode() == n1) {

					srcNode = m.getAttributes().getNamedItem("src").getNodeValue();

					if (srcNode.equals(myNode)) {
						List<String> targetNodes = new ArrayList<String>();

						type = m.getAttributes().getNamedItem("type").getNodeValue();

						switch (type) {
						case "async":
							trgNode = m.getAttributes().getNamedItem("target").getNodeValue();
							qsize = 0;
							targetNodes.add(trgNode);
							Replication r1 = new Replication(qsize, targetNodes);
							repList.add(r1);
							break;

						case "sync":
							trgNode = m.getAttributes().getNamedItem("target").getNodeValue();
							qsize = 1;
							targetNodes.add(trgNode);
							Replication r2 = new Replication(qsize, targetNodes);
							repList.add(r2);
							break;

						case "quorum":
							qsize = Integer.parseInt(m.getAttributes().getNamedItem("qsize").getNodeValue());
							NodeList M = doc.getElementsByTagName("qparticipant");
							for (int x = 0; x < M.getLength(); x++) {
								Node g = M.item(x);
								if (g.getParentNode() == m) {
									trgNode = g.getAttributes().getNamedItem("name").getNodeValue();
									targetNodes.add(trgNode);

								}

							}
							Replication r3 = new Replication(qsize, targetNodes);
							repList.add(r3);
							break;

						}
						replicationPaths.put(startNode, repList);
					}

				}

			}

		}

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

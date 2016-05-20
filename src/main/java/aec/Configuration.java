package aec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.w3c.dom.Node;

public class Configuration {

	
	private HashMap<String, String> mappingNodeIP;
	private String myNode; 
	private String fileName;
	  
	
	public HashMap<String, Method> readXMLConfiguration(String myNode, String fileName) throws ParserConfigurationException, SAXException, IOException {

		String startNode;
		String type;
		List <String> target = new ArrayList<String>();;
		String trgNode;
		String srcNode;
		int qsize;

		HashMap<String, Method> h = new HashMap<String, Method>();

		File inputConfig = new File(fileName);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(inputConfig);
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
		return h;
	}
}

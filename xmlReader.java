import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;

public class xmlReader {

	public enum methods {
		sync, async, quorum
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

		String myNode = "nodeA";
		String startNode;
		String type;
		String srcNodetrg;
		String srcNode;
		String qSize;

		HashMap<String, methods> h = new HashMap<String, methods>();

		File inputConfig = new File("config.xml");
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
							srcNodetrg = m.getAttributes().getNamedItem("src").getNodeValue() + "To"
									+ m.getAttributes().getNamedItem("target").getNodeValue();
							h.put(srcNodetrg, methods.async);

						} else if (type.equals("sync")) {
							srcNodetrg = m.getAttributes().getNamedItem("src").getNodeValue() + "To"
									+ m.getAttributes().getNamedItem("target").getNodeValue();
							h.put(srcNodetrg, methods.sync);

						} else if (type.equals("quorum")) {
							srcNode = m.getAttributes().getNamedItem("src").getNodeValue();
							qSize = m.getAttributes().getNamedItem("qsize").getNodeValue();
							NodeList M = doc.getElementsByTagName("qparticipant");
							for (int b = 0; b < M.getLength(); b++) {
								Node g = M.item(b);
								if (g.getParentNode() == m) {
									srcNodetrg = srcNode + "To" + g.getAttributes().getNamedItem("name").getNodeValue()
											+ "." + qSize;
									h.put(srcNodetrg, methods.quorum);
								}
							}
						}
					}
				}
			}
		}
	}
}

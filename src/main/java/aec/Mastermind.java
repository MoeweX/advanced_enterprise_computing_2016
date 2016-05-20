package aec;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class Mastermind {
	
	static Logger logger = Logger.getLogger(Mastermind.class.getName());

	/*
	 * Wir brauchen:
	 * 	XML Leser <- Anne
	 *  Konfigurationsklasse, in der XML Informationen für eigenen Knoten gesichert werden
	 *  	HashMap mit Ziel als Key und Enum-Value für Methode
	 *  Kommunikationsklasse -> organisiert auch, wann die Speicherklasse etwas speichern darf, etc.
	 *  Speicherklasse, die Informationen sichert und logt (log by log4j) <- Jonathan
	 */
	
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		String replicationPathsURI = "https://dl.dropboxusercontent.com/u/23672500/examplepath.xml";
		String hostsURI = "https://dl.dropboxusercontent.com/u/23672500/examplehosts.xml";
		String myNode = "nodeC";
		Configuration c = new Configuration(myNode, replicationPathsURI, hostsURI);
		c.parseReplicationPaths();
		logger.info(c.getReplicationPathsForNode("nodeA"));
		logger.info(c.getReplicationPathsForNode("nodeB"));
		logger.info(c.getReplicationPathsForNode("nodeC"));
		c.parseHostIPs();
		logger.info("All information provided?: " + c.testAllNodeInformationProvided());
		logger.info(c.getHostIPForNode("nodeA"));
		logger.info(c.getHostIPForNode("nodeB"));
		logger.info(c.getHostIPForNode("nodeC"));

	}

}

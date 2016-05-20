package aec;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class Mastermind {
	
	static Logger logger = Logger.getLogger(Mastermind.class.getName());
	
	public static Configuration c;

	/*
	 * Wir brauchen:
	 * 	XML Leser <- Anne
	 *  Konfigurationsklasse, in der XML Informationen für eigenen Knoten gesichert werden
	 *  	HashMap mit Ziel als Key und Enum-Value für Methode
	 *  Kommunikationsklasse -> organisiert auch, wann die Speicherklasse etwas speichern darf, etc.
	 *  Speicherklasse, die Informationen sichert und logt (log by log4j) <- Jonathan
	 */
	
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ParseException {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("p", "replicationPathsURI", true, "URI to download replicationPaths file");
		options.addOption("h", "hostsURI", true, "URI to download hosts file");
		options.addOption("n", "myNode", true, "the name of this node");
		options.addOption("s", "sendPort", true, "used port for sending [8085]");
		options.addOption("r", "receivePort", true, "used port for receiving [8086]");
		// automatically generate the help statement
		CommandLine line = parser.parse(options, args);
		
		//validate that all required options have been set
		if (!line.hasOption("p") || !line.hasOption("h") || !line.hasOption("n")) {
			logger.error("Please make sure to set all options!");
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("aec", options);
			System.exit(1);
		}
		
		logger.info("replicationPathsURI: " + line.getOptionValue("p"));
		logger.info("hostsURI: " + line.getOptionValue("h"));
		logger.info("myNode: " + line.getOptionValue("n"));
		
		Mastermind.c = new Configuration(line.getOptionValue("n"), 
				line.getOptionValue("p"), line.getOptionValue("h"));
		
		//print not required options if set
		if (line.hasOption("s")) {
			logger.info("sendPort: " + line.getOptionValue("s"));
			Mastermind.c.setSendPort(Integer.parseInt(line.getOptionValue("s"))); 
		}
		if (line.hasOption("r")) {
			logger.info("receiveport: " + line.getOptionValue("r"));
			Mastermind.c.setReceivePort(Integer.parseInt(line.getOptionValue("r"))); 
		}
		
		c.parseReplicationPaths();
		logger.info(c.getReplicationPathsStringForNode("nodeA"));
		logger.info(c.getReplicationPathsStringForNode("nodeB"));
		logger.info(c.getReplicationPathsStringForNode("nodeC"));
		c.parseHostIPs();
		logger.info("All information provided?: " + c.testAllNodeInformationProvided());
		logger.info(c.getHostIPStringForNode("nodeA"));
		logger.info(c.getHostIPStringForNode("nodeB"));
		logger.info(c.getHostIPStringForNode("nodeC"));

	}

}

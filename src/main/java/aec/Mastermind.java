package aec;

import org.apache.log4j.Logger;

public class Mastermind {
	
	static Logger logger = Logger.getLogger(Mastermind.class.getName());

	/*
	 * Wir brauchen:
	 * 	XML Leser <- Anne
	 *  Konfigurationsklasse, in der XML Informationen für eigenen Knoten gesichert werden
	 *  	HashMap mit Ziel als Key und Enum-Value für Methode
	 *  Kommunikationsklasse, die weiß wie die anderen Maschinenen erreichbar sind (Dropbox)
	 *  	-> organisiert auch, wann die Speicherklasse etwas speichern darf, etc.
	 *  Speicherklasse, die Informationen sichert und logt (log by log4j) <- Jonathan
	 */
	
	
	public static void main(String[] args) {
		logger.info("Hello World");
	}

}

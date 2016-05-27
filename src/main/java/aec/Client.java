package aec;

import java.util.Arrays;

import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Sender;

public class Client {

	private static String host = "localhost";
	private static int port = 8084;
	
	public static void main(String[] args) {
		
		Sender s = new Sender(host, port);
		Message m = new Message("nodeD", 1, "Test-Wert");
		Request req = new Request(m, "replicate", "client");
		System.out.println(s.sendMessage(req, 15000));
		
		/*
		Sender s2 = new Sender(host, port);
		Request req2 = new Request("1", "get", "client");
		System.out.println(s2.sendMessage(req2, 15000).getResponseMessage());
		
		Sender s3 = new Sender(host, port);
		Request req3 = new Request(Arrays.asList(new String[] {"1", "1"}), "delete", "client");
		System.out.println(s3.sendMessage(req3, 15000));
		*/
	}
	
	/**
	 * TODO
	 * 	- Read in XML file for client
	 *  - Read in XML file from dropbox for hosts
	 *  - Create for each input target one thread
	 *  - start all threads -> send data to desired targets
	 *  - wait for time specified in sleep
	 */
	
}

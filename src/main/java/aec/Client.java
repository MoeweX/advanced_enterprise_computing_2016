package aec;

import java.util.Arrays;

import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Sender;

@SuppressWarnings("unused")
public class Client {

	private static String host = "localhost";
	private static int port = 8081;
	
	public static void main(String[] args) {
		
		Sender s = new Sender(host, port);
		Message m = new Message("nodeA", 1, "Test-Wert");
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
	
}

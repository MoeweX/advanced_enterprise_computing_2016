package aec;

import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.Sender;

public class Client {

	private static String host = "localhost";
	private static int port = 8081;
	
	public static void main(String[] args) {
		Sender s = new Sender(host, port);
		Message m = new Message("nodeA", 1, "Test-Wert");
		Request req = new Request(m, "messages", "client");
		System.out.println(s.sendMessage(req, 10000));
	}
	
}

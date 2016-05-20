package aec;

import java.io.IOException;

import org.apache.log4j.Logger;

import de.tub.ise.hermes.AsyncCallbackRecipient;
import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Receiver;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.RequestHandlerRegistry;
import de.tub.ise.hermes.Response;
import de.tub.ise.hermes.Sender;

public class Communication {
	
	static Logger logger = Logger.getLogger(Communication.class.getName());
	@SuppressWarnings("unused")
	private Receiver receiver;
	
	public Communication() {
		RequestHandlerRegistry reg = RequestHandlerRegistry.getInstance();
		reg.registerHandler("sync", new SyncRequestHandler());
		//TODO register other handlers
		try {
			receiver = new Receiver(Mastermind.c.getReceivePort());
		} catch (IOException e) {
			logger.error("Port " + Mastermind.c.getReceivePort() + " was already taken.");
			System.exit(1);
		}
	}
	
	public void replicateData(String startNode, Integer key, String value) {
		// get targets //TODO must return list instead of only one Method
		//Quorum m = Mastermind.c.getReplicationPathsForStartNode(startNode);
		
		/*
		if (m.type == Quorum.methods.sync) {
			String targetnode = m.zielKnoten.get(0);
			String ip = Mastermind.c.getHostIPForNode(targetnode);
			Integer port = Mastermind.c.getHostPortForNode(targetnode);		
			Sender s = new Sender(ip, port);
			Request req = new Request("TestSend", "sync", Mastermind.c.getMyNode());
			s.sendMessageAsync(req, new AsyncCallbackRecipient() {
				
				@Override
				public void callback(Response resp) {
					logger.info("Received an answer: " + resp.getResponseMessage());
				}
			});
		} else if (m.type == Quorum.methods.async) {
			//TODO
		} else if (m.type == Quorum.methods.quorum) {
			//TODO
		}
		*/
	}
	
	class SyncRequestHandler implements IRequestHandler {

		@Override
		public Response handleRequest(Request req) {
			logger.info("Received a message: " + (String) req.getItems().get(0));
			Response resp = new Response("Received message!", true, req);
			return resp;
		}

		@Override
		public boolean requiresResponse() {
			return true;
		}
		
	}
	
}

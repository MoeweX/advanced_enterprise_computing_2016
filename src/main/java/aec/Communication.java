package aec;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

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
		reg.registerHandler("messages", new MessageRequestHandler());
		try {
			receiver = new Receiver(Mastermind.c.getReceivePort());
		} catch (IOException e) {
			logger.error("Port " + Mastermind.c.getReceivePort() + " was already taken.");
			System.exit(1);
		}
	}
	
	public void replicateData(Message message) {
		QuorumCollection quorumCollection = new QuorumCollection(message.getKey(), message.getValue());
		
		List<Replication> replications = Mastermind.c.getReplicationPathsForStartNode(message.getStartNode());
		//save Sender and Request, because it must be send AFTER the quorumCollection was created
		HashMap<Sender, Request> senderRequest = new HashMap<Sender,Request>();
		for (Replication r: replications) {
			Quorum quorum = new Quorum(r.getQsize());
			//do this for all nodes of quorum
			for (String node: r.getTargetNodes()) {
				Sender s = new Sender(Mastermind.c.getHostIPForNode(node), Mastermind.c.getHostPortForNode(node));
				Request req = new Request(message, "messages", Mastermind.c.getMyNode());
				//add ID to quorum
				quorum.addSendRequestID(req.getRequestId());
				//save Sender and Request to hashmap for later usage
				senderRequest.put(s, req);
			}
			quorumCollection.addQuorum(quorum);
		}
		
		// now send all messages and create callbacks
		for (Sender s: senderRequest.keySet()) {
			Request req = senderRequest.get(s);
			s.sendMessageAsync(req, new AsyncCallbackRecipient() {
				
				@Override
				public void callback(Response resp) {
					//add ID to the correct Quorum
					if (!quorumCollection.addReceivedRequestIDToItsQuorum(resp.getResponseMessage())) {
						logger.warn("ID " + resp.getResponseMessage() + " could not be assigned to a Quorum!");
					}
					
				}
			});
		}
		
		while (!quorumCollection.writeValueToMemory()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// data written to memory, we can answer now the one who send a message to us
	}
	
	class MessageRequestHandler implements IRequestHandler {

		@Override
		public Response handleRequest(Request req) {
			Message message = (Message) req.getItems().get(0);
			logger.info("Received " + message + " from " + 
					req.getOriginator() + " with ID " + req.getRequestId());
			replicateData(message);
			//when we are here, we successfully replicated the data and wrote it in our memory
			Response resp = new Response(req.getRequestId(), true, req);
			return resp;
		}

		@Override
		public boolean requiresResponse() {
			return true;
		}
		
	}
	
	class Message implements Serializable {

		private static final long serialVersionUID = 1886301432166925753L;
		
		private String startNode;
		private Integer key;
		private String value;
		
		public Message(String startNode, Integer key, String value) {
			this.startNode = startNode;
			this.key = key;
			this.value = value;
		}

		public String getStartNode() {
			return startNode;
		}

		public Integer getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return "Message(startNode="+startNode+",key="+key+",value="+value+")";
		}
		
	}
	
	
}

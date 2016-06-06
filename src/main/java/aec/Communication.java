package aec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.awaitility.core.ConditionTimeoutException;

import de.tub.ise.hermes.AsyncCallbackRecipient;
import de.tub.ise.hermes.IRequestHandler;
import de.tub.ise.hermes.Receiver;
import de.tub.ise.hermes.Request;
import de.tub.ise.hermes.RequestHandlerRegistry;
import de.tub.ise.hermes.Response;
import de.tub.ise.hermes.Sender;

public class Communication {
	
	static Logger logger = Logger.getLogger(Communication.class.getName());
	private Receiver receiver;
	
	public Communication() {
		RequestHandlerRegistry reg = RequestHandlerRegistry.getInstance();
		reg.registerHandler("replicate", new ReplicateRequestHandler());
		reg.registerHandler("get", new GetRequestHandler());
		reg.registerHandler("delete", new DeleteRequestHandler());
		reg.registerHandler("alive", new AliveRequestHandler());
		try {
			receiver = new Receiver(Mastermind.c.getReceivePort());
			receiver.start();
		} catch (IOException e) {
			logger.error("Port " + Mastermind.c.getReceivePort() + " was already taken.");
			System.exit(1);
		}
	}
	
	/**
	 * Before data is replicated, ask all sync nodes in replications, whether they are alive.
	 * Needs message, because the target node also needs to check its sync nodes for beeing alive, 
	 * and the message stores the startNode.
	 * @param replications
	 * @param message
	 * @return true, if all snyc nodes are alive, otherwise false
	 */
	public boolean testAlive(List<Replication> replications, Message message) {
		List<String> requestIDs = new ArrayList<String>();
		//save Sender and Request, because it must be send AFTER the IDs were saved
		HashMap<Sender, Request> senderRequest = new HashMap<Sender,Request>();
		for (Replication r: replications) {
			if (r.getQsize() == 1 && r.getTargetNodes().size() == 1) {
				String node = r.getTargetNodes().get(0);
				Sender s = new Sender(Mastermind.c.getHostIPForNode(node), Mastermind.c.getHostPortForNode(node));
				Request req = new Request(message, "alive", Mastermind.c.getMyNode());
				//add ID to requestIDs
				requestIDs.add(req.getRequestId());
				//save Sender and Request to hashmap for later usage
				senderRequest.put(s, req);
				logger.debug("Asking sync-node " + node + " with messageID = " + req.getRequestId() + " wether it is alive.");
			}
		}
		// now send all messages and create callbacks
		for (Sender s: senderRequest.keySet()) {
			Request req = senderRequest.get(s);
			s.sendMessageAsync(req, new AsyncCallbackRecipient() {
				
				@Override
				public void callback(Response resp) {
					if (resp.responseCode() == false) {
						logger.warn("Target of message " + resp.getResponseMessage() + " is not alive.");
					}
					//remove ID from requestIDs
					if (!requestIDs.remove(resp.getResponseMessage())) {
						logger.warn("ID " + resp.getResponseMessage() + " was not in list of asked sync-nodes!");
					} else {
						logger.debug("Response " + resp.getResponseMessage() + " tells that node is alive.");
					}
				}
			});
		}

		try {
			Awaitility.setDefaultPollDelay(10, TimeUnit.MILLISECONDS);
			Awaitility.await().atMost(new Duration(10, TimeUnit.SECONDS)).until(new Callable<Boolean>() {
				
				@Override
				public Boolean call() throws Exception {
					return requestIDs.isEmpty();
				}
			});
		} catch(ConditionTimeoutException e) {
			logger.warn(e.getMessage());
			return false;
		}	
		return true;
	}
	
	public boolean replicateData(Message message) {
		QuorumCollection quorumCollection = new QuorumCollection(message.getKey(), message.getValue());
		
		List<Replication> replications = Mastermind.c.getReplicationPathsForStartNode(message.getStartNode());
		if (replications == null) {
			//we can just write and do not need to wait for anybody
			return quorumCollection.writeValueToMemory();	
		}
		
		/* DAVID DOES NOT WANT THIS
		//ask all sync nodes, weather they are available, if one does not respond -> replication fails
		if (!testAlive(replications, message)) {
			return false;
		} */
		
		//save Sender and Request, because it must be send AFTER the quorumCollection was created
		HashMap<Sender, Request> senderRequest = new HashMap<Sender,Request>();
		for (Replication r: replications) {
			Quorum quorum = new Quorum(r.getQsize());
			//do this for all nodes of quorum
			for (String node: r.getTargetNodes()) {
				Sender s = new Sender(Mastermind.c.getHostIPForNode(node), Mastermind.c.getHostPortForNode(node));
				Request req = new Request(message, "replicate", Mastermind.c.getMyNode());
				//add ID to quorum
				quorum.addSendRequestID(req.getRequestId());
				//save Sender and Request to hashmap for later usage
				senderRequest.put(s, req);
				logger.debug("Asking node " + node + " with messageID = " + req.getRequestId() + " to replicate.");
			}
			quorumCollection.addQuorum(quorum);
		}
		
		// now send all messages and create callbacks
		for (Sender s: senderRequest.keySet()) {
			Request req = senderRequest.get(s);
			s.sendMessageAsync(req, new AsyncCallbackRecipient() {
				
				@Override
				public void callback(Response resp) {
					if (resp.responseCode() == false) {
						logger.warn("Target of message " + resp.getResponseMessage() + " could not replicate.");
					}
					//add ID to the correct Quorum
					if (!quorumCollection.addReceivedRequestIDToItsQuorum(resp.getResponseMessage())) {
						logger.warn("ID " + resp.getResponseMessage() + " could not be assigned to a Quorum!");
					} else {
						logger.debug("Response " + resp.getResponseMessage() + " assigned to quorum.");
					}
				}
			});
		}

		try {
			Awaitility.setDefaultPollDelay(1, TimeUnit.MICROSECONDS);
			Awaitility.await().atMost(new Duration(10, TimeUnit.SECONDS)).until(quorumCollection.checkAllQuorumsSuccessfulCallable());
		} catch(ConditionTimeoutException e) {
			logger.warn(e.getMessage());
			return false;
		}
		return quorumCollection.writeValueToMemory();
		// data written to memory, we can answer now the one who send a message to us
	}
	
	class ReplicateRequestHandler implements IRequestHandler {

		@Override
		public Response handleRequest(Request req) {
			// the message is the first item of the request
			Message message = (Message) req.getItems().get(0);
			logger.debug("Received " + message + " from " + 
					req.getOriginator() + " with ID " + req.getRequestId());
			if (replicateData(message)) {
				//when we are here, we successfully replicated the data and wrote it in our memory
				Response resp = new Response(req.getRequestId(), true, req);
				logger.debug("Telling " + req.getOriginator() + " that we replicated.");
				return resp;
			}
			//replication failed
			Response resp = new Response(req.getRequestId(), false, req);
			logger.warn("We were unable to replicate the data.");
			return resp;
		}

		@Override
		public boolean requiresResponse() {
			return true;
		}
		
	}
	
	class AliveRequestHandler implements IRequestHandler {

		@Override
		public Response handleRequest(Request req) {
			// the message is the first item of the request
			Message message = (Message) req.getItems().get(0);
			logger.debug(req.getOriginator() + " asks wether we are alive.");
			List<Replication> replications = Mastermind.c.getReplicationPathsForStartNode(message.getStartNode());
			if (replications == null) {
				//we can just answer alive
				logger.debug("Telling " + req.getOriginator() + " that I and all other nodes after me are alive");
				return new Response(req.getRequestId(), true, req);
			} else {
				if (testAlive(replications, message)) {
					//when we are here, all sync nodes after me are alive
					logger.debug("Telling " + req.getOriginator() + " that I and all other nodes after we are alive");
					return new Response(req.getRequestId(), true, req);
				}
			}
			//not all sync nodes are alive
			logger.warn("Some nodes are not alive!");
			return new Response(req.getRequestId(), false, req);
		}

		@Override
		public boolean requiresResponse() {
			return true;
		}
		
	}
	
	class GetRequestHandler implements IRequestHandler {

		@Override
		public Response handleRequest(Request req) {
			// the key is the first item of the request
			Integer key;
			Response resp = new Response("", false, req);
			try {
				key = Integer.parseInt((String) req.getItems().get(0));
				resp = new Response(Memory.get(key), true, req);
				logger.debug("Returning " + resp.getResponseMessage() + " for key " + key);
			} catch (Exception e) {
				logger.warn("Key " + req.getItems().get(0) + " was no Integer.");
			}
			return resp;
		}

		@Override
		public boolean requiresResponse() {
			return true;
		}
		
	}
	
	class DeleteRequestHandler implements IRequestHandler {

		@Override
		public Response handleRequest(Request req) {
			// the key is the first item of the request
			Integer key = -1;
			Response resp = new Response("", false, req);
			try {
				key = Integer.parseInt((String) req.getItems().get(0));
				resp = new Response(Memory.delete(key), true, req);
				logger.debug("Deleted " + resp.getResponseMessage() + " for key " + key);
			} catch (Exception e) {
				logger.warn("Key " + req.getItems().get(0) + " was no Integer.");
				return resp;
			}
			// send to all other nodes message to delete async, if second item is a 1
			try {
				Integer second = Integer.parseInt((String) req.getItems().get(1));
				if (second == 1) {
					logger.debug("Asking all other nodes to delete key " + key);
					List<String[]> hosts = Mastermind.c.getAllHosts();
					for (String[] hostA: hosts) {
						if (hostA[0].equals(Mastermind.c.getHostIPForNode(Mastermind.c.getMyNode())) && 
								hostA[1].equals(""+Mastermind.c.getHostPortForNode(Mastermind.c.getMyNode()))) {
							// don't send message to ourself
						} else {
							Sender s = new Sender(hostA[0], Integer.parseInt(hostA[1]));
							Request req2 = new Request("" + key, "delete", Mastermind.c.getMyNode());
							s.sendMessageAsync(req2, new AsyncCallbackRecipient() {
								
								@Override
								public void callback(Response resp) {
									logger.info("Got response that " + resp.getResponseMessage() + " was deleted."); 
								}
							});
						}	
					}
				}
			} catch (Exception e) {
				// do nothing, we don't need to send it
			}
			return resp;
		}

		@Override
		public boolean requiresResponse() {
			return true;
		}
		
	}
	
}

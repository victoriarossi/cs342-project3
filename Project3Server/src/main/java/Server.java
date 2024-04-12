import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;


public class Server{

	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	static ArrayList<String> clientID = new ArrayList<>();
	TheServer server;
	private Consumer<Serializable> callback;


	Server(Consumer<Serializable> call){
	
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread {

		public void run() {

			try (ServerSocket mysocket = new ServerSocket(5555);) {
				System.out.println("Server is waiting for a client!");

				// keeps accepting new client connections
				while (true) {
					ClientThread c = new ClientThread(mysocket.accept());
					clients.add(c);
					c.start();
				}
			}//end of try
			catch (Exception e) {
				callback.accept("Server socket did not launch");
			}

		}
	}

		class ClientThread extends Thread{
		
			Socket connection;
			ObjectInputStream in;
			ObjectOutputStream out;

			String clientName = "";


			ClientThread(Socket s){
				this.connection = s; // stores client's socket connection
			}

			
			public void run(){

				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);

					// processes incoming messages from client
					while (true) {
						Message message = (Message) in.readObject();

						// checks if the message is a username check request
						if (message.getMessageType() == Message.MessageType.BROADCAST && "checkUser".equals(message.getMessageContent())) {
							String initialName = message.getUserID();
							if (!clientID.contains(initialName)) {
								clientID.add(initialName);
								clientName = initialName;
								callback.accept(clientName + " has connected to server.");
								updateClients(new Message("Server", "New User", Message.MessageType.BROADCAST, new ArrayList<>(clientID)));
								out.writeObject(new Message("Server", "Ok Username", Message.MessageType.PRIVATE,new ArrayList<>(clientID)));
							} else {
								out.writeObject(new Message("Server", "Taken Username", Message.MessageType.PRIVATE));
							}
						}
						else {
							// handles regular messages
//							callback.accept(clientName + " sent: " + message.getMessageContent());
//							updateClients(new Message(message, clientID));
							updateClients(message);
						}
					}
				}
				catch (Exception e) {
					callback.accept(clientName + " has left the chat.");

					synchronized (clientID) {
						clientID.remove(clientName);
					}
					updateClients(new Message("Server", "User left", Message.MessageType.BROADCAST, new ArrayList<>(clientID)));

					synchronized (clients) {
						clients.remove(this);
					}
				}
			}//end of run

			// method to send a message to all clients
			public void updateClients(Message message) {
				if(message.getMessageType() == Message.MessageType.BROADCAST) {
					for(ClientThread t : clients) {
						if(t.clientName != "") {
							try {
								t.out.writeObject(message);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				else if (message.getMessageType() == Message.MessageType.PRIVATE) {
					String recipient = message.getUserIDReceiver();
					for(ClientThread t : clients) {
						if(t.clientName.equals(recipient)) {
							try {
								t.out.writeObject(message);
//								break;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}//end of client thread
}

	

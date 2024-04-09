import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

public class Server{

	int count = 1;

	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	ArrayList<String> clientID = new ArrayList<>();
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


				while (true) {
					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept("client has connected to server: " + "client #" + count);
					clients.add(c);
					c.start();
					count++;
				}
			}//end of try
			catch (Exception e) {
				callback.accept("Server socket did not launch");
			}

		}
	}
	

		class ClientThread extends Thread{
			int count;
		
			Socket connection;
			ObjectInputStream in;
			ObjectOutputStream out;

			String clientName = "";

			ClientThread(Socket s, int count){
				this.connection = s; // stores client's socket connection
				this.count = count;
			}

			// method to send a message to all clients
			public void updateClients(Message message) {
				for(ClientThread t : clients) {
					try {
						t.out.writeObject(message);
					}
					catch(Exception e) {e.printStackTrace();}
				}
			}
			
			public void run(){

				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);


					while (clientName == "") {
						try {
							Message usernameMsg = (Message) in.readObject();
							String initialName = usernameMsg.getUserID();

							if (!clientID.contains(initialName)) {
								clientID.add(initialName);
								clientName = initialName;
								Message data = new Message("Server", "Ok Username", Message.MessageType.PRIVATE);
								out.writeObject(data);
								updateClients(data);
							} else {
								Message data = new Message("Server", "Taken Username", Message.MessageType.PRIVATE);
								out.writeObject(data);
								updateClients(data);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					while (true) {
						try {
							// reads message object from the client
							Message data = (Message) in.readObject();
							callback.accept(clientName + " sent: " + data.getMessageContent());
							updateClients(data);
						} catch (Exception e) {
							callback.accept(clientName + " has disconnected.");
							clientID.remove(clientName);
							clients.remove(this);
							break;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}//end of run


		}//end of client thread


}


	
	

	

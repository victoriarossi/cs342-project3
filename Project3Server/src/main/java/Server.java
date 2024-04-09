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
	String clientName = "";
	
	
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
//					callback.accept(clientName + " has connected to server: ");
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

			
			public void run(){

				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);

					Message usernameMsg = (Message) in.readObject();
					String initialName = usernameMsg.getUserID();

					synchronized (clientID) {
						if (!clientID.contains(initialName)) {
							clientID.add(initialName);
							clientName = initialName;
							callback.accept(clientName + " has connected to server.");
							out.writeObject(new Message("Server", "Ok Username", Message.MessageType.PRIVATE));
						} else {
							out.writeObject(new Message("Server", "Taken Username", Message.MessageType.PRIVATE));
							return;
						}
					}


					while (true) {
						// reads message object from the client
						Message data = (Message) in.readObject();
						callback.accept(clientName + " sent: " + data.getMessageContent());
						updateClients(data);
					}
				}
				catch (Exception e) {
					callback.accept(clientName + " has disconnected");
					synchronized (clientID) {
						clientID.remove(clientName);
						clients.remove(this);
					}
				}
			}//end of run

			// method to send a message to all clients
			public void updateClients(Message message) {
				for(ClientThread t : clients) {
					try {
						t.out.writeObject(message);
					}
					catch(Exception e) {e.printStackTrace();}
				}
			}


		}//end of client thread


}


	
	

	

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
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}

				// creates welcome message for new clients (CHANGE)
				Message welcomeMessage = new Message("Server", "New client on server: client #" + count, Message.MessageType.BROADCAST);
				updateClients(welcomeMessage);

				while(true) {
					try {
						// reads message object from the client
						Message data = (Message) in.readObject();
						callback.accept("client: " + count + " sent: " + data.getMessageContent());
						updateClients(data);

					}
					catch(Exception e) {
						callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
						clients.remove(this);
						break;
					}
				}
			}//end of run


		}//end of client thread
}


	
	

	

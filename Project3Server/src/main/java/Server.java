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

	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	
	
	Server(Consumer<Serializable> call){
	
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread{
		
		public void run() {
		
			try(ServerSocket mysocket = new ServerSocket(5555);){
				System.out.println("Server is waiting for a client!");


				while(true) {

					Socket clientSocket = mysocket.accept();
					ClientThread c = new ClientThread(clientSocket);
					c.start();


					}
				}//end of try
					catch(Exception e) {
						callback.accept("Server socket did not launch");
					}
				}//end of while
		}
	

		class ClientThread extends Thread{
			
		
			Socket connection;
			String username; // stores connected client's username
			ObjectInputStream in;
			ObjectOutputStream out;

			ClientThread(Socket s){
				this.connection = s; // stores client's socket connection
			}

			// method to send a message to all clients
			public void updateClients(String message) {
				for (ClientThread t : clients) { // iterates through all connected clients
					try {
						t.out.writeObject(message); // sends message to each client
					}
					catch (Exception e) {
						e.printStackTrace(); // prints stack trace for exception thrown
					}
				}
			}
			
			public void run(){

				try {
					out = new ObjectOutputStream(connection.getOutputStream());
					in = new ObjectInputStream(connection.getInputStream());
					connection.setTcpNoDelay(true);

					// read username as first piece of data from client
					username = in.readObject().toString();

					synchronized (clients) {
						for (ClientThread client : clients) {
							if (client.username != null && client.username.equals(username)) {
								out.writeObject("Username is taken.");
								return;
							}
						}
						// adds new client thread to list of clients
						clients.add(this);
					}

					callback.accept(username + " has connected to the server");

					// notifies all clients about new connection
					updateClients(username + " has joined server");

					while (true) {
						String data = in.readObject().toString();
						callback.accept(username + " send: " + data); // process incoming data from client
						updateClients(username + " said: " + data); // send received message to all clients
					}
				}
				catch(Exception e){
					callback.accept("Oops... Something went wrong with the connection for " + username);
					updateClients(username + " has left the server!");
					clients.remove(this); // removes client from list when disconnected
				}
				}//end of run
			
			
		}//end of client thread
}


	
	

	

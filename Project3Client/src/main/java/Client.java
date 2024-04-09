import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;



public class Client extends Thread{

	
	Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private Consumer<Serializable> callback;
	
	Client(Consumer<Serializable> call){
		callback = call;
	}
	
	public void run() {
		
		try {
			socketClient= new Socket("127.0.0.1",5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);

			while (true) {
				try {
					Message message = (Message) in.readObject(); // reads incoming message object from server
					callback.accept(message); // passes received Message object to callback for processing
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		catch(Exception e) {
			e.printStackTrace();
		}

    }

	// Method to send a Message object to the server
	public void send(Message message) {
		
		try {
			out.writeObject(message); // writes Message object to the ObjectOutputStream
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}

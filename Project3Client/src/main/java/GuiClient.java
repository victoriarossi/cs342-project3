import java.io.Serializable;
import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{

	TextField c1;
	Button b1;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;
	ListView<String> listItems2;

	Label connectionStatus;
	
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data -> {
				Message msg = (Message) data;
				Platform.runLater(() -> {
					if ("Ok Username".equals(msg.getMessageContent())) {
						primaryStage.setScene(sceneMap.get("client"));
					}
					else if ("Taken Username".equals(msg.getMessageContent())) {
						connectionStatus.setText("Username is taken. Try another one.");
					}
					else {
						listItems2.getItems().add(msg.toString());
					}
				});
		});
							
		clientConnection.start();

		listItems2 = new ListView<String>();

		c1 = new TextField();
		b1 = new Button("Send");
		b1.setOnAction(e->{
			String messageContent = c1.getText();
			String currUsername = clientConnection.getUsername();
			Message message = new Message(currUsername, messageContent, Message.MessageType.BROADCAST);
			clientConnection.send(message);

			c1.clear();
		});
		
		sceneMap = new HashMap<String, Scene>();

		sceneMap.put("client",  createClientGui());
		sceneMap.put("clientLogin", createLoginScene(primaryStage)); // adds login screen to scene map
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });


		primaryStage.setScene(sceneMap.get("clientLogin")); // starts the scene in the login scene
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}

	private Scene createLoginScene(Stage primaryStage) {

		Label title = new Label("Enter username:");

		TextField usernameField = new TextField();

		Button connectBtn = new Button("Connect");
		connectBtn.setOnAction(e -> {
			String usernameAttempt = usernameField.getText();
			if (!usernameAttempt.trim().isEmpty()) {
				clientConnection.setUsername(usernameAttempt);
				connectionStatus.setText("");
			}
			else {
				connectionStatus.setText("Username cannot be empty.");
			}
		});

		VBox root = new VBox(10, title, usernameField, connectBtn);
		root.setStyle("-fx-background-color: #DDC6A3");

		// returns login scene
		return new Scene(root,400, 300);
	}

	public Scene createClientGui() {

		clientBox = new VBox(10, c1,b1,listItems2);
		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");
		return new Scene(clientBox, 400, 300);
		
	}

}

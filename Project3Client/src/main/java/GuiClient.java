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
	TextField usernameField = new TextField();

	Button connectBtn = new Button("Connect");

	TextField c1;
	Button b1;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;
	ListView<String> listItems2;

	Label connectionStatus;

	String btnStyle = "-fx-background-color: #DDC6A3; -fx-text-fill: black; -fx-background-radius: 25px; -fx-padding: 10; -fx-cursor: hand";
	String titleStyle = "-fx-font-size: 24; -fx-font-weight: bold";
	String subtitleStyle = "-fx-font-size: 18; -fx-font-weight: bold";
	
	
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
					Alert alert = new Alert(Alert.AlertType.ERROR, "Username is taken. Try another one.");
					alert.setHeaderText("Username Error");
					alert.showAndWait();
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
		title.setStyle(titleStyle);

		usernameField.setMaxWidth(200);
		usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 25px");

		connectBtn.setOnAction(e -> {
			String usernameAttempt = usernameField.getText();
			if (!usernameAttempt.isEmpty()) {
				clientConnection.setUsername(usernameAttempt);
			}
			else {
				showAlert("Username cannot be empty.", Alert.AlertType.ERROR);
			}
		});

		connectBtn.setStyle(btnStyle);
		VBox root = new VBox(40, title, usernameField, connectBtn);
		root.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		root.setAlignment(Pos.CENTER);

		// returns login scene
		return new Scene(root,500, 400);
	}

	public Scene createClientGui() {
		Label title = new Label("Input your message:");
		title.setStyle(subtitleStyle + "; -fx-padding: 10");

		TextField messageTextField = new TextField();
		messageTextField.setMaxWidth(250);
		messageTextField.setStyle("-fx-padding: 10; -fx-background-radius: 25px;");

		Label sendTo = new Label("Send to: ");
		sendTo.setStyle(subtitleStyle);

		Button allUsers = new Button("All users");
		Button oneUser = new Button("One user");
		allUsers.setStyle(btnStyle);
		oneUser.setStyle(btnStyle);
		HBox btns = new HBox(20, allUsers, oneUser);
		btns.setAlignment(Pos.CENTER);

		allUsers.setOnAction( e -> {
			String messageContent = messageTextField.getText();
			String currUsername = clientConnection.getUsername();
			Message msg = new Message(currUsername, messageContent, Message.MessageType.BROADCAST);
			clientConnection.send(msg);
			messageTextField.clear();
		});

		oneUser.setOnAction( e -> {
			//something
		});

		allUsers.disableProperty().bind(messageTextField.textProperty().isEmpty());
		oneUser.disableProperty().bind(messageTextField.textProperty().isEmpty());


		clientBox = new VBox(20, title, messageTextField, sendTo, btns, listItems2);
		clientBox.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		VBox.setMargin(clientBox, new Insets(30));
		clientBox.setAlignment(Pos.CENTER);

		return new Scene(clientBox, 500, 400);
	}

	private void showAlert(String message, Alert.AlertType type) {
		Alert alert = new Alert(type, message);
		alert.showAndWait();
	}

	public Scene optionsScreen(Stage stage){
		VBox root = new VBox(40);
		root.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		root.setAlignment(Pos.CENTER);
		return new Scene(root,500, 400);
	}

}

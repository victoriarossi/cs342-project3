import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


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

	// styling strings for different UI
	String btnStyle = "-fx-background-color: #DDC6A3; -fx-text-fill: black; -fx-background-radius: 25px; -fx-padding: 14; -fx-cursor: hand; -fx-font-size: 18";
	String titleStyle = "-fx-font-size: 24; -fx-font-weight: bold";
	String subtitleStyle = "-fx-font-size: 18; -fx-font-weight: bold";
	
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// initialize client connection and setup receiving messages
		clientConnection = new Client(data -> {
			Message msg = (Message) data;
			Platform.runLater(() -> {
				if ("Ok Username".equals(msg.getMessageContent())) {
					primaryStage.setScene(sceneMap.get("options"));
				}
				else if ("Taken Username".equals(msg.getMessageContent())) {
					showAlert("Username is taken. Try another one.", Alert.AlertType.ERROR);
				}
				else {
					listItems2.getItems().add(msg.toString());
				}
			});
		});

		clientConnection.start();

		listItems2 = new ListView<String>();

		c1 = new TextField(); // input field for messages
		b1 = new Button("Send"); // send button for messages
		b1.setOnAction(e->{
			String messageContent = c1.getText();
			String currUsername = clientConnection.getUsername();
			Message message = new Message(currUsername, messageContent, Message.MessageType.BROADCAST);
			clientConnection.send(message);

			c1.clear();
		});

		// scene map for different scenes
		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("client",  createClientGui(primaryStage));
		sceneMap.put("clientLogin", createLoginScene(primaryStage)); // adds login screen to scene map
		sceneMap.put("options", createOptionsScreen(primaryStage)); // adds the options screen to scene map
		
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


	// creates the initial login scene
	private Scene createLoginScene(Stage primaryStage) {

		Label title = new Label("Enter username:");
		title.setStyle(titleStyle);

		usernameField.setMaxWidth(200);
		usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 25px");

		// handles connect button action. Does not allow taken username or empty username
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

	// creates main client UI
	public Scene createClientGui(Stage primaryStage) {
		BorderPane pane =  new BorderPane();

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

		// Create back button
		Image home = new Image("back_arrow.png");
		ImageView homeView = new ImageView(home);
		homeView.setFitHeight(15);
		homeView.setFitWidth(15);
		Button backBtn = new Button();
		backBtn.setOnAction( e -> {
			primaryStage.setScene(sceneMap.get("options"));
		});
		backBtn.setGraphic(homeView);
		backBtn.setStyle(btnStyle.concat("-fx-font-size: 14; -fx-padding: 10; -fx-background-radius: 25px;"));

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);
		Color backgroundColor = Color.web("#F4DAB3");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		clientBox = new VBox(20, title, messageTextField, sendTo, btns, listItems2);
		clientBox.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		VBox.setMargin(clientBox, new Insets(30));
		clientBox.setAlignment(Pos.CENTER);

		pane.setCenter(clientBox);

		return new Scene(pane, 500, 400);
	}


	// shows popup for invalid usernames
	private void showAlert(String message, Alert.AlertType type) {
		Alert alert = new Alert(type, message);
		alert.setHeaderText(null);
		alert.showAndWait();
	}

	// creates options scene
	public Scene createOptionsScreen(Stage primaryStage){
		Button sendMessage = new Button("Send Message");
		Button users = new Button("View All Users");
		Button messages = new Button("View Messages");
		sendMessage.setStyle(btnStyle);
		users.setStyle(btnStyle);
		messages.setStyle(btnStyle);

		// when you click send, changes the scene
		sendMessage.setOnAction(e -> {
			primaryStage.setScene(sceneMap.get("client"));
		});

		VBox root = new VBox(40, sendMessage, users, messages);
		root.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		root.setAlignment(Pos.CENTER);
		return new Scene(root,500, 400);
	}

}

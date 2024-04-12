import java.io.Serializable;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class GuiClient extends Application{
	TextField usernameField = new TextField();

	Button connectBtn = new Button("Connect");

	private TextField messageTextField;

	private String selectedUser;

	TextField c1;
	Button b1;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;

	private String messageContent;
	ListView<String> listItems2;
	ListView<String> displayListUsers;
	ListView<String> displayListItems;
	ObservableList<String> storeUsersInListView;

	String currUsername;


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
					// updates the user list
					updateUserList(msg);
					primaryStage.setScene(sceneMap.get("options"));
				}
				else if ("Taken Username".equals(msg.getMessageContent())) {
					showAlert("Be original, Professor McCarty dislikes copycats!", primaryStage);
				}
				else {

					// updates the user list as long as it contains users
					if (msg.getListOfUsers() != null) {
						updateUserList(msg);
					}

					boolean isPrivate = msg.getMessageType() == Message.MessageType.PRIVATE;
					boolean isForCurrentUser = isPrivate && msg.getUserIDReceiver().equals(clientConnection.getUsername());

					if (isPrivate){
						if (isForCurrentUser || msg.getUserID().equals(clientConnection.getUsername())) {
							String privateMsg = "Whisper from " + msg.getUserID() + ": " + msg.getMessageContent();
							listItems2.getItems().add(privateMsg);
						}
					}
					else {
						if (!"New User".equals(msg.getMessageContent())) {
							listItems2.getItems().add(msg.getUserID() + ": " + msg.getMessageContent());
						}
					}
				}
			});
		});

		clientConnection.start();
		// initialize lists view
		listItems2 = new ListView<String>();
		storeUsersInListView = FXCollections.observableArrayList();
		displayListUsers = new ListView<>();
		displayListItems = new ListView<>();

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
		sceneMap.put("options", createOptionsScene(primaryStage)); // adds the options screen to scene map
		sceneMap.put("users", createViewUsersScene(primaryStage)); // adds the view users screen to scene map
		sceneMap.put("selectUser", createSelectUserScene(primaryStage, messageContent, currUsername)); //add the select user screen to scene map
		sceneMap.put("viewMessages", createViewMessages(primaryStage));

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
				currUsername = usernameAttempt;
			}
			else {
				showAlert("Professor McCarty can't grade invisible students!", primaryStage);
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

		messageTextField = new TextField();
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

		oneUser.setOnAction(e -> {
			messageContent = messageTextField.getText();
			primaryStage.setScene(sceneMap.get("selectUser"));
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
	private void showAlert(String message, Stage primaryStage) {

		VBox root = new VBox(20);
		root.setAlignment(Pos.CENTER);
		root.setStyle("-fx-background-color: #ffcccc");

		Label header = new Label(message);
		header.setFont(new Font("Arial", 16));
		header.setStyle("-fx-text-fill: #550000");

		Button returnBtn = new Button("I understand");
		returnBtn.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 10px; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-radius: 5px");
		returnBtn.setOnAction(e -> {
			primaryStage.setScene(sceneMap.get("clientLogin"));
		});

		root.getChildren().addAll(header, returnBtn);

		Scene errorScene = new Scene(root, 400, 300);
		primaryStage.setScene(errorScene);
		primaryStage.show();

	}

	// creates options scene
	public Scene createOptionsScene(Stage primaryStage){
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

		// when you click view users, changes the scene
		users.setOnAction( e -> {
			primaryStage.setScene(sceneMap.get("users"));
		});

		messages.setOnAction(e -> {
			primaryStage.setScene(sceneMap.get("viewMessages"));
		});

		VBox root = new VBox(40, sendMessage, users, messages);
		root.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		root.setAlignment(Pos.CENTER);
		return new Scene(root,500, 400);
	}

	public Scene createViewUsersScene(Stage primaryStage){
		BorderPane pane = new BorderPane();

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

		Label title = new Label("List of all users:");
		title.setStyle(titleStyle);

		VBox users = new VBox(20, title, displayListUsers);
		users.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		VBox.setMargin(users, new Insets(30));
		users.setAlignment(Pos.CENTER);
		displayListUsers.setMaxWidth(400);
		displayListUsers.setMaxHeight(250);
		pane.setCenter(users);
		return new Scene(pane, 500, 400);
	}

	public Scene createSelectUserScene(Stage primaryStage, String messageContent, String currUsername){
		BorderPane pane = new BorderPane();

		// Create back button
		Image home = new Image("back_arrow.png");
		ImageView homeView = new ImageView(home);
		homeView.setFitHeight(15);
		homeView.setFitWidth(15);
		Button backBtn = new Button();
		backBtn.setOnAction( e -> {
			primaryStage.setScene(sceneMap.get("client"));
		});
		backBtn.setGraphic(homeView);
		backBtn.setStyle(btnStyle.concat("-fx-font-size: 14; -fx-padding: 10; -fx-background-radius: 25px;"));

		BorderPane.setAlignment(backBtn, Pos.TOP_LEFT);
		pane.setTop(backBtn);

		Color backgroundColor = Color.web("#F4DAB3");
		pane.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

		Label title = new Label("Select the user you want to send to:");
		title.setStyle(titleStyle);

//		final String[] receiverUsername = new String[1];
		displayListItems.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedUser = displayListItems.getSelectionModel().getSelectedItem();
			}
		});
		
		Button send = new Button("Send");
		send.setStyle(btnStyle);
		send.setOnAction(e -> {
			String currMsgContent = messageTextField.getText();
			String usernameCurrent = clientConnection.getUsername();
			Message msg = new Message(usernameCurrent, currMsgContent, selectedUser);
			clientConnection.send(msg);
			listItems2.getItems().add("Sent to " + selectedUser + ": " + currMsgContent);
			messageTextField.clear();
			primaryStage.setScene(sceneMap.get("client"));
		});

		VBox users = new VBox(20, title, displayListItems, send);
		users.setStyle("-fx-background-color: #F4DAB3; -fx-font-family: 'serif'");
		VBox.setMargin(users, new Insets(30));
		users.setAlignment(Pos.CENTER);
		displayListItems.setMaxWidth(400);
		displayListItems.setMaxHeight(250);
		pane.setCenter(users);
		return new Scene(pane, 500, 400);
	}

	public Scene createViewMessages(Stage primaryStage) {
		VBox root = new VBox(10, listItems2); // adds message chat to vbox

		return new Scene(root, 500, 400);
	}

	// helper function to update the user list
	private void updateUserList(Message msg) {
		storeUsersInListView.clear();
		storeUsersInListView.addAll(msg.getListOfUsers());
		displayListUsers.setItems(storeUsersInListView);
		for(String user: msg.getListOfUsers()) {
			if(!user.equals(currUsername) && !displayListItems.getItems().contains(user))
				displayListItems.getItems().add(user);
		}
	}
}

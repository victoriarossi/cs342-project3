import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.Alert.AlertType;

public class GuiClient extends Application{

	TextField usernameField; // stores the username
	Button connectButton; // button to connect user to server

	TextField c1;
	Button b1;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;
	
	ListView<String> listItems2;
	
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data->{
				Platform.runLater(()->{
					if (data.toString().equals("Username is taken.")) {
						showAlert("Error", "Username is taken. Please choose a different name.");
					}

			});
		});
							
		clientConnection.start();

		listItems2 = new ListView<String>();
		
		c1 = new TextField();
		b1 = new Button("Send");
		b1.setOnAction(e->{clientConnection.send(c1.getText()); c1.clear();});
		
		sceneMap = new HashMap<String, Scene>();

		sceneMap.put("client",  createClientGui());
		sceneMap.put("clientLogin", createLoginScene());
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });


		primaryStage.setScene(sceneMap.get("clientLogin"));
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}

	private Scene createLoginScene() {
		BorderPane layout = new BorderPane();

		layout.setStyle("-fx-background-color: #DDC6A3");

		Label titleLabel = new Label("Enter Username");
		titleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black");
		BorderPane.setAlignment(titleLabel, Pos.CENTER);
		layout.setTop(titleLabel);
		layout.setPadding(new Insets(10, 0, 10, 0));

		usernameField = new TextField();
		usernameField.setPromptText("Enter Username Here");
		usernameField.setPrefHeight(30);
		usernameField.setMaxWidth(200);
		layout.setCenter(usernameField);
		BorderPane.setAlignment(usernameField, Pos.CENTER);

		connectButton = new Button("Connect");
		connectButton.setPrefHeight(30);
		connectButton.setPrefWidth(100);
		layout.setBottom(connectButton);
		BorderPane.setAlignment(connectButton, Pos.CENTER);
		connectButton.setOnAction(e -> handleConnect());

		BorderPane.setAlignment(connectButton, Pos.CENTER);

		return new Scene(layout, 450, 300);
	}

	private void handleConnect() {
		String username = usernameField.getText().trim();
		if (!username.isEmpty()) {
			clientConnection.send(username);
		}
		else {
			showAlert("Error", "Username cannot be empty. Please try again.");
		}
	}

	private void showAlert(String title, String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
		usernameField.requestFocus();
	}

	public Scene createClientGui() {
		
		clientBox = new VBox(10, c1,b1,listItems2);
		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");
		return new Scene(clientBox, 400, 300);
		
	}

}

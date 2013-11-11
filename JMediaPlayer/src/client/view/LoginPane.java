package client.view;

import java.io.IOException;

import client.Client;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * View class that displays the login components to the user
 * upon the opening of the client GUI.
 * 
 * @author Andrzej Brynczka
 *
 */
public class LoginPane extends BorderPane {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	private final String BUTTON_ID_LOGIN = "Login";
	private final String TEXTFIELD_ID_USERNAME = "Enter username";
	private final String TEXTFIELD_ID_SERVERPASS = "Enter server password";
	private final String TEXTFIELD_ID_IP = "Enter server IP";
	private final String TEXTFIELD_ID_PORT = "Enter server port";
	
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	private Client m_client;
	
	private TextField m_userNameTF;
	private TextField m_serverPassTF;
	private TextField m_ipTF;
	private TextField m_portTF;
	
	private Label m_feedback;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	public LoginPane(Client a_client){
		m_client = a_client;
				
		VBox loginBox = new VBox();
		loginBox.getChildren().addAll( createLogin() );
		
		//leave space on top for the menu bar
		setCenter( loginBox );
		BorderPane.setMargin( loginBox, new Insets(8,0,0,0));
		setMinSize(400, 500);
		setPrefSize(500, 500);
		setMaxSize(1000, 700);
	}
	
	private Pane createLogin(){
		
		//create the labels, textfields, and login button
		Label userNameLabel = new Label("Username:");
		m_userNameTF = new TextField("username");
		m_userNameTF.setId( TEXTFIELD_ID_USERNAME );
		m_userNameTF.setEditable( true );
		m_userNameTF.setMaxWidth( 150 );
		
		Label serverPassLabel = new Label("Server Password:");
		m_serverPassTF = new TextField("");
		m_serverPassTF.setId( TEXTFIELD_ID_SERVERPASS );
		m_serverPassTF.setEditable( true );
		m_serverPassTF.setMaxWidth( 150 );
		
		Label ipLabel = new Label("Server IP:");
		m_ipTF = new TextField("localhost");
		m_ipTF.setId( TEXTFIELD_ID_IP );
		m_ipTF.setEditable( true );
		m_ipTF.setMaxWidth( 150 );
		
		Label portLabel = new Label("Port #:");
		m_portTF = new TextField("5376");
		m_portTF.setId( TEXTFIELD_ID_PORT );
		m_portTF.setEditable( true );
		m_portTF.setMaxWidth( 150 );
		
		Button loginButton = new Button("Login");
		loginButton.setId( BUTTON_ID_LOGIN );
		loginButton.setOnAction( getButtonHandler() );
		 
		//create and bind the feedback label to the response fed 
		//to the client from the server
		m_feedback = new Label("");
		m_feedback.textProperty().bind( m_client.failedLoginFeedbackProperty());

		//create the grid pane to store the information in
		GridPane loginGrid = new GridPane();
		
		//set the alignment properties of the labels and button
		GridPane.setHalignment( userNameLabel, HPos.RIGHT );
		GridPane.setHalignment( serverPassLabel, HPos.RIGHT );
		GridPane.setHalignment( ipLabel, HPos.RIGHT );
		GridPane.setHalignment( portLabel, HPos.RIGHT );
		GridPane.setMargin(loginButton, new Insets(10, 0, 0, 0));
		GridPane.setRowSpan( m_feedback, 5);
		GridPane.setColumnSpan( m_feedback, 2);
		
		//add the labels, textfields, and button to the grid
		loginGrid.add(userNameLabel, 0, 3);
		loginGrid.add(m_userNameTF, 1, 3);
        
		loginGrid.add(serverPassLabel, 0, 4);
		loginGrid.add(m_serverPassTF, 1,4);
        
		loginGrid.add( ipLabel, 0, 5);
		loginGrid.add( m_ipTF,  1, 5);
		
		loginGrid.add( portLabel, 0, 6);	
		loginGrid.add( m_portTF, 1,6);
		
		loginGrid.add( loginButton, 1, 7);
		
		loginGrid.add( m_feedback, 0, 8);
		
		
		return loginGrid;			
	}
	
	private EventHandler<ActionEvent> getButtonHandler(){
		
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
		
			@Override
			public void handle(ActionEvent event) {
				Button source = (Button) event.getSource();
				
				//handle the button's event based on ID

				switch( source.getId() )
				{
					case BUTTON_ID_LOGIN:
						//login to the client
						String username = m_userNameTF.getText();
						String serverPass = m_serverPassTF.getText();
						String ip = m_ipTF.getText();
						int port = Integer.parseInt( m_portTF.getText() );
						
						System.out.println("attempting login");
						m_client.setServerLoginInfo(username, serverPass, ip, port);
						try {
							m_client.connectToServer();
						} catch (IOException e) {
							m_client.setFailedLoginFeedback(
									"Unable to connect to server. \n" 
									+ e.getMessage() );
							m_client.terminateConnection();
						}
						
						break;
					default:
						break;			
				}		
			}	
		};
		
		return handler;
	}
}

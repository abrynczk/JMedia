package client;

import client.view.ClientView;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * The driver class for the client, which starts the main
 * client class and client view.
 * @author Andrzej Brynczka
 *
 */
public class ClientDriver extends Stage {

	private Client m_client;
	private ClientView m_clientView;
	
	public ClientDriver(){
		
		m_client = new Client();
		m_clientView = new ClientView( m_client, this );
		setScene( m_clientView );

		setOnCloseRequest( new EventHandler<WindowEvent>(){

			@Override
			public void handle(WindowEvent event) {
				System.out.println("Attempting to close client");
				
				m_client.terminateConnection();	
				System.out.println("terminated connection...");
				
				close();
				System.out.println("Closed the client");
			}
			
		});
		setOnHidden( new EventHandler<WindowEvent>(){

			@Override
			public void handle(WindowEvent event) {
				System.out.println("Attempting to close client");
				
				m_client.terminateConnection();	
				System.out.println("terminated connection...");
				
				close();
				System.out.println("Closed the client");
			}
			
		});
	}	
}

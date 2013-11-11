package client;

import client.messages.Message.MessageResponse;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Container for the client data associated directly with the user.
 * 
 * @author Andrzej Brynczka
 *
 */
public class User {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/**
	 * This User's username on the server
	 */
	private String m_userName;
	/**
	 * The user's success at attempting to login to the server
	 * as an admin
	 * 
	 * MessageResponse.Success if so, MessageResponse.Failure otherwise
	 */
	private SimpleObjectProperty<MessageResponse> m_adminLoginSuccess;
	
	/**
	 * The user's mute status, as maintained by the server
	 */
	private boolean m_mute;
	
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * Construct the complete User object with all contained
	 * data
	 * @param a_userName String, the user's name
	 * @param a_adminSuccess MessageResponse, the user's success at
	 * 	logging in as an admin
	 * @param a_mute boolean, true if the user is muted, false otherwise
	 */
	User(String a_userName, MessageResponse a_adminSuccess, boolean a_mute){
		m_userName = a_userName;
		m_adminLoginSuccess = 
				new SimpleObjectProperty<MessageResponse>( a_adminSuccess );
		m_mute = a_mute;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the user's name
	 * @return String containing the name
	 */
	public String getUserName(){
		return m_userName;
	}
	
	/**
	 * Get the user's success at attempting to login as an admin
	 * @return MessageResponse, the success or failure
	 */
	public MessageResponse getAdminLoginSuccess(){
		return m_adminLoginSuccess.getValue();
	}
	
	/**
	 * Get the SimpleObjectProperty for the adminLoginSuccess variable
	 * @return the SimpleObjectProperty
	 */
	public SimpleObjectProperty<MessageResponse> adminLoginSuccessProperty(){
		return m_adminLoginSuccess;
	}
	
	/**
	 * Get the user's mute status, as viewed by the server
	 * @return true if the user is muted, false otherwise
	 */
	public boolean isMute(){
		return m_mute;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the user's name
	 * 
	 * @param a_userName String, the name to set
	 */
	public void setUserName(String a_userName){
		m_userName = a_userName;
	}
	
	/**
	 * Set the user's admin login success rate, based on the message
	 *  received from the server as a response
	 *  
	 * @param a_adminLogginSuccess MessageResponse, the server's response
	 */
	public void setAdminLoginSuccess(MessageResponse a_adminLogginSuccess){
		m_adminLoginSuccess.setValue( a_adminLogginSuccess );
	}
	
	/**
	 * Set the user's mute status, as maintained by the server
	 * @param a_mute boolean, true if muted or false if not
	 */
	public void setMute(boolean a_mute){
		m_mute = a_mute;
	}
}

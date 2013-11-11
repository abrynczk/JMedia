package server.messages;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Message containing information sent via a message attempting to login 
 * to administrator status. 
 * <p>
 * Contains the response from the server, determining the validity of the
 * provided administrator password, and functionality to convert the 
 * information into a proper response to the client via the 
 * <code>sendMessage</code> method.
 * 
 * @author Andrzej Brynczka
 *
 */
public class AdminLoginMessage extends Message {

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The name of the user that sent the login request*/
	private String m_senderName;
	
	/** The admin password sent in the message*/
	private String m_adminPass;
	
	/** The validity of the password sent in the message.
	 *  Stored in <code>MessageResponse</code> format for 
	 *  ability to be converted into a message response. */
	private MessageResponse m_passValidity;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the complete admin-login message, ready to be sent back as a 
	 * response to the client through the <code>sendMessage</code> function.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message's header
	 * @param a_sender <code>String</code>, the name of the sender
	 * @param a_password <code>String</code>, the administrator password 
	 * 	provided in the message
	 * @param a_valid <code>MessageResponse</code>, the administrator 
	 * 	password's validity provided by the server
	 */
	public AdminLoginMessage(MessageHeader a_header, String a_sender, 
			String a_password, MessageResponse a_valid) {
		super(a_header);
		m_senderName = a_sender;
		m_adminPass = a_password;
		m_passValidity = a_valid;
	}
	
	/**
	 * Create the basic admin-login message, ready to accept further 
	 *  information through mutator functions.
	 * @param a_header <code>MessageHeader</code>, the message's header
	 */
	public AdminLoginMessage(MessageHeader a_header){
		super(a_header);
	}
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the name of the user that sent the request.
	 * @return <code>String</code - the name of the user
	 */
	public String getSenderName(){
		return m_senderName;
	}
	
	/**
	 * Get the admin password sent in the message
	 * @return <code>String</code>, the password
	 */
	public String getAdminPassword(){
		return m_adminPass;
	}
	
	/**
	 * Get the validity of the password provided in the message
	 * @return <code>MessageResponse</code> - <code>Success</code>
	 * 	if the given password was correct, <code>Failure</code> otherwise
	 */
	public MessageResponse getPasswordValidity(){
		return m_passValidity;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the name of the user that sent the login request
	 * @param a_name <code>String</code>, the name of the sender
	 * @return <code>boolean</code> - <code>false</code> if the name
	 * 	was equal to <code>null</code>, <code>true</code> otherwise
	 */
	public boolean setSenderName( String a_name ){
		if( a_name == null ){
			return false;
		}
		
		m_senderName = a_name;
		return true;
	}
	/**
	 * Set the admin password provided in the message
	 * @param a_password <code>String</code>, the password to set
	 * @return <code>boolean</code> - <code>false</code> if the password is
	 * 	null, <code>true</code> otherwise
	 */
	public boolean setAdminPassword( String a_password ){
		if( a_password == null ){
			return false;
		}
		
		m_adminPass = a_password;
		return true;
	}
	
	/**
	 * Set the validity of the password provided in the message
	 * @param a_response <code>MessageResponse</code>, the validity
	 * 	(<code>Success</code>/<code>Failure</code>) of the password
	 * @return <code>MessageResponse</code> - <code>false</code> if the
	 * 	response is equal to <code>INVALID</code>, <code>true</code> otherwise
	 */
	public boolean setPasswordValidity( MessageResponse a_response ){
		if( a_response == MessageResponse.INVALID ){
			return false;
		}
		m_passValidity = a_response;
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the response to the admin login attempt to the given stream.
	 * Included are:
	 * </br>the message header
	 * </br>the login response byte
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {		
		//write the header code
		a_stream.write( m_header.getHeaderCode().getBytes() );
		
		//write the response byte
		a_stream.write ( m_passValidity.getCode() );	
		
		a_stream.flush();
		return true;
	}

}

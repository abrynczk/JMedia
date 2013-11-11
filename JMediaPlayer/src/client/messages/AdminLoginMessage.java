package client.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Message containing information to send when attempting to gain access
 * to administrator status on the server.
 * 
 * @author Andrzej Brynczka
 *
 */
public class AdminLoginMessage extends Message {

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The admin password to send in the message*/
	private String m_adminPass;	
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the complete admin-login message with its response,
	 * provided as a response message from the server after sending
	 * a request.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message's header
	 * @param a_password <code>String</code>, the administrator password 
	 * 	to be provided in the message
	 * @param a_valid <code>MessageResponse</code>, the server's response
	 * 	to the admin login indicating success or failure
	 */
	public AdminLoginMessage(MessageHeader a_header, String a_password, 
			MessageResponse a_valid) {
		super(a_header);
		m_adminPass = a_password;
		
	}
	
	/**
	 * Create the complete admin login request message, ready to be
	 * sent to the server.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message's header
	 * @param a_password <code>String</code>, the password to send
	 */
	public AdminLoginMessage(MessageHeader a_header, String a_password ){
		super(a_header);
		m_adminPass = a_password;
	}
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the admin password to be sent in the message
	 * @return <code>String</code>, the password
	 */
	public String getAdminPassword(){
		return m_adminPass;
	}
	
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the admin password to be sent in the message
	 * 
	 * @param a_password <code>String</code>, the password to set
	 * @return <code>boolean</code> - <code>false</code> if the password is
	 * 	null, <code>true</code> otherwise
	 */
	public boolean setAdminPassword(String a_password){
		if( a_password == null ){
			return false;
		}
		
		m_adminPass = a_password;
		return true;
	}
	
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the the admin login message to the given stream, attempting to
	 * gain access to administrator rights on the server.
	 * Included are:
	 * </br>the message header
	 * </br>the login password
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header code
		a_stream.write( m_header.getHeaderCode().getBytes() );
		
		//write the password's length and the password
		outData.writeInt( m_adminPass.length() );
		a_stream.write ( m_adminPass.getBytes() );	
		
		a_stream.flush();
		return true;
	}

}

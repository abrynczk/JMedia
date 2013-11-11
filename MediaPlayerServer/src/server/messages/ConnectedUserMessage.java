package server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Message class used to aware a client of a user connecting or disconnecting
 * from the server. Connection vs disconnection is determined by header code.
 * @author Andrzej Brynczka
 *
 */
public class ConnectedUserMessage extends Message {
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** 
	 * The name of the current client that connected or disconnected 
	 * from the server 
	 * */
	private String m_user;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create a message to handle indication of a user's connection status,
	 * to be sent as an update of a user connecting or disconnecting from 
	 * the server.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message's header code
	 * @param a_connectedUser <code>String</code>, the username of the client
	 * 	status
	 */
	public ConnectedUserMessage(MessageHeader a_header, String a_connectedUser){
		super(a_header);
		m_user = a_connectedUser;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the username of the client that connected/disconnected
	 * @return <code>String</code>, the username
	 */
	public String getUserName(){
		return m_user;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the connected/disconnected client's username
	 * @param a_name <code>String</code>, the name
	 * @return <code>boolean</code> - <code>false</code> if the name is null,
	 * 	<code>true</code> otherwise
	 */
	public boolean setUserName(String a_name){
		if( a_name == null ){
			return false;
		}
		m_user = a_name;
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the ConnectedUser information to the given stream.
	 * Included are:
	 * </br>the header code(indicating either connection or disconnection)
	 * </br>the size of the client's name and the name itself
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header code
		a_stream.write( m_header.getHeaderCode().getBytes() );
		
		//write the user's name 
		outData.writeInt( m_user.length() );
		a_stream.write( m_user.getBytes() );
		
		a_stream.flush();
		return true;
	}

}

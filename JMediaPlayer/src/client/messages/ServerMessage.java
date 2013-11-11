package client.messages;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Class containing information and functionality for receiving
 * basic informative messages from the server.
 * @author Andrzej Brynczka
 *
 */
public class ServerMessage extends Message {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************
	/** The maximum size of an error message */
	public static final int MAX_MESSAGE_SIZE = 250;

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The error message to send to a client */
	private String m_errorMessage;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the basic error message shell, ready to be filled through
	 * mutator functions.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message's header
	 */
	public ServerMessage(MessageHeader a_header) {
		super(a_header);
		m_errorMessage = null;
	}

	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the message that was read from the server
	 * @return <code>String</code> - the message
	 */
	public String getMessage(){
		return m_errorMessage;
	}
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************	
	/**
	 * Set the message that was read from the server
	 * 
	 * @param a_message <code>String</code>, the message that was sent
	 * @return <code>boolean</code> - <code>true</code> if the message is a 
	 * 	valid String, <code>false</code> if the message is 
	 *  <code>null</code> or its size is > <code>MAX_MESSAGE_SIZE</code>
	 */
	public boolean setMessage(String a_message){
		if( a_message == null || a_message.length() > MAX_MESSAGE_SIZE){
			return false;
		}
		
		m_errorMessage = a_message;
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * There is no data to be sent to the server for this message type.
	 * This always returns false;
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		return false;
	}

}

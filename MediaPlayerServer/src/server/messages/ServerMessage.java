package server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Class containing information and functionality for sending
 * server messages to clients.
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
	
	/** The name of the client to send the message to */
	private String m_receiver;
	
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
		m_receiver = null;
	}

	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the username of the client that will be receiving this
	 * message
	 * @return <code>String</code> - the receiver's username
	 */
	public String getReceiverName(){
		return m_receiver;
	}
	
	/**
	 * Get the message that will be sent to a client
	 * @return <code>String</code> - the message
	 */
	public String getMessage(){
		return m_errorMessage;
	}
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the username of the client that will be receiving this message
	 * 
	 * @param a_name <code>String</code>, the username
	 * @return <code>boolean</code> - <code>true</code> if a valid String is
	 * 	provided, <code>false</code> if the string is <code>null</code>
	 */
	public boolean setReceiverName(String a_name){
		if( a_name == null ){
			return false;
		}
		m_receiver = a_name;
		return true;
	}
	
	/**
	 * Set the message to send to the client.
	 * 
	 * @param a_message <code>String</code>, the message to send
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
	 * Send the ServerError information to the given stream.
	 * Included are:
	 * </br>the message header
	 * </br>the size of the error message and the message itself
	 * 
	 * @return <code>boolean</code> - <code>false</code> if the error
	 * 	message is not set(and therefore not sent), 
	 *  <code>true</code> otherwise
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		if( m_errorMessage == null ){
			return false;
		}
	
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header code
		a_stream.write( m_header.getHeaderCode().getBytes() );
		a_stream.flush();

		//write the size of the message and the message itself
		outData.writeInt( m_errorMessage.length() );
		a_stream.write( m_errorMessage.getBytes() );

		a_stream.flush();
		return true;
	}

}

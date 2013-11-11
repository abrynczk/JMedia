package server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class containing a sender's name and the sender's chat message,
 * for simplified handling of the message data.
 * @author Andrzej Brynczka
 *
 */
public class ChatMessage extends Message {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	/**
	 * Max number of characters per chat message, to prevent major spam.
	 */
	public static int MESSAGE_CHAR_LIMIT = 250;
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The user's message  */
	protected String m_message;
	
	/** The user who sent the message. */
	protected final String m_sender;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the complete chat message, ready to be sent through 
	 * the <code>sendMessage</code> method.
	 * </br></br>
	 * If the message size exceeds <code>MESSAGE_CHAR_LIMIT</code>, all 
	 * characters past the limit are cut-off.
	 * 
	 * @param a_header <code>String</code>, the message header
	 * @param a_message <code>String</code>, the message sent by the user
	 * @param a_sender <code>String</code>, the sender's user name
	 */
	public ChatMessage(MessageHeader a_header, String a_message, 
			String a_sender) {
		super(a_header);
		
		setMessage( a_message );
		m_sender = a_sender;		
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the user's message.
	 * @return	<code>String</code> - the user's message.
	 */
	public String getMessage(){
		return m_message;
	}
	
	/**
	 * Get the length of the user's message.
	 * @return	<code>int</code> - the size of the user's message.
	 */
	public int getMessageLength(){
		return m_message.length();
	}
	
	/**
	 * Get the sender's username
	 * @return <code>String</code> - the name
	 */
	public String getSenderName(){
		return m_sender;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the chat message to be sent.
	 * <p> If the message size exceeds <code>MESSAGE_CHAR_LIMIT</code>, all
	 * characters past the limit are cut-off.
	 * 
	 * @param a_msg <code>String</code>, the message to set
	 * @return <code>boolean</code> - <code>false</code> if the message is null,
	 * 	<code>true</code> otherwise
	 */
	public boolean setMessage(String a_msg){
		if( a_msg == null ){
			return false;
		}
		
		//cut off any characters that exceed the character limit
		if( a_msg.length() > MESSAGE_CHAR_LIMIT){
			m_message = a_msg.substring(0, MESSAGE_CHAR_LIMIT);
		}
		else{
			m_message = a_msg;
		}
		
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the ChatMessage information to the given stream.
	 * Included are:
	 * </br>the message header
	 * </br>the size of the sender's name and the sender's name
	 * </br>the size of the message and the message
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header
		a_stream.write( m_header.getHeaderCode().getBytes() );
			
		//write the sender's name
		outData.writeInt( m_sender.length() );
		a_stream.write( m_sender.getBytes() );
		a_stream.flush();
		
		//write the message length and the message itself
		outData.writeInt( m_message.length() );
		a_stream.write( m_message.getBytes() );
		a_stream.flush();
		
		return true;	
	}	
}
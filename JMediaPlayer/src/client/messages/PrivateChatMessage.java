package client.messages;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class containing message information on private messages.
 * @author Andrzej Brynczka
 *
 */
public class PrivateChatMessage extends ChatMessage {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The user that will receive this message */
	protected final String m_receiver;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the complete private chat message, ready to be sent 
	 * through its <code>sendMessage</code> method.
	 * </br></br>
	 * If the message size exceeds <code>MESSAGE_CHAR_LIMIT</code>, all 
	 * characters past the limit are cut-off.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message header
	 * @param a_message <code>String</code>, the message to send
	 * @param a_sender <code>String</code>, the sender's username
	 * @param a_receiver <code>String</code>, the receiver's username
	 */
	public PrivateChatMessage(MessageHeader a_header, String a_message, 
			String a_sender, String a_receiver) {
		
		super(a_header, a_message, a_sender);	
		m_receiver = a_receiver;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the username of the client that will be receiving this message
	 * 
	 * @return <code>String</code> - the name
	 */
	public String getReceiverName(){
		return m_receiver;
	}

	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the PrivateChatMessage's information to the given stream.
	 * Included are: 
	 * </br>the message header
	 * </br>the size of the sender's name and the sender's name
	 * </br>the size of the receiver's name and the receiver's name
	 * </br>the size of the message and the message
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException{
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header
		a_stream.write( m_header.getHeaderCode().getBytes() );
		
		//write the sender's name
		outData.writeInt( m_sender.length() );
		a_stream.write( m_sender.getBytes() );
		
		//write the receiver's name
		outData.writeInt( m_receiver.length() );
		a_stream.write( m_receiver.getBytes() );
		
		//write the message length and the message itself
		outData.writeInt( m_message.length() );
		a_stream.write( m_message.getBytes() );	
		
		a_stream.flush();
		return true;
	}
}

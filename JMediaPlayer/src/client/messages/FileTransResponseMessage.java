package client.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Message for the file transfer responses. Used to respond to the initial
 * file transfer request, alongside other stages within the file transfer
 * (such as the end of a file data transmission).
 * 
 * @author Andrzej Brynczka
 *
 */
public class FileTransResponseMessage extends FileTransferMessage {

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The response to send through this message */
	private MessageResponse m_response;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create a response message for a file transfer, ready to be sent 
	 * through the <code>sendMessage</code> method.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message header
	 * @param a_transferID <code>int</code>, the transfer ID for the current
	 * 	series of file transfer messages
	 * @param a_stage <code>TransferStage</code>, the stage of transfer
	 * 	to which this message serves as a response to
	 * @param a_sender <code>String</code>, the user name of the sender
	 * @param a_receiver <code>String</code>, the user name of the receiver
	 * @param a_fileName <code>String</code>, the name of the file being sent
	 * @param a_response <code>MessageResponse</code>, the response to send
	 */
	public FileTransResponseMessage(MessageHeader a_header, int a_transferID,
			TransferStage a_stage, String a_sender, String a_receiver,
			String a_fileName, MessageResponse a_response) {
		
		super(a_header, a_transferID, a_stage, 
				a_sender, a_receiver, a_fileName);
		
		m_response = a_response;
	}

	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the message response for the current message
	 * 
	 * @return <code>MessageResponse</code> - <code>Success</code> if the
	 * 	original message was accepted, <code>Failure</code> otherwise
	 */
	public MessageResponse getResponse(){
		return m_response;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the message response for the current message
	 * 
	 * @param a_response <code>MessageResponse</code>, the response to set
	 * @return <code>boolean</code> - <code>false</code> if the response is
	 * 	<code>INVALID</code>, <code>true</code> otherwise
	 */
	public boolean setResponse( MessageResponse a_response ){
		if( a_response == MessageResponse.INVALID ){
			return false;
		}
		m_response = a_response;
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the FileTransResponse message with information regarding the
	 * success/failure or acceptance/decline of a preceding message to the
	 * given stream.
	 * Included are: 
	 * </br>the message header
	 * </br>the transfer stage byte-based code
	 * </br>the transfer ID integer
	 * </br>the size of the receiver's user name and the receiver's name
	 * </br>the size of the file name and the file name
	 * </br>the byte-based response code
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException{
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header, transfer stage, and transferID
		a_stream.write( m_header.getHeaderCode().getBytes() );
		a_stream.write( m_transferStage.getCode() );
		outData.writeInt( m_transferID );
				
		//write the receiver's username
		outData.writeInt( m_receiverName.length() );
		a_stream.write( m_receiverName.getBytes() );
		a_stream.flush();
		
		//send filename
		outData.writeInt( m_fileName.length() );
		a_stream.write( m_fileName.getBytes() );
		
		//send message response
		a_stream.write( m_response.getCode() );
		
		a_stream.flush();
		return true;
	}
	
}

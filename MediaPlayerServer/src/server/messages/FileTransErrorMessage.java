package server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An error message containing specialized file transfer information, allowing
 * for its errors to be crafted around specified file transfer stages.
 * 
 * @author Andrzej Brynczka
 *
 */
public class FileTransErrorMessage extends FileTransferMessage {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	/** The maximum size of an error message */
	public static final int MAX_MESSAGE_SIZE = 250;
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The error message to send */
	private String m_errorMsg;
		
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the basic file transfer error message, ready to have its 
	 * message set.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message header
	 * @param a_transferID <code>int</code>, the transfer ID for the current
	 * 	series of file transfers
	 * @param a_stage <code>TransferStage</code>, the current stage of the
	 * 	transfer
	 * @param a_sender <code>String</code>, the user name of the sender
	 * @param a_receiver <code>String</code>, the user name of the receiver
	 * @param a_fileName <code>String</code>, the name of the file being sent
	 */
	public FileTransErrorMessage(MessageHeader a_header, int a_transferID,
			TransferStage a_stage, String a_sender, String a_receiver,
			String a_fileName) {
		
		super(a_header, a_transferID, a_stage, a_sender, 
				a_receiver, a_fileName);
		setErrorMsg("");
	}
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the error message to be sent to the client	
	 * @return <code>String</code> - the message
	 */
	public String getErrorMsg(){
		return m_errorMsg;
	}
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the message to send to the client.
	 * 
	 * @param a_msg <code>String</code>, the message to send
	 * @return <code>boolean</code> - <code>true</code> if the message is a 
	 * 	valid String, <code>false</code> if the message is 
	 *  <code>null</code> or its size is > <code>MAX_MESSAGE_SIZE</code>
	 */
	public boolean setErrorMsg(String a_msg){
		if( a_msg == null || a_msg.length() > MAX_MESSAGE_SIZE){
			return false;
		}
		
		m_errorMsg = a_msg;
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the FileTransError message to the given stream, indicating an
	 * issue within the current file transfer stage.
	 * Included are:
	 * </br>the message header
	 * </br>the transfer stage byte-based code
	 * </br>the transfer ID integer
	 * </br>the size of the file name and the file name
	 * </br>the size of the error message and the message itself
	 * 
	 * @return <code>boolean</code> - <code>false</code> if the error message
	 * 	is not set, <code>true</code> otherwise
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException{
		if( m_errorMsg == null ){
			return false;
		}

		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header, transfer stage, and transferID
		a_stream.write( m_header.getHeaderCode().getBytes() );
		a_stream.write( m_transferStage.getCode() );
		outData.writeInt( m_transferID );
		
		//write the size of the filename, and the name
		outData.writeInt( m_fileName.length() );
		a_stream.write( m_fileName.getBytes() );
		a_stream.flush();
		
		//write the message size and the message
		outData.writeInt( m_errorMsg.length() );
		a_stream.write( m_errorMsg.getBytes() );
		
		a_stream.flush();
		return true;
	}
}

package server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Message for the initial file transfer request, provided by
 * the client that requested a file transfer.
 * 
 * @author Andrzej Brynczka
 *
 */
public class FileTransRequestMessage extends FileTransferMessage {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The size of the file being requested, in bytes */
	private int m_sizeOfFile;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create a complete transfer request message, ready to be sent to a
	 * client through the <code>sendMessage</code> method.
	 * 
	 * @param a_header <code>MessageHeader</code>, the header sent by the 
	 * 		  client distinguishing the request
	 * @param a_transferID <code>int</code>, the ID for this file transfer
	 * @param a_stage <code>TransferStage</code>, the current stage of this 
	 * 			file transfer
	 * @param a_sender <code>String</code>, the username of the client to 
	 * 			requesting the exchange
	 * @param a_receiver <code>String</code>, the username of the client to be
	 * 			recieving the file
	 * @param a_fileName <code>String</code>, the name of the file to be 
	 * 			transfered
	 * @param a_sizeOfFile <code>int</code>, the size of the file to be 
	 * 			transfered
	 * @throws Exception if provided size of file is invalid
	 */
	public FileTransRequestMessage(MessageHeader a_header, int a_transferID,
			TransferStage a_stage, String a_sender, String a_receiver,
			String a_fileName, long a_sizeOfFile) throws Exception {
		super(a_header, a_transferID, a_stage, a_sender, 
				a_receiver, a_fileName);
		
		if( !setSizeOfFile( a_sizeOfFile ) ){
			throw new Exception("Invalid file size");
		}
	}		
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the size of the file being transfered
	 * @return <code>int</code> - the size of the file, in bytes
	 */
	public int getFileSize(){
		return m_sizeOfFile;
	}
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the size(in bytes) of the file being transfered
	 * @param a_size <code>int</code>, the size of the file
	 * @return <code>boolean</code> - <code>true</code> if the size is valid
	 * 	<code>false</code> otherwise
	 */
	private boolean setSizeOfFile(long a_size){
		if( a_size < 0 || a_size > Integer.MAX_VALUE){
			return false;
		}
		
		m_sizeOfFile = (int) a_size;
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the FileTransRequest information to the given stream(the receiver
	 * 	of the request). 
	 * Included are:
	 * </br>the message header
	 * </br>the transfer stage byte-based code
	 * </br>the transfer ID integer
	 * </br>the size of the sender's user name and the sender's name
	 * </br>the size of the file's name and the file's name
	 * </br>the file's size
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header, transfer stage, transfer ID and sender's name
		super.sendMessage( a_stream );
		
		//write file name
		outData.writeInt( m_fileName.length() );
		a_stream.write( m_fileName.getBytes() );
		
		//write the file size
		outData.writeInt( m_sizeOfFile );

		a_stream.flush();
		return true;
	}
}

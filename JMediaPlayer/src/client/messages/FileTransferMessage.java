package client.messages;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The base message class for all file transfer based messages.
 * @author Andrzej Brynczka
 *
 */
public abstract class FileTransferMessage extends Message {

	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************		
	/**
	 * Aids in tracking the stage of a file transfer message.
	 * <p>
	 * Includes also the functionality to acquire the numeric byte-based 
	 * code associated with any desired file transfer stage, or to get 
	 * the associated enumeration of a numeric code.
	 * 
	 * @author Andrzej Brynczka
	 */
	public enum TransferStage{
		/**
		 * Stage 1 - The initial request for file transfer from the sender.
		 */
		STAGE1_RequestFromSender( (byte) 1),
		
		/**
		 * Stage 2 - The response from the receiver for the initial request.
		 */
		STAGE2_ResponseToRequest( (byte) 2),
		
		/**
		 * Stage 3 - The transfer of file data from sender to receiver.
		 */
		STAGE3_DataTransmission( (byte) 3),
		
		/**
		 * Stage 4 - The response from the sender indicating end of data
		 * transmission.
		 */
		STAGE4_TransEndResponse( (byte) 4),
		
		/**
		 * Stage 5 - The transfer is complete.
		 */
		STAGE5_Done( (byte) 5),
		
		ERROR( (byte) -1 );
				
		/** The byte-based code associated with the transfer stage */
		private byte m_code;	
		TransferStage(byte a_code){
			m_code = a_code;
		}
		
		/**
		 * Get the numeric code for the desired file transfer stage.
		 * 
		 * @return <code>byte</code> - the numeric code for a given transfer 
		 * 	stage
		 */
		public byte getCode(){
			return m_code;
		}
		
		/**
		 * Get a TransferStage enum from a numeric code.
		 * 
		 * @param a_code <code>byte</code>, a numeric code for a given 
		 * 		transfer stage
		 * @return <code>TransferStage</code> - the code's enum
		 */
		public static TransferStage fromNum(byte a_code){
			switch(a_code){
			case 1:
				return STAGE1_RequestFromSender;
			case 2:
				return STAGE2_ResponseToRequest;
			case 3:
				return STAGE3_DataTransmission;
			case 4:
				return STAGE4_TransEndResponse;
			case 5:
				return STAGE5_Done;
			default:
				return ERROR;			
			}
		}
	}
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The ID for the current series of file transfer messages.
	 *  Used to associate numerous file transfer messages to a single file
	 *  */
	protected final int m_transferID;
	
	/** The transfer stage of the current message */
	protected final TransferStage m_transferStage;
	
	/** The name of the user sending the file  */
	protected final String m_senderName;
	
	/** The name of the user receiving the file */
	protected final String m_receiverName;
	
	/** The name of the file being(or to be) transfered */
	protected final String m_fileName;
	
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the complete FileTransferMessage object, containing basic 
	 * information on a file transfer message.
	 * 
	 * @param a_header <code>MessageHeader</code>,the header for the file 
	 * 			transfer message
	 * @param a_transferID <code>int</code>, an ID for the current file's 
	 * 			transfer messages
	 * @param a_stage <code>TransferStage</code>, the current stage of this 
	 * 			file's transfer
	 * @param a_sender <code>String</code>, the sender's user name
	 * @param a_receiver <code>String</code>, the receiver's user name
	 */
	public FileTransferMessage(MessageHeader a_header, int a_transferID, 
			TransferStage a_stage, String a_sender, String a_receiver, 
			String a_fileName){
		super(a_header);
		m_senderName = a_sender;
		m_receiverName = a_receiver;
		
		m_transferID = a_transferID;
		m_transferStage = a_stage;
		
		m_fileName = a_fileName;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the file transfer message's transfer ID.
	 * 
	 * @return <code>int</code> - the transfer ID
	 */
	public int getTransferID(){
		return m_transferID;
	}
	
	/**
	 * Get the message's transfer stage
	 * 
	 * @return <code>TransferStage</code> - the current stage in transfer
	 */
	public TransferStage getTransferStage(){
		return m_transferStage;
	}	

	/**
	 * Get the user name of the user sending the file
	 * 
	 * @return <code>String</code> - the sender's name
	 */
	public String getSenderName(){
		return m_senderName;
	}
	
	/**
	 * Get the user name of the user receiving the file
	 * 
	 * @return <code>String</code> - the receiver's name
	 */
	public String getReceiverName(){
		return m_receiverName;
	}
	
	/**
	 * Get the name of the file being transfered
	 * 
	 * @return <code>String</code> - the file's name
	 */
	public String getFileName(){
		return m_fileName;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	@Override
	public abstract boolean sendMessage(OutputStream a_stream) 
			throws IOException;
}

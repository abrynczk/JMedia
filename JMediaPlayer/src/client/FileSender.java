package client;

import java.io.IOException;

import javafx.application.Platform;

import client.messages.FileTransDataMessage;
import client.messages.FileTransResponseMessage;
import client.messages.FileTransferMessage.TransferStage;
import client.messages.Message.MessageHeader;
import client.messages.Message.MessageResponse;

/**
 * Thread meant to handle the process of sending a user's
 * 	chosen file data to the server.
 * 
 * The thread manages an individual file's transfer during its lifetime 
 * and interacts with the ClientWriter class to send the file.
 * @author Andrzej Brynczka
 *
 */
public class FileSender extends Thread{
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/**
	 * The file information ticket for the file being sent
	 */
	private FileTransferTicket m_fileTicketToSend;
	
	/**
	 * Reference to the main ClientWriter object, through
	 * which the file data is sent to the server
	 */
	private ClientWriter m_clientWriter;
	
	/**
	 * Reference to the main client object
	 */
	private Client m_client;
	
	/**
	 * Indication of whether to end the file transmission,
	 * used in the case of a user canceling a transfer.
	 */
	private boolean m_endTransmission;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Constructs the complete FileSender thread, ready to be
	 * 	run to being transmission.
	 * 
	 * @param a_ticket	FileTransferTicket, the ticket containing 
	 * 	information on the file to be sent
	 * @param a_clientWriter  ClientWriter, the reference to the main
	 * 	writer thread
	 * @param a_client  Client, the reference to the main client
	 */
	public FileSender(FileTransferTicket a_ticket, ClientWriter a_clientWriter,
			Client a_client){
		m_fileTicketToSend = a_ticket;
		m_clientWriter = a_clientWriter;
		m_client = a_client;
		m_endTransmission = false;
	}
	
	/**
	 * Signals this FileSender thread to end its transmission.
	 */
	public void endTransmission(){
		m_endTransmission = true;
		interrupt();
	}
	
	/**
	 * Transmit the provided file to the server with the aid of the
	 * 	ClientWriter thread.
	 */
	@Override
	public void run() {
		//calculate the number of file segments to send
		m_fileTicketToSend.setTotalDataSeg( 
				(int) Math.ceil(
						((double)m_fileTicketToSend.getFileSize() 
						/ FileTransDataMessage.MAX_SEGMENT_SIZE)) );

		System.out.println("about to start sending segments, total: " 
				+ m_fileTicketToSend.getTotalDataSeg());
		 
		//gather the data segments and send them to the clientwriter to 
		//send to the server
		int bytesRead = 0;
		int segIndex = 1;
		while( m_endTransmission == false && 
				segIndex <= m_fileTicketToSend.getTotalDataSeg() ){
			
			try {
				byte[] readData = 
						new byte[ FileTransDataMessage.MAX_SEGMENT_SIZE ];
				
				//read the maximum amount of available bytes per segment
				bytesRead = m_fileTicketToSend.readData( readData,
						FileTransDataMessage.MAX_SEGMENT_SIZE );
				
				//update the ticket to reflect the current segment being sent
				m_fileTicketToSend.setCurrentDataSegNum( segIndex );

				//create the data message and send the data
				FileTransDataMessage ftdMsg = new FileTransDataMessage(
						MessageHeader.FILE_Transfer, 
						m_fileTicketToSend.getTransferID(), 
						TransferStage.STAGE3_DataTransmission, 
						m_fileTicketToSend.getSenderName(), 
						m_fileTicketToSend.getReceiverName(), 
						m_fileTicketToSend.getFileName(), 
						segIndex, 
						m_fileTicketToSend.getTotalDataSeg(), 
						bytesRead, 
						readData);
			
				m_clientWriter.addMessage( ftdMsg );
				
				
				//update the segment index
				segIndex++;
			} catch (Exception e) {
				System.out.println("Failed to send file:" + e.getMessage() );
				m_endTransmission = true;
			}	
			
			if( ( segIndex % 50 == 0 ) || (segIndex % 75 == 0)){
				//wait every so often to ensure that
				//the data transmission messages don't clog the 
				//socket used to transfer messages
				try {
					synchronized( this ){
						wait(1000);//1 second
					}
				} catch (InterruptedException e) {
					//continue...
				}
			}
		}
		
		//done sending the file, create a response message to indicate
		//the transmission's end
		final FileTransResponseMessage transEndMsg = 
				new FileTransResponseMessage(
					MessageHeader.FILE_Transfer, 
					m_fileTicketToSend.getTransferID(), 
					TransferStage.STAGE4_TransEndResponse, 
					m_fileTicketToSend.getSenderName(), 
					m_fileTicketToSend.getReceiverName(),
					m_fileTicketToSend.getFileName(), 
					MessageResponse.Success );
		
		if( m_endTransmission == true ){
			//transmission canceled early, 
			//modify response to reflect the failure
			transEndMsg.setResponse( MessageResponse.Failure );
		}
		
		//send the message and modify the file ticket 
		//to reflect the end of transmission
		m_clientWriter.addMessage( transEndMsg );
		m_fileTicketToSend.setTransferStage( TransferStage.STAGE5_Done );
		m_client.removeFileSender( transEndMsg.getTransferID() );
		
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.addChatMessageToList( transEndMsg );
			}
		});
		
		//close the file stream
		try {
			m_fileTicketToSend.closeFileReadStream();
		} catch (IOException e) {
			System.out.println("Error attempting to close stream to file " 
					+ m_fileTicketToSend.getFileName() 
					+ ".\n" + e.getMessage() );
		}
		
	}
}

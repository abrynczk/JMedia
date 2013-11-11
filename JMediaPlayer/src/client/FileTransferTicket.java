package client;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


import client.messages.FileTransferMessage.TransferStage;

/** Class used to store all information on an individual file transfer,
 * to be created after the response to a transfer request is made valid. 
 * 
 * @author Andrzej Brynczka
 * 
 * */
public class FileTransferTicket {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/**
	 * The name of the user that is sending this file
	 */
	private String m_sender;
	/**
	 * The name of the user that is receiving this file
	 */
	private String m_receiver;
	
	/**
	 * Indication of whether this client is sending or receiving
	 * this file(to prevent need to check above names often)
	 */
	private boolean m_receiving = false;
	
	/**
	 * The file's unique transfer id created upon startup of the
	 * transmission process by the server.
	 */
	private int m_transferID;
	
	/**
	 * The current stage of the transfer process
	 * 
	 * @see client.messages.FileTransferMessage.TransferStage
	 */
	private TransferStage m_transferStage;
	
	/**
	 * The name of the file being transfered
	 */
	private String m_fileName;
	/**
	 * The size of the file being transfered(in bytes)
	 */
	private int m_sizeOfFile;
	
	/**
	 * The number of bytes exchanged so far during the transmission
	 */
	private int m_bytesExchanged;
	
	/**
	 * The number of the file data segments transfered
	 */
	private int m_currentDataSegment;
	
	/**
	 * The total number of data segments to be transfered before the 
	 * 	file transfer is complete
	 */
	private int m_totalDataSegments;
	
	/**
	 * The filepath for the file being transfered
	 */
	private String m_filePath;
	/**
	 * The inputstream to the file being sent 
	 * (This variable is used only by the sender)
	 */
	private FileInputStream m_fileIn;
	
	//receiver only
	/**
	 * The file making up the data that is received
	 * (This variable is used only by the receiver)
	 */
	private File m_receivedFile;
	
	/**
	 * The outputstream to the file being received
	 * (This variable is used only by the receiver)
	 */
	private FileOutputStream m_fileOut;
	
	//gui based
	/**
	 * The progress of the file transfer
	 * (This property is utilized by the view classes 
	 * to provide automatic progress updating through 
	 * binding to javafx components)
	 */
	private DoubleProperty m_progress;
	
	/**
	 * A bindable property used to display the 
	 * <code>TransferStage<code> status to
	 * the view components 
	 */
	private StringProperty m_status;
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Construct a FileTransferTicket with the basic required
	 * data necessary to engage in a file transfer.
	 * 
	 * @param a_transferID  	int, the file's unique transfer id
	 * @param a_currentStage    TransferStage, the file's stage in the transfer
	 * @param a_fileName        String, the file's name
	 * @param a_fileSize  		int, the file's size in bytes
	 * @param a_senderName		String, the sender's name
	 * @param a_recieverName 	String, the receiver's name
	 * @param a_receiving  		boolean, indication of whether or not this 
	 * 		client is sending or receiving the file
	 * @author Andrzej Brynczka
	 */
	public FileTransferTicket(int a_transferID, TransferStage a_currentStage, 
			String a_fileName, int a_fileSize, 
			String a_senderName, String a_recieverName, boolean a_receiving){
		
		m_sender = a_senderName;
		m_receiver = a_recieverName;
		
		m_receiving = a_receiving;
		m_transferID = a_transferID;
		m_transferStage = a_currentStage;
		
		m_fileName = a_fileName;
		m_sizeOfFile = a_fileSize;
		
		m_filePath = null;
		m_bytesExchanged = 0;
		m_currentDataSegment = 0;
		m_totalDataSegments = 0;
		m_receivedFile = null;
		m_fileOut = null;
		m_fileIn = null;
		
		m_progress = new SimpleDoubleProperty(0);
		m_status = new SimpleStringProperty("");
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************

	/**
	 * Get the name of the user that is sending this file
	 * 
	 * @return String, containing the user's name
	 */
	public String getSenderName(){
		return m_sender;
	}
	
	/**
	 * Get the name of the user that is receiving this file
	 * 
	 * @return String, containing the receiver's name
	 */
	public String getReceiverName(){
		return m_receiver;
	}
	/**
	 * Get the unique transfer id associated to this file transmission
	 * 
	 * @return int, containing the transfer id
	 */
	public int getTransferID(){
		return m_transferID;
	}
	
	/**
	 * Get the transmission's current transfer stage
	 * 
	 * @return the current TransferStage of the transmission
	 */
	public TransferStage getTransferStage(){
		return m_transferStage;
	}
	
	/**
	 * Get the filepath to the transmitted file
	 * 
	 * @return String, containing the path, or null if not set
	 */
	public String getFilePath(){
		return m_filePath;
	}
	
	/**
	 * Get the file name of the file being transmitted
	 * 
	 * @return String, containing the file name
	 */
	public String getFileName(){
		return m_fileName;
	}
	
	/**
	 * Get the transmitted file's size
	 * @return int, containing the size in bytes
	 */
	public int getFileSize(){
		return m_sizeOfFile;
	}
	
	/**
	 * Get the number of bytes currently exchanged during the transmission
	 * 
	 * @return int, containing the number of bytes
	 */
	public int getNumOfBytesExchanged(){
		return m_bytesExchanged;
	}
	
	/**
	 * Get the file segment number of the most recently 
	 * 	transmitted file data segment
	 * 
	 * @return int, containing the current file segment number
	 */
	public int getCurrentDataSegNum(){
		return m_currentDataSegment;
	}
	
	/**
	 * Get the total number of file segments to be transfered
	 * 	during the transmission
	 * 
	 * @return int, containing the total number of segments
	 */
	public int getTotalDataSeg(){
		return m_totalDataSegments;
	}
	
	/**
	 * Get the current progress of the file data transfer, as a percent.
	 * 
	 * @return double, the progress ranging from 0 to 1,
	 * 	where 0 is 0% and 1 is 100%
	 */
	public DoubleProperty getProgress(){
		return m_progress;
		
	}
	
	/**
	 * Get the current TransferStage status for the transmission
	 * 
	 * @return String, the TransferStage status
	 */
	public String getStatus(){
		return m_status.get();
	}
	
	/**
	 * Get the TransferStage status property for the transmission, to be
	 * used by binded to JavaFX components for visual updates
	 * 
	 * @return StringProperty, for the current transmission's status
	 */
	public StringProperty StatusProperty(){
		return m_status;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the transmission's TransferStage bindable status property value
	 * 
	 * @param a_status TransferStage, the transmission's current Stage
	 */
	public void setStatus(TransferStage a_status){
		String stringStatus = "";
		
		switch( a_status ){
		case STAGE1_RequestFromSender:
			stringStatus = "Requesting Response";
			break;
		case STAGE3_DataTransmission:
			stringStatus = "Transmitting Data";
			break;
		case STAGE4_TransEndResponse:
		case STAGE5_Done:
			stringStatus = "Done/Canceled";
			break;
		case ERROR:
			stringStatus = "Stopped - Error";
			break;
		}
		
		m_status.set( stringStatus );
	}
	
	/**
	 * Set the file transmission's unique transfer id
	 * 
	 * @param a_ID int, the transfer id to set
	 * @return true if the id was set, false if the
	 * 	given id was less than 0
	 */
	public boolean setTransferID(int a_ID){
		if( a_ID < 0 ){
			return false;
		}
		
		m_transferID = a_ID;
		return true;
	}
	
	/**
	 * Set the file transmission's current TransferStage
	 * 
	 * @param a_stage TransferStage, the current stage
	 */
	public void setTransferStage(TransferStage a_stage){
		m_transferStage = a_stage;
		setStatus( a_stage );
	}
	
	/**
	 * Set the number of the current file data segment being transfered
	 * <p>
	 * 
	 * File transfer progress, denoted by this ticket's progress property
	 * {@link #getProgress()}, is updated in relation to the total
	 * number of data segments to be sent when this value is set.
	 * 
	 * @param a_segNum int, the number of the data segment
	 * @return true if the number was set, false if the number
	 * 	was less than 0 or greater than the total number of segments
	 */
	public boolean setCurrentDataSegNum(int a_segNum){
		if( a_segNum < 0 || a_segNum > m_totalDataSegments ){
			return false;
		}
		
		m_currentDataSegment = a_segNum;
		setProgress( 
				(double)m_currentDataSegment / (double)m_totalDataSegments );
		return true;
	}
	
	/**
	 * Set the total number of file data segments that are to be sent
	 * 	through this transmission.
	 * 
	 * @param a_totalSegments int, the total number of segments
	 * @return true if the total number of segments is set, false if
	 * 	the total segments are less than 1
	 */
	public boolean setTotalDataSeg(int a_totalSegments){
		if( a_totalSegments < 1 ){
			return false;
		}
		
		m_totalDataSegments = a_totalSegments;
		return true;
	}
	
	/**
	 * Set the file path for the current file being transfered
	 * 
	 * @param a_path String, the path to set
	 * @return true if the path was set, false if the given path
	 * 	was null
	 */
	public boolean setFilePath(String a_path){
		if( a_path == null ){
			return false;
		}
		m_filePath = a_path;
		return true;
	}
	
	/**
	 * Set the transmission's progress property
	 * 
	 * @param a_progress double, the progress as a percentage
	 * 	(from 0.0 to 1.0)
	 */
	private void setProgress( double a_progress ){
		m_progress.set( a_progress );
	}
	
	/**
	 * Add to the number of bytes that have been exchanged
	 * 	during the current file's transmission.
	 * 
	 * @param a_numOfBytes int, the number of bytes to add
	 * @return true if the bytes were added, false if 
	 * 	the given number of bytes was less than 0
	 */
	private boolean addBytesExchanged(int a_numOfBytes){
		if(  a_numOfBytes < 0){
			return false;
		}
		
		m_bytesExchanged += a_numOfBytes;
		return true;
	}
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Create the new <code>File</code> object for the 
	 * 	file being received from the transmission.
	 * Make sure that the filepath is set in this ticket
	 * beforehand.
	 * <p>
	 * This function also creates a FileOutputStream to the
	 * 	newly created file, allowing for the file to be written
	 * 	to using {@link FileTransferTicket#writeData(int, byte[])}
	 * 
	 * @return true if the new file was made, false if a "new" file
	 * 	already exists
	 * @throws IOException if an error occured when attempting
	 * 	to create the new file
	 */
	public boolean createNewFile() throws IOException{
		if( m_receivedFile != null ){
			//cannot create the file if it already exists
			return false;
		}
		
		m_receivedFile = new File( m_filePath + "/" + m_fileName);
		
		if( m_receivedFile.createNewFile() == false ){
			//file already exists at this path
			return false;
		}
		
		m_fileOut = new FileOutputStream( m_receivedFile );
		return true;
	}
	
	/**
	 * Write the given data to the file that is being received(Create the
	 * file first using {@link FileTransferTicket#createNewFile()}.
	 * 
	 * @param a_numOfBytes int, the number of bytes to write
	 * @param a_fileData byte[], the data to write
	 * @return boolean, false if the file was not yet created
	 * (and therefore its output stream not yet open) or if the
	 * 	number of bytes to write is less than 0, true otherwise
	 * @throws IOException if an error occurs while writing to the file
	 */
	public boolean writeData(int a_numOfBytes, byte[] a_fileData) 
			throws IOException{
		if( m_fileOut == null ){
			return false;
		}
		
		//keep track of the total bytes exchanged
		if( addBytesExchanged(a_numOfBytes) == false ){
			return false;
		}
		
		BufferedOutputStream buffOut = new BufferedOutputStream( m_fileOut );	
		buffOut.write( a_fileData, 0, a_numOfBytes);
		buffOut.flush();
		
		return true;
	}
	
	/**
	 * Close the FileOutputStream held by this ticket.
	 * @return true if the OutputStream was closed, false if the
	 * 	ticket did not have an OutputStream open
	 * @throws IOException if an error occurs attempting to close
	 * 	the ticket's OutputStream
	 */
	public boolean closeFileWriteStream() throws IOException{
		if( m_fileOut == null){
			return false;
		}
		m_fileOut.close();
		return true;
	}
	
	/**
	 * Delete the file written to when receiving data during the
	 * 	transmission.(Use this if a transmission gets canceled, to ensure
	 * 	no useless data is left around)
	 * @return true if the file was deleted, false if there was no file to
	 * 	delete
	 */
	public boolean deleteFile(){
		if( m_receivedFile == null ){
			return false;		
		}
		
		m_receivedFile.delete();
		return true;
	}
	
	/**
	 * Open the file that is to be sent through the file transfer.
	 * 
	 * <p>
	 * 	This function also sets a FileInputStream to the opened file,
	 * 	allowing for the file's data to be read with 
	 * 	{@link FileTransferTicket#readData(byte[], int)}
	 * 
	 * @return true if the file is opened and the FileInputStream connected,
	 * 	false otherwise
	 * @throws FileNotFoundException
	 */
	public boolean openFileToRead() throws FileNotFoundException{
		//should not open the file to read if the file is being
		//sent to the client
		if( m_receiving == true ){
			return false;
		}
		File fileToRead = new File( m_filePath );
		m_fileIn = new FileInputStream( fileToRead );
		return true;
	}
	
	@Override
	public boolean equals(Object a_obj){
		if( a_obj == null || !(a_obj instanceof FileTransferTicket) ){
			return false;
		}
		
		FileTransferTicket tempTicket = (FileTransferTicket) a_obj;
		
		//all tickets have unique ids
		if( tempTicket.m_transferID != m_transferID && 
				!tempTicket.m_fileName.equals( m_fileName) ){
			return false;
		}
		
		return true;
	}
	/**
	 * Read a given number of bytes of data from the current file
	 * to be transfered (Open it first using <code>openFileToRead()</code>).
	 * 
	 * @param a_OutDataContainer byte[], the byte array into which
	 * 	the read data will be written to
	 * @param a_bytesToRead int, the number of bytes to read
	 * 	from the file
	 * @return int, the number of bytes read
	 * @throws IOException if an error occurs while reading from the file
	 */
	public int readData(byte[] a_OutDataContainer, int a_bytesToRead) 
			throws IOException{
		//Number of bytes to read must be positive and the client
		//should not be reading from the file that it is receiving
		if( a_bytesToRead <= 0 || m_receiving == true){
			return 0;
		}
		
		if( addBytesExchanged( a_bytesToRead ) == false ){
			return 0;
		}
		
		int bytesRead = 0;
		//bytesRead = m_fileIn.read( a_dataContainer, 0, a_bytesToRead );
		BufferedInputStream ff = new BufferedInputStream( m_fileIn );
		bytesRead = ff.read( a_OutDataContainer, 0, a_bytesToRead);
		
		return bytesRead;
	}
	
	/**
	 * Close the FileInputStream to the file being sent in
	 * 	the transfer
	 * 
	 * @return true if the FileInputStream held by the ticket was closed,
	 * 	false if the this ticket had no InpuStream to close
	 * @throws IOException
	 */
	public boolean closeFileReadStream() throws IOException{
		if( m_fileIn == null ){
			return false;
		}
		m_fileIn.close();
		return true;
	}

}

package server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class containing information on the messages sending the file data
 * being sent between clients.
 * 
 * @author Andrzej Brynczka
 *
 */
public class FileTransDataMessage extends FileTransferMessage {

	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	/** The maximum size of a segment, in bytes */
	public static final int MAX_SEGMENT_SIZE = 32 * 1024;//bytes
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The # of current data segment that is being sent */
	private int m_currentDataSegment;
	
	/** The total number of data segments that will be sent */
	private int m_totalDataSegments;

	/** The size of the current data segment */
	private int m_sizeOfCurSeg;
	
	/** The sent data segment containing the file data */
	private byte[] m_dataSegment;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create a complete FileTransDataMessage object containing all of the
	 * information received from the client.  The message is ready to be
	 * sent to the receiver through its <code>sendMessage</code> method.
	 * 
	 * @param a_header <code>MessageHeader</code>, the header provided in the 
	 * 			message
	 * @param a_transferID <code>int</code>, the ID for the current 
	 * 			file transfer process
	 * @param a_stage <code>TransferStage</code>, the current stage of the file 
	 * 			transfer process
	 * @param a_sender <code>String</code>, the user that is sending the file
	 * @param a_receiver <code>String</code>, the user that is receiving the 
	 * 			file
	 * @param a_numOfCurSeg <code>int</code>, the current data segment number 
	 * @param a_numOfTotSeg <code>int</code>, the total number of data segments 
	 * 			for the file
	 * @param a_sizeOfCurSeg <code>int</code>, the size(in bytes) of the 
	 * 			current data segment
	 * @param a_segment <code>byte[]</code>, the file data for the current data 
	 * 			segment
	 * @throws Exception if provided data segment information is invalid
	 */
	public FileTransDataMessage(MessageHeader a_header, int a_transferID,
			TransferStage a_stage, String a_sender, String a_receiver,
			String a_fileName, int a_numOfCurSeg, int a_numOfTotSeg, 
			int a_sizeOfCurSeg, byte[] a_segment) throws Exception {
		super(a_header, a_transferID, a_stage, a_sender, 
				a_receiver, a_fileName);

		
		if( setTotalDataSegments( a_numOfTotSeg ) == false ){
			throw new Exception("ERROR: Invalid number of data segments");
		}
		
		if( setCurDataSegNumber( a_numOfCurSeg ) == false ){
			throw new Exception(
					"ERROR: Current data segment number is invalid");
		}
		
		if( setSizeOfCurSeg( a_sizeOfCurSeg ) == false ){
			throw new Exception(
					"ERROR: Invalid data segment size(SEGMENT_SIZE)");
		}
		
		if( setDataSegment( a_segment ) == false ){
			throw new Exception("ERROR: Invalid data segment size(SEGMENT)");
		}
	}

	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the number of the current data segment
	 * @return <code>int</code> - the number of the data segment
	 */
	public int getCurDataSegNumber(){
		return m_currentDataSegment;
	}
	
	/**
	 * Get the total number of data segments that will be sent
	 * @return <code>int</code> - the total number of segments
	 */
	public int getTotalDataSegments(){
		return m_totalDataSegments;
	}
	
	/**
	 * Get the size of the current data segment
	 * @return <code>int</code> - the segment's size, in bytes
	 */
	public int getSizeOfCurSeg(){
		return m_sizeOfCurSeg;
	}
	
	/**
	 * Get the current data segment
	 * @return <code>byte[]</code> - the segment
	 */
	public byte[] getDataSegment(){
		return m_dataSegment;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the number of the current data segment
	 * @param a_segNum <code>int</code>, the number of the segment
	 * @return <code>boolean</code> - <code>false</code> if the number is
	 * 	less than 1 or greater than the total number of segments,
	 * 	<code>true</code> otherwise
	 */
	public boolean setCurDataSegNumber(int a_segNum){
		if( a_segNum < 1 || a_segNum > m_totalDataSegments){
			return false;
		}
		m_currentDataSegment = a_segNum;
		return true;
	}
	
	/**
	 * Set the number of total data segments
	 * @param a_segNum <code>int</code>, the number of the segment
	 * @return <code>boolean</code> - <code>false</code> if the number is
	 * 	less than 1 or greater than <code>Integer.MAX_VALUE</code>
	 */
	public boolean setTotalDataSegments(int a_segNum){
		if( a_segNum < 1 || a_segNum > Integer.MAX_VALUE ){
			return false;
		}
		
		m_totalDataSegments = a_segNum;
		return true;
	}
	
	/**
	 * Set the size of the current segment
	 * @param a_segSize <code>int</code>, the size of the segment
	 * @return <code>boolean</code> - <code>false</code> if the number is
	 * 	less than 0 or greater than <code>MAX_SEGMENT_SIZE</code>
	 */
	public boolean setSizeOfCurSeg(int a_segSize){
		if( a_segSize > MAX_SEGMENT_SIZE 
				|| a_segSize < 0){
			return false;
		}
		
		m_sizeOfCurSeg = a_segSize;
		return true;
	}
	
	/**
	 * Set the data segment, as an array of bytes
	 * @param a_segment <code>byte[]</code>, the data segment
	 * @return <code>boolean</code> - <code>false</code> if the size of
	 * 	the segment is greater than <code>MAX_SEGMENT_SIZE</code>, 
	 * 	<code>true</code> otherwise
	 */
	public boolean setDataSegment(byte[] a_segment){
		if( a_segment.length > MAX_SEGMENT_SIZE){
			return false;
		}
		
		m_dataSegment = a_segment;
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the FileTransData information to the receiving client through
	 * the given stream.
	 * Included are:
	 * </br>the message header
	 * </br>the transfer stage byte-based code
	 * </br>the transfer ID integer
	 * </br>the size of the sender's username and the sender's name
	 * </br>the size of the file name and the file name
	 * </br>the # of the current data segment
	 * </br>the total number of data segments for the file
	 * </br>the size of the current segment
	 * </br>the data segment containing the file data
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException{
		//write the header, transfer stage, transferID, and sender name
		super.sendMessage( a_stream );
		
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the file name
		outData.writeInt( m_fileName.length() );
		a_stream.write( m_fileName.getBytes() );
		
		//write the # of the current data segment
		//and the number of total segments
		outData.writeInt( m_currentDataSegment );
		outData.writeInt( m_totalDataSegments );
		
		//write the size of the current segment and the segment itself
		outData.writeInt( m_sizeOfCurSeg );
		a_stream.flush();
		
		//write the data segment
		a_stream.write( m_dataSegment, 0, m_sizeOfCurSeg );
		a_stream.flush();
		
		return true;
	}

		
}

package client.messages;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The base message class, containing header code information and outlining
 * required message functionality.
 * @author Andrzej Brynczka
 *
 */
public abstract class Message {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	/**
	 * Aids in the tracking of message header codes.
	 * Use to acquire the 4-character code for a desired message header, or
	 * to get the header enumeration for a 4-character code.
	 * @author Andrzej Brynczka
	 *
	 */
	public enum MessageHeader{
		LOGIN("0001"),
		LOGOUT("0002"),
		
		REG_SendChatMess("0100"),
		
		PRIV_SendChatMess("0200"),
		
		FILE_Transfer("0300"),
		
		ADMIN_Login("0800"),
		ADMIN_PunishList("0809"),
		ADMIN_PunishUser("0810"),
		ADMIN_RemovePunishment("0811"),
		
		SERVER_UserList("0905"),
		SERVER_AddNewUser("0906"),
		SERVER_RemoveUser("0907"),
		SERVER_Kicked("0970"),
		SERVER_Error("0999"),
		
		INVALID("9999");
		
		/** The 4 character header code used in messages */
		private final String headerCode;
		
		/**
		 * Sets the 4 character header code associated with the current enum.
		 * @param code <code>String</code>, the 4 character code
		 */
		MessageHeader(String code){
			headerCode = code;
		}
		
		/**
		 * Get the 4 character message code for the given header.
		 * @return <code>String</code> - the 4 character code
		 */
		public String getHeaderCode(){
			return headerCode;
		}
		
		/**
		 * Convert the string-based 4 character header code into its 
		 * MessageHeader enum.
		 * 
		 * @param code <code>String</code>, the header code
		 * @return <code>MessageHeader</code> - the header's enum
		 */
		public static MessageHeader fromString(String code){
			switch(code){
			case "0001":
				return LOGIN;
			case "0002":
				return LOGOUT;
			case "0100":
				return REG_SendChatMess;
			case "0200":
				return PRIV_SendChatMess;
			case "0300":
				return FILE_Transfer;
			case "0800":
				return ADMIN_Login;
			case "0809":
				return ADMIN_PunishList;
			case "0810":
				return ADMIN_PunishUser;
			case "0811":
				return ADMIN_RemovePunishment;
			case "0905":
				return SERVER_UserList;
			case "0906":
				return SERVER_AddNewUser;
			case "0907":
				return SERVER_RemoveUser;
			case "0970":
				return SERVER_Kicked;
			case "0999":
				return SERVER_Error;
			default:
				return INVALID;	
			}
		}
		
	}
	
	/**
	 * Tracks message response values and their associated message codes.
	 * @author Andrzej Brynczka
	 *
	 */
	public enum MessageResponse{
		Success( (byte) 1 ),
		Failure( (byte) 0 ),
		
		INVALID( (byte) -1 );
		
		/** The single byte based message code associated to the response */
		private byte m_code;
		
		/**
		 * Create the MessageResponse with its associated byte-based message
		 * code.
		 * @param a_code <code>byte</code>, the enum's message code
		 */
		MessageResponse(byte a_code){
			m_code = a_code;
		}
		
		/**
		 * Get the message code value for the desired message response
		 * @return <code>byte</code> - containing the response's code
		 */
		public byte getCode(){
			return m_code;
		}
		
		/**
		 * Get the <code>MessageResponse</code> enum associated to the
		 * given byte-based message code.
		 * @param a_code <code>byte</code>, the message code
		 * @return <code>MessageResponse</code> - the message code's enum
		 * 	response
		 */
		public static MessageResponse fromByte(byte a_code){
			switch(a_code){
			case 1:
				return Success;
			case 0: 
				return Failure;
			default:
				return INVALID;
			}
		}
	}
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	
	/** The message's header */
	protected MessageHeader m_header;

	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	
	/**
	 * Create a basic message containing only its header.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message's header
	 */
	Message(MessageHeader a_header){
		m_header = a_header;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the message's header
	 * 
	 * @return <code>MessageHeader</code> - the message's header enum
	 */
	public MessageHeader getHeader(){
		return m_header;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Convert the current message information into an appropriate response
	 * message and send it to the given stream.
	 * 
	 * @param a_stream <code>OutputStream</code>, the stream to send the message
	 * 	to
	 * @return <code>boolean</code> - <code>true</code> if the message was sent,
	 * 	<code>false</code> otherwise
	 * @throws IOException if an error occurs when attempting to create the
	 * 	response
	 */
	public abstract boolean sendMessage(OutputStream a_stream) 
			throws IOException;	
}
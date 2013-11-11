package server.messages;

import java.io.IOException;
import java.io.OutputStream;

public class LoginMessage extends Message {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	/**
	 * Contains information on the client's success at attempting
	 * to login to the server. Included are functions to get the
	 * enumeration's byte-based message code and the ability to
	 * acquire an associated enumeration from a given byte.
	 * 
	 * @author Andrzej Brynczka
	 *
	 */
	public enum LoginCondition{
		SUCCESS( (byte) 1 ),
		SUCCESS_Muted( (byte) 2 ),
		
		
		FAILURE_MultiLogin( (byte) 101 ),
		FAILURE_IPBanned( (byte) 102 ),
		FAILURE_UsernameTooLong( (byte) 110 ),
		FAILURE_UsernameInvalidCharacters( (byte) 111),
		FAILURE_UsernameInUse( (byte) 112 ),
		FAILURE_ServerPassTooLong( (byte) 120 ),
		FAILURE_ServerPassInvalid( (byte) 121 ),
				
		INVALID( (byte) -1);
		
		/** The enumeration's byte-based message code */
		private byte m_conditionCode;
		
		LoginCondition(byte a_code){
			m_conditionCode = a_code;
		}
		
		/**
		 * Get the byte associated to the current enum
		 * @return byte, the login condition in its byte-based format
		 */
		public byte getCode(){
			return m_conditionCode;
		}
		
		/**
		 * Get the LoginCondition enum for the given byte
		 * @param a_code <code>byte</code>, the byte-based message
		 * 	code from which to gain an enum
		 * @return <code>LoginCondition</code> - an enum associated
		 * 	to the given byte
		 */
		public static LoginCondition fromByte( byte a_code ){
			switch( a_code ){
			case 1:
				return SUCCESS;
			case 2:
				return SUCCESS_Muted;
			case 101:
				return FAILURE_MultiLogin;
			case 102:
				return FAILURE_IPBanned;
			case 110:
				return FAILURE_UsernameTooLong;
			case 111:
				return FAILURE_UsernameInvalidCharacters;
			case 112:
				return FAILURE_UsernameInUse;
			case 120:
				return FAILURE_ServerPassTooLong;
			case 121:
				return FAILURE_ServerPassInvalid;
			default:
				return INVALID;
			}
		}
	}
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The client's success at attempting to login to the server */
	private LoginCondition m_condition;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the complete LoginMessage, ready to be sent to the
	 * client through its <code>sendMessage</code> function.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message header
	 * @param a_condition <code>LoginCondition</code>, the client's
	 * 	success at attempting to login into the server
	 */
	public LoginMessage(MessageHeader a_header, LoginCondition a_condition) {
		super(a_header);
		m_condition = a_condition;
		
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the current login condition.
	 * @return <code>LoginCondition</code> - the current login condition
	 */
	public LoginCondition getCondition(){
		return m_condition;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the current login condition status.
	 * @param a_condition <code>LoginCondition</code>, the condition
	 * 	to set
	 * @return <code>boolean</code> - <code>false</code> if the 
	 * 	condition is null or <code>INVALID</code>, <code>true</code>
	 * 	otherwise
	 */
	public boolean setCondition(LoginCondition a_condition){
		if( a_condition == LoginCondition.INVALID || 
				a_condition == null){
			return false;
		}
		
		m_condition = a_condition;
		return true;
	}
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the LoginMessage indicating the client's success at 
	 * attempting to login to the server.
	 * Included are:
	 * </br>the message header
	 * </br>the login condition byte
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		
		//write the header
		a_stream.write( m_header.getHeaderCode().getBytes() );
		
		//write the login condition
		a_stream.write( m_condition.getCode() );
		
		a_stream.flush();
		return true;
	}

}

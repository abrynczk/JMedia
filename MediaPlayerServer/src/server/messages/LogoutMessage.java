package server.messages;

import java.io.IOException;
import java.io.OutputStream;
//NOT BEING USED CURRENTLY
/**
 * The message received from a client when the client wishes to
 * disconnect from the server.
 * Currently only holds the message's header.
 * 
 * @author Andrzej Brynczka
 *
 */
public class LogoutMessage extends Message {

	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create the basic Logout message, received from a disconnecting
	 * client.
	 * @param a_header <code>MessageHeader</code>, the message's header
	 */
	public LogoutMessage(MessageHeader a_header) {
		super(a_header);
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************	
	/**
	 * Does not currently send anything, as the logout message is
	 * received by the server from the client.
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		return false;
	}

}

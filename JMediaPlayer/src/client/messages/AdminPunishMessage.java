package client.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import client.messages.PunishmentInfo.Direction;
import client.messages.PunishmentInfo.Punishment;

/**
 * Class containing information on the AdminPunish message sent either
 * as a response to a request by another user(if being punished) or to
 * be sent by this user to request a punishment against another.
 * 
 * Used for both <code>ADMIN_PunishUser</code> and 
 * <code>ADMIN_RemovePunishment</code> messages.
 * @author Andrzej Brynczka
 *
 */
public class AdminPunishMessage extends Message {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The username of the admin making the punishment request */
	private String m_adminName;
	
	/** The information on the punishment and user to take action against */
	private PunishmentInfo m_punishmentInfo;

	/** The response received from the server(after sending request) */
	private MessageResponse m_serverResponse;
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	
	/**
	 * Create a complete AdminPunishMessage object containing all
	 * punishment information.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message's header
	 * @param a_punishment <code>Punishment</code>, the punishment to
	 * 	enact on the target
	 * @param a_direction <code>Direction</code>, the direction of the
	 * 	punishment
	 * @param a_admin <code>String</code>, the name of the admin that
	 * 	initiated the request
	 * @param a_target <code>String</code>, the target's user name
	 * @param a_targetIP <code>String</code>, the target's IP, provided if
	 * 	required for the message type
	 */
	public AdminPunishMessage(MessageHeader a_header, Punishment a_punishment,
			Direction a_direction, String a_admin, String a_target, 
			String a_targetIP) {
		super( a_header );
		
		m_punishmentInfo = new PunishmentInfo( a_target, a_targetIP, 
				a_punishment, a_direction );	
		m_adminName = a_admin;
		m_serverResponse = null;
		
	}
	
	/**
	 * Create a complete AdminPunishMessage object containing all
	 * punishment information.
	 * 
	 * @param a_header <code>MessageHeader</code>, the message's header
	 * @param a_punishInfo <code>PunishmentInfo</code>, information on the
	 * 	punishment to enact, its direction, and the user being targeted
	 * @param a_adminName <code>String</code>, the admin that requested the
	 * 	punishment
	 */
	public AdminPunishMessage(MessageHeader a_header, 
			PunishmentInfo a_punishInfo, String a_adminName){
		super( a_header );
		
		m_punishmentInfo = a_punishInfo;
		m_adminName = a_adminName;
		m_serverResponse = null;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the punishment being requested
	 * @return <code>Punishment</code> - currently: 
	 * 	<code>KICK</code>, <code>MUTE</code>, or <code>BAN</code>
	 */
	public Punishment getPunishment(){
		return m_punishmentInfo.getPunishment();
	}
	
	/**
	 * Get the direction of the punishment
	 * @return <code>Direction</code> - the direction of the punishment
	 * 	against the target
	 */
	public Direction getDirection(){
		return m_punishmentInfo.getDirection();
	}
	
	/**
	 * Get the username of the admin that sent the request
	 * @return <code>String</code> - the admin's name
	 */
	public String getAdminName(){
		return m_adminName;
	}
	
	/**
	 * Get the username of the user to be punished
	 * @return <code>String</code> - the target of the punishment
	 */
	public String getTargetName(){
		return m_punishmentInfo.getTargetName();
	}
	
	/**
	 * Get the target's IP as a string
	 * @return <code>String</code> - the IP as a string
	 */
	public String getTargetIP(){
		return m_punishmentInfo.getTargetIP();
	}
	
	/**
	 * Get the server's response to the punishment request
	 * @return <code>MessageResponse</code> - the response
	 */
	public MessageResponse getServerResponse(){
		return m_serverResponse;
	}
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the punishment to take against the target
	 * 
	 * @param a_punishment <code>Punishment</code>, the punishment to take
	 * @return <code>boolean</code> - <code>false</code> if the punishment
	 * 	is <code>INVALID</code> or null, <code>true</code> otherwise
	 */
	public boolean setPunishment(Punishment a_punishment){

		return m_punishmentInfo.setPunishment( a_punishment );	
	}
	
	/**
	 * Set the direction of the punishment to make against the target
	 * 
	 * @param a_direction <code>Direction</code>, the direction of the
	 * 	punishment
	 * @return <code>boolean</code> - <code>false</code> if the given
	 * 	direction is <code>INVALID</code> or null, <code>true</code> otherwise
	 */
	public boolean setDirection(Direction a_direction){
		return m_punishmentInfo.setDirection( a_direction );
	}
	
	/**
	 * Set the user name of the admin making the request.
	 * 
	 * @param a_adminName <code>String</code>, the admin's name
	 * @return <code>boolean</code> - <code>false</code> if the name
	 *  is null, <code>true</code> otherwise
	 */
	public boolean setAdminName(String a_adminName){
		if( a_adminName == null ){
			return false;
		}
		
		m_adminName = a_adminName;
		return true;
	}
	
	/**
	 * Set the user name of the target to have an action taken against
	 * 	it.
	 * 
	 * @param a_targetName <code>String</code>, the target's name
	 * @return <code>boolean</code> - <code>false</code> if the name
	 *  is null, <code>true</code> otherwise
	 */
	public boolean setTargetName(String a_targetName){
		return m_punishmentInfo.setTargetName( a_targetName );
	}
	
	/**
	 * Set the user's IP address in string format.
	 * 
	 * @param a_IP <code>String</code>, the user's IP
	 * @return <code>boolean</code> - <code>true</code> if an IP is given
	 * 	in string format
	 */
	public boolean setTargetIP(String a_IP){
		return m_punishmentInfo.setTargetIP( a_IP );
	}
	
	/**
	 * Set the server's response to the punishment request
	 * 
	 * @param a_response <code>MessageResponse</code>, the response to set
	 * @return <code>boolean</code> - <code>false</code> if the response
	 * 	is <code>INVALID</code>, <code>true</code> otherwise
	 */
	public boolean setServerResponse(MessageResponse a_response){
		if( a_response == MessageResponse.INVALID ){
			return false;
		}
		
		m_serverResponse = a_response;
		return true;
	}
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the AdminPunish request to the given stream, requesting 
	 * that the server commit the punishment.
	 * Included are:
	 * </br>the message header
	 * </br>the punishment's 4-character message code
	 * </br>the size of the admin's name and the admin's name
	 * </br>the size of the target's name and the target's name
	 * </br>the size of the target's IP and the target's IP, if applicable 
	 * 	to the message type
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header code
		a_stream.write( m_header.getHeaderCode().getBytes() );
		
		//write the punishment's reference code
		a_stream.write( m_punishmentInfo.getPunishment().getCode().getBytes());
		
		//write the direction's code
		a_stream.write( m_punishmentInfo.getDirection().getCode() );
		
		//write the name of the admin that made the request
		outData.writeInt( m_adminName.length() );
		a_stream.write( m_adminName.getBytes() );
		
		//write the name of the user to be punished
		outData.writeInt( m_punishmentInfo.getTargetName().length() );
		a_stream.write( m_punishmentInfo.getTargetName().getBytes() );
		
		//write the target's IP if the message requires it
		if( m_header == MessageHeader.ADMIN_RemovePunishment ){
			outData.writeInt( m_punishmentInfo.getTargetIP().length() );
			a_stream.write( m_punishmentInfo.getTargetIP().getBytes() );
		}
		
		a_stream.flush();
		return true;
	}
}

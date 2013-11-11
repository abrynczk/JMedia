package client.messages;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import client.messages.PunishmentInfo.Punishment;

/**
 * Message used to collect information on a group of punished users
 * and their punishments, received from the server.
 * @author Andrzej Brynczka
 *
 */
public class AdminPunishListMessage extends Message{

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** List of punished users and their punishment types */
	private ArrayList<PunishmentInfo> m_punishedUsers;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Create a basic AdminPunishListMessage, ready to have a list of
	 * punished user information added to it.
	 */
	public AdminPunishListMessage(MessageHeader a_header){
		super( a_header );
		m_punishedUsers = new ArrayList<PunishmentInfo>();
	}
	
	/**
	 * Create an AdminPunishListMessage with a pre-built collection of
	 * information on a set of punished users.
	 * 
	 * @param a_punishedUsers Collection&lt;PunishmentInfo&gt;, the set of
	 * 	punishment information for a group of users
	 */
	public AdminPunishListMessage(MessageHeader a_header, 
			Collection<PunishmentInfo> a_punishedUsers){
		
		super( a_header );
		m_punishedUsers = new ArrayList<PunishmentInfo>( a_punishedUsers );
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the number of punished users
	 * 
	 * @return	<code>int</code> - the number of users
	 */
	public int getPunishmentListSize(){
		return m_punishedUsers.size();
	}
	
	/**
	 * Get the PunishmentInfo for a particular user
	 * 
	 * @param a_index <code>int</code>, the index of the desired user
	 * @return <code>PunishmentInfo</code> for the desired user, or
	 * 	<code>null</code> if the given index is outside of the
	 * 	array of users
	 */
	public PunishmentInfo getPunishedUser(int a_index){
		if( a_index < 0 || a_index > m_punishedUsers.size() ){
			return null;
		}
		return m_punishedUsers.get( a_index );
	}
	
	/**
	 * Get the entire collection of punished user information
	 * 
	 * @return <code>Collection&lt;PunishmentInfo&gt;</code> containing
	 * 	the user information
	 */
	public Collection<PunishmentInfo> getPunishedUsers(){
		return m_punishedUsers;
	}
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Clear the list of punished users from this message
	 */
	public void clearList(){
		m_punishedUsers.clear();
	}
	
	/**
	 * Add a set of punishment information for a user
	 * 
	 * @param a_userInfo <code>PunishmentInfo</code>, the information for
	 * 	a single user
	 */
	public void addPunishedUserInfo( PunishmentInfo a_userInfo ){
		m_punishedUsers.add( a_userInfo );
	}
	
	/**
	 * Add a set of punishment information for a user
	 * 
	 * @param a_user <code>String</code>, the user's name
	 * @param a_ip <code>String</code>, the user's IP, in string format
	 * @param a_punishment <code>Punishment</code>, the user's punishment
	 */
	public void addPunishedUserInfo( String a_user, String a_ip, 
			Punishment a_punishment ){
		
		m_punishedUsers.add( 
				new PunishmentInfo(a_user, a_ip, a_punishment, null ) );
	}
	
	/**
	 * Add a group of users' punishment information
	 * 
	 * @param a_userGroup Collection&lt;PunishmentInfo&gt;, the collection
	 * 	of punishment information
	 */
	public void addPunishedUsers( Collection<PunishmentInfo> a_userGroup ){
		m_punishedUsers.addAll( a_userGroup );
	}
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	
	/**
	 * This function sends a request to the server, signaling a desire
	 * to receive a list of punished users.
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {	
		
		//simply send the message header to request a list
		a_stream.write( m_header.getHeaderCode().getBytes() );
		a_stream.flush();
		
		return true;
	}
}

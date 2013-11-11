package server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import server.messages.PunishmentInfo.Punishment;

/**
 * Message used to send a list of all the punished users to a client.
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
	 * @param a_punishedUsers Collection&lt;PunishmentInfo&gt;, the set of
	 * 	punishment information for a group of users
	 */
	public AdminPunishListMessage(MessageHeader a_header, 
			Collection<PunishmentInfo> a_punishedUsers){
		
		super( a_header );
		m_punishedUsers = new ArrayList<PunishmentInfo>( a_punishedUsers );
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
	 * @param a_userInfo <code>PunishmentInfo</code>, the information for
	 * 	a single user
	 */
	public void addPunishedUserInfo( PunishmentInfo a_userInfo ){
		m_punishedUsers.add( a_userInfo );
	}
	
	/**
	 * Add a set of punishment information for a user
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
	 * Send the AdminPunishList to the given stream, providing a client 
	 * with the list of punished users and their punishments.
	 * Included are:
	 * </br>the header
	 * </br>the number of punished users
	 * </br>
	 * </br>for each user:
	 * 		</br>the size of the username and the username
	 * 		</br>the size of the ip address string and the ip address string
	 * 		</br>the 4 character punishment code
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException {
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header
		a_stream.write( m_header.getHeaderCode().getBytes() );
		
		//write the number of users
		outData.writeInt( m_punishedUsers.size() );
		
		//write each user's information
		for( int i = 0; i < m_punishedUsers.size(); i++ ){
			//size of username and the username
			outData.writeInt( m_punishedUsers.get(i).getTargetName().length() );
			a_stream.write( m_punishedUsers.get(i).getTargetName().getBytes() );
			
			//size of ip and the ip
			outData.writeInt( m_punishedUsers.get(i).getTargetIP().length() );
			a_stream.write( m_punishedUsers.get(i).getTargetIP().getBytes() );
			
			//the punishment code
			a_stream.write( 
					m_punishedUsers.get(i)
						.getPunishment().getCode().getBytes() );
			
			a_stream.flush();
		}
		
		return true;
	}
}

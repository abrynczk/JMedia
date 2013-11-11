package server.messages;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Vector;

/**
 * Message class used to contain the list of clients connected to the server,
 * to be sent to a client.
 * @author Andrzej Brynczka
 *
 */
public class UserListMessage extends Message {

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The name of the user that will receive this message */
	private final String m_receiver;
	
	/** The number of users currently connected to the server */
	private int m_numOfUsers;
	
	/** The collection of user names for the connected users */
	private Vector<String> m_users;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************

	/**
	 * Create a message containing the collection of connected clients,
	 * ready to be sent to a client through its <code>sendMessage</code> 
	 * function.
	 * 
	 * @param a_header <code>MessageHeader</code>, the header for this message
	 * @param a_receiver <code>String</code>, the user to receive this message
	 * @param a_userCollection <code>Collection&lt;String&gt;</code>, the 
	 * 	usernames of the connected clients
	 */
	public UserListMessage(MessageHeader a_header, String a_receiver,
			Collection<String> a_userCollection) {
		
		super( a_header );
		m_receiver = a_receiver;
		m_numOfUsers = a_userCollection.size();
		
		m_users = new Vector<String>( m_numOfUsers );
		m_users.addAll( a_userCollection );
	}

	/**
	 * Create a basic message containing the collection of connected clients, 
	 * ready to have the list of users appended through its mutator functions.
	 * 
	 * @param a_header <code>MessageHeader</code>, the header for this message
	 * @param a_receiver <code>String</code>, the user to receive this message
	 */
	public UserListMessage(MessageHeader a_header, String a_receiver){
		super( a_header );
		m_receiver = a_receiver;
		m_numOfUsers = 0;
		m_users = new Vector<String>( 10 );
	}

	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************

	/**
	 * Get the name of the user that this message will be sent to
	 * @return <code>String</code>, the user's name
	 */
	public String getReceiverName(){
		return m_receiver;
	}
	
	/**
	 * Get the total number of clients included within this user list
	 * @return <code>int</code>, the number of clients
	 */
	public int getNumOfUsers(){
		return m_numOfUsers;
	}
	
	/**
	 * Get the collection of client usernames within the list
	 * @return <code>Collection&lt;String&gt;</code>, the collection of names
	 */
	public Collection<String> getUsers(){
		return m_users;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************	
	/**
	 * Add a user to the list of names
	 * @param a_userName <code>String</code>, a client's username
	 * @return <code>boolean</code> - <code>true</code> if the name is a
	 * 	valid String, <code>false</code> if the name is <code>null</code>
	 */
	public boolean addUser(String a_userName){
		if( a_userName == null ){
			return false;
		}
		
		m_users.add(a_userName);
		m_numOfUsers++;
		return true;
	}
	
	/**
	 * Add a collection of usernames to the list
	 * @param a_userCollection <code>Collection&lt;String&gt;</code>, the
	 * 	collection of names
	 */
	public void addUsers(Collection<String> a_userCollection){
		m_users.addAll( a_userCollection );
		m_numOfUsers += a_userCollection.size();
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Send the UserListMessage information to the given stream.
	 * Included are:
	 * </br>the message header
	 * </br>the number of users
	 * </br>the list of users(size of name and name, for each user)
	 */
	@Override
	public boolean sendMessage(OutputStream a_stream) throws IOException{
		DataOutputStream outData = new DataOutputStream( a_stream );
		
		//write the header code
		a_stream.write( m_header.getHeaderCode().getBytes() );
		
		//write the total number of users in the list
		outData.writeInt( m_numOfUsers );
		
		//write the usernames
		for( int i = 0; i < m_numOfUsers; i++ ){
			//write the size of the name first, then the name
			outData.writeInt( m_users.get( i ).length() );	
			a_stream.write( m_users.get( i ).getBytes() );
			a_stream.flush();
		}
		
		return true;
	}
	
}

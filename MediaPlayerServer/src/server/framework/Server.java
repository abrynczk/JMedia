package server.framework;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Collection;

import server.messages.Message.MessageHeader;
import server.messages.PunishmentInfo;
import server.messages.PunishmentInfo.Direction;
import server.messages.ServerMessage;


/**
 * The main class that accepts connections from users, sends users
 * to UserHandler threads to be dealt with, and engages with the database
 * to manage punishment.
 * 
 * @author Andrzej Brynczka
 *
 */
public class Server {

	/** Reference to the message dispatcher */
	private MessageDispatcher m_dispatcher;
	
	/**  Container for basic server information */
	private ServerData m_data;
	
	/** Database accessor containing client punishment */
	private static DatabaseAccess m_dataBase;

	/**
	 * Create a server with provided <code>ServerData</code> to use as
	 * 			initialization data
	 * @param a_data <code>ServerData</code>, the object containing the
	 * 			initialization data
	 */
	Server(ServerData a_data){
		m_data = a_data;
		m_dispatcher = new MessageDispatcher( this );
		m_dataBase = new DatabaseAccess();
	}
	
	/**
	 * Create a server object with a provided .ini file from which to load
	 * 			initialization data
	 * @param a_fileName <code>String</code>, the filename of the server 
	 * 			initialization file
	 */
	Server(String a_fileName){
		m_data = initServer( a_fileName );
		m_dispatcher = new MessageDispatcher( this );
		m_dataBase = new DatabaseAccess();
	}
	
	//********************************************************
	//Server data Get Functions
	//********************************************************
	/**
	 * Get the server's name.
	 * 
	 * @return <code>String</code>, the server's name
	 * @author Andrzej Brynczka
	 */
	public String getServerName(){
		return m_data.getServerName();
	}
	
	/**
	 * Get the password required to access the server.
	 * 
	 * @return <code>String</code>, the password
	 * @author Andrzej Brynczka
	 */
	public String getServerPass(){
		return m_data.getServerPass();
	}
	
	/**
	 * Get the password required to be entered by a user to receive access to
	 * 	administrator powers.
	 * 
	 * @return <code>String</code>, the password 
	 * @author Andrzej Brynczka
	 */
	public String getAdminPass(){
		return m_data.getAdminPass();
	}
	
	/**
	 * Get the multiple-login status, determining whether or not the server
	 * 	permits users with the same IP to connect to the server.
	 * 
	 * @return <code>boolean</code>, <code>true</code> if multiple logins from 
	 * 	the same IP are acceptable or <code>false</code> if not
	 * @author Andrzej Brynczka
	 */
	public boolean allowMultiLogin(){
		return m_data.allowMultiLogin();
	}
	
	//********************************************************
	//Database functions
	//********************************************************
	/**
	 * Check if the provided IP is banned
	 * 
	 * @param a_IP <code>InetAddress</code>, the IP to check
	 * @return <code>boolean</code> - <code>true</code> if the IP is banned
	 * 	<code>false</code> otherwise
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	public boolean isBanned(InetAddress a_IP) throws SQLException{
		return m_dataBase.isBanned( a_IP.getHostAddress() );
	}
	
	/**
	 * Check if the given IP is muted
	 * 
	 * @param a_IP <code>InetAddress</code>, the IP to check
	 * @return <code>boolean</code> - <code>true</code> if the IP is muted
	 * 	<code>false</code> otherwise
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	public boolean isMuted(InetAddress a_IP) throws SQLException{
		return m_dataBase.isMuted( a_IP.getHostAddress() );
	}
	
	/**
	 * Set the given IP's ban status in the database and kick the user
	 * if the ban is set.
	 * 
	 * @param a_targetName <code>String</code>, the target's name
	 * @param a_IP <code>String</code>, the IP to modify
	 * @param a_direction <code>Direction</code>, the desired change to status(
	 * 	<code>SET_PUNISHMENT</code> to ban, <code>REMOVE_PUNISHMENT</code> 
	 * 	to un-ban)
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	protected void saveBanStatus(String a_targetName, 
			String a_IP, Direction a_direction) throws SQLException{
		
		if( a_direction == Direction.SET_PUNISHMENT ){ 
			//save the user's ban status in the database, ensuring that
			//it will not be allowed to login in the future
			m_dataBase.setBanned( a_IP, a_targetName ); 
			
			//kick the user from the server
			kickUser( a_targetName );
		}
		else{ 
			//remove the user's ban status from the server, allowing future
			//logins
			m_dataBase.removeBan( a_IP ); 
		}
	}
	
	/**
	 * Set the given IP's mute status in the database and modify the user's 
	 * current mute status on the server.
	 * 
	 * @param a_targetName <code>String</code>, the target's name
	 * @param a_IP <code>String</code>, the IP to modify
	 * @param a_direction <code>Direction</code>, the desired change to status(
	 * 	<code>SET_PUNISHMENT</code> to mute, <code>REMOVE_PUNISHMENT</code> 
	 * 	to un-mute)
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	protected void saveMuteStatus(String a_targetName , 
			String a_IP, Direction a_direction) throws SQLException{
		
		if( a_direction == Direction.SET_PUNISHMENT ){	
			//save the user's mute status for the future
			m_dataBase.setMuted( a_IP, a_targetName ); 
			
			//set the user's current mute status on the server
			muteUser( a_targetName, true );
		}
		else{ 
			//remove the user's mute status, ensuring future logins don't
			//enable a mute
			m_dataBase.removeMute( a_IP ); 
			
			//remove the user's current mute status on the server
			muteUser( a_targetName, false );
			
		}
	}
	
	/**
	 * Get a collection of all of the punished users and their punishments
	 * 
	 * @return Collection&lt;PunishmentInfo&gt; - containing information on
	 * 	all of the punished users: their username at the time, their IP address
	 * 	in string format, and their punishment
	 * @throws SQLException if an error occurs while querying the database
	 * 	for the information
	 * @author Andrzej Brynczka
	 */
	protected Collection<PunishmentInfo> getAllPunished() throws SQLException{
		return m_dataBase.getAllPunished();
	}
	
	//********************************************************
	//Utility Functions
	//********************************************************
	/**
	 * Set the given user's mute status, either enabling or disabling
	 * their ability to send chatmessages to other users.
	 * @param a_targetName String, the target user's name
	 * @param a_mute boolean, decisition to mute or unmute the user
	 * 	(true == mute, false == un-mute )
	 * @author Andrzej Brynczka
	 */
	protected void muteUser(String a_targetName, boolean a_mute){
		UserHandler targetHandler = m_dispatcher.getUserHandler( a_targetName );
		System.out.println("muting: " + a_targetName);
		if( targetHandler == null ){
			//user disconnected
			return;
		}
		
		System.out.println("muted");
		//set the user's current mute status
		targetHandler.getUser().setMuted( a_mute );
	}
	
	/**
	 * Kick the given user from the server, terminating their connection.
	 * @param a_targetName String, the target user's name
	 * @author Andrzej Brynczka
	 */
	protected void kickUser(String a_targetName){
		UserHandler targetHandler = m_dispatcher.getUserHandler( a_targetName );
		
		if( targetHandler == null ){
			//user already disconnected
			return;
		}
		
		//kick the target and remove it from the dispatcher's records
		ServerMessage servMsg = new ServerMessage(MessageHeader.SERVER_Kicked);
		servMsg.setReceiverName( a_targetName );
		servMsg.setMessage("You've been kicked by an admin!");
		
		targetHandler.terminateConnection( servMsg );
	}
	
	/**
	 * Begin execution of the server.
	 * @throws IOException if the server cannot be started.
	 * @author Andrzej Brynczka
	 */
	public void run() throws IOException{
		ServerSocket listenSocket = new ServerSocket( m_data.getPort() );
		m_dispatcher.start();
		
		while ( true ) {		
			Socket socket = listenSocket.accept();
			UserHandler userThread = new UserHandler(socket, m_dispatcher,this);
			userThread.start();
			System.out.println("Launched thread for new user with IP: " 
					+ socket.getInetAddress());
			
		}	
	}
	
	/**
	 * Signal the main server to stop accepting new connections and exit.
	 * @author Andrzej Brynczka
	 */
	public void shutDownServer(String a_reason){
		if( a_reason != null ){
			System.out.println( a_reason );
		}
		System.exit(1);
		
	}
	/**
	 * Initialize the server with parameters provided within a given .ini file.
	 * 
	 * @param a_fileName <code>String</code>, the filename of the .ini file 
	 * 	with the required initialization parameters needed to run the server.
	 * @return <code>ServerData</code> with the loaded initialization parameters
	 * 	or with default parameters if the file cannot be loaded
	 * @see <code>ServerData</code> class file, for information on the 
	 * 	default parameters
	 * @author Andrzej Brynczka
	 */
	private ServerData initServer(String a_fileName){
		ServerData servData;
		if( !(new File(a_fileName).exists()) ){
			//file not found, try to create a file with default information
			//and start the server with default initialization values.
			try {
				ServerData.createIni(a_fileName);
				
				System.out.println(a_fileName +" not found.");
				System.out.println("New server.ini file created with default"
						+ " initialization values.");
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.out.println("No initialization file created.");
			}
			
			System.out.println("Using default values.");	
			servData = ServerData.createDefaultData();
			
		}
		else{
			//initialization file exists, attempt to load its data		
			try {
				servData = ServerData.loadIni(a_fileName);
				System.out.println("Values loaded from " + a_fileName);						
			} catch (IOException e) {
				//couldn't load the data from the .ini file, use default values
				System.out.println(e.getMessage());
				System.out.println("Using default values.");
				
				servData = ServerData.createDefaultData();
			}		
		}
		
		return servData;
	}
		
	public static void main(String argv[]){
		Server server = new Server("server.ini");
		try {
			server.run();
		} catch (IOException e) {
			System.out.println("Unable to start server: " + e.getMessage());
		}
	}
}

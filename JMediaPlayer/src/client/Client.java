package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import client.messages.AdminLoginMessage;
import client.messages.AdminPunishListMessage;
import client.messages.AdminPunishMessage;
import client.messages.PunishmentInfo;
import client.messages.PunishmentInfo.Direction;
import client.messages.ChatMessage;
import client.messages.FileTransRequestMessage;
import client.messages.FileTransResponseMessage;
import client.messages.FileTransferMessage.TransferStage;
import client.messages.Message;
import client.messages.Message.MessageHeader;
import client.messages.Message.MessageResponse;
import client.messages.PrivateChatMessage;


/**
 *  Class used to facilitate communication with the server.
 *  
 *  <p>
 *  This class launches the read/write threads that directly handle
 *  the communication with the server through the socket. 
 *  <p>
 *  It also manages user information, incoming/outgoing messages,
 *  and file transfer requests(with the aid of the FileSender thread)
 *  
 * @author Andrzej Brynczka
 *
 */
public class Client {
	
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************
	/** The maximum number of file's allowed to be sent at a time */
	public final int MAX_CONCURRENT_FILE_SENDS = 3;
	
	/**
	 * The maximum number of messages to be maintained and displayed at a time
	 */
	public final int MAX_MESSAGES_DISPLAYED = 100;
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/**
	 * Reference to the main User object containing information on the
	 * client's username, admin status, and mute status
	 */
	private User m_user;
	
	/**
	 * Reference to the socket used to connect to the server
	 */
	private Socket m_socket;
	
	/**
	 * The server's ip address, as a string
	 */
	private String m_serverIP;
	
	/**
	 * The server's port
	 */
	private int m_port;
	
	/**
	 * The server's password
	 */
	private String m_serverPass;
	
	/**
	 * Reference to the ClientWriter thread that sends messages to the
	 * server
	 */
	private ClientWriter m_writerThread;
	/**
	 * Reference to the ClientReader thread that receives and processes
	 * messages sent from the server
	 */
	private ClientReader m_readerThread;
	
	/**
	 * Table of FileSender threads, that manage individual file
	 * transmissions.
	 * 
	 * The Integer key is equal to the file transfer id of the
	 * file being sent by the FileSender
	 * 
	 */
	private Hashtable<Integer, FileSender> m_fileSenderTable;
	
	
	/**
	 * Indicator of whether or not the client is logged into the server.
	 * 
	 * Created as a SimpleBooleanProperty to allow for its value to be
	 * bound to components in the view, leading to the immediate display of 
	 * the chat screen upon successful login
	 */
	private SimpleBooleanProperty m_loggedIn;
	
	/**
	 * Feedback provided from the server after a failed login attempt
	 * 
	 * As a SimpleStringProperty, its value is bound to components in
	 * the view to allow for changes in feedback to be immediately displayed
	 */
	private SimpleStringProperty m_failedLoginFeedback;
	
	/**
	 * Indicator of whether the client is currently terminating the
	 * connection to the server.
	 */
	private boolean m_terminating;
	
	/**
	 * Determines whether private messages are to be ignored completely
	 */
	private boolean m_ignorePrivateMessages;
	
	/**
	 * Determines whether file requests should be ignored completely
	 */
	private boolean m_ignoreFileRequests;
	
	/**
	 * Contains the name of the user to most recently private message
	 * this client
	 */
	private String m_mostRecentPMSender;
	
	/**
	 * List of known users connected to the server
	 */
	private ObservableList<String> m_userList;
	
	/**
	 * List of users being ignored by this client
	 */
	private ObservableList<String> m_ignoreList;
	
	/**
	 * List of chat messages received from the server
	 */
	private ObservableList<Message> m_chatMessages;
	
	/**
	 * List of private messages received from the server
	 */
	private ObservableList<PrivateChatMessage> m_privateMessages;
	
	/**
	 * List of punishment information on users known to be suffering
	 * from an admin punishment
	 */
	private ObservableList<PunishmentInfo> m_userPunishInfoList;
	
	//<file transfer ID, FileTicket>
	/**
	 * Table of FileTransferTickets that are currently in the process
	 * of file transmission.
	 * 
	 * The table's Integer key = the transfer ID of the file
	 * transfer associated to the key's ticket
	 */
	private Hashtable<Integer, FileTransferTicket> m_fileTicketTable;

	/**
	 * Table of pending FileTransferTickets - tickets for 
	 * file transfers requested by this client that have not yet
	 * received a response.
	 * 
	 * The table's Integer key = the hash of the pending ticket's
	 * associated filename and receiver name
	 * ( (fileName + receivername).hashCode() )
	 */
	private Hashtable<Integer, FileTransferTicket> m_fileTicketPendingTable;

	/**
	 * List of FileTransferTickets that have been started/requested
	 * by this client
	 */
	private ObservableList<FileTransferTicket> m_sentTickets;
	
	/**
	 * List of FileTransferTickets that have been started due to
	 * file requests from other users
	 */
	private ObservableList<FileTransferTicket> m_receivedTickets;
	
	/**
	 * List of FileTickets formed from other users' requesting
	 * a file transmission with this client
	 */
	private ObservableList<FileTransferTicket> m_responseRequestTickets;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * Constructs the basic Client object.
	 * 
	 * <p>
	 * Start the client's connection to the server through
	 * {@link #setServerLoginInfo(String, String, String, int) }
	 * and
	 * {@link #connectToServer() }
	 */
	public Client(){
		m_user = null;
		m_serverIP = null;
		m_port = 0;
		m_serverPass = null;
		m_socket = null;
		m_writerThread = null;
		m_readerThread = null;
		
		m_loggedIn = new SimpleBooleanProperty( false );
		m_failedLoginFeedback = new SimpleStringProperty("");
		m_mostRecentPMSender = null;
		m_terminating = false;
		
		m_userList = FXCollections.observableArrayList();
		m_ignoreList = FXCollections.observableArrayList();
		
		m_chatMessages = FXCollections.observableArrayList();
		m_privateMessages = FXCollections.observableArrayList();
		
		m_userPunishInfoList = FXCollections.observableArrayList();
		
		m_fileTicketTable = new Hashtable<Integer, FileTransferTicket>();
		m_fileTicketPendingTable = new Hashtable<Integer, FileTransferTicket>();
		m_fileSenderTable = new Hashtable<Integer, FileSender>( 
				MAX_CONCURRENT_FILE_SENDS );
		
		m_sentTickets = FXCollections.observableArrayList();
		m_receivedTickets = FXCollections.observableArrayList();
		
		m_responseRequestTickets = FXCollections.observableArrayList();
		
		m_ignorePrivateMessages = false;
		m_ignoreFileRequests = false;
	}
	
	
	/**
	 * Get the adminLoginSuccess' object property
	 * 
	 * @return SimpleObjectProperty for the adminLoginSuccess value
	 */
	public SimpleObjectProperty<MessageResponse> adminLoginSuccessProperty(){
		return m_user.adminLoginSuccessProperty();
	}
	
	/**
	 * Get the loggedIn boolean property
	 * 
	 * @return SimpleBooleanProperty for the loggedIn value
	 */
	public SimpleBooleanProperty loggedInProperty(){
		return m_loggedIn;
	}

	/**
	 * Get the failedLoginFeedback property
	 * 
	 * @return SimpleStringProperty for the failedLoginFeedback value
	 */
	public SimpleStringProperty failedLoginFeedbackProperty(){
		return m_failedLoginFeedback;
	}
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Determine if the client is terminating its connection
	 * from the server
	 * 
	 * @return true if the client is terminating its connection,
	 * 	false if it is not
	 * @author Andrzej Brynczka
	 */
	public boolean isTerminating(){
		return m_terminating;
	}
	
	/**
	 * Determine this client's mute status
	 * 
	 * @return true if the client is muted, false otherwise
	 */
	public boolean isMute(){
		return m_user.isMute();
	}
	
	/**
	 * Determine if this client is ignoring private messages from others
	 * 
	 * @return true is the client is ignoring private messages, 
	 * 	false otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean isIgnorePM(){
		return m_ignorePrivateMessages;
	}
	
	/**
	 * Determine if this client is ignoring file transfer requests from others
	 * 
	 * @return true if this client is ignoring file transfer requests,
	 * 	false otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean isIgnoreFileRequests(){
		return m_ignoreFileRequests;
	}
	
	/**
	 * Determine if this client is ignoring all interaction with the
	 * 	given user
	 * 
	 * @param a_user  String, the user to the ignore against
	 * @return true if the given user is being ignored, false otherwise
	 */
	public boolean isIgnoring( String a_user ){
		return m_ignoreList.contains( a_user );
	}
	
	
	/**
	 * Get the ObservableList that contains the FileTransferTickets
	 * 	for transfer requests sent to this client.
	 * 
	 * @return ObservableList containing the FileTransferTickets
	 * @author Andrzej Brynczka
	 */
	public ObservableList<FileTransferTicket> getSentTicketsList(){
		return m_sentTickets;
	}
	
	/**
	 * Get the ObservableList that contains the FileTransferTickets
	 * 	for transfer requests that were started by this client
	 * 
	 * @return ObservableList containing the tickets
	 * @author Andrzej Brynczka
	 */
	public ObservableList<FileTransferTicket> getReceivedTicketsList(){
		return m_receivedTickets;
	}
	
	/**
	 * Get the ObservableList containing the list of users
	 * 	logged into the server
	 * 
	 * @return ObservableList containing the names of logged in users
	 * @author Andrzej Brynczka
	 */
	public ObservableList<String> getUserList(){
		return m_userList;
	}
	
	/**
	 * Get the ObservableList containing the list of users that
	 * 	are being ignored by this client
	 * 
	 * @return ObservableList containing the names of ignored users
	 * @author Andrzej Brynczka
	 */
	public ObservableList<String> getIgnoreList(){
		return m_ignoreList;
	}
	
	/**
	 * Get the ObservableList containing the list of messages received
	 * 	from the server.
	 * 
	 * @return the ObservableList of Message objects
	 * @author Andrzej Brynczka
	 */
	public ObservableList<Message> getChatMessageList(){
		return m_chatMessages;
	}
	
	/**
	 * Get the ObservableList containing the list of private messages
	 * 	received by this client
	 * 
	 * @return the ObservableList of PrivateChatMessage objects
	 * @author Andrzej Brynczka
	 */
	public ObservableList<PrivateChatMessage> getPrivateChatMessageList(){
		return m_privateMessages;
	}
	

	/**
	 * Get the ObservableList containing FileTransferTickets that 
	 * 	require a response to file transfer requests.
	 * 
	 * @return ObservableList containing the tickets looking for
	 * 	a response to their requests
	 */
	public ObservableList<FileTransferTicket> getResponseRequestTicketList(){
		return m_responseRequestTickets;
	}
	
	/**
	 * Get the ObservableList containing the known punishment information 
	 * 
	 * @return ObservableList containing punishment information on known
	 * 	users
	 */
	public ObservableList<PunishmentInfo> getPunishmentInfoList(){
		return m_userPunishInfoList;
	}
	
	/**
	 * Get this client's designated username
	 * 
	 * @return String, the client's username
	 * @author Andrzej Brynczka
	 */
	public String getUserName(){
		return m_user.getUserName();
	}
	
	/**
	 * Get the server password provided when making the server login attempt
	 * 
	 * @return String containing the server password
	 * @author Andrzej Brynczka
	 */
	public String getServerPassword(){
		return m_serverPass;
	}
	
	/**
	 * Get the client's socket that is being used to connect to the server
	 * 
	 * @return Socket connecting this client to the server, or null
	 * 	if the socket is not yet set
	 * @author Andrzej Brynczka
	 */
	public Socket getSocket(){
		return m_socket;
	}
		
	/**
	 * Get the name of the user who most recently private
	 * 	messaged this client.
	 * @return String containing the user's name or null if the 
	 * 	value has not yet been set
	 * @author Andrzej Brynczka
	 */
	public String getMostRecentPMSender(){
		return m_mostRecentPMSender;
	}

	/**
	 * Get the admin loggin success status
	 * 
	 * @return MessageResponse indicating attempted login success
	 */
	public Message.MessageResponse getAdminLogginSuccess(){
		return m_user.getAdminLoginSuccess();
	}

	/**
	 * Get the number of files currently being sent by the client.
	 * 
	 * @return int, the number of files being sent
	 */
	public int getNumOfCurrentFileSends(){
		return m_fileSenderTable.size();
	}
	
	/**
	 * Get a FileTransferTicket containing information on a
	 *  file transfer that is requesting a response
	 *  
	 * @param a_index int, the index of the ticket on its list
	 * @return the desired FileTransferTicket located at the provided
	 *  index or null if the index was out of bounds
	 */
	public FileTransferTicket getResponseRequestTicket(int a_index){
		if( a_index >= m_responseRequestTickets.size() || a_index < 0 ){
			return null;
		}
		
		return m_responseRequestTickets.get(a_index);
	}
	
	/**
	 * Get the pending FileTransferTicket associated to the 
	 *  the combination of the given receiver name and file name
	 *  
	 * @param a_receiver String, the name of the receiver designated
	 *  by the pending ticket
	 * @param a_fileName String, the name of the file designated by
	 *  the pending ticket
	 * @return FileTransferTicket contained for the given
	 *  receiver name and file, or null if none found
	 */
	public FileTransferTicket getPendingFileTicketFromTransferTable(
			String a_receiver, String a_fileName){
		
			return m_fileTicketPendingTable.get( 
					(a_fileName + a_receiver).hashCode() );
	}
	
	/**
	 * Get a FileTransferTicket from the central file ticket table
	 * 
	 * @param a_transferID int, the desired ticket's transfer id
	 * @return FileTransferTicket associated to the given transfer id, 
	 *  or null if none found
	 */
	public FileTransferTicket getFileTicketFromTransferTable(
				int a_transferID ){
		
		return m_fileTicketTable.get( a_transferID );
	}
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the required server login information.
	 * 
	 * @param a_userName  String, this client's desired username
	 * @param a_serverPass  String, the server's password
	 * @param a_serverIP String, the IP address associated to the server
	 * @param a_port  int, the server's port number
	 * @author Andrzej Brynczka
	 */
	public void setServerLoginInfo(String a_userName, String a_serverPass,
			String a_serverIP, int a_port){
		m_user = new User(a_userName, MessageResponse.INVALID, false);
		m_serverIP = a_serverIP;
		m_port = a_port;
		m_serverPass = a_serverPass;
	}
	
	/**
	 * Set the client's terminating status
	 * 
	 * @param a_terminating  boolean, the condition to set
	 * @author Andrzej Brynczka
	 */
	public synchronized void setTerminating(boolean a_terminating){
		m_terminating = a_terminating;
	}
	
	/**
	 * Set this client's "ignorePM" status
	 * 
	 * @param a_ignore boolean, true to ignore private messages, 
	 * 	false to receive them
	 * @author Andrzej Brynczka
	 */ 
	public void setIgnorePM(boolean a_ignore){
		m_ignorePrivateMessages = a_ignore;
	}
	
	/**
	 * Set this client's "ignoreFileRequests" status
	 * 
	 * @param a_ignore boolean, true to ignore file requests,
	 * 	false to receive them again
	 * @author Andrzej Brynczka
	 */
	public void setIgnoreFileRequests(boolean a_ignore){
		m_ignoreFileRequests = a_ignore;
	}
	
	/**
	 * Set this client's stored mute status, received from the server
	 * 
	 * @param a_muted boolean, true to mute or false to remove the mute
	 * @author Andrzej Brynczka
	 */
	protected void setMute(boolean a_muted){
		m_user.setMute( a_muted );
	}
	
	/**
	 * Set this client's admin loggin success status from the
	 * 	server's message response
	 * 
	 * @param a_adminLogginSuccess MessageResponse, indicating the 
	 * 	login success
	 */
	public void setAdminLoginSuccess( 
			final Message.MessageResponse a_adminLogginSuccess ){
		
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
            	m_user.setAdminLoginSuccess( a_adminLogginSuccess );
			}
		});
	}
	
	/**
	 * Set the list containing known PunishmentInfo data
	 * 
	 * @param a_collectionOfPunishments Collection, containing punishmentInfo
	 * 	to be set
	 * @return true if the Collection is set, false otherwise
	 */
	public boolean setPunishmentInfoList(
			Collection<PunishmentInfo> a_collectionOfPunishments){
		
		return m_userPunishInfoList.setAll( a_collectionOfPunishments );
	}
	
	/**
	 * Set the this client's login status
	 * 
	 * @param a_loggedIn boolean, true if logged in or false if not
	 */
	protected void setLoggedIn(final boolean a_loggedIn){
		m_loggedIn.setValue( a_loggedIn );
	}
	

	/**
	 * Set this client's failed login feedback, received from the server
	 *  after failing a login
	 *  
	 * @param a_feedback String, the feedback received
	 */
	public void setFailedLoginFeedback(final String a_feedback){	
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_failedLoginFeedback.setValue( a_feedback );
			}
		});
		
	}
	
	/**
	 * Set the list of connected users
	 * 
	 * @param a_userList Collection, containing the list of users known
	 *  to be connected 
	 */
	public void setUserList(Collection<String> a_userList){
		m_userList.setAll( a_userList );
	}
	
	
	/**
	 * Add a user to the user list
	 * 
	 * @param a_name String, the name of the user to add
	 * @return true if the user is added
	 */
	public boolean addToUserList(String a_name){
		return m_userList.add( a_name );
	}
	
	/**
	 * Add a user to the ignore list
	 * 
	 * @param a_name String, the user to ignore
	 * @return true if the user was ignored
	 */
	public boolean addToIgnoreList(String a_name){
		return m_ignoreList.add( a_name );
	}
	
	/**
	 * Add a FileTransferTicket for a currently active file transfer
	 *  to the central table containing information on all active
	 *  transmissions
	 *  
	 * @param a_ticket FileTransferTicket, the ticket to add
	 */
	public void addFileTicketToTransferTable( FileTransferTicket a_ticket ){
		m_fileTicketTable.put( a_ticket.getTransferID() , a_ticket );
	}
	
	/**
	 * Add a FileTransferTicket to the list containing FileTransferTickets
	 * 	started by this client.
	 * 
	 * @param a_ticket  FileTransferTicket, the ticket to add
	 * @return true if the ticket is added
	 * @author Andrzej Brynczka
	 */
	public boolean addToSentTicketsList(FileTransferTicket a_ticket){
		return m_sentTickets.add( a_ticket );
	}
	
	/**
	 * Add a FileTransferTicket to the list containing FileTransferTicket
	 * 	transmissions received from other users
	 * 
	 * @param a_ticket FileTransferTicket, the ticket to add
	 * @return true if the ticket is added
	 * @author Andrzej Brynczka
	 */
	public boolean addToReceivedTicketsList(FileTransferTicket a_ticket){
		return m_receivedTickets.add( a_ticket );
	}
	
	/**
	 * Add the given message to the chat message list
	 * 
	 * @param a_message Message, the message to add
	 * @return true if the message is added, false otherwise
	 */
	public boolean addChatMessageToList(Message a_message){
		if( a_message == null ){
			return false;
		}
		
		if( m_chatMessages.size() >= MAX_MESSAGES_DISPLAYED ){
			m_chatMessages.remove(0);
		}

		return m_chatMessages.add( a_message );
	}
	
	/**
	 * Add the given message to the private chat list
	 * 
	 * @param a_message PrivateChatMessage, the mesage to add
	 * @return true if the message is added, false otherwise
	 */
	public boolean addMessageToPrivateChatList(PrivateChatMessage a_message){
		if( a_message == null ){
			return false;
		}
		
		if( m_privateMessages.size() >= MAX_MESSAGES_DISPLAYED ){
			m_privateMessages.remove(0);
		}
		
		m_mostRecentPMSender = a_message.getSenderName();
		return m_privateMessages.add( a_message );
	}
	
	/**
	 * Add a FileTransferTicket containing information on a file
	 *  transfer request to the list containing the tickets
	 *  seeking a response
	 *  
	 * @param a_ticketToRespondTo FileTransferTicket, the ticket to add
	 * @return true if the ticket was added, false if it was null
	 */
	public boolean addResponseRequestTicketToList(
				FileTransferTicket a_ticketToRespondTo){
		
		if( a_ticketToRespondTo == null ){
			return false;
		}
		
		return m_responseRequestTickets.add( a_ticketToRespondTo );
	}
	
	/**
	 * Remove a ticket from the list containing FileTransferTickets
	 * 	with transmissions started by this client
	 * 
	 * @param a_ticket FileTransferTicket, ticket to remove
	 * @return true if the ticket is removed, false otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean removeFromSentTicketsList(FileTransferTicket a_ticket){
		return m_sentTickets.remove( a_ticket );
	}
	
	/**
	 * Remove a ticket from the list containing FileTransferTickets
	 * 	with transmissions received from other users
	 * 
	 * @param a_ticket FileTransferTicket, ticket to remove
	 * @return true if the ticket is removed, false otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean removeFromReceivedTicketsList(FileTransferTicket a_ticket){
		return m_receivedTickets.remove( a_ticket );
	}
		
	/**
	 * Remove the provided punishment information from the
	 *	list containing data on known punishments
	 *
	 * @param a_pMsg AdminPunishMessage, containing necessary punishment
	 * 	data
	 * @return true if the removal was a success, false if the list
	 * 	did not contain the provided element
	 */
	public boolean removeFromPunishmentInfoList(AdminPunishMessage a_pMsg){
		PunishmentInfo info = new PunishmentInfo();
		info.setPunishment( a_pMsg.getPunishment() );
		info.setTargetIP( a_pMsg.getTargetIP() );
		info.setTargetName( a_pMsg.getTargetName() );
		
		return removeFromPunishmentInfoList( info );
	}
	
	/**
	 * Remove the provided punishment information from the
	 *	list containing data on known punishments
	 *
	 * @param a_pInfo PunishmentInfo, containing necessary punishment
	 * 	data
	 * @return true if the removal was a success, false if the list
	 * 	did not contain the provided element
	 */
	public boolean removeFromPunishmentInfoList(PunishmentInfo a_pInfo){
		return m_userPunishInfoList.remove( a_pInfo );
	}
	
	/**
	 * Remove a file sender from the FileSender table held by the client.
	 * 
	 * @param a_transferID int, key associated to the FileSender within
	 * 	the table
	 */
	public void removeFileSender(int a_transferID){
		m_fileSenderTable.remove( a_transferID );
	}
	
	
	/**
	 * Remove a file transfer ticket with data on a transfer
	 *  requesting a response from the response-request list
	 *  
	 * @param a_index int, the index of the ticket to remove
	 * @return true if the ticket was removed, false otherwise
	 */
	public boolean removeResponseRequestTicketFromList(int a_index){
		if( a_index >= m_responseRequestTickets.size() || a_index < 0 ){
			return false;
		}
		
		m_responseRequestTickets.remove( a_index );
		return true;
	}
	
	/**
	 * Remove a user from the user list
	 * 
	 * @param a_name String, the user to remove
	 * @return true if the user was removed, false otherwise
	 */
	public boolean removeFromUserList(String a_name){
		return m_userList.remove( a_name );
	}
		

	/**
	 * Remove a user from the ignore list
	 * 
	 * @param a_name String, the user to remove from the ignore list
	 * @return true if the user was removed from the ignore list
	 */
	public boolean removeFromIgnoreList(String a_name){
		return m_ignoreList.remove( a_name );
	}
	

	/**
	 * Delete a FileTransferTicket from the central ticket list
	 * 
	 * @param a_transferID int, the transfer id of the ticket to delete
	 */
	public void deleteFileTicketFromTransferTable( int a_transferID ){
		m_fileTicketTable.remove( a_transferID );
	}
	
	/**
	 * Delete a pending FileTransferTicket from the pending ticket table
	 * 
	 * @param a_receiver String, the name of the receiver specified
	 *  by the pending ticket
	 * @param a_fileName String, the name of the file specified
	 *  by the pending ticket
	 */
	public void deletePendingFileTicketFromTransferTable(String a_receiver, 
			String a_fileName){
		m_fileTicketPendingTable.remove((a_fileName + a_receiver).hashCode());
		
	}

	// *********************************************************
	// *******Client Message Initialization Functions***********
	// *********************************************************
	/**
	 * Send a file transfer request message aimed at the given user
	 * 
	 * @param a_filePath String, the path of the file to send
	 * @param a_fileName String, the name of the file to send
	 * @param a_fileSize long, the size of the file to send(in bytes)
	 * @param a_receiver String, the name of the user to aim the
	 * 	file transfer request at
	 * @author Andrzej Brynczka
	 */
	public void requestFileTransfer( String a_filePath, 
			String a_fileName, long a_fileSize, String a_receiver ){
		System.out.println("Sending file request");
		try{
			//create the file ticket for record keeping
			FileTransRequestMessage ftrMsg = new FileTransRequestMessage(
					MessageHeader.FILE_Transfer, 
					0,  // transferID will acquired from server, not set yet
					TransferStage.STAGE1_RequestFromSender, 
					m_user.getUserName(), 
					a_receiver, 
					a_fileName, 
					a_fileSize);
			
			//create the request message to be sent to the server
			FileTransferTicket pendingTicket = new FileTransferTicket(
					0, //transferID will be acquired from server 
					TransferStage.STAGE1_RequestFromSender, 
					a_fileName, 
					(int) a_fileSize, 
					getUserName(), 
					a_receiver, 
					false);
			
			//set the file-to-send's filepath
			pendingTicket.setFilePath( a_filePath );
		
			//add the file request to the request pending table, to be picked up
			//and added to the complete file ticket if the request is accepted
			m_fileTicketPendingTable.put( 
						(a_fileName + a_receiver).hashCode(), 
						pendingTicket );
			
			//send the message
			m_writerThread.addMessage( ftrMsg );
			System.out.println("sent file request");
		
		}
		catch(Exception e){
			//TODO let user know file size is invalid
			//TODO make this return false and throw a dialog up
			System.out.println("file too big");
		}
	}
	
	/**
	 * Send a response message to the user who issued the request
	 * 	specified in the given FileTransferTicket
	 * 
	 * @param a_ticket FileTransferTicket, the ticket containing information
	 * 	on the original file request
	 * @param a_response MessageResponse, the response to send in the
	 * 	response message
	 * @author Andrzej Brynczka
	 */
	public void sendFileResponse(FileTransferTicket a_ticket, 
			MessageResponse a_response){
		
		System.out.println("sending response to request from user: " 
				+ getUserName());
		
		//create the response message 
		FileTransResponseMessage ftrMsg = new FileTransResponseMessage(
				MessageHeader.FILE_Transfer, a_ticket.getTransferID(), 
				a_ticket.getTransferStage(), 
				getUserName(), a_ticket.getSenderName(), 
				a_ticket.getFileName(), a_response);
		
		if( a_response == MessageResponse.Success && 
				a_ticket.getTransferStage() == 
					TransferStage.STAGE2_ResponseToRequest){
			try {
				//client wishes to engage in the transfer...
				
				//update the new stage in the ticket and create the new file
				a_ticket.setTransferStage( 
						TransferStage.STAGE3_DataTransmission );
				a_ticket.createNewFile();
				
				//add the ticket to the client's records
				addFileTicketToTransferTable( a_ticket );
				addToReceivedTicketsList( a_ticket );
				
			} catch (IOException e) {
				//failed to create the new file, 
				//send message response indicating failure
				System.out.println("Failed to create file " 
						+ a_ticket.getFileName() + ". \n" + e.getMessage() );
				
				System.out.println("Declining request.");
				
				ftrMsg.setResponse( MessageResponse.Failure );
			}
		}
		
		System.out.println("Sent file response: " + ftrMsg.getResponse());
		m_writerThread.addMessage( ftrMsg );
	}
	
	/**
	 * Send a chat message(Regular or private) to the server
	 * 
	 * @param a_message String, the message to send
	 */
	public void sendChatMessageToServer(String a_message){
		System.out.println("sending chat message");
		if( a_message.length() > ChatMessage.MESSAGE_CHAR_LIMIT){
			a_message = a_message.substring(0, ChatMessage.MESSAGE_CHAR_LIMIT );
		}
		
		Message msg;
		if( a_message.startsWith("@") == true ){
			//send a private message
			int messageSeperator = a_message.indexOf(" ");
			
			if( messageSeperator == -1 ){
				return;
			}
			
			//separate the message from the receiver's name
			String receiver = a_message.substring(1, messageSeperator);
			String message = a_message.substring(messageSeperator+1);
			
			msg = new PrivateChatMessage(MessageHeader.PRIV_SendChatMess, 
					message, getUserName(), receiver);

		}
		else{
			msg = new ChatMessage(MessageHeader.REG_SendChatMess, 
					a_message, m_user.getUserName() );
		}

		
		m_writerThread.addMessage( msg );	
	}
	 
	/**
	 * Send a message to attempt an admin login
	 * 
	 * @param a_password String, the admin password to send
	 */
	public void sendAdminLoginMessage( String a_password ){
		AdminLoginMessage alMsg = new AdminLoginMessage(
				MessageHeader.ADMIN_Login, a_password ); 
				
		System.out.println("send admin login with pass: " + a_password );
		m_writerThread.addMessage( alMsg );
		
	}
	
	/**
	 * Send a message attempting to punish another user
	 * 
	 * @param a_target String, the name of the user to target
	 * @param a_punishment PunishMent, the type of punishment to enact
	 */
	public void sendAdminPunishMessage( 
			String a_target, PunishmentInfo.Punishment a_punishment ){
		
		AdminPunishMessage apMsg = new AdminPunishMessage(
				MessageHeader.ADMIN_PunishUser, 
				a_punishment, 
				Direction.SET_PUNISHMENT, 
				getUserName(), 
				a_target,
				null
				);
		
		System.out.println("Sending to writer: " 
				+ a_punishment.toString() + " against user " + a_target);
		m_writerThread.addMessage( apMsg );
	}
	
	/**
	 * Send a message attempting to remove an admin punishment from
	 * 	a set of users(or single user)
	 * 
	 * @param a_userList Collection, of PunishmenInfo for users known to be
	 * 	punished
	 */
	public void sendAdminPunishRemoveMessage( 
			Collection<PunishmentInfo> a_userList ){
		
		for(Iterator<PunishmentInfo> it = a_userList.iterator(); it.hasNext();){
			PunishmentInfo currentUserInfo = it.next();
			
			//create the remove punishment message for the current user
			AdminPunishMessage apRvmMsg = new AdminPunishMessage(
					MessageHeader.ADMIN_RemovePunishment,
					currentUserInfo.getPunishment(),
					Direction.REMOVE_PUNISHMENT,
					getUserName(),
					currentUserInfo.getTargetName(),
					currentUserInfo.getTargetIP()
					);
			
			//provided the message to the writer thread to be 
			//sent to the server
			m_writerThread.addMessage( apRvmMsg );
		}
		
	}
	
	/**
	 * Send a message requesting the list of punished users
	 */
	public void sendAdminPunishListRequest(){
		AdminPunishListMessage aplMsg = 
				new AdminPunishListMessage( MessageHeader.ADMIN_PunishList );
		
		m_writerThread.addMessage( aplMsg );
	}
	
	// *********************************************************
	// *******************Utility Functions*********************
	// *********************************************************
	/**
	 * Terminate the connection to the server, closing all
	 * 	ClientReader, ClientWriter, and FileSender threads that are
	 * 	handling their respective tasks for the client.
	 */
	public void terminateConnection(){
	
		if( m_socket == null || isTerminating() ){
			return;
		}
		
		setTerminating( true );
		
		//if in the process of a file transfer, aware the other
		//users of this client's coming disconnection
		if( m_fileTicketTable.size() > 0 ){
			Collection<FileTransferTicket> ticketSet = 
						m_fileTicketTable.values();
			
			for( Iterator<FileTransferTicket> it = 
						ticketSet.iterator(); it.hasNext(); ){
				FileTransferTicket currentTicket = it.next();
				
				//don't send messages for those transfers that
				//are already completed
				if( currentTicket.getTransferStage() != 
												TransferStage.STAGE5_Done ){
					//send a message for the current file indicating the
					//failure to complete the transfer
					String receiver;
					if( currentTicket.getSenderName().equals( getUserName() ) ){
						receiver = currentTicket.getReceiverName();
					}
					else{
						receiver = currentTicket.getSenderName();
					}
					FileTransResponseMessage ftrMsg = 
							new FileTransResponseMessage(
								MessageHeader.FILE_Transfer, 
								currentTicket.getTransferID(), 
								TransferStage.STAGE4_TransEndResponse, 
								getUserName(), 
								receiver, 
								currentTicket.getFileName(), 
								MessageResponse.Failure
								);
					
					//send the message to the server
					m_writerThread.addMessage( ftrMsg );
					System.out.println("sending fileEnd ticket to " 
							+ receiver);
				}	
			}
			
			m_fileTicketTable.clear();
		}
		
		//if in the process of sending actual file data, end the file transfer
		if( m_fileSenderTable.size() > 0 ){
			Collection<FileSender> senderSet = m_fileSenderTable.values();
			
			for( Iterator<FileSender> it = 
								senderSet.iterator(); it.hasNext(); ){
				FileSender currentSender = it.next();
				currentSender.endTransmission();
				
				try {
					//wait for a final message indicating the transmission end
					//is sent to the writerthread within the file sender
					currentSender.join();
				} catch (InterruptedException e) {
					System.out.println("Interrupted when joining on file" +
							" sender during connection termination");
					System.out.println( e.getMessage() );
				}
			}
			
			m_fileSenderTable.clear();
		}
	
		//close the socket connection
		try {
			System.out.println("Preventing additional messages." 
					+ "joining on writer");
			
			if( m_writerThread != null && m_writerThread.isAlive() ){
				System.out.println("joining on writer");
				m_writerThread.continueSendingMessages( false );
				m_writerThread.join();
			}
			
			if( m_readerThread != null ){
				m_readerThread.interrupt();
			}

			System.out.println("both threads are down, closing socket");

		} catch ( InterruptedException e ) {
			System.out.println("ERROR: ending writer/reader threads" 
					+" during connection termination: ");
			System.out.println( e.getMessage() );
		} finally{
			try {
				m_socket.close();
				System.out.println("Socket closed");
			} catch (IOException e) {
				System.out.println("ERROR: closing socket during"
						+" connection termination.");
				System.out.println( e.getMessage() );
			}
			
			//clear and free all variables to ensure collection
			m_socket = null;
			m_readerThread = null;
			m_writerThread = null;
			m_mostRecentPMSender = null;
			setLoggedIn( false );
			//setFailedLogginFeedback( "" );
			setAdminLoginSuccess( MessageResponse.INVALID );
			
			m_chatMessages.clear();
			m_privateMessages.clear();
			m_fileSenderTable.clear();
			m_fileTicketTable.clear();
			m_fileTicketPendingTable.clear();
			
		}	
		setTerminating( false );	
	}
	
	/**
	 * Connect to the server, using previously defined values for
	 * 	serverIP and port set in <code>setServerLoginInfo()</code>
	 * 
	 * @see {@link Client#setServerLoginInfo(String, String, String, int)}
	 * @throws UnknownHostException if an invalid IP address is
	 * 	provided
	 * @throws IOException if an error occurs when creating the socket
	 * 	used to connect to the server
	 */
	public void connectToServer() throws UnknownHostException, IOException{
		m_socket = new Socket( m_serverIP, m_port );
		m_writerThread = new ClientWriter(this);
		m_readerThread = new ClientReader(this);
		
		m_writerThread.start();
		m_readerThread.start();		
	}
	

	/**
	 * Attempt to begin a file data transfer sequence
	 * 
	 * @param a_ticket	FileTransferTicket, containing information
	 * 	on the file to send
	 * @return true if the file has begun transmission, false if
	 * 	the current limit for file send transmissions has
	 * 	been reached
	 */
	public boolean beginFileDataSend(FileTransferTicket a_ticket){
		if( m_fileSenderTable.size() >= MAX_CONCURRENT_FILE_SENDS ){
			return false;
		}
		FileSender fileSenderThread = 
				new FileSender( a_ticket, m_writerThread, this);
	
		//start the file sender thread and save it in a table to keep
		//track of its activity
		fileSenderThread.start();
		m_fileSenderTable.put( a_ticket.getTransferID(), fileSenderThread );
		
		return true;
	}
	
	/**
	 * Kill a file data transmission with the given transfer id
	 * 
	 * @param a_transferID int, the transfer id whose transmission is to be 
	 * 	killed
	 */
	public void killFileDataSend(int a_transferID ){
		System.out.println("got a kill send request for id:" + a_transferID);
		FileSender fileSenderThread = m_fileSenderTable.get( a_transferID );
		
		if( fileSenderThread != null ){
			System.out.println("CARRYING OUT THE KILL");
			fileSenderThread.endTransmission();	
			m_fileSenderTable.remove( a_transferID );
		}
		
	}
	
	
	
}

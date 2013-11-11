package server.framework;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import server.messages.AdminLoginMessage;
import server.messages.AdminPunishListMessage;
import server.messages.AdminPunishMessage;
import server.messages.FileTransDataMessage;
import server.messages.FileTransErrorMessage;
import server.messages.FileTransResponseMessage;
import server.messages.LoginMessage;
import server.messages.LoginMessage.LoginCondition;
import server.messages.Message;
import server.messages.ChatMessage;
import server.messages.Message.MessageResponse;
import server.messages.PrivateChatMessage;
import server.messages.FileTransferMessage;
import server.messages.FileTransRequestMessage;
import server.messages.Message.MessageHeader;
import server.messages.FileTransferMessage.TransferStage;
import server.messages.PunishmentInfo.Direction;
import server.messages.PunishmentInfo.Punishment;
import server.messages.ServerMessage;
import server.messages.UserListMessage;

/**
 * Thread-based handler of an individual user client.
 * @author Andrzej Brynczka
 *
 */
public class UserHandler extends Thread{
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	
	private final Socket m_socket;
	
	/** Container for the client's individual data */
	private User m_user;
	
	/** Reference to the message dispatcher thread */
	private final MessageDispatcher m_dispatcher;
	
	/** Reference to the main server thread */
	private final Server m_server;
	
	/** Queue of messages to be sent to this client */
	private Queue<Message> m_messagesToReceive;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * Creates the UserHandler thread that handles an individual client's
	 * 	communication with the server
	 * @param a_userSocket <code>Socket</code>, the client's socket
	 * @param a_dispatcher <code>MessageDispatcher</code>, the message 
	 * 	dispatcher thread
	 * @param a_server <code>Server</code>, a reference to the main server
	 */
	UserHandler(Socket a_userSocket, MessageDispatcher a_dispatcher, 
			Server a_server){
		m_socket = a_userSocket;
		m_dispatcher = a_dispatcher;
		m_server = a_server;
		m_user = null;//user information has not yet been acquired
		m_messagesToReceive = new LinkedList<Message>();
	}
	
	@Override
	public void run(){
		//get the client's input stream
		InputStream clientIn = null;
		try {
			clientIn = m_socket.getInputStream();
		} catch (IOException e) {
			//error accessing stream, warn the client if possible and exit
			System.out.println("Unable to access input stream for " 
					+ m_socket.getInetAddress().getHostAddress() );

			//create an error message to send, if possible
			ServerMessage errorMsg = 
					new ServerMessage(MessageHeader.SERVER_Error);
			errorMsg.setMessage("ERROR: Unable to access input stream");
			
			terminateConnection( errorMsg );
		}
	
		//handle login
		try {
			handleLogIn( clientIn );
		} catch (IOException e1) {
			//error handling login information, warn the client and exit
			ServerMessage errorMsg = 
					new ServerMessage(MessageHeader.SERVER_Error);
			
			errorMsg.setMessage("ERROR: Unable to process login information"
					+ e1.getMessage() );
			terminateConnection( errorMsg );
		}
		
		//set 2 second timeout on the read
		try {	
			m_socket.setSoTimeout(2000);
			
		} catch (SocketException e1) {
			System.out.println("Error with setting socket timeout.");
			ServerMessage errorMsg = 
					new ServerMessage(MessageHeader.SERVER_Error);
			
			errorMsg.setMessage("ERROR: Error with socket protocol."
					+ e1.getMessage() );
			
			terminateConnection( errorMsg );
		}
		
		//reader for message headers
		DataInputStream headerReader = new DataInputStream( clientIn );
		
		//read from the client and sent it messages, for as long as the user's
		//thread lives
		while( !isInterrupted() ){
			try {
				
				//send a message to the user if there are any in the backlog
				if( !m_messagesToReceive.isEmpty() )
				{
					do{
						//send the first message in the queue
						Message msg = m_messagesToReceive.poll();
						sendMessage( msg );
						
						//if there is nothing to read, simply continue
						//to send messages
						System.out.println("sent a message");
					}while( headerReader.available() < 4 && 
							!m_messagesToReceive.isEmpty());
				}
				

				//get the message header, if available
				byte[] headerCode = new byte[4];
				int readBytes = headerReader.read( headerCode, 0, 4);

				//check that the client did not disconnect
				if( readBytes == -1 ){
					terminateConnection( null );
				}
				

				MessageHeader header = MessageHeader.fromString( 
						new String(headerCode) );

				System.out.println( "Read header: " + header.toString() );

				//handle the message based on type
				switch( header ){
				case REG_SendChatMess:
					handleRegChatMessage( m_socket.getInputStream() );
					break;
				case PRIV_SendChatMess:
					handlePrivMessage( m_socket.getInputStream() );
					break;
				case FILE_Transfer:
					handleFileTransMessage( m_socket.getInputStream() );
					break;
				case ADMIN_Login:
					handleAdminLogin( m_socket.getInputStream() );
					break;
				case ADMIN_PunishList:
					handleAdminPunishList();
					break;
				case ADMIN_PunishUser:
				case ADMIN_RemovePunishment:
					handleAdminPunish( header, m_socket.getInputStream() );
					break;
				default:
					System.out.println("problem header" );
					break;
				}
				

			} catch( SocketTimeoutException timeOutEx ){
				//socket timed out on read, continue onward
			} catch (IOException e) {
				System.out.println( "ERROR: Handling streams for user " + 
						m_user.getUserName() + " at: " + 
						m_socket.getInetAddress().getHostAddress() );
				
				ServerMessage errorMsg = 
						new ServerMessage(MessageHeader.SERVER_Error);
				
				errorMsg.setMessage("ERROR: Unable to process message input." 
						+ e.getMessage());
				terminateConnection( errorMsg );
			}
		}
			
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the thread's user
	 * @return <code>User</code>, the thread's user object
	 * @author Andrzej Brynczka
	 */
	public User getUser(){
		return m_user;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Add a message to this thread's queue, ensuring that it gets sent to
	 * this thread's user.
	 * @param a_msg Message, the message to send to this user
	 */
	public synchronized void addMessageToQueue(Message a_msg){
		m_messagesToReceive.add( a_msg );
	}
		
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Sends the given message to this client.
	 * 
	 * @param a_msg <code>Message</code>, the message to send to the client
	 * @throws IOException if an error occurs during writing to the socket
	 * @author Andrzej Brynczka
	 */
	private void sendMessage(Message a_msg) throws IOException{
		a_msg.sendMessage( m_socket.getOutputStream() );
		m_socket.getOutputStream().flush();
	}
	
	/**
	 * Creates a <code>UserListMessage</code>, to be sent to this thread's
	 * user, that contains the entire list of connected users.
	 * @return <code>UserListMessage</code> containing the list of connected
	 * users
	 * @author Andrzej Brynczka
	 */
	private UserListMessage createUserListMessage(){
		//create the message
		UserListMessage ulMsg = new UserListMessage(
				MessageHeader.SERVER_UserList, m_user.getUserName());
		
		//add the list of users
		ulMsg.addUsers( m_dispatcher.getUserCollection() );
		return ulMsg;
	}
	
	/**
	 * Terminate the connection by removing this thread from the dispatcher's
	 * 	user collection and killing this thread.
	 * 
	 * @param a_fileMessage Message, a final message to send to this user
	 * 	before termination( set to null if no message is to be sent)
	 * @author Andrzej Brynczka
	 */
	protected void terminateConnection(Message a_finalMessage){
		System.out.println("Terminating connection to " 
				+ m_socket.getInetAddress().getHostAddress());
		
		//send a final message, if provided
		if( a_finalMessage != null){
			try{
				sendMessage(a_finalMessage);
			}catch(IOException e){
				System.out.println("Failed to send final message to "
						+ m_socket.getInetAddress().getHostAddress());
			}
		}
		
		//remove the user from the dispatcher's table, if it exists
		if( m_user != null ){
			m_dispatcher.removeUser( m_user.getUserName() );
		}
		
		//kill the thread
		this.interrupt();
		
		//close the connection
		try {
			m_socket.close();
		} catch (IOException e) {
			//need to close connection in any case
		}
	}
	
	/**
	 * Handle the this user's attempt to login. If the login is successful,
	 * the user receives a login message and a list of connected users.
	 * Otherwise, an appropriate login message is sent back with information
	 * on the failure.
	 * @param a_inStream InputStream, the stream from which to read the user's
	 * 	login message containing the login details
	 * @throws IOException if an error occurs when reading from the stream
	 * @author Andrzej Brynczka
	 */
	private void handleLogIn(InputStream a_inStream) throws IOException{
		//create a basic login message, ready to be modified and sent
		//to the user upon an error during login
		LoginMessage loginMsg = new LoginMessage(
				MessageHeader.LOGIN, LoginCondition.SUCCESS);
		
		//********************************************************
		//Check that the IP is not already logged into the server
		//********************************************************
		System.out.println("Attempting to handle login");
		System.out.println("Checking multilog and ip");
		if( m_server.allowMultiLogin() == false && 
				m_dispatcher.existsIP(m_socket.getInetAddress()) == true){

			//IP is already logged in. Send the error msg to user and exit
			loginMsg.setCondition( LoginCondition.FAILURE_MultiLogin );
			
			terminateConnection( loginMsg );
			return;
		}

		//********************************************************
		//Accept client's username
		//********************************************************
		//get the size of the client's username
		System.out.println("Reading size of username...");
		int size = readInt( a_inStream );
		System.out.println("Size of username: " + size);
		
		//check that the size of the username is valid
		if( size > ServerData.MAX_NAME_SIZE ){
			//Size of user name is too large
			
			loginMsg.setCondition( LoginCondition.FAILURE_UsernameTooLong );
			terminateConnection( loginMsg );
			return;
		}
		
		//get the client's username
		System.out.println("Reading username...");
		String userName = readString( a_inStream, size );
		System.out.println("Username: " + userName);
		
		//check that the name has no spaces
		if( userName.indexOf(' ') != -1 ){
			loginMsg.setCondition( 
					LoginCondition.FAILURE_UsernameInvalidCharacters );
			terminateConnection( loginMsg );
		}
			
		//check if a connected user already has the given username
		if( m_dispatcher.existsUserName( userName ) ){
			
			//Username is already in use
			loginMsg.setCondition( LoginCondition.FAILURE_UsernameInUse );
			terminateConnection( loginMsg );
			return;
		}
		//********************************************************
		//Accept the server password
		//********************************************************
		//get the size of the password
		System.out.println("Reading size of server password...");
		size = readInt( a_inStream );
		System.out.println("Size of password: " + size);
	
		//check that the size is valid
		if( size > ServerData.MAX_NAME_SIZE ){
			//Password size too large
			
			loginMsg.setCondition( LoginCondition.FAILURE_ServerPassTooLong );
			terminateConnection( loginMsg );
			return;
		}
		
		//Read the password
		System.out.println("Reading server password...");
		String serverPassword = readString( a_inStream, size );
		System.out.println("Server password: " + serverPassword);
		
		//Check that the password is correct
		if( !serverPassword.equals((String) m_server.getServerPass()) ){
			//incorrect server password provided
			
			loginMsg.setCondition( LoginCondition.FAILURE_ServerPassInvalid );
			terminateConnection( loginMsg );
			return;
		}

		//********************************************************
		//Check if the client has any prior punishment still in effect
		//********************************************************
		
		//check client's ban status
		try {
			System.out.println("Checking ban status...");
			if( m_server.isBanned( m_socket.getInetAddress() ) ){
				//This IP is banned
				System.out.println(m_socket.getInetAddress().getHostAddress() 
						+ " is banned.");
				
				loginMsg.setCondition( LoginCondition.FAILURE_IPBanned );
				terminateConnection( loginMsg );
				return;
			}
			
		} catch (SQLException | NullPointerException e) {
			System.out.println( e.getMessage() );
			System.out.println("Unable to determine ban status of client:"
					+ m_socket.getInetAddress().getHostAddress() );
			System.out.println("Client was provided access to server.");
		}
		
		//check client's mute status
		boolean muted = false;
		try{
			System.out.println("Checking mute status...");
			if( m_server.isMuted( m_socket.getInetAddress() ) ){
				//This IP is muted
				System.out.println(m_socket.getInetAddress().getHostAddress() 
						+ " is muted.");
				muted = true;
				
				//let the client know it is muted
				loginMsg.setCondition( LoginCondition.SUCCESS_Muted );
			}
		} catch (SQLException | NullPointerException e) {
			System.out.println( e.getMessage() );
			System.out.println("Unable to determine mute status of client:"
					+ m_socket.getInetAddress().getHostAddress() );
			System.out.println("Client will be free to speak.");
		}
		
		//********************************************************
		//Create the new user and add it to the dispatcher's records
		//********************************************************
		m_user = new User(userName, m_socket, muted, false);
		m_dispatcher.addUser(userName, this);
		
		//Alert the client that it is now logged-in
		sendMessage( loginMsg );
		
		//send the list of connected users
		UserListMessage userListMsg = createUserListMessage();
		sendMessage( userListMsg );
	}
	
	/**
	 * Create a <code>RegularChatMessage</code> object from the incoming 
	 * 	message and send it to the dispatcher.
	 * @param a_inStream <code>InputStream</code>, the stream to the client's
	 * 	socket from which the message is to be read
	 * @throws IOException if an error occurs when reading from the stream
	 */
	private void handleRegChatMessage(InputStream a_inStream) 
			throws IOException {

		System.out.println("in chat handler");
		
		//read the sender name
		int sizeOfSenderName = readInt( a_inStream );
		System.out.println("read size of name: " + sizeOfSenderName);
		String sender = readString( a_inStream, sizeOfSenderName );
		System.out.println("read name:" + sender);
		
		//read the int that specifies the size of the message
		int sizeOfMessage = readInt( a_inStream );
		System.out.println( "read size of message: " + sizeOfMessage);
		//read the chat message
		String message = readString( a_inStream, sizeOfMessage );
		System.out.println("read message: " + message );
		
		if( m_user.isMuted() ){
			//user is muted, don't send its messages
			return;
		}
		
		//create the new message and provide it to the 
		//dispatcher to send to others
		ChatMessage chatMsg = new ChatMessage(
				MessageHeader.REG_SendChatMess, 
				message, 
				sender);
	
		System.out.println("added message");
		m_dispatcher.addMessage( chatMsg );
	}
	
	/**
	 * Create a <code>PrivateChatMessage</code> object from the incoming
	 * 	message and sent it to the dispatcher.
	 * 
	 * @param a_inStream <code>InputStream</code>, the stream to the client's
	 * 	socket from which the message is to be read
	 * @throws IOException if an error occurs when attempting to read from
	 * 	the stream
	 * @author Andrzej Brynczka
	 */
	private void handlePrivMessage(InputStream a_inStream) 
			throws IOException{

		//read the sender name
		int size = readInt( a_inStream );
		String sender = readString( a_inStream, size );
		
		//get receiver name
		size = readInt( a_inStream );
		String receiver = readString( a_inStream, size );
		
		//get the message
		size = readInt( a_inStream );
		String message = readString( a_inStream, size );
		
		if( m_user.isMuted() ){
			//user is muted, don't send its messages
			return;
		}
		
		//pass this message to the dispatcher, which will aid in sending
		//it to the receiver
		PrivateChatMessage privMsg = new PrivateChatMessage(
				Message.MessageHeader.PRIV_SendChatMess, 
				message, 
				sender, 
				receiver);
		
		m_dispatcher.addMessage( privMsg );
	}
	
	
	/**
	 * Organizes the handling of all file transfer messages
	 * 
	 * @param a_inStream <code>InputStream</code>, the stream to read from
	 * @throws IOException if an error occurs when reading from the stream
	 * @author Andrzej Brynczka
	 */
	private void handleFileTransMessage(InputStream a_inStream) 
			throws IOException { 
		
		//get the transfer stage to determine the point of transfer
		byte stage = (byte) a_inStream.read();

		System.out.println("File transmission message, stage: " 
				+ TransferStage.fromNum(stage).toString());
		//read the message based on its current stage in the transfer
		switch( TransferStage.fromNum( stage ) ){
		case STAGE1_RequestFromSender:
			_FileTranRequest( a_inStream );
			break;
		case STAGE2_ResponseToRequest:
		case STAGE4_TransEndResponse:
			_FileTransResponse( a_inStream, TransferStage.fromNum( stage ));
			break;
		case STAGE3_DataTransmission:
			_FileTransData( a_inStream );
			break;
		default:
			break;
		}
	}
	
	/**
	 * Creates a <code>FileTransRequestMessage</code> object from the incoming
	 * 	message and sends it to the dispatcher to ensure that the proper
	 * 	target receives it.
	 * 
	 * @param a_inStream <code>InputStream</code>, the stream to the client's
	 * 	socket from which the message is to be read
	 * @throws IOException if an error occurs when reading from the stream
	 * @author Andrzej Brynczka
	 */
	private void _FileTranRequest(InputStream a_inStream) 
			throws IOException {

		System.out.println("in trans request");
		
		//read the current file transfer ID( just 0, ID will be generated next )
		int transferID = readInt( a_inStream );
		System.out.println(" id " + transferID );
		
		//read the receiver's username
		int size = readInt( a_inStream );
		System.out.println(" Size " + size);
		
		String receiver = readString( a_inStream, size );
		System.out.println(" receiver " + receiver );
		
		//read the file name
		size = readInt( a_inStream );
		System.out.println(" Size " + size);
		
		String fileName = readString( a_inStream, size );
		System.out.println(" filename " + fileName );
		
		//read the int that specifies size of the file
		int fileSize = readInt( a_inStream );
		System.out.println(" fileSize " + fileSize);
		
		//client cannot send files to itself
		if( receiver.equals( m_user.getUserName() ) ){
			ServerMessage servMsg = 
					new ServerMessage(MessageHeader.SERVER_Error);
			servMsg.setMessage("ERROR: Unable to send file to yourself.");
			addMessageToQueue( servMsg );
			return;
		}
		System.out.println("got request to: " + receiver + " with " 
				+ fileName + " size "+ fileSize);
		
		//create the new file message object and give it to the dispatcher
		int newTransferID = FileTransferMessage.generateTransferID();
		try {
			FileTransRequestMessage fileTranMessage;
			fileTranMessage = new FileTransRequestMessage(
					MessageHeader.FILE_Transfer, 
					newTransferID,
					TransferStage.STAGE1_RequestFromSender, 
					m_user.getUserName(), 
					receiver,
					fileName, 
					fileSize);
			
			m_dispatcher.addMessage( fileTranMessage );	
		} catch (Exception e) {
			// FILE SIZE INVALID
			
			//send the TransferStage.ERROR message to the sender to indicate
			//an error
			FileTransErrorMessage errorMsg;
			errorMsg = new FileTransErrorMessage(MessageHeader.FILE_Transfer,
					newTransferID, 
					TransferStage.ERROR, 
					m_user.getUserName(), 
					receiver, 
					fileName);
			errorMsg.setErrorMsg("Unsupported file size");
			
			m_dispatcher.addMessage( errorMsg );
		}
	
	}

	/**
	 * Create a <code>FileTransResponseMessage</code> object for the incoming
	 * 	message and send it to the dispatcher.
	 * 
	 * @param a_inStream <code>InputStream</code>, the stream to the client's
	 * 	socket from which the message is to be read
	 * @throws IOException if an error occurs when reading from the stream
	 * @author Andrzej Brynczka
	 */
	private void _FileTransResponse(InputStream a_inStream, 
			TransferStage a_stage) throws IOException{
	
		System.out.println("got response");
		//read the current file transfer ID
		int transferID = readInt( a_inStream );
		
		
		//read the receiver's username
		int size = readInt( a_inStream );
		String receiver = readString( a_inStream, size );
		
		//get the filename of the file to be transfered
		size = readInt( a_inStream );
		String fileName = readString( a_inStream, size );
		
		//read the byte that specifies the response
		byte response = (byte) a_inStream.read();
		
	
		System.out.println("response: " + response );
		//create the new file message object and give it to the dispatcher
		FileTransResponseMessage tranMessage = new FileTransResponseMessage(
				MessageHeader.FILE_Transfer, 
				transferID,
				a_stage, 
				m_user.getUserName(), 
				receiver, 
				fileName, 
				MessageResponse.fromByte(response) );

		m_dispatcher.addMessage( tranMessage );
	}
	
	/**
	 * Creates the <code>FileTransDataMessage</code> object for the incoming 
	 * 	message and adds it to the message dispatcher's message queue.
	 * 
	 * @param a_inStream <code>InputStream</code>, the stream to the client's
	 * 	socket from which the message is to be read
	 * @throws IOException if an error occurs when reading from the stream
	 * @author Andrzej Brynczka
	 */
	private void _FileTransData(InputStream a_inStream) 
			throws IOException{
		System.out.println("sending data");
		
		//read the int that specifies the current file transfer ID
		int transferID = readInt( a_inStream );
	
		//read the receiver's username
		int size = readInt( a_inStream );
		String receiver = readString( a_inStream, size );
		
		//read the file name
		size = readInt( a_inStream );
		String fileName = readString( a_inStream, size );
			
		//get the int specifying the number of the current data segment
		int curSeg = readInt( a_inStream );
		
		//get the int specifying the total number of data segments
		int totSeg = readInt( a_inStream );
		
		//get the int specifying the size of the current segment
		int segSize = readInt( a_inStream );
		
		//get the current data segment
		byte[] messageData = readBytes( a_inStream, segSize, segSize);
		
		System.out.println("Server got data segment " + curSeg);
		System.out.println("receiver : " + receiver +" segSize: " + segSize );
		
		//create the file transfer message
		try {
			FileTransDataMessage dataMessage = new FileTransDataMessage(
					MessageHeader.FILE_Transfer, 
					transferID,
					TransferStage.STAGE3_DataTransmission, 
					m_user.getUserName(), 
					receiver, 
					fileName, 
					curSeg, totSeg, segSize, 
					messageData);
			
			System.out.println("sent data");
			m_dispatcher.addMessage( dataMessage );
		} catch (Exception e) {
			// FILE SEGMENT ERROR
			
			//send the TransferStage.ERROR message to the sender to indicate
			//an error
			FileTransErrorMessage errorMsg;
			errorMsg = new FileTransErrorMessage(MessageHeader.FILE_Transfer,
					transferID, 
					TransferStage.ERROR, 
					m_user.getUserName(), 
					receiver, 
					fileName);
			errorMsg.setErrorMsg(e.getMessage());
			
			m_dispatcher.addMessage( errorMsg );
		}
	}
	
	/**
	 * Handle the user's attempt at logging in as an admin. An 
	 * AdminLoginMessage is sent back with the results of the attempt.
	 * 
	 * @param a_inStream InputStream, the stream from which to read the
	 * 	user's sent login details.
	 * @throws IOException if an error occurs when reading from the
	 *  provided stream
	 *  @author Andrzej Brynczka
	 */
	private void handleAdminLogin(InputStream a_inStream) throws IOException {
		System.out.println("reading admin login message");
		
		//read the password length
		int size = readInt( a_inStream );
		
		//read the provided password
		String password = readString( a_inStream, size);
	
		//determine if the password was valid
		MessageResponse passwordValidity;
		if( password.equals( m_server.getAdminPass() ) ){
			//validate the password
			passwordValidity = MessageResponse.Success;
			
			//set the client as an admin
			m_user.setAdmin( true );
		}
		else{
			//invalid password
			passwordValidity = MessageResponse.Failure;
		}
		
		//create the AdminLogin message with the provided data, 
		//and send back a response
		AdminLoginMessage adLoginMsg = new AdminLoginMessage(
				MessageHeader.ADMIN_Login, 
				m_user.getUserName(),
				password, 
				passwordValidity);
		
		m_dispatcher.addMessage( adLoginMsg );
	}
	
	/**
	 * Handles the user's admin punishment message. Acts out the
	 * message's punishment if the provided information is valid.
	 * 
	 * @param a_header MessageHeader, the message's header, which
	 * details the type of punishment to be enacted
	 * @param a_inStream InputStream, the stream from which the 
	 * 	message details are to be read
	 * @throws IOException if an error occurs when reading from
	 * 	the message stream
	 * @author Andrzej Brynczka
	 */
	private void handleAdminPunish(MessageHeader a_header, 
			InputStream a_inStream) throws IOException {

		System.out.println("handling admin punish message");
		
		
		//get the punishment's 4-character reference code
		String punishCode = readString( a_inStream, 4);
			
		//get the punishment direction's byte-based code
		byte directionCode = (byte) a_inStream.read();
		
		//get the name of the name of the admin that requested the punishment
		int size = readInt( a_inStream );
		String adminName = readString( a_inStream, size );
		
		//get the name of the target
		size = readInt( a_inStream );
		String targetName = readString( a_inStream, size );
		
		//get the IP of the target if applicable
		String targetIP = null;
		if( a_header == MessageHeader.ADMIN_RemovePunishment ){
			size = readInt( a_inStream );
			targetIP = readString( a_inStream, size );
		}
		
		//check if the user sending the request is an admin
		if( !m_user.isAdmin() ){
			//Not an admin.
			//Should not have had access to this message, ignore it.
			return;
		}
		
		//get the enumerations from the provided codes
		Punishment punishment = Punishment.fromString( punishCode );
		Direction direction = Direction.fromByte( directionCode );
		
		//create the punishment message to be acted on and responded to
		AdminPunishMessage punMsg = new AdminPunishMessage(
				a_header, 
				punishment, 
				direction, 
				adminName, 
				targetName,
				targetIP);
		

		if( a_header == MessageHeader.ADMIN_PunishUser ){
			//get the target's ip to check against the database of punishments
			targetIP = m_dispatcher.getIP( punMsg.getTargetName() )
							.getHostAddress();
		}
		
		//determine if the request can be acted on by checking if the IP
		//is valid, then act on it if possible
		if( targetIP == null ){
			//target IP address not found, cannot act on the request
			punMsg.setCommitStatus( false );
		}
		else{
			//target IP found, attempt to put the punishment into effect
			
			try {	
				if( punishment == Punishment.BAN ){
					//set the ban status(auto-kicked from server if being set)
					m_server.saveBanStatus( targetName, targetIP, direction);
				}
				else if( punishment == Punishment.MUTE ){
					//set the target's MUTE status
					m_server.saveMuteStatus( targetName, targetIP, direction );
				}
				else{
					//last remaining punishment is KICK, can only be set
					m_server.kickUser( targetName );
				}
				
				//punishment acted on, modify message to reflect the change
				punMsg.setCommitStatus( true );
				
			} catch (SQLException e) {
				//error on setting the punishment
				System.out.println("ERROR: Attempting to set punishment.");
				System.out.println( e.getMessage() );
			}
		}
		
		//send back a response indicating whether or not the punishment was
		//committed by the server
		m_dispatcher.addMessage( punMsg );
	}
	
	/**
	 * Use when the user has requested a list of punished users. Queries
	 * the server for the list of users and sends it back, or an
	 * error message indicating the failure.
	 * 
	 * @author Andrzej Brynczka
	 */
	private void handleAdminPunishList(){
		try {
			//create the message of punished users
			AdminPunishListMessage aplMsg = new AdminPunishListMessage( 
					MessageHeader.ADMIN_PunishList, m_server.getAllPunished());
			
			//send the message to this user
			System.out.println("Got all punished users, sending message");
			addMessageToQueue( aplMsg );
		} catch (SQLException e) {
			System.out.println("Unable to access punishment list.");
			
			//notify the user client of the error
			ServerMessage servMsg = new ServerMessage(
					MessageHeader.SERVER_Error);
			servMsg.setMessage("ERROR: Unable to access punishment list.");
			addMessageToQueue( servMsg );
		}
	}
	
	/**
	 * Reads an int from the given stream. 
	 * 
	 * @param a_stream <code>InputStream</code>, the stream to read from
	 * @return <code>int</code>, an int read from the stream
	 * @throws IOException if an error occurs when reading from the stream
	 * @author Andrzej Brynczka
	 */
	private int readInt(InputStream a_stream) throws IOException{
		DataInputStream inData = new DataInputStream( a_stream );
		
		return inData.readInt();
	}
	
	/**
	 * Reads a string from the input stream. 
	 * 
	 * @param a_stream <code>InputStream</code>, the stream to read from
	 * @param a_sizeOfString <code>int</code>, the length(# of chars) of 
	 * 	the string
	 * @return <code>String</code>, the string read from the stream
	 * @throws IOException if the stream is closed when attempting to read 
	 * 	from it, or an error occurs during the read
	 * @author Andrzej Brynczka
	 */
	private String readString(InputStream a_stream, int a_sizeOfString) 
			throws IOException{
		
		byte[] readData = readBytes(a_stream, a_sizeOfString, a_sizeOfString);
		
		String readString = new String( readData );
		return readString;
	}
	
	/**
	 * Reads an array of bytes from the given stream.
	 * 
	 * @param a_stream <code>InputStream</code>, the stream to read from
	 * @param a_bytesToRead <code>int</code>, the number of bytes to read
	 * @param a_bufferSize <code>int</code>, the size to set the read buffer
	 * @return <code>byte[]</code>, the bytes read from the stream
	 * @throws IOException if an error occurs when reading from the stream or 
	 *  if the stream is closed when attempting to read from it
	 * @author Andrzej Brynczka
	 */
	private byte[] readBytes(InputStream a_stream, int a_bytesToRead, 
			int a_bufferSize) throws IOException{
		byte[] data = new byte[a_bufferSize];//the data read from the stream
		int totalRead = 0;//number of bytes read in total
		int currRead = 0;//number of bytes read in current read
		
		do{
			//read the desired number of bytes
			currRead = a_stream.read(data, 
					totalRead, a_bytesToRead - totalRead);
		
			//check that the read was valid
			if( currRead == -1 ){
				throw new IOException("Input stream was closed with EOF");
			}
			
			//update the total number of bytes read
			totalRead += currRead;
			
		}while( totalRead != a_bytesToRead );
		
		return data;
	}
}

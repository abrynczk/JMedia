package client;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import javafx.application.Platform;


import client.messages.AdminLoginMessage;
import client.messages.AdminPunishMessage;
import client.messages.FileTransResponseMessage;
import client.messages.LoginMessage;
import client.messages.LoginMessage.LoginCondition;
import client.messages.PunishmentInfo;
import client.messages.PunishmentInfo.Direction;
import client.messages.PunishmentInfo.Punishment;
import client.messages.ChatMessage;
import client.messages.Message;
import client.messages.ServerMessage;
import client.messages.Message.MessageHeader;
import client.messages.Message.MessageResponse;
import client.messages.PrivateChatMessage;
import client.messages.FileTransferMessage.TransferStage;

/**
 * Class that handles the process of receiving messages from the
 * 	server and acting on them.
 * @author Andrzej Brynczka
 *
 */
public class ClientReader extends Thread{
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** Reference to the main client object */
	private Client m_client;
	
	/** Input stream from which messages from the server are read */
	private InputStream m_inStream;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * Constructs a ready-to-read ClientReader object
	 * @param a_client Client, reference to the main client object
	 * @throws IOException if an error occurs when attempting to 
	 * 	get the InputStream from the client's connected socket
	 * @author Andrzej Brynczka
	 */
	ClientReader(Client a_client) throws IOException{
		m_client = a_client;
		m_inStream = a_client.getSocket().getInputStream();
	}

	/**
	 * Reads from the socket's input stream until forced to shutdown,
	 * sitting on the read call until a valid header is read. Once a valid
	 * message is received, the message is processed and the next message
	 * read(or waited on).
	 */
	@Override
	public void run(){

		//read messages from server	
		String headerCode;
		MessageHeader header;
		while( !isInterrupted() ){
			try{
				//read the header first
				headerCode = readString( m_inStream, 4 );
				header = MessageHeader.fromString( headerCode );
				System.out.println(" got a message: " + headerCode );
				
				//read the message based on the header code
				switch( header ){
				case LOGIN:
					handleLogin();
					break;
				case REG_SendChatMess:
					handleRegularChatMessage();
					break;
				case PRIV_SendChatMess:
					handlePrivateMessage();
					break;
				case FILE_Transfer:
					handleFileTransferMessage();
					break;
				case ADMIN_Login:
					handleAdminLoginMessage();//response to admin login request
					break;
				case ADMIN_PunishList:
					handleAdminPunishListMessage();
					break;
				case ADMIN_PunishUser:
				case ADMIN_RemovePunishment:
					handleAdminPunishMessage( header );
					break;
				case SERVER_UserList:
					handleUserList();
					break;
				case SERVER_AddNewUser:
					handleAddUser();
					break;
				case SERVER_RemoveUser:
					handleRemoveUser();
					break;
				case SERVER_Error:
					handleServerErrorMessage();
					break;
				case SERVER_Kicked:
					handleServerKickedMessage();
					break;
				default:
					System.out.println("Bad header from server");
					break;
				}
			}
			catch(IOException e){
				System.out.println("Error reading stream in client reader: " 
						+ e.getMessage());
				break;
			}
		}
		
		//server disconnect or error, done reading
		System.out.println("Exited clientreader");
		m_client.terminateConnection();
	}
	
	/**
	 * Convenience function for use in simplifying the sending 
	 * 	of messages to client
	 * @param a_message Message, the message to add to the client's 
	 * 	list of messages to display
	 */
	private void addChatMessageToClient(final Message a_message){
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.addChatMessageToList( a_message );
			}
		});
	}
	
	/**
	 * Processes the login response message received from the server.
	 * Provides feedback to the client based on the outcome.
	 * 
	 * @throws IOException if an error occurs when reading from the
	 * 	socket's input stream
	 */
	private void handleLogin() throws IOException {
		
		//read the login condition
		LoginCondition condition = LoginCondition.fromByte( 
				(byte) m_inStream.read() );
		
		//declare variables for use in several cases
		final LoginMessage loginMsg = 
				new LoginMessage(MessageHeader.LOGIN, condition);		
		final String loginFeedback;
		
		//determine the success of the login attempt;
		switch( condition ){
		case SUCCESS_Muted:
			addChatMessageToClient( loginMsg );
			
			Platform.runLater( new Runnable() {
				@Override
				public void run(){
					m_client.setLoggedIn( true );
					m_client.setMute( true );
					
					System.out.println("Logged into server, but are muted!");
				}
			});

			return;
		case SUCCESS:
			//success, send the message to the client to be displayed
			addChatMessageToClient( loginMsg );
			
			//aware the client of the loggin
			Platform.runLater( new Runnable() {
				@Override
				public void run(){
					m_client.setLoggedIn( true );			
					System.out.println("Logged into server! \n Welcome");

				}
			});

			return;
		case FAILURE_MultiLogin:
			loginFeedback = "ERROR: This IP address is already logged in.";
			break;
		case FAILURE_UsernameTooLong:
			loginFeedback = "ERROR: The provided username is too long.";
			break;
		case FAILURE_UsernameInvalidCharacters:
			loginFeedback = "ERROR: The provided username uses invalid" 
					+ " characters. \n Do not use: spaces";
			break;
		case FAILURE_UsernameInUse:
			loginFeedback = "ERROR: The provided username is already in use.";
			break;
		case FAILURE_ServerPassTooLong:
			loginFeedback = "ERROR: The provided server password is too long.";
			break;
		case FAILURE_ServerPassInvalid:
			loginFeedback = "ERROR: The provided server password is incorrect.";
			break;
		case FAILURE_IPBanned:
			loginFeedback = "ERROR: This IP address is banned from the server.";
			break;
		default:
			loginFeedback = "";
			break;
		}	
		
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.setFailedLoginFeedback( loginFeedback );
			}
		});
	}
	
	/**
	 * Processes a regular chat message received from the server,
	 * 	organizing its data and providing it to the client.
	 * 
	 * @throws IOException if an error occurs when reading from
	 * 	the socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void handleRegularChatMessage() throws IOException{
		//read the sender name
		int sizeOfSenderName = readInt( m_inStream );
		String sender = readString( m_inStream, sizeOfSenderName );
		
		//read the int that specifies the size of the message
		int sizeOfMessage = readInt( m_inStream );
		
		//read the chat message
		String message = readString( m_inStream, sizeOfMessage );
		
		//dispose of the message if the client decided to
		//ignore the sender
		if( m_client.isIgnoring( sender ) ){
			return;
		}
		
		//create the new message and provide it to the 
		//dispatcher to send to others
		final ChatMessage chatMsg = new ChatMessage(
				Message.MessageHeader.REG_SendChatMess, 
				message, 
				sender);
	
		addChatMessageToClient( chatMsg );	
	}

	/**
	 * Processes private messages sent from the server.
	 * 	Organizes the private message data and notifies the client
	 * 	of the special message.
	 * 
	 * @throws IOException if an error occurs when reading from the
	 * 	socket input stream
	 * @author Andrzej Brynczka
	 */
	private void handlePrivateMessage() throws IOException{
		//read the sender name
		int size = readInt( m_inStream );
		String sender = readString( m_inStream, size );
		
		//get receiver name(this client)
		size = readInt( m_inStream );
		String receiver = readString( m_inStream, size );
		
		//get the message
		size = readInt( m_inStream );
		String message = readString( m_inStream, size );
		
		if( m_client.isIgnorePM() || m_client.isIgnoring( sender ) ){
			return;
		}
		
		final PrivateChatMessage pMsg = new PrivateChatMessage(
				Message.MessageHeader.PRIV_SendChatMess, 
				message, 
				sender, 
				receiver);
		
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.addChatMessageToList( pMsg );
				m_client.addMessageToPrivateChatList( pMsg );
			}
		});
		
	}
	
	/**
	 * Distributes the file transfer messages to more specific
	 * 	helper functions based upon the messages' TransferStages.
	 * 	
	 * @throws IOException if an error occurs when reading from
	 * 	the socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void handleFileTransferMessage() throws IOException{

		//get the transfer stage to determine the point of transfer
		byte stage = (byte) m_inStream.read();

		//get the transfer ID
		int transID = readInt( m_inStream );
	
		System.out.println("got stage:" + TransferStage.fromNum( stage ));
		//read the message based on its current stage in the transfer
		switch( TransferStage.fromNum( stage ) )
		{
			case STAGE1_RequestFromSender:
				_FileTranRequest( transID );
				break;
			case STAGE2_ResponseToRequest:
			case STAGE4_TransEndResponse:
				_FileTransResponse( transID, stage );
				break;
			case STAGE3_DataTransmission:
				_FileTransData( transID );
				break;
			default:
				break;
		}

	}
	
	/**
	 * Processes FileTransferRequest messages, maintaining information
	 * 	on another user's desire to engage in a file transfer.
	 * @param a_transID int, the file transfer message's unique transfer id
	 * @throws IOException
	 */
	private void _FileTranRequest( int a_transID ) throws IOException{	
		//get the sender's username
		int size = readInt( m_inStream );
		String sender = readString( m_inStream, size );
		
		//get the file name
		size = readInt( m_inStream );
		String fileName = readString( m_inStream, size );
		
		//get the file size
		int fileSize = readInt( m_inStream );
		
		//create a new file ticket to keep track of this file's series of
		//transfer messages
		final FileTransferTicket fileTicket = new FileTransferTicket(a_transID, 
				TransferStage.STAGE1_RequestFromSender, fileName, 
				fileSize, sender, m_client.getUserName(), true);
		
		//check if the client is accepting file requests
		if( m_client.isIgnoreFileRequests() || m_client.isIgnoring( sender ) ){
			//client is ignoring file requests
			//send an automatic decline
			fileTicket.setTransferStage(TransferStage.STAGE2_ResponseToRequest);
			m_client.sendFileResponse( fileTicket, MessageResponse.Failure );
			return;
		}
		
		//aware the client of a new file request by adding the ticket 
		//to a designated tracking list
		fileTicket.setTransferStage( TransferStage.STAGE2_ResponseToRequest );
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.addResponseRequestTicketToList( fileTicket );
			}
		});
		
	}
	
	/**
	 * Processes the FileTransferResponse messages, which indicate
	 * 	successes/failures of transfer requests and data transfers.
	 * 
	 * @param a_transID int, the filetransfer message's unique transfer id
	 * @param a_stageCode byte, the message's byte-based TransferStage code
	 * @throws IOException if an error occurs when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void _FileTransResponse(
			final int a_transID, final byte a_stageCode ) throws IOException{
		System.out.println("got a response message");
		
		//get the sender's username
		int size = readInt( m_inStream );
		System.out.println(" size " + size );
		final String sender = readString( m_inStream, size );
		System.out.println("sender " + sender);
		
		//get the file name
		size = readInt( m_inStream );
		System.out.println(" size " + size );
		final String fileName = readString( m_inStream, size );
		System.out.println("filename " + fileName);
		
		//get the response code byte
		MessageResponse response = 
				MessageResponse.fromByte( (byte) m_inStream.read() );
		
		System.out.println(" got response " + response.toString() );
		
		if( response == MessageResponse.Failure ){
			//Request for a transfer was denied, or an error data 
			//transmission. End the series of transfers for this file.
				
			if( TransferStage.fromNum( a_stageCode ) == 
					TransferStage.STAGE2_ResponseToRequest){
				//the other client declined a file request
							
				Platform.runLater( new Runnable() {
					@Override
					public void run(){
						m_client.deletePendingFileTicketFromTransferTable( 
								sender, fileName) ;
					}
				});
				
			}
			else{
				//failure at transmission end(canceled the transmission)
				System.out.println("ended file transmission, deleting ticket");
				
				FileTransferTicket ticket = 
						m_client.getFileTicketFromTransferTable( a_transID );
				ticket.setTransferStage( TransferStage.STAGE5_Done );
				
				//delete the unfinished file, if applicable
				ticket.closeFileWriteStream();
				ticket.deleteFile();
				
				//kill the sending of the file, if applicable
				Platform.runLater( new Runnable() {
					@Override
					public void run(){
						m_client.deleteFileTicketFromTransferTable(a_transID);
						m_client.killFileDataSend( a_transID );
					}
				});
							
			}
				
		}
		else{//received a successful response to a file transfer ticket
			
			if( TransferStage.fromNum( a_stageCode ) == 
					TransferStage.STAGE2_ResponseToRequest ){
				//the message was a response to this client's transfer request
				
				//get the pending ticket made when the request was initiated 
				final FileTransferTicket ticket = 
						m_client.getPendingFileTicketFromTransferTable(
									sender, fileName);
				
				//modify the file's ticket to signal next stage 
				ticket.setTransferStage(TransferStage.STAGE3_DataTransmission);
				ticket.setTransferID( a_transID );
				
				//remove the ticket from the pending table and add it to the 
				//main ticket table
				Platform.runLater( new Runnable() {
					@Override
					public void run(){
						m_client.deletePendingFileTicketFromTransferTable(
								sender, fileName);
						m_client.addFileTicketToTransferTable( ticket );
						
						//prepare the file to be read from
						try {
							ticket.openFileToRead();
							
							//notify the user that the request was accepted
							m_client.addToSentTicketsList( ticket );
							m_client.beginFileDataSend( ticket );
						} catch (FileNotFoundException e) {
							System.out.println(ticket.getFileName() 
									+ " not found.");
							
							ticket.setTransferStage( 
									TransferStage.STAGE5_Done );
							ticket.setStatus( TransferStage.ERROR );
							m_client.sendFileResponse( ticket, 
									MessageResponse.Failure );
						}
					}
				});
	
			}
			else{//response == success, stagecode == TransferStage4_TransEnd
	
				//done sending file data, close the ticket and aware the user
				FileTransferTicket ticket = 
						m_client.getFileTicketFromTransferTable( a_transID );
				ticket.setTransferStage( TransferStage.STAGE5_Done );
				
				try{
					ticket.closeFileWriteStream();
				}
				catch(IOException e){
					System.out.println("Error on file close.");
				}
				
				System.out.println("completed file download");
				m_client.deleteFileTicketFromTransferTable( a_transID );
			}
		}	
		
		//prepare the received message to be displayed in the chat
		final FileTransResponseMessage ftrMsg = new FileTransResponseMessage(
				MessageHeader.FILE_Transfer, 
				a_transID, TransferStage.fromNum(a_stageCode), 
				sender, m_client.getUserName(), fileName, response);
		
		addChatMessageToClient( ftrMsg );	
	}
	
	/**
	 * Processes the FileTransData messages, holding file data sent
	 * 	by another user.
	 * 
	 * @param a_transID int, the message's unique transfer id
	 * @throws IOException if an error occurs when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void _FileTransData(int a_transID) throws IOException{	
		System.out.println("got a data message");
		//get the sender's username
		int size = readInt( m_inStream );
		String sender = readString( m_inStream, size );
		
		//get the file name
		size = readInt( m_inStream );
		String fileName = readString( m_inStream, size );
		
		//get the current data segment #
		final int currentDataSegment = readInt( m_inStream );
		
		//get the total number of segments
		final int totalSegments = readInt( m_inStream );
		
		//get the size of the current segment
		int sizeOfCurrenSeg = readInt( m_inStream );
		
		
		//get the file data segment
		byte[] data = readBytes(m_inStream, sizeOfCurrenSeg, sizeOfCurrenSeg);
	
		System.out.println("read the data message, writing to file");
		
		//update the file ticket and write the file data
		final FileTransferTicket ticket = 
				m_client.getFileTicketFromTransferTable( a_transID );
		if( ticket == null ){
			return;
		}
		ticket.setTotalDataSeg( totalSegments );
		ticket.setCurrentDataSegNum( currentDataSegment );
		ticket.writeData( sizeOfCurrenSeg , data );


		System.out.println("wrote to file segment " + currentDataSegment 
				+ " of" + totalSegments);
	}
	
	/**
	 * Process user list messages, which carry the names of the
	 * 	users connected to the server.
	 * 
	 * @throws IOException if an error occurs when reading from the
	 * 	socket input stream;
	 * @author Andrzej Brynczka
	 */
	private void handleUserList() throws IOException{
		//get the number of users
		int numOfUsers = readInt( m_inStream );

		//get the usernames
		final Vector<String> users = new Vector<String>( numOfUsers );
		
		for( int i = 0; i < numOfUsers; i++ ){
			int sizeOfName = readInt( m_inStream );
			String userName = readString( m_inStream, sizeOfName );
			users.add( userName );
		}
		
		System.out.println("got new user list from server: " + numOfUsers);
		
		//send the list to the main client thread
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.setUserList( users );
			}
		});
	}
	
	/**
	 * Processes the Server message's "AddNewUser" variant,
	 * 	containing the name of a newly connected user.
	 * 
	 * @throws IOException if an error occured when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void handleAddUser() throws IOException{
		//get the user's name
		int sizeOfName = readInt( m_inStream );
		final String name = readString( m_inStream, sizeOfName );
		
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.addToUserList( name );
			}
		});
	}
	
	/**
	 * Processes the Server message's remove-user variant,
	 * 	containing the name of the user that disconnected from the server.
	 * 
	 * 
	 * @throws IOException if an error occured when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void handleRemoveUser() throws IOException{
		//get the user's name
		int sizeOfName = readInt( m_inStream );
		final String name = readString( m_inStream, sizeOfName );
		
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.removeFromUserList( name );
				m_client.removeFromIgnoreList( name );
			}
		});
	}
	
	/**
	 * Processes the AdminLoginMessage's response from the server,
	 * 	indicating whether or not the client has logged in successfully.
	 * 
	 * @throws IOException if an error occured when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void handleAdminLoginMessage() throws IOException{
		//get the 1 byte response
		byte responseByte = (byte) m_inStream.read();
		
		final MessageResponse response = 
				MessageResponse.fromByte( responseByte );
		
		//set the user's admin status based on response
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				//notify the client and user of the attempt's success
				m_client.setAdminLoginSuccess( response );
				
				//notify the user of the outcome
				if( response == MessageResponse.Success ){
					AdminLoginMessage alMsg = new AdminLoginMessage(
							MessageHeader.ADMIN_Login, "", response);
					
					m_client.addChatMessageToList( alMsg );
				}
			}
		});

	}
	
	/**
	 * Processes the AdminPunishMessage, which carries information on
	 * 	an admin punishment and the punishment's success/failure.
	 * 
	 * @param a_header	MessageHeader, the message's header
	 * @throws IOException if an error occured when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void handleAdminPunishMessage(MessageHeader a_header) 
			throws IOException{
		
		System.out.println("handling admin punish message: " 
											+ a_header.toString() );
		//get the punishment's 4-character reference code
		String punishCode = readString( m_inStream, 4);
			
		//get the punishment direction's byte-based code
		byte directionCode = (byte) m_inStream.read();
		
		//get the name of the name of the admin that requested the punishment
		int size = readInt( m_inStream );
		String adminName = readString( m_inStream, size );
		
		//get the name of the target
		size = readInt( m_inStream );
		String targetName = readString( m_inStream, size );
		
		//get the target's IP if applicable
		String targetIP = null;
		if( a_header == MessageHeader.ADMIN_RemovePunishment ){
			size = readInt( m_inStream );
			targetIP = readString( m_inStream, size );
			
		}
		//get the byte-based status of the punishment request
		byte statusResponseCode = (byte) m_inStream.read();
		
		
		//get the enumerations from the provided codes
		MessageResponse statusResponse = 
				MessageResponse.fromByte( statusResponseCode );
		
		Punishment punishment = Punishment.fromString( punishCode );
		Direction direction = Direction.fromByte( directionCode );
		
		//create a message for simplified handling
		final AdminPunishMessage aprMsg = new AdminPunishMessage(
				a_header, 
				punishment, 
				direction, 
				adminName, 
				targetName,
				targetIP
				);
		aprMsg.setServerResponse( statusResponse );
		
		//determine the message's intent
		if( adminName.equals( m_client.getUserName() ) ){
			//this user initiated the response

			//modify the stored punishmentlist if it exists
			if( a_header == MessageHeader.ADMIN_RemovePunishment ){
				Platform.runLater( new Runnable() {
					@Override
					public void run(){
						m_client.removeFromPunishmentInfoList( aprMsg );
					}
				});
				
			}
		}
		else{
			//this user is being punished
			//only received if the punishment was successful

			if( punishment == Punishment.MUTE ){
				if( direction == Direction.SET_PUNISHMENT ){
					m_client.setMute( true );
				}
				else{
					m_client.setMute( false );
				}
			}
		}
		
		//display a message in the chat screen indicating the response
		addChatMessageToClient( aprMsg );
		
	}

	
	/**
	 * Processes the AdminPunishList message, which carries the
	 * 	names of all punished users and their punishments.
	 * 
	 * @throws IOException if an error occured when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	private void handleAdminPunishListMessage() throws IOException{
		System.out.println("received punishlist");
		//get the number of users
		int numOfUsers = readInt( m_inStream );
		
		//get each user's information
		final ArrayList<PunishmentInfo> punishedUsers = 
				new ArrayList<PunishmentInfo>( numOfUsers );
		
		int size;
		String name;
		String IP;
		Punishment punishment;
		for( int i = 0; i < numOfUsers; i++ ){
			
			//get the name
			size = readInt( m_inStream );
			name = readString( m_inStream, size );
			
			//get the IP
			size = readInt( m_inStream );
			IP = readString( m_inStream, size );
			
			//get the punishment code
			punishment = Punishment.fromString( readString( m_inStream, 4 ) );
			
			//create the new set of information and add it to the collection
			PunishmentInfo currentUser = 
					new PunishmentInfo(name, IP, punishment, null);
			
			punishedUsers.add( currentUser );	
		}
		
		System.out.println("setting punished users list");
		Platform.runLater( new Runnable() {
			@Override
			public void run(){
				m_client.setPunishmentInfoList( punishedUsers );
			}
		});
		
	}
	
	/**
	 * Processes the Server Error message - a simple notification
	 * 	of an error.
	 * 
	 * @throws IOException if an error occured when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	public void handleServerErrorMessage() throws IOException{
		//read the message and pass it to the client
		int size = readInt( m_inStream );
		String message = readString( m_inStream, size );
		
		ServerMessage servMsg = new ServerMessage(MessageHeader.SERVER_Error);
		servMsg.setMessage( message );
		
		addChatMessageToClient( servMsg );
	}
	
	/**
	 * Processes the SERVER_Kicked message, which notifies the user
	 * 	that he/she was kicked from the server.
	 * 
	 * @throws IOException if an error occured when reading from the
	 * 	socket's input stream
	 * @author Andrzej Brynczka
	 */
	public void handleServerKickedMessage() throws IOException{
		//read the message and pass it to the client
		int size = readInt( m_inStream );
		String message = readString( m_inStream, size );
		
		ServerMessage servMsg = new ServerMessage(MessageHeader.SERVER_Kicked);
		servMsg.setMessage( message );
		
		addChatMessageToClient( servMsg );
		
		//user was kicked, close the client
		m_client.terminateConnection();
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Reads an int from the given stream. 
	 * 
	 * @param a_stream <code>InputStream</code>, the stream to read from
	 * @return <code>int</code>, an int read from the stream
	 * @throws IOException if an error occurs when reading from the stream
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
	 */
	private String readString(InputStream a_stream, int a_sizeOfString) 
			throws IOException{
		
		byte[] readData = readBytes( a_stream, a_sizeOfString, a_sizeOfString);
		
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
	 */
	private byte[] readBytes(InputStream a_stream, int a_bytesToRead, 
			int a_bufferSize) throws IOException{
		byte[] data = new byte[a_bufferSize];//the data read from the stream
		int totalRead = 0;//number of bytes read in total
		int currRead = 0;//number of bytes read in current read
		
		//read from the stream until the desired number of bytes are read
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

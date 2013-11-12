JMedia
======

JavaFX based media library/player with chat service


Server Design Summary
=============================================================================
The media player chat server consists of two main packages: a framework that consists of necessary models and workers that carry about basic functionality, and the messages that transport data between the server and clients.

The framework consists of:

    -	DatabaseAccess – Handler of interaction between the server and the database, used for the storage and retrieval of administrator-requested punishments.

    	Punishment information is stored in a single table, SERVER_PUNISHMENTS. The table contains, on each entry: 
    		IP_ADDRESS	VARCHAR(150)
    		USERNAME	VARCHAR(30)
    		PUNISHMENT	VARCHAR(4)

    	The PUNISHMENT column contains the 4 byte character code associated to a specific punishment. (Code information is maintained in class PunishmentInfo; current codes are “0001” for a Kick, “0002” for a Mute, “0003” for a Ban)

    -	Server – Launches the UserHandler worker threads, DatabaseAccess instance, and the MessageDispatcher, then sits on the listening socket and waits for new connections

    -	ServerData – Handles loading and saving of initialization values  upon server startup, and serves as a container for those values during server execution.

    -	User class – Container for information on individual users that connect to the server. Holds their socket, username, and mute and admin status values.

    -	UserHandler – Thread that handles the reading/writing of data from/ to a socket associated to an individual user, with one being launched for every user that connects to the server.
    The thread maintains a queue of messages that are to be sent to its designated user.  It sits in loop, sending messages to its user until its queue becomes empty or it becomes aware of an incoming message to read.  
    The thread utilizes a 2 second timeout on the socket read, providing a balance to the possibly heavy write-based looping and providing the designated client time to respond.
    The UserHandler acts on the messages it receives, checking with the server for validity of login passwords, keeping track of admin/mute statuses, and sending messages to the MessageDispatcher when needed.

    -	MessageDispatcher – Thread designed to receive messages read by UserHandler threads and send them out to targeted clients, or all clients if required. 
    The dispatcher‘s main purposes is to receive a message from a UserHandler(likely read from its user’s socket) and send the given message to its target.  
    To do this, the dispatcher maintains a table with references to all connected UserHandler threads, allowing it to pass messages where needed.  
    Due to its knowledge of connected users, the dispatcher also provides the service of notifying UserHandlers of new and lost connections, and can provide threads with lists of known users.


The message package consists of:

    -	PunishmentInfo – Container for information on punishment options. Contains the Punishment enumeration which assigns a 4-byte code to every punishment.

    -	Message – The base message class.  It contains the MessageHeader enumeration that provides information on all known messages and their purpose.
    
    -	AdminLoginMessage – Message dedicated to administrator login attempts, with a MessageHeader code of “0800”.
    Contains 4 bytes(int) for the size of a provided password, followed by the password itself. 
    UserHandler responds with a message containing a single byte indicating success(MessageResponse enumeration); the response message shares the “0800” admin login message header.

    -	AdminPunishMessage – Message dedicated to admin punishment attempts, with a MessageHeader code of “0810”.
    Server receives the 4 bytes indicating the punishment(PunishmentInfo.Punishment), 1 byte indicating direction(SET punishment vs Remove punishment), the name of the admin who request the message and the name of the target.
    Client receives back the same message with same data(for identification), alongside an additional byte indicating success or failure.
      
      -	AdminPunishListMessage – Message sent to server indicating a request for the list of punished users.  MessageHeader = “0809”

    -	ChatMessage – a regular chat message to be received by all users. MessageHeader = “0100”.
    Contains the sender’s name and message data

    -	ConnectedUserMessage – Message to be sent to client’s indicating either a new connection to the server or user’s disconnection. 
    MessageHeader = “0906” if new user is added, “0907” if a user disconnected.  
    Only the affected user’s name follows the header.

    -	FileTransferMessage – Base Message for the file transfer series of messages. 
    
    Specifies the TransferStage enumeration that list information on exact points in a file transmission including:  
    
        o	Stage1 – the initial request
        o	Stage2 – the response to the original request
        o	Stage3 – the actual data transmission phase
        o	Stage4 – the transmission “end response”
        o	Stage5 – transmission is complete
        
        This base class also provides the static “generateTransferID()” function to be used by the server to generate unique transfer ids – new required id information to aid in identification of file transmissions.  
        The ID allows files transfers to be identified without need of examining file or user information, for so long as the ID is properly provided and maintained on each end of the transfer.

    -	FileTransRequestMessage – Contains information on the initial Stage1 of the file transfer
    -	FileTransResponseMessage – Contains information on the Stage2 and Stage4 phases of the file transfer, both acting as responses changes in the transmission process
    -	FileTransDataMessage – Contains information on the Stage3 phase of file transmission, involving the process of sending the file data between clients.
    -	FileTransErrorMessage – Contains basic information on the file transmission, focused more heavily on notifying client’s of an issue

    -	LoginMessage – The very first message sent to the server from a client – MessageHeader equal to “0001”. 
    Contains the LoginCondition enumeration, which assigns a single byte to various login conditions allowing for quicker identification of login failure when the one-byte response is sent back to the client( 5 bytes total with the 4 byte header).

    -	PrivateChatMessage – The chat message meant for a specified individual. MessageHeader equal to “0200”.  
    Sends same data as a regular chat message, with the addition of the name of the user to receive this message

    -	ServerMessage – The generic message containing only the name of the receiving client and a message.
    MessageHeader code depends on its use, though it its currently used primarily for codes “0970”(Kicked from server) and “0990”(Server Error).

    -	UserListMessage – The message containing a list usernames for all connected users, to be sent to a client upon initial login. MessageHeader “0905”.


Header overview:
=============================================================================

LOGIN MESSAGE	
	
	0001 – LoginMessage – send the necessary login information, or response back

REGULAR CHAT MESSAGES		(0100 - 0199)
  
  	0100 – ChatMessage – send a regular message to all
					
PRIVATE MESSAGES			(0200 - 0299)

	0200 – PrivateChatMessage – send a private message
					
FILE TRANSFER MESSAGES		(0300 - 0399)

	0300 – FileTransferMessage(and its sub messages)
					
ADMIN MESSAGES			(0800 - 0899)
	
	0800 – AdminLoginMessage – send the admin login info, or response back
	0809 – AdminPunishListMessage – send the list of punished users
  	0810 - AdminPunishMessage(set the punishment KICK,MUTE,BAN)
	0811 – AdminPunishMessage(remove the punishment) 
											
SERVER MESSAGES			(0900 - 0999)

	0905 – UserListMessage – send the list of users
	0906 – ConnectedUserMessage - New users connected
	0907 – ConnectedUserMessage - User disconnected
						
	0970 – ServerMessage - Kicked from server
	0999 – ServerMessage – Error

*Note: A header code may mean something slightly different to the Client than it does to the Server based entirely on the purpose of that header.
	
	Example: 
	To the server, an incoming message with header “0001” for LoginMessage signifies that a username and password will be provided after the header(int for username size -> the username -> an int for password size -> the password).
	To the client, however, an incoming message with header “0001” for LoginMessage signifies that a response to a previous login attempt is being returned. In this message, only a single byte of data is contained after the header – the LoginMessage.LoginCondition code indicating varying degrees of success or failure.


**Note 2: All of the messages mentioned here are utilized within the Client design, and will therefore not be mentioned again in the forthcoming Client summary.
 

Client Design Summary
=============================================================================
Whereas the Server design limited the reading and writing of an individual client socket to a single thread, in an attempt to keep the total thread count low while maintaining sufficient working speeds, the Client utilizes a more split approach.   There is no worry in maintaining multiple threads for such work on the client side, as the threat of overloading the program becomes severely diminished in the directly controlled environment. 

The framework consists of:

    -	Client – The overall manager of interaction with the server.   
    It accepts the server information from the user and uses it to make the connection, then launches the ClientWriter and ClientReader threads to handle the writing and reading from the socket.
    In the case of a need to shutdown, the client notifies each working thread of its need to stop and attempts to close the environment in a controlled manner.

    -	ClientWriter – The working thread that performs all of the writing of messages to the socket.  
    It sits in a loop on a wait call, waking when it is notified of a message being added to its queue. 
    It then sends the message to server and continues to write any remaining messages from its queue. 
    When the queue empties, the Writer returns to it block on a wait().

    -	ClientReader – The working thread that performs all of the reading of messages from the socket.  
    It sits on a loop within a read() call, waiting to read data from the server.  
    It uses the incoming message’s header to determine how it processes further data to be read.

    -	FileSender – The working thread that manages the sending of file data during file transmissions.  
    Each FileSender manages a single data transmission in its lifetime, and it communicates directly with the ClientWriter to send its data; the FileSender packages the file data into a file transfer message, then passes it to the ClientWriter’s queue to be sent.

    -	FileTransferTicket – Container for all data utilized during the file transfer process.  
    FileTransferTickets are kept within tables and lists in the main Client object and continually created, updated, or deleted based on their associated files’ transfer status.

    -	User – Container for basic user-oriented information including displayed username, mute status, and admin login success. 
    A single instance is contained within the main client object
 
 

Library Design Summary
=============================================================================
The media library splits up media data based upon media type, then stores and displays it accordingly. Due to the need of persistent data storage, the library makes great use of a database.
 
 
The framework consists of:
	
	-  DataAccess – Class meant to communicate with the database, providing functionality to retrieve, save, update, and delete all provided data. 
	
	It creates 5 tables:
	MUSIC		NAME		VARCHAR(250)
			ARTIST		VARCHAR(250)
			ALBUM		VARCHAR(100)
			LENGTH	BIGINT
			GENRE		VARCHAR(50)
			FILELOC		VARCHAR(500)		PRIMARY KEY
	
	VIDEO		NAME		VARCHAR(250)
			LENGTH	BIGINT
			GENRE		VARCHAR(50)
			FILELOC		VARCHAR(500)		PRIMARY KEY

	PLAYLIST	PLNAME	VARCHAR(250)		PRIMARY KEY
			TYPE		VARCHAR(25)

	MUSICPL		PLNAME	VARCHAR(250)		FOREIGN KEY ON PLAYLIST
			SONG		VARCHAR(250)
			FILELOC		VARCHAR(500)		FOREIGN KEY ON MUSIC

	VIDEOPL	PLNAME	VARCHAR(250)		FOREIGN KEY ON PLAYLIST
			VIDEO		VARCHAR(250)
			FILELOC		VARCHAR(500)		FOREIGN KEY ON VIDEO

	*Lengths are all stored as a number  of seconds
	**All foreign keys on playlist, video, and music  tables are meant to cascade and update and death, ensuring that any changes made to the main three tables are carried out amongst the others

	-  MediaItem – General container for media items, holding data on item name, length, and file path.

	-  Song – Specialized container for music based media. Contains additional artist name, album, and genre data.

	-  Video – Specialized container for video based media. Holds additional genre data.

	-  ObservablePlaylist – Specialized container for media items, adding the ability to easily move items up and down the list as desired for use in the “Now Playing” media list. Holds MediaItem objects in an ObservableList, allowing JavaFX components to bind to its contents to simplify the updating of visual elements.

	-  Library – Model for the media library, containing and managing the various lists of media and their contents, as well as media player types. Keeps track of the VLC directory path and processes the saving/loading of the file associated with keeping track of the location(once provided correctly by the user).


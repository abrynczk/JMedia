package server.framework;

import java.net.InetAddress;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import server.messages.AdminLoginMessage;
import server.messages.AdminPunishMessage;
import server.messages.ConnectedUserMessage;
import server.messages.FileTransferMessage;
import server.messages.Message;
import server.messages.Message.MessageHeader;
import server.messages.PrivateChatMessage;
import server.messages.UserListMessage;
import server.messages.ChatMessage;

/**
 * The class serving as a dispatcher of messages between clients on the server.
 * @author Andrzej Brynczka
 */
public class MessageDispatcher extends Thread{
	
	/**
	 * Hashtable container for user threads. 
	 * Paired on &lt;UserName, UserHandler&gt;.
	 */
	private Hashtable<String, UserHandler> m_users;
	private Queue<Message> m_messageList;
	
	/** Reference to the main server */
	private final Server m_server;
	
	/**
	 * Create a dispatcher for the given server, which accepts messages
	 * 	from its <code>UserHandler</code>'s and distributes them based on
	 * 	type among the clients.
	 * Used only within the public <code>getDispatcher()</code> to ensure 
	 * that only one dispatcher thread is active at a time.
	 */
	public MessageDispatcher(Server a_server){
		m_messageList = new LinkedList<Message>();
		m_users = new Hashtable<String, UserHandler>();
		m_server = a_server;
	}
	
	/**
	 * The dispatcher sits in a loop and waits for messages to be placed into
	 * its message list. When a message is added, the thread wakes up and
	 * sends the message to the users that make up its receiver list.
	 * @author Andrzej Brynczka
	 */
	@Override
	public void run(){
		try{
			while( !isInterrupted() ){	
					if( !m_messageList.isEmpty() ){			
						//retrieve the first message in the queue and send it
						System.out.println("Dispatcher about to send message");
						Message msg = m_messageList.poll();
						sendMessage( msg );			
						
					}
					else{
						synchronized( this ){
							//wait for more messages to be placed in queue
							wait();
						}
					}
			}
		}catch (InterruptedException e) {
			m_server.shutDownServer("Dispatcher interrupted, " 
					+ "shuting down server...");
		}
	}
	
	/**
	 * Dispatch a message to all client threads.
	 * 
	 * @param a_msg <code>Message</code>, the message to send to all client
	 * 	threads
	 * @author Andrzej Brynczka
	 */
	private void sendMessageToAll(Message a_msg){
		for(UserHandler userHandler : m_users.values() ){
			System.out.println("dispatcher sent regchat/userRemove/userAdd "
					+ " to userhandler");
			userHandler.addMessageToQueue( a_msg );
		}
	}
	
	/**
	 * Dispatch a message to all clients except to the client that sent the
	 * message. ( Unlike <code>sendMessageToAll(Message)</code> which sends the
	 * message to ALL client threads )
	 * @param a_msg Message, the message to send
	 * @author Andrzej Brynczka
	 */
	private void sendMessagesToAllButSelf(Message a_msg){
		String sender;
		//find the sender
		switch( a_msg.getHeader() ){
		case SERVER_RemoveUser:
		case SERVER_AddNewUser:
			sender = ((ConnectedUserMessage) a_msg).getUserName();
			break;
		case REG_SendChatMess:
			sender = ((ChatMessage) a_msg).getSenderName();
			break;
		default:
				return;
		}
		
		//send the message to all but the sender
		for(UserHandler userHandler : m_users.values() ){
			System.out.println("dispatcher sending user connect status");
			if( !userHandler.getUser().getUserName().equals( sender ) ){
				userHandler.addMessageToQueue( a_msg );
			}
		}
	}
	/**
	 * Dispatch a message to the message's designated receiver.
	 * 
	 * @param a_msg <code>Message</code>, a valid message with receiver 
	 * 	information(<code>PrivateChatMessage</code>, 
	 * 	<code>FileTransferMessage</code>, etc)
	 * @author Andrzej Brynczka
	 */
	private void sendMessage( Message a_msg ){
		String receiverName = "";
		UserHandler receiverThread = null;
		
		switch( a_msg.getHeader() ){
		case REG_SendChatMess:
			sendMessageToAll( a_msg );
			break;
		case PRIV_SendChatMess:
			receiverName = ((PrivateChatMessage) a_msg).getReceiverName();
			
			//send the message back to the sender as well, as private messages
			//are two-way conversations
			String senderName = ((PrivateChatMessage) a_msg).getSenderName();
			System.out.println("send pm to " + senderName 
					+ " and " + receiverName);
			
			//get the users' thread and add the message to their queues
			UserHandler senderThread = m_users.get( senderName );
			receiverThread = m_users.get( receiverName );
			if( senderThread == null || receiverThread == null ){
				break;
			}
			senderThread.addMessageToQueue( a_msg );
			receiverThread.addMessageToQueue( a_msg );
			break;
		case FILE_Transfer:
			//get the name of the user to send the message to
			receiverName = ((FileTransferMessage) a_msg).getReceiverName();
			
			//get the user's thread and add the message to its queue
			receiverThread = m_users.get( receiverName );
			if( receiverThread == null ){ 
				break;
			}
			
			receiverThread.addMessageToQueue( a_msg );
			break;
		case ADMIN_Login:
			//get the name of the user to send the response to
			receiverName = ((AdminLoginMessage) a_msg).getSenderName();
			
			//get the user's writer thread and add the message to its queue
			receiverThread = m_users.get( receiverName );
			if( receiverThread == null ){
				break;
			}
			
			receiverThread.addMessageToQueue( a_msg );
			break;
		case ADMIN_PunishUser:
		case ADMIN_RemovePunishment:
			AdminPunishMessage apMsg = (AdminPunishMessage) a_msg;
			if( apMsg.getCommitStatus() == true ){
				//punishment enacted, send the response to both the target
				//and the admin
				
				sendMessageToAll( a_msg );
			}
			else{
				//punishment not enacted, simply aware the admin of the failure
				
				receiverThread = m_users.get( apMsg.getAdminName() );
				if( receiverThread == null ){
					break;
				}
				receiverThread.addMessageToQueue(a_msg);
				
			}
			break;
		case SERVER_UserList:
			//send the list of connected users to the receiver
			receiverName = ((UserListMessage) a_msg).getReceiverName();
			receiverThread = m_users.get( receiverName );
			if( receiverThread == null ){ 
				break;
			}
			receiverThread.addMessageToQueue( a_msg );
			break;
		case SERVER_AddNewUser:
		case SERVER_RemoveUser:			
			sendMessagesToAllButSelf( a_msg );
			break;
		default:
			break;
			
		}
	}
	
	/**
	 * Determine if the dispatcher's message queue is empty.
	 * 
	 * @return <code>boolean</code> - <code>true</code> if empty, 
	 * 	<code>false</code> otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean isMessageListEmpty(){
		return m_messageList.isEmpty();
	}
	
	/**
	 * Add a message to the dispatcher's message queue.
	 * Blocks multiple threads from accessing it due to synchronization.
	 * 
	 * @param a_message <code>Message</code>, a message to add to the queue
	 * @author Andrzej Brynczka
	 */
	public synchronized void addMessage(Message a_message){	
		System.out.println("dispatcher received message");
		m_messageList.add( a_message );
		notify();//wake dispatcher up if it is waiting for more messages
	}
	
	/**
	 * Add user information to the dispatcher's collection of users, to ensure
	 * the client receives messages from the dispatcher. Use to register a 
	 * user's login.
	 * Also sends a message to all other clients notifying them of the new
	 * user.
	 
	 * 
	 * @param a_userName <code>String</code>, the client's username
	 * @param a_handler <code>UserHandler</code>, the client's handler thread
	 * @author Andrzej Brynczka
	 */
	public synchronized void addUser(String a_userName, UserHandler a_handler){
		System.out.println("dispatcher received user");
		m_users.put( a_userName, a_handler );
		
		//aware the other client's about the user's connection
		ConnectedUserMessage connectedUser = new ConnectedUserMessage(
				MessageHeader.SERVER_AddNewUser, 
				a_userName );
		addMessage( connectedUser );
	}
	
	/**
	 * Remove a client from the dispatcher's collection of users.
	 * Sends a message indicating the removal of the user to all other clients.
	 * 
	 * @param a_userName <code>String</code>, the username of the client 
	 * 	to remove
	 * @author Andrzej Brynczka
	 */
	public synchronized void removeUser(String a_userName){
		System.out.println("removing user: " + a_userName );
		m_users.remove( a_userName );
		
		//aware the other clients about the user's disconnection
		ConnectedUserMessage disconnectedUser = new ConnectedUserMessage(
				MessageHeader.SERVER_RemoveUser, 
				a_userName ); 
		addMessage( disconnectedUser );
	}
	
	protected UserHandler getUserHandler( String a_userName ){
		return m_users.get( a_userName );
	}
	/**
	 * Get a <u>copy</u> of the usernames for all connected clients
	 * 
	 * @return <code>String[]</code>, the copy of names
	 * @author Andrzej Brynczka
	 */
	public synchronized Collection<String> getUserCollection(){
		
		//get the collection of names
		Collection<String> collection = m_users.keySet();
		
		//convert the collection to a copy, within array format, and return it
		return collection;
	}
	
	/**
	 * Check if the given IP address matches that of a connected user
	 * 
	 * @param a_IP <code>InetAddress</code>, an IP address
	 * @return <code>boolean</code> - <code>true</code> if a connected user
	 * 	has the same IP address as the one provided, 
	 *  <code>false</code> otherwise
	 *  @author Andrzej Brynczka
	 */
	public boolean existsIP(InetAddress a_IP){
		for(UserHandler userHandler : m_users.values() ){
			if( a_IP.equals( userHandler.getUser().getIP() ) ){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if the given username has already been taken by a previous client
	 * 
	 * @param a_name <code>String</code>, the username to check
	 * @return <code>boolean</code> - <code>true</code> if the username is 
	 * 	already in use, <code>false</code> otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean existsUserName(String a_name){
		return m_users.containsKey( a_name );
	}
	
	/**
	 * Get the <code>InetAddress</code> IP address of a the desired user. 
	 * 
	 * @param a_userName String, the user whose IP is to be accessed
	 * @return <code>InetAddress</code> of the user, or null if the
	 * desired user's <code>UserHandler</code> cannot be found
	 * @author Andrzej Brynczka
	 */
	public InetAddress getIP(String a_userName){
		UserHandler userHandler = m_users.get( a_userName );
		if( userHandler == null ){
			return null;
		}
		
		return userHandler.getUser().getIP();
	}
}

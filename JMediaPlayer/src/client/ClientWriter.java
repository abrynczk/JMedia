package client;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.LinkedList;
import java.util.Queue;

import client.messages.Message;

/**
 * Class that handles writing of messages to the server for the client.
 * @author Andrzej Brynczka
 *
 */
public class ClientWriter extends Thread{

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** Reference to the main client object */
	private Client m_client;
	
	/** Output stream to the socket that is connected
	 * to the server, to be used for sending messages.
	 */
	private OutputStream m_outStream;
	
	/** Messages to be sent to the server */
	private Queue<Message> m_messagesToSend;
	
	/** Indicator of where or not to stop messages. Should be set to 
	 * false when the thread is to be closed.
	 */
	private boolean m_continueSendingMessages;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * The ClientWriter constructor.
	 * 
	 * @param a_client Client, reference to the main client object
	 * @throws IOException if an error occurs when attempting to
	 * 	get the output file stream from the client's socket
	 * @author Andrzej Brynczka
	 */
	ClientWriter(Client a_client) throws IOException{
		m_messagesToSend = new LinkedList<Message>();
		m_client = a_client;
		m_outStream = a_client.getSocket().getOutputStream();	
		
		m_continueSendingMessages = true;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Check if there are any messages to be sent to the server
	 * 
	 * @return <code>boolean</code> - <code>true</code> if there are messages
	 * 	in the queue to send, <code>false</code> otherwise
	 */
	public boolean isQueueEmpty(){
		return m_messagesToSend.isEmpty();
	}
	
	
	/**
	 * Retrieve the first message from the queue(removing it from the queue)
	 * 
	 * @return <code>Message</code>, the retrieved message
	 */
	private synchronized Message getFirstMessage(){
		return m_messagesToSend.poll();
	}
	
	
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Determine whether or not the thread is to be allowed to
	 * 	continue to send messages
	 * 
	 * @param a_continue boolean, indicator of whether to continue
	 * 	sending messages to the server
	 * @author Andrzej Brynczka
	 */
	public synchronized void continueSendingMessages( boolean a_continue ){
		m_continueSendingMessages = a_continue;
		if( a_continue == false ){
			notify();
		}
	}
	
	/**
	 * Provide a message to be sent to the server.
	 * 
	 * </br></br>This function notifies the writer
	 * of the addition to the queue.
	 * 
	 * @param a_message <code>Message</code>, a message to send
	 * @author Andrzej Brynczka
	 */
	public synchronized void addMessage(Message a_message){
		if( m_continueSendingMessages == false ){
			notify();//let the know writer know its time to stop
			return;
		}
		m_messagesToSend.add( a_message );
		notify();
	}
		
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************	
	/**
	 * Send a message to the server
	 * 
	 * @param a_message <code>Message</code>, the message to send
	 * @throws IOException if an error occurs when attempting to send
	 */
	private void sendMessage(Message a_message) throws IOException{
		if( a_message != null ){
			a_message.sendMessage( m_outStream );
		}
	}
	
	/**
	 * Continue to send messages to the server until the queue empties.
	 * When the message queue empties, wait until a new message is added
	 * (and this writer is notified of the addition) before attempting
	 * to write again.
	 * 
	 */
	@Override
	public void run(){
		//send login message first, the simply continue
		DataOutputStream outData = new DataOutputStream( m_outStream );
		System.out.println("sending data");
		try{
			//send username size and the username
			outData.writeInt( m_client.getUserName().length() );
			outData.flush();
			m_outStream.write( m_client.getUserName().getBytes() );
			m_outStream.flush();
			
			//send server password size and the password
			outData.writeInt( m_client.getServerPassword().length() );
			outData.flush();
			m_outStream.write( m_client.getServerPassword().getBytes() );
			m_outStream.flush();
		}
		catch(IOException e){
			System.out.println("Error writing login info: " + e.getMessage() );
			m_client.terminateConnection();
		}
		System.out.println("Sent the login information");
		
		//write to the server until forced to close
		while( !isInterrupted() ){
			try{
				if( isQueueEmpty() ){
					
					if( m_continueSendingMessages == false ){
						//queue is empty and no additional messages
						//are to be accepted - exit the thread
						interrupt();
						break;
					}
					
					//wait until a message is added to the queue	
					synchronized( this ){
						wait();
					}
				}
				
				Message msg = getFirstMessage();
				if( msg != null ){
					sendMessage( msg );
				}
			}
			catch(IOException |  InterruptedException e){
				System.out.println("Error in client writer: " 
						+ e.getMessage() );
				break;
			}
		}
		
		//server disconnect or error, done writing
		System.out.println("exited clientwriter");
		m_client.terminateConnection();
	}
}

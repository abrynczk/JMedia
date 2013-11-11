package server.framework;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Class containing information on an individual client connected to the server
 * @author Andrzej Brynczka
 */
public class User {
	private String m_UserName;
	private final Socket m_Socket;
	private boolean m_Muted;
	private boolean m_Admin;
	
	/**
	 * Create the basic User class object to hold a client's information
	 * 
	 * @param a_UserName <code>String</code>, the client's username
	 * @param a_Socket <code>Socket</code>, the client's socket
	 * @param a_IsMuted <code>boolean</code>, the client's muted status
	 * @param a_IsAdmin <code>boolean</code>, the client's admin status
	 */
	User(String a_UserName, Socket a_Socket, boolean a_Muted, 
			boolean a_Admin){
		m_UserName = a_UserName;
		m_Socket = a_Socket;
		m_Muted = a_Muted;
		m_Admin = a_Admin;
	}

	/**
	 * Get the user's admin status.
	 * 
	 * @return <code>boolean</code> - <code>true</code> if user is admin, 
	 * 	<code>false</code> otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean isAdmin(){
		return m_Admin;
	}
	
	/**
	 * Set the user's admin status.
	 * 
	 * @param a_admin <code>boolean</code>, the user's admin status to be set.
	 * 	<code>true</code> to set user as admin, <code>false</code> to remove
	 * 	admin powers.
	 * @author Andrzej Brynczka
	 */
	public void setAdmin(boolean a_admin){
		m_Admin = a_admin;
	}
	
	/**
	 * Set the user's mute status.
	 * 
	 * @param a_muted <code>boolean</code>, the user's mute status to be set.
	 * 	<code>true</code> to mute the user, <code>false</code> to remove mute
	 * @author Andrzej Brynczka
	 */
	public void setMuted(boolean a_muted){
		m_Muted = a_muted;
	}
	
	/**
	 * Get the user's mute status.
	 * 
	 * @return <code>boolean</code>, the user's mute status. <code>true</code>
	 * 	if the user is muted, <code>false</code> if not
	 * @author Andrzej Brynczka
	 */
	public boolean isMuted(){
		return m_Muted;
	}
		
	/**
	 * Get the client's username
	 * 
	 * @return <code>String</code>, the client's username.
	 * @author Andrzej Brynczka
	 */
	public String getUserName(){
		return m_UserName;
	}
	
	/**
	 * Set the client's username
	 * 
	 * @param a_userName <code>String</code>, the client's username
	 * @author Andrzej Brynczka
	 */
	public void setUserName(String a_userName){
		m_UserName = a_userName;
	}
	
	/**
	 * Get the client's socket.
	 * 
	 * @return <code>Socket</code>, the client's socket
	 * @author Andrzej Brynczka
	 */
	public Socket getSocket(){
		return m_Socket;
	}
	
	/**
	 * Get the client's IP address
	 * 
	 * @return <code>InetAddress</code>, the client's IP address
	 * @author Andrzej Brynczka
	 */
	public InetAddress getIP(){
		return m_Socket.getInetAddress();
	}
}

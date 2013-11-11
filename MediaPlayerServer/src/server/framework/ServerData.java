package server.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Handles server variables, and the creation and loading of the
 * server.ini file.
 * @author Andrzej Brynczka
 */
public class ServerData {

	/**
	 * Valid server initialization variables.
	 *
	 */
	enum Init_Vars{
		/**
		 * String. Maximum of <code>MAX_STRING_SIZE</code> characters.
		 */
		SERVERNAME,
		
		/**
		 * String. Maximum of <code>MAX_STRING_SIZE</code> characters. 
		 * Provided if the server is to require a password to be accessed.
		 */
		SERVERPASSWORD,
		
		/**
		 * String. Maximum of <code>MAX_STRING_SIZE</code> characters. 
		 * Given password is required to be provided by a user 
		 * to receive administrator powers.
		 */
		ADMINPASSWORD,
		
		/**
		 * Boolean. True if multiple users are to be allowed
		 * to login from the same ip address. False otherwise.
		 */
		MULTILOGIN,
		
		/**
		 * Int. Any number from 1025 to 65535.
		 */
		PORT;
	}
	
	/**
	 * Maximum size of a string-based initialization value.
	 * All character's after the final character are ignored.
	 */
	public final static int MAX_NAME_SIZE = 20;
	
	/**
	 * Default server port number.
	 */
	public final static int DEFAULT_PORT = 5376;
	
	private String m_serverName;
	private String m_serverPass;
	private String m_adminPass;
	private boolean m_multiLogin;
	private int m_port;
	
	/**
	 * An object to hold server initialization variables.
	 * 
	 * @param a_serverName String, the name of the server
	 * @param a_serverPass String, password required to access the server
	 * @param a_adminPass String, a password for use of admin powers
	 * @param a_multiLogin boolean, indication of whether to allow multiple
	 * 			logins per IP
	 */
	ServerData(String a_serverName, String a_serverPass,
			String a_adminPass, boolean a_multiLogin, int a_port){
		if( a_serverName == null ){ m_serverName = ""; }
		else{ m_serverName = a_serverName; }
		
		if( a_serverPass == null ){ m_serverPass = ""; }
		else{ m_serverPass = a_serverPass; }
		
		if( a_adminPass == null ){ m_adminPass = ""; }
		else{ m_adminPass = a_adminPass; }
		
		if( a_port > 65535 || a_port < 1025){ m_port = DEFAULT_PORT; }
		else{ m_port = a_port; }
		
		m_multiLogin = a_multiLogin;
	}
	
	/**
	 * Get the server's name.
	 * 
	 * @return <code>String</code>, the server's name
	 * @author Andrzej Brynczka
	 */
	public String getServerName(){
		return m_serverName;
	}
	
	/**
	 * Get the password required to access the server.
	 * 
	 * @return <code>String</code>, the password
	 * @author Andrzej Brynczka
	 */
	public String getServerPass(){
		return m_serverPass;
	}
	
	/**
	 * Get the password required to be entered by a user to receive access to
	 * 	administrator powers.
	 * 
	 * @return <code>String</code>, the password
	 * @author Andrzej Brynczka 
	 */
	public String getAdminPass(){
		return m_adminPass;
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
		return m_multiLogin;
	}
	
	/**
	 * Get the server's port number.
	 * @return <code>int</code>, the port number
	 * @author Andrzej Brynczka
	 */
	public int getPort(){
		return m_port;
	}
	
	/**
	 * Create a <code>ServerData</code> object with its default initialization
	 * parameters.
	 * 
	 * @return <code>ServerData</code> with default values
	 * @author Andrzej Brynczka
	 */
	public static ServerData createDefaultData(){
		return new ServerData("Server1", "", "", false, DEFAULT_PORT);
	}
	
	/**
	 * Create a new server.ini file with default initialization values.
	 * 
	 * @param a_fileName <code>String</code>, the name of a .ini file with
	 * 	required server initialization values
	 * @throws IOException if the file cannot be written to
	 * @author Andrzej Brynczka
	 */
	public static void createIni(String a_fileName) throws IOException{
		BufferedWriter fileOut = new BufferedWriter( 
				new FileWriter(a_fileName) );
		
		fileOut.write(Init_Vars.SERVERNAME.toString() + " = Server1");
		fileOut.newLine();
		
		fileOut.write(Init_Vars.SERVERPASSWORD.toString() + " = ");
		fileOut.newLine();
		
		fileOut.write(Init_Vars.ADMINPASSWORD.toString() +" = ");
		fileOut.newLine();
		
		fileOut.write(Init_Vars.MULTILOGIN.toString() + " = false");
		fileOut.newLine();
		
		fileOut.write(Init_Vars.PORT.toString() + " = " + DEFAULT_PORT);
		fileOut.newLine();
		
		fileOut.flush();
		fileOut.close();
	}
	
	
	/**
	 * Loads the server.ini file to accept server variables.
	 * 
	 * @param a_fileName <code>String</code>, the name of a .ini file with
	 * 	required server initialization values
	 * @return <code>Hashtable &lt;String, String&gt;</code> containing 
	 * 			&lt;variable, value&gt; pairs of the data read from the file
	 * @throws IOException if the data cannot be read from the file
	 * @author Andrzej Brynczka
	 */
	public static ServerData loadIni(String a_fileName) throws IOException {	
		BufferedReader fileIn = new BufferedReader( 
				new FileReader(a_fileName) );
	
		
		//Read all of the information from the file, line by line, and split
		//on "=" to create <variable, value> pairs.
		Hashtable<String, String> data = new Hashtable<String, String>();
		String line;
		
		System.out.println("Loading " + a_fileName + ": ");
		while( (line = fileIn.readLine()) != null ){
			String[] lineInfo = line.split("=");
			String variable = lineInfo[0].trim().toUpperCase();
			String value = "";
			
			if( lineInfo.length > 1 ){
				value = lineInfo[1].trim();
			}
			
			System.out.println(variable + " = " + value);
			
			data.put( variable, value );
		}
		System.out.println("Done.");
		fileIn.close();
		
		//parse the read data and return a ServerData object
		return parseIniData(data);
	}
	
	/**
	 * Parse the loaded initialization variable/value pairs and create a
	 * <code>ServerData</code> object.
	 * @param a_data <code>Hashtable&lt;String, String&gt;</code> containing 
	 * 			&lt;variable, value&gt; pairs of the data read from the file
	 * @return <code>ServerData</code> containing the loaded initialization
	 * 			values
	 * @author Andrzej Brynczka
	 */
	private static ServerData parseIniData(Hashtable<String, String> a_data){
		ServerData servData;
		
		//parse the loaded information for usable pairs
		String serverName = a_data.get( 
				Init_Vars.SERVERNAME.toString() );
		String serverPass = a_data.get( 
				Init_Vars.SERVERPASSWORD.toString() );
		String adminPass = a_data.get( 
				Init_Vars.ADMINPASSWORD.toString() );
		
		//check that the information has proper string length
		//ignore any character's after the last accepted character
		if( serverName.length() > MAX_NAME_SIZE ){
			serverName = serverName.substring(0, MAX_NAME_SIZE);
		}
		if( serverPass.length() > MAX_NAME_SIZE ){
			serverPass = serverPass.substring(0, MAX_NAME_SIZE);
		}
		if( adminPass.length() > MAX_NAME_SIZE ){
			adminPass = adminPass.substring(0, MAX_NAME_SIZE);
		}
		
		//set multiLogin to false if variable does not exist, or 
		//has any value other than true(case-insensitive)	
		boolean multiLogin = Boolean.parseBoolean(
				a_data.get( Init_Vars.MULTILOGIN.toString() ) );
	
		//get the provided port number
		int port;
		try{
			port = Integer.parseInt( a_data.get( Init_Vars.PORT.toString() ) );
		}
		catch(NumberFormatException e){
			System.out.println("Invalid port number given.");
			System.out.println("Setting port to default: " + DEFAULT_PORT);
			port = DEFAULT_PORT;
		}
		
		
		servData = new ServerData(serverName, serverPass, adminPass, 
				multiLogin, port);

		return servData;
	}
}

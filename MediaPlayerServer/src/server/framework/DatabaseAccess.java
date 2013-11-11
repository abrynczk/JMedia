package server.framework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;


import server.messages.PunishmentInfo;
import server.messages.PunishmentInfo.Punishment;


/**
 * Class to handle data exchange between the server and database
 * @author Andrzej Brynczka
 *
 */
public class DatabaseAccess {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	/**
	 * Set of sql tables containing the server's information with the database
	 * @author Andrzej Brynczka
	 *
	 */
	enum ServerTables{
		/**
		 * Table containing information on punished users.
		 */
		SERVER_PUNISHMENTS;
	}
		
	/** List of column names within the SERVER_PUNISHMENTS table */
	private final static String IP_ADDRESS = "IP_ADD";
	private final static String USERNAME = "USERNAME";
	private final static String PUNISHMENT = "PUNISHMENT";
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************

	/**
	 * Connection to the sql database
	 */
	private static Connection m_dbConn;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************

	/**
	 * Connect to the database
	 */
	static{
		String dataBaseURL = "jdbc:h2:~/test;AUTO_SERVER=TRUE";
		//String dataBaseURL = "jdbc:h2:~/test";
		String dataBaseUser = "sa";
		String dataBasePass = "";
			
		try {
			//Connect to the database
			Class.forName("org.h2.Driver");
			m_dbConn = DriverManager.getConnection(dataBaseURL, 
					dataBaseUser, dataBasePass);
			
			//Initiate the server's required data scheme's if not yet prepared
		    String query = "CREATE TABLE IF NOT EXISTS " + 
		    	"SERVER_PUNISHMENTS(" + IP_ADDRESS + " VARCHAR(150), " 
		    		+ USERNAME +" VARCHAR(30), " 
		    		+ PUNISHMENT +" CHAR(4))";
		    Statement stmt = m_dbConn.createStatement();
		    stmt.execute(query);
		    
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println( e.getMessage() );
			System.out.println("Server will run without access to " +
					"client punishments.");
		}
	}
	
	//********************************************************
	//Punishment Checkers
	//********************************************************
	/**
	 * Check if the provided IP suffers from the given punishment
	 * 
	 * @param a_IP <code>String</code>, the IP to check
	 * @param a_punCode <code>String</code>, the code for the punishment to 
	 * 	check against
	 * @return <code>boolean</code> - <code>true</code> if the IP suffers
	 * 	from the current punishment,<code>false</code> otherwise
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	private boolean isPunished(String a_IP, String a_punCode) 
			throws SQLException{
		String query = "SELECT PUNISHMENT FROM " 
				+ ServerTables.SERVER_PUNISHMENTS.toString()
				+" WHERE " + IP_ADDRESS + " = '" + a_IP + "'"
				+" AND " + PUNISHMENT +" = '" + a_punCode +"'";
	
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		//If a punishment for a ban(code 003) exists for this IP,
		//than this IP is banned
		if(rs.next())
		{
			return true;
		}
	
		return false;
	}
	
	/**
	 * Check if the provided IP is banned
	 * 
	 * @param a_IP <code>String</code>, the IP to check
	 * @return <code>boolean</code> - <code>true</code> if the IP is banned
	 * 	<code>false</code> otherwise
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	public boolean isBanned(String a_IP) throws SQLException{
		return isPunished( a_IP, Punishment.BAN.getCode());
	}
	
	/**
	 * Check if the given IP is muted
	 * 
	 * @param a_IP <code>String</code>, the IP to check
	 * @return <code>boolean</code> - <code>true</code> if the IP is muted
	 * 	<code>false</code> otherwise
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	public boolean isMuted(String a_IP) throws SQLException{
		return isPunished( a_IP, Punishment.MUTE.getCode());
	}
	
	//********************************************************
	//Punishment Setters
	//********************************************************
	/**
	 * Punish the current IP with the given punishment
	 * 
	 * @param a_IP <code>String</code>, the IP to set, in string format
	 * @param a_username <code>String</code>, the current name of the user 
	 * 	being punished
	 * @param a_punCode <code>String</code>, the code for the punishment to 
	 * 	check against
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	private void setPunishment(String a_IP, String a_username, String a_punCode) 
			throws SQLException{
		String insert = "INSERT INTO " 
				+ ServerTables.SERVER_PUNISHMENTS.toString() 
				+ "(" + IP_ADDRESS + ", " + USERNAME + ", " + PUNISHMENT 
				+ ") VALUES('" + a_IP + "', '" + a_username + "', '" 
				+ a_punCode +"')";

			
		Statement stmt = m_dbConn.createStatement();
		stmt.execute(insert);
	}
	
	/**
	 * Mute the provided IP
	 * 
	 * @param a_IP <code>String</code>, the IP to mute, in string format
	 * @param a_username <code>String</code>, the current name of the user 
	 * 	being punished
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	public void setMuted(String a_IP, String a_username) throws SQLException{
		setPunishment( a_IP, a_username, Punishment.MUTE.getCode() );
	}
	
	/**
	 * Ban the provided IP
	 * 
	 * @param a_IP <code>String</code>, the IP to ban, in string format
	 * @param a_username <code>String</code>, the current name of the user 
	 * 	being punished
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	public void setBanned(String a_IP, String a_username) throws SQLException{
		setPunishment( a_IP, a_username, Punishment.BAN.getCode() );
	}
	
	//********************************************************
	//Punishment Removers
	//********************************************************
	/**
	 * Remove the desired punishment from the current IP
	 * 
	 * @param a_IP <code>String</code>, the IP from which to remove
	 * 	the punishment, in string format
	 * @param a_punCode <code>String</code> the code for the punishment to 
	 * 	check against
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	private void removePunishment(String a_IP, String a_punCode) 
			throws SQLException{
		String delete = "DELETE FROM " 
				+ ServerTables.SERVER_PUNISHMENTS.toString() + " WHERE "
				+ "IP_ADD = '" + a_IP +"' AND PUNISHMENT = '"
				+ a_punCode + "'";
			
		Statement stmt = m_dbConn.createStatement();
		stmt.execute( delete );
	}
	
	/**
	 * Remove the mute status from the provided IP
	 * 
	 * @param a_IP <code>String</code>, the IP of the user to remove the
	 * 	mute from, in string format
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	public void removeMute(String a_IP) throws SQLException{
		removePunishment( a_IP, Punishment.MUTE.getCode() );
	}
	
	/**
	 * Remove the ban status from the provided IP
	 * 
	 * @param a_IP <code>InetAddress</code>,  the IP of the user to remove the
	 * 	ban from, in string format
	 * @throws SQLException if a database access error occurs
	 * @author Andrzej Brynczka
	 */
	public void removeBan(String a_IP) throws SQLException{
		removePunishment( a_IP, Punishment.BAN.getCode() );
	}
	
	//********************************************************
	//Punishment Getters
	//********************************************************
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
	public Collection<PunishmentInfo> getAllPunished() throws SQLException{
		String query = "SELECT * FROM SERVER_PUNISHMENTS";
		
		ArrayList<PunishmentInfo> punishments = new ArrayList<PunishmentInfo>();
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while( rs.next() )
		{	
			PunishmentInfo currentPun = new PunishmentInfo();
			currentPun.setTargetName( rs.getString( USERNAME ) );
			currentPun.setTargetIP( rs.getString( IP_ADDRESS ) );
			currentPun.setPunishment( Punishment.fromString( 
					rs.getString( PUNISHMENT ) ) );
			
			punishments.add( currentPun );
		}

		return punishments;
	}
}

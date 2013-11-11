package server.messages;

/**
 * Container for information on an admin punishment option to be
 * enacted on a user, for use in messages.
 * @author Andrzej Brynczka
 *
 */
public class PunishmentInfo {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	/**
	 * List of actions an admin can take against a client. Contains also
	 * the functionality to acquire the associated punishment message code.
	 * @author Andrzej Brynczka
	 *
	 */
	public enum Punishment{
		KICK("0001"),
		MUTE("0002"),
		BAN("0003"),
		
		INVALID("9999");
		
		/** The 4 character code used in messages to reference the punishment*/
		private String m_code;
		
		Punishment(String a_code){
			m_code = a_code;
		}
		
		/** Get the 4 character code associated to the punishment 
		 * @return <code>String</code> - the 4 character message code
		 * */
		public String getCode(){
			return m_code;
		}
		
		/**
		 * Get the Punishment enum from the given 4-character code
		 * @param a_code <code>String</code>, the 4-character punishment code
		 * @return <code>Punishment</code> - the code's Punishment enum
		 */
		public static Punishment fromString(String a_code){
			Punishment punishment;
			switch( a_code ){
			case "0001":
				punishment = KICK;
				break;
			case "0002":
				punishment = MUTE;
				break;
			case "0003":
				punishment = BAN;
				break;
			default:
				punishment = INVALID;
				break;
			}
			
			return punishment;
		}
	}
	
	
	/**
	 * List of the possible directions a punishment can take(EX:SET or REMOVE)
	 * when enacted on a user. Contains also functionality to acquire
	 * the direction's byte-based code to be used in messages.
	 * @author Andrzej
	 *
	 */
	public enum Direction{
		SET_PUNISHMENT( (byte) 1 ),
		REMOVE_PUNISHMENT( (byte) 2 ),
		
		INVALID( (byte) -1 );
		
		/** The byte-based code representing punishment direction */
		private byte m_code;
		
		Direction(byte a_code){
			m_code = a_code;
		}
		
		/** Get the byte-based code associated to the direction
		 * @return <code>byte</code> - a byte representing the direction */
		public byte getCode(){
			return m_code;
		}
		
		/**
		 * Get the <code>Direction</code> enum represented by the given byte
		 * @param a_code <code>byte</code>, the byte from which to get a
		 * 	Direction
		 * @return <code>Direction</code> - the enum representing the 
		 *  given byte
		 */
		public static Direction fromByte( byte a_code ){
			Direction direction;
			
			switch( a_code ){
			case 1:
				direction = SET_PUNISHMENT;
				break;
			case 2:
				direction = REMOVE_PUNISHMENT;
			default:
				direction = INVALID;
				break;
			}
			
			return direction;
		}
	}
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The name of the user to be targeted by an admin */
	private String m_targetName;
	
	/** The targeted user's IP address as a string*/
	private String m_targetIP;
	
	/** The punishment to be enacted on the targeted user */
	private Punishment m_punishment;
	
	/** The direction of the punishment being enacted */
	private Direction m_direction;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	
	/**
	 * Create the complete PunishmentInfo object, containing information
	 * on the user being targeted, the user's IP, a punishment to enact,
	 * and the direction of the punishment.
	 * 
	 * @param a_targetName <code>String</code>, the user to act against
	 * @param a_targetIP <code>String</code>, the user's IP in string format
	 * @param a_punishment <code>Punishment</code>, the user's punishment
	 * @param a_direction <code>Direction</code>, the direction of the 
	 * 	punishment
	 */
	public PunishmentInfo(String a_targetName, String a_targetIP, 
			Punishment a_punishment, Direction a_direction){
		
		m_targetName = a_targetName;
		m_targetIP = a_targetIP;
		m_punishment = a_punishment;
		m_direction = a_direction;
	}
	
	/**
	 * Create an empty PunishmentInfo object, ready to be modified through
	 * mutator functions.
	 */
	public PunishmentInfo(){
		m_targetName = null;
		m_targetIP = null;
		m_punishment = null;
		m_direction = null;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	
	/**
	 * Get the username of the user to be punished/un-punished
	 * @return <code>String</code> - the target of the punishment
	 */
	public String getTargetName(){
		return m_targetName;
	}
	
	/**
	 * Get the user's IP address in string format
	 * @return <code>String</code> - the user's IP
	 */
	public String getTargetIP(){
		return m_targetIP;
	}
	
	/**
	 * Get the punishment being requested
	 * @return <code>Punishment</code> - currently: 
	 * 	<code>KICK</code>, <code>MUTE</code>, or <code>BAN</code>
	 */
	public Punishment getPunishment(){
		return m_punishment;
	}
	
	/**
	 * Get the direction of the punishment
	 * @return <code>Direction</code> - the direction of the punishment
	 * 	against the target
	 */
	public Direction getDirection(){
		return m_direction;
	}
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the user name of the target to have an action taken against
	 * 	it.
	 * @param a_name <code>String</code>, the target's name
	 * @return <code>boolean</code> - <code>false</code> if the name
	 *  is null, <code>true</code> otherwise
	 */
	public boolean setTargetName(String a_name){
		if( a_name == null ){
			return false;
		}
		
		m_targetName = a_name;
		return true;
	}
	
	/**
	 * Set the user's IP address in string format.
	 * @param a_IP <code>String</code>, the user's IP
	 * @return <code>boolean</code> - <code>true</code> if an IP is given
	 * 	in string format
	 */
	public boolean setTargetIP(String a_IP){
		m_targetIP = a_IP;
		return true;
	}
	
	/**
	 * Set the punishment to take against the target
	 * @param a_punishment <code>Punishment</code>, the punishment to take
	 * @return <code>boolean</code> - <code>false</code> if the punishment
	 * 	is <code>INVALID</code> or null, <code>true</code> otherwise
	 */
	public boolean setPunishment(Punishment a_punishment){
		if( a_punishment == null || a_punishment == Punishment.INVALID){
			return false;
		}
		
		m_punishment = a_punishment;
		return true;
	}
	
	/**
	 * Set the direction of the punishment to make against the target
	 * @param a_direction <code>Direction</code>, the direction of the
	 * 	punishment
	 * @return <code>boolean</code> - <code>false</code> if the given
	 * 	direction is <code>INVALID</code> or null, <code>true</code> otherwise
	 */
	public boolean setDirection(Direction a_direction){
		if( a_direction == null || a_direction == Direction.INVALID ){
			return false;
		}
		
		m_direction = a_direction;
		return true;
	}
}

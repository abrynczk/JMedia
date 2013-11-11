package library.model;

/**
 * Container for data on generic media items, useful for 
 * managing associated data.
 * @author Andrzej Brynczka
 *
 */
public class MediaItem {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The media's title name */
	private String m_name = "";
	
	/** The media's length in seconds */
	private long m_length;
	
	/** The filepath to the media item's location */
	private String m_filePath = "";
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * The basic constructor for MediaItem objects, requiring
	 * the two most vital variables.
	 * @param a_name String, the media's title
	 * @param a_filePath String, the path to the media's file
	 */
	public MediaItem(String a_name, String a_filePath){
		m_name = a_name;
		m_filePath = a_filePath;
		
		m_length = 0;
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the media's title
	 * @return String containing the item's title
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Get the media item's length in seconds
	 * @return long containing the length in seconds
	 */
	public long getLengthAsLong() {
		return m_length;
	}

	/**
	 * Get the media item's length formatted as a string(hh:mm:ss)
	 * @return a String containing the formatted length
	 */
	public String getLength(){
		long seconds = m_length;		
		int hours =  (int)(seconds / 3600) ;
		int mins = (int)((seconds / 60) - (hours * 60));

		seconds -= (hours * 3600) + (mins * 60) ;

		return String.format("%d:%02d:%02d", hours, mins, seconds);
	}

	
	/**
	 * Get the media item's file path
	 * @return String containing the file path
	 */
	public String getFilePath() {
		return m_filePath;
	}

	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the media's name
	 * 
	 * @param a_name String, containing the media's name
	 * @return true if the name was set, false if not
	 * (due to given name being null)
	 */
	public boolean setName(String a_name) {
		if( a_name == null ){
			return false;
		}
		m_name = a_name;
		return true;
	}
	
	
	/**
	 * Set the media item's length in seconds
	 * 
	 * @param a_length long, containing the length in seconds
	 * @return true if the length was set, false if not
	 * (due to negative length);
	 */
	public boolean setLength(long a_length) {
		if( a_length < 0 ){
			return false;
		}
		
		m_length = a_length;
		return true;
	}
	
	/**
	 * Set the media item's file path
	 * 
	 * @param a_filePath String, containing the file path
	 * @return true if the file path was set, false if not
	 * (due to the given path being null)
	 */
	public boolean setFilePath(String a_filePath){
		if( a_filePath == null ){
			return false;
		}
		m_filePath = a_filePath;
		return true;

	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	@Override
	public boolean equals( Object a_item ){
		if( a_item == null ){
			return false;
		}
		
		if( !(a_item instanceof MediaItem) ){
			return false;
		}
		
		//items with the same name and filepath are the same item
		MediaItem itemToCompare = (MediaItem) a_item;
		if( !m_filePath.equals( itemToCompare.getFilePath() ) ||
				!m_name.equals( itemToCompare.m_name ) ){
			return false;
		}
		
		return true;
	}
}

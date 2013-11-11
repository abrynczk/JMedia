package library.model;

/**
 * Container for information pertaining to video-based media files, 
 * used to aid in management of necessary data.
 * @author Andrzej Brynczka
 *
 */
public class Video extends MediaItem{
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The video's genre data */
	private String m_genre = "";
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * The basic constructor for the Video object, accepting
	 * only the required data.
	 * 
	 * @param a_name String, the video's title
	 * @param a_filePath String, the filepath to the video
	 */
	public Video(String a_name, String a_filePath){
		super( a_name, a_filePath );
		setName( a_name );
		
		setGenre( "" );
		setLength( 0 );
	}
	
	/**
	 * An extended constructor for the Video object.
	 * 
	 * @param a_name String, the video's title 
	 * @param a_length long, the length of the Video file in seconds
	 * @param a_filePath String, the filepath to the video
	 */
	public Video(String a_name, long a_length, String a_filePath){
		super( a_name, a_filePath );

		setLength( a_length );
		setGenre( "" );
	}
	
	/**
	 * An extended constructor for the Video object, accepting all
	 * possible values.
	 * @param a_name String, the video's title 
	 * @param a_length long, the length of the Video file in seconds
	 * @param a_genre String, the video's genre
	 * @param a_filePath String, the filepath to the video
	 */
	public Video(String a_name, long a_length, String a_genre, String a_filePath) {
		super( a_name, a_filePath );
		setLength( a_length );
		setGenre( a_genre );
	}
	
	// *********************************************************
	// ******************** Getters ****************************
	// *********************************************************
	/**
	 * Get the video's genre data
	 * @return String containing the genre
	 */
	public String getGenre() {
		return m_genre;
	}

	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the video's genre data
	 * @param a_genre String, the genre to set
	 * @return true if the genre was set successfully, false if the genre
	 * 	was null and not set
	 */
	public boolean setGenre(String a_genre) {
		if( a_genre == null ){
			return false;
		}
		
		m_genre = a_genre;
		return true;
	}
}

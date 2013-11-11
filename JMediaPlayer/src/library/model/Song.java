package library.model;

/**
 * Container holding information on Song-based media items. Aids
 * in management of song-based data.
 * 
 * @author Andrzej Brynczka
 *
 */
public class Song extends MediaItem{

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The name of the song's artist */
	private String m_artist = "";
	
	/** The name of the song's album */
	private String m_album = "";
	
	/** The song's genre */
	private String m_genre = "";

	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * The basic Song constructor, accepting only the required
	 * parameters.
	 * 
	 * @param a_name String, the song's title name
	 * @param a_filePath String, the filepath to the song
	 */
	public Song(String a_name, String a_filePath){
		super( a_name, a_filePath );
		setArtist( "" );
		setAlbum( "" );
		setLength( 0 );
		setGenre( "" );
	}
	
	/**
	 * An extended Song constructor, accepting all parameters.
	 * 
	 * @param a_name String, the song's title name
	 * @param a_artist String, the song's artist name
	 * @param a_album String, the song'st album name
	 * @param a_length long, the length of the song in seconds
	 * @param a_genre String, the song's genre
	 * @param a_filePath String, the filepath to the song
	 */
	public Song(String a_name, String a_artist, String a_album, long a_length,
			String a_genre, String a_filePath){
		super( a_name, a_filePath );
		setArtist( a_artist );
		setAlbum( a_album );
		setLength( a_length );
		setGenre( a_genre );
	}
	
	/**
	 * An alternative Song constructor, accepting another Song object
	 * from which it copies its values.
	 * 
	 * @param a_song Song, the song object to copy
	 */
	public Song(Song a_song){
		super( a_song.getName(), a_song.getFilePath() );
		setArtist( a_song.getArtist() );
		setAlbum( a_song.getAlbum() );
		setLength( a_song.getLengthAsLong() );
		setGenre( a_song.getGenre() );
	}
	
	// *********************************************************
	// ******************** Getters ****************************
	// *********************************************************
	/**
	 * Get the song's artist name
	 * 
	 * @return String containing the artist name
	 */
	public String getArtist() {
		return m_artist;
	}

	/**
	 * Get the song's album name
	 * 
	 * @return String containing the song's album
	 */
	public String getAlbum() {
		return m_album;
	}
	
	/**
	 * Get the song's genre
	 * 
	 * @return String containing the song' genre
	 */
	public String getGenre() {
		return m_genre;
	}

	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the song's artist name
	 * 
	 * @param a_artist String, the artist name to set
	 * @return true if the artist was set, false if the artist
	 * 	was not set(given string was null)
	 */
	public boolean setArtist(String a_artist) {
		if( a_artist == null ){
			return false;
		}
		
		m_artist = a_artist;
		return true;
	}
	
	/**
	 * Set the song's album name
	 * 
	 * @param a_album String, the album name to set
	 * @return true if the album was set, false if the album
	 * 	was not set(given string was null)
	 */
	public boolean setAlbum(String a_album) {
		if( a_album == null ){
			return false;
		}
		
		m_album = a_album;
		return true;
	}	
	
	/**
	 * Set the song's genre
	 * 
	 * @param a_genre String, the song's genre
	 * @return true if the genre was set, false if the genre
	 * 	was not set(given string was null)
	 */
	public boolean setGenre(String a_genre) {
		if( a_genre == null ){
			return false;
		}
		
		m_genre = a_genre;
		return true;
	}
}

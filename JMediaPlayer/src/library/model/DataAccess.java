package library.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Class meant to manage interaction with the database, allowing for the
 *  storage of media information. The class connects to the database on
 *  creation and handles all database connectivity for the media player
 *  library.
 *  
 * @author Andrzej Brynczka
 *
 */
public class DataAccess {
	private static Connection m_dbConn;
	private static String m_dataBaseUser = "sa";
	private static String m_dataBasePass = "";
	
	private static String m_dataBaseURL = "jdbc:h2:~/test;AUTO_SERVER=TRUE";
	
	/**
	 * @author Andrzej Brynczka
	 * Enum for the database table names
	 */
	enum TableType{
		MUSIC,
		VIDEO,
		PLAYLIST,
		MUSICPL,
		VIDEOPL
	}
	
	static {
		// make the connection and create tables if necessary
		try {
			Class.forName("org.h2.Driver");
			m_dbConn = DriverManager.getConnection(m_dataBaseURL, 
					m_dataBaseUser, m_dataBasePass);
			String query ;
			Statement stmt;

			//create MUSIC table
		    query = "CREATE TABLE IF NOT EXISTS " + TableType.MUSIC.toString() 
		    		+ "(NAME VARCHAR(250), ARTIST VARCHAR(100), " 
		    		+ "ALBUM VARCHAR(100), LENGTH BIGINT, GENRE VARCHAR(50), "
		    		+ "FILELOC VARCHAR(500), " 
		    		+ "PRIMARY KEY(FILELOC))";
		    stmt = m_dbConn.createStatement();
		    stmt.execute(query);
		    
		    //create VIDEO table
		    query = "CREATE TABLE IF NOT EXISTS " + TableType.VIDEO.toString() 
		    		+ "(NAME VARCHAR(250), LENGTH BIGINT, GENRE VARCHAR(50), " 
		    		+ "FILELOC VARCHAR(500), "
		    		+ "PRIMARY KEY(FILELOC))";
		    stmt = m_dbConn.createStatement();
		    stmt.execute(query);
		    
		    //create PLAYLIST table
		    query = "CREATE TABLE IF NOT EXISTS " 
		    		+ TableType.PLAYLIST.toString() 
		    		+ "( PLNAME VARCHAR(250), TYPE VARCHAR(25), " 
		    		+ "PRIMARY KEY(PLNAME) )";
		    stmt = m_dbConn.createStatement();
		    stmt.execute(query);
		    
		    //create MUSICPL table
		    query = "CREATE TABLE IF NOT EXISTS " 
		    		+ TableType.MUSICPL.toString() 
		    		+ "(PLNAME VARCHAR(250), SONG VARCHAR(250), " 
		    		+ "FILELOC VARCHAR(500), "
		    		+ "FOREIGN KEY(PLNAME) REFERENCES " 
		    		+ TableType.PLAYLIST.toString()
		    		+ "(PLNAME) ON UPDATE CASCADE ON DELETE CASCADE, "
		    		+ "FOREIGN KEY(FILELOC) REFERENCES "
		    		+ TableType.MUSIC.toString() + "(FILELOC) ON UPDATE "
		    		+ "CASCADE ON DELETE CASCADE)";
		    stmt = m_dbConn.createStatement();
		    stmt.execute(query);
		    
		    //create VIDEOPL table
		    query = "CREATE TABLE IF NOT EXISTS " 
		    		+ TableType.VIDEOPL.toString() 
		    		+ "(PLNAME VARCHAR(250), SONG VARCHAR(250), " 
		    		+ "FILELOC VARCHAR(500), "
		    		+ "FOREIGN KEY(PLNAME) REFERENCES " 
		    		+ TableType.PLAYLIST.toString()
		    		+ "(PLNAME) ON UPDATE CASCADE ON DELETE CASCADE, "
		    		+ "FOREIGN KEY(FILELOC) REFERENCES "
		    		+ TableType.VIDEO.toString() + "(FILELOC) ON UPDATE "
		    		+ "CASCADE ON DELETE CASCADE)";
		    stmt = m_dbConn.createStatement();
		    stmt.execute(query);
		    	    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// *********************************************************
	// ****Functions That Determine Existence of Desired Data***
	// *********************************************************
	/**
	 * Determine if the desired artist exists in the data-set
	 * 
	 * @param a_name String, the name of the artist 
	 * @return a boolean indicating whether the artist exists
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public boolean artistExists(String a_name) throws SQLException{
		String query = "SELECT * FROM " + TableType.MUSIC.toString()
				+ " WHERE ARTIST = '" + a_name.replaceAll("'", "''") + "'";
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		//if there is a result, the given artist exits
		return rs.next();
	}
	
	/**
	 * Determine if the desired song exists in the data-set
	 * 
	 * @param a_filePath String, the filepath for the song
	 * @return a boolean indicating whether the song exists
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public boolean songExists(String a_filePath) throws SQLException{
		String query = "SELECT * FROM " + TableType.MUSIC.toString() 
				+ " WHERE FILELOC = '" + a_filePath.replaceAll("'", "''") 
				+ "'";
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		//if there is a result from the search, the song exists
		return rs.next();
	}
	
	/**
	 * Determine if the desired video exists in the data-set
	 * 
	 * @param a_filePath String, the file path to the desired video
	 * @return a boolean indicating whether the video exists
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public boolean videoExists(String a_filePath) throws SQLException{
		String query = "SELECT * FROM " + TableType.VIDEO.toString() 
				+ " WHERE FILELOC = '" + a_filePath.replaceAll("'", "''")
				+ "'";
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		//if there is a result from the search, the video exists
		return rs.next();
	}
	
	/**
	 * Determine if the desired playlist exists in the data-set
	 * 
	 * @param a_name String, the name of the playlist 
	 * @return a boolean indicating whether the playlist exists
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka 
	 */
	public boolean playlistExists(String a_name) throws SQLException{
		String query = "SELECT * FROM " + TableType.PLAYLIST.toString() 
				+ " WHERE PLNAME = '" + a_name.replaceAll("'", "''") + "'";		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		//if there is a result, the playlist exists
		return rs.next();
	}
	
	/**
	 * Determine if the given playlist is a music playlist
	 * 
	 * @param a_playlistName String, the playlist to check
	 * @return true if the playlist is a music playlist, false otherwise
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka 
	 */
	public boolean isMusicPlaylist(String a_playlistName) throws SQLException{
		String query = "SELECT PLNAME FROM " + TableType.PLAYLIST 
				+" WHERE PLNAME = '" + a_playlistName + "' AND TYPE = '"
				+"MUSIC'";
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		//check if the table has any playlists with the given name
		if( !rs.next() ){
			return false;
		}		
		
		return true;
	}
	
	/**
	 * Determine if the given playlist is a video playlist 
	 * 
	 * @param a_playlistName String, the playlist to check
	 * @return true if the playlist is a video playlist, false otherwise
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka 
	 */
	public boolean isVideoPlaylist(String a_playlistName) throws SQLException{
		String query = "SELECT PLNAME FROM " + TableType.PLAYLIST 
				+" WHERE PLNAME = '" + a_playlistName + "' AND TYPE = '"
				+"VIDEO'";
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		//check if the table has any playlists with the given name
		if( !rs.next() ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Determine if the given song exists in the desired playlist
	 * 
	 * @param a_pl String, the name of the playlist 
	 * @param a_songPath String, the filepath of the song
	 * @return a boolean indicating whether the song exists in the playlist
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public boolean songInPL(String a_pl, String a_songPath) throws SQLException 
			
	{
		String query = "SELECT * FROM " + TableType.MUSICPL.toString()
				+ " WHERE PLNAME = '" + a_pl.replaceAll("'", "''") 
				+ "' AND FILELOC = '" 
				+ a_songPath.replaceAll("'", "''") +"'";
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		//if there is a result, the song exists in the playlist
		return rs.next();
	}
	
	/**
	 * Determine if the given video exists in the desired playlist
	 * 
	 * @param a_pl String, the name of the playlist 
	 * @param a_videoPath String, the filepath of the video
	 * @return a boolean indicating whether the video exists in the playlist
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public boolean videoInPL(String a_pl, String a_videoPath) 
			throws SQLException 
			
	{
		String query = "SELECT * FROM " + TableType.VIDEOPL.toString()
				+ " WHERE PLNAME = '"+a_pl.replaceAll("'", "''")
				+"' AND FILELOC = '" 
				+ a_videoPath.replaceAll("'", "''") + "'";
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		//if there is a result, the video exists in the playlist
		return rs.next();
	}

	// *********************************************************
	// ***************** Database Selectors ********************
	// *********************************************************
	/**
	 * Get the number of items in a desired table
	 * 
	 * @param a_tableType TableType, type of database table to count. 
	 * 	IE: MUSIC, VIDEO, PLAYLIST
	 * @return an int indicating the total number of items in given table
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka 
	 */
	public int getNumOfType(TableType a_tableType) throws SQLException 
			
	{
		String query = "SELECT COUNT(*) FROM " + a_tableType.toString();
		int numItems = 0;
		
		
		//create to the database and make the count request
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		System.out.println("Getting number of " + 
				a_tableType.toString() + ".");	
		
		//get the total number of items
		numItems = rs.getInt(1);
		
		System.out.println("Got " + numItems + 
				" " + a_tableType.toString());
		
		return numItems;	
	}
	
	/**
	 * Get the stored song information for the file at the
	 * provided file path.
	 * 
	 * @param a_fileLoc <code>String</code>, a file path for the file
	 * 	to search for
	 * @return <code>Song</code> with the stored song information, or
	 * 	<code>null</code> if no information is found
	 * @throws SQLException 
	 */
	public Song getSong(String a_fileLoc) throws SQLException{
		String query = "SELECT * FROM " + TableType.MUSIC.toString() 
				+ " WHERE FILELOC = '" + a_fileLoc.replaceAll("'", "''") 
				+ "'";	
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		Song newSong = null;
		if( rs.next() ){
			String name = rs.getString( "NAME" );
			String artist = rs.getString( "ARTIST" );
			String album = rs.getString( "ALBUM" );
			String genre = rs.getString( "GENRE" );
			long length = rs.getLong( "LENGTH" );
			
			try {
				newSong = new Song( name, artist, album, 
										length, genre, a_fileLoc );
			} catch (Exception e) {
				System.out.println("Invalid song filepath specified");
				newSong = null;
			}
		}
		
		return newSong;
	}
	
	/**
	 * Get the stored video information for the file at the
	 * provided file path
	 * 
	 * @param a_fileLoc <code>String</code>, a file path for the file
	 * 	to search for
	 * @return <code>Video</code> with the stored video information, or
	 * 	<code>null</code> if no information is found
	 * @throws SQLException 
	 */
	public Video getVideo(String a_fileLoc) throws SQLException{
		String query = "SELECT * FROM " + TableType.VIDEO.toString()
				+ " WHERE FILELOC = '" + a_fileLoc.replaceAll("'", "''")
				+ "'";
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		Video newVideo = null;
		if( rs.next() ){
			String name = rs.getString( "NAME" );
			String genre = rs.getString( "GENRE" );
			long length = rs.getLong( "LENGTH" );
			
			try {
				newVideo = new Video( name, length, genre, a_fileLoc );
			} catch (Exception e) {
				System.out.println("Invalid video filepath specified.");
				newVideo = null;
			}
		}
		
		return newVideo;
	}
	
	/**
	 * Helper function. Provide a result set meant to return a list of
	 * <code>Song</code> information and this function will read through the
	 * set and return a collection of <code>Song</code> objects.
	 * 
	 * @param a_resultSet <code>ResultSet</code>, the result from a SQL query
	 * 	on the database meant to contain information on songs
	 * @return Collection&lt;Song&gt; containing the collection of Song objects,
	 * 	(the collection will be of size 0 if nothing is returned from the 
	 * 	result set)
	 * @throws SQLException if an error occurs when querying the database
	 */
	private Collection<Song> collectSongs( ResultSet a_resultSet ) 
			throws SQLException{
		
		ArrayList<Song> mediaList = new ArrayList<Song>();
		
		while( a_resultSet.next() ){
			String name = a_resultSet.getString("NAME");
			String artist = a_resultSet.getString("ARTIST");
			String album = a_resultSet.getString("ALBUM");
			long length = a_resultSet.getLong("LENGTH");
			String genre = a_resultSet.getString("GENRE");
			String fileLoc = a_resultSet.getString("FILELOC");
			
			
			try {
				Song newSong;
				System.out.println(name + ": " + fileLoc );
				newSong = new Song(name, artist, album, length, 
						genre, fileLoc);
				
				mediaList.add(newSong);
			} catch (Exception e) {
				System.out.println("Invalid file location on song: " 
						+ name + " with artist " + artist);
				System.out.println("Did not add to collection.");
			}
		}
		
		return mediaList;
	}
	
	/**
	 * Helper function. Provide a result set meant to return a list of
	 * <code>Video</code> information and this function will read through the
	 * set and return a collection of <code>Video</code> objects.
	 * 
	 * @param a_resultSet <code>ResultSet</code>, the result from a SQL query
	 * 	on the database meant to contain information on videos
	 * @return Collection&lt;Video&gt; containing the collection of 
	 * 	Video objects, (the collection will be of size 0 if nothing is 
	 *  returned from the result set)
	 * @throws SQLException if an error occurs when querying the database
	 */
	private Collection<Video> collectVideos( ResultSet a_resultSet ) 
			throws SQLException{
		
		ArrayList<Video> mediaList = new ArrayList<Video>();
		
		while( a_resultSet.next() )
		{
			String name = a_resultSet.getString("NAME");
			long length = a_resultSet.getLong("LENGTH");
			String genre = a_resultSet.getString("GENRE");
			String fileLoc = a_resultSet.getString("FILELOC");
			
			
			try {
				Video newVideo;
				newVideo = new Video(name, length, genre, fileLoc);
				mediaList.add( newVideo );
			} catch (Exception e) {
				System.out.println("Invalid file location on video: " + name);
				System.out.println("Did not add to collection. ");
			}
		}
		
		return mediaList;
	}
	
	/**
	 * Get the data on all of the songs contained within the database
	 * 
	 * @return Collection of Songs returned from the database
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Song> getAllSongs() throws SQLException{
		String query = "SELECT * FROM " + TableType.MUSIC.toString();
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		return collectSongs( rs );
	}
	
	/**
	 * Get all songs with the designated name contained within the database
	 * 
	 * @param a_name String, the name to check
	 * @param a_exact boolean, true for exact search, false otherwise
	 * @return the Collection of all songs with the given name
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Song> getAllSongsWithName(
			String a_name, boolean a_exact) throws SQLException{
		
		String query;
		if( a_exact ){
			//search for exactly this name
			query = "SELECT * FROM " + TableType.MUSIC.toString()
					+ " WHERE NAME = '" 
					+ a_name.replaceAll("'", "''") 
					+"'";
		}
		else{
			//search for songs with names similar to the provided artist
			query = "SELECT * FROM " + TableType.MUSIC.toString()
					+ " WHERE NAME LIKE '%" 
					+ a_name.replaceAll("'", "''") 
					+"%'";
		}
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		return collectSongs( rs );
	}
	
	/**
	 * Get all songs with the designated artist name within the database
	 * 
	 * @param a_artist String, the artist name to check
	 * @param a_exact boolean, true for exact search and 
	 * 	false otherwise
	 * @return Collection of songs with the given artist name
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Song> getAllSongsWithArtist(
			String a_artist, boolean a_exact) throws SQLException{
		String query;
		if( a_exact ){
			//search for exactly this artist name
			query = "SELECT * FROM " + TableType.MUSIC.toString()
					+ " WHERE ARTIST = '" 
					+ a_artist.replaceAll("'", "''") 
					+"'";
		}
		else{
			//search for artist with names similar to the provided artist
			query = "SELECT * FROM " + TableType.MUSIC.toString()
					+ " WHERE ARTIST LIKE '%" 
					+ a_artist.replaceAll("'", "''") 
					+"%'";
		}
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		return collectSongs( rs );
	}
	
	/**
	 * Get all songs with the designated album within the database
	 * 
	 * @param a_album String, the album to check
	 * @param a_exact boolean, true for exact search
	 * @return Collection of songs with the given album
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Song> getAllSongsWithAlbum(
			String a_album, boolean a_exact) throws SQLException{
		
		String query;
		if( a_exact ){
			//search for exactly this album name
			query = "SELECT * FROM " + TableType.MUSIC.toString()
					+ " WHERE ALBUM = '" 
					+ a_album.replaceAll("'", "''") 
					+"'";
		}
		else{
			//search for names similar to this album name
			query = "SELECT * FROM " + TableType.MUSIC.toString()
					+ " WHERE ALBUM LIKE '%" 
					+ a_album.replaceAll("'", "''") 
					+"%'";
		}
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		return collectSongs( rs );
	}
	
	/**
	 * Get all Songs with the designated genre within the database
	 * 
	 * @param a_genre String, the genre to search
	 * @param a_exact boolean, true for exact search
	 * @return Collection of songs with the given genre
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Song> getAllSongsWithGenre(
			String a_genre, boolean a_exact) throws SQLException{
		
		String query;
		if( a_exact ){
			//search for exactly this genre
			query = "SELECT * FROM " + TableType.MUSIC.toString()
					+ " WHERE GENRE = '" 
					+ a_genre.replaceAll("'", "''") 
					+"'";
		}
		else{
			//search for genres similiar to this
			query = "SELECT * FROM " + TableType.MUSIC.toString()
					+ " WHERE GENRE LIKE '%" 
					+ a_genre.replaceAll("'", "''") 
					+"%'";
		}
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		return collectSongs( rs );
	}
	
	//get collections of videos
	public Collection<Video> getAllVideos() throws SQLException {
		String query = "SELECT * FROM " + TableType.VIDEO.toString();
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		return collectVideos( rs );
	}
	
	/**
	 * Gell all videos with the designated name from within the database
	 * 
	 * @param a_name String, the name to search against
	 * @param a_exact boolean, true for exact search
	 * @return Collection of vidoes with the designated name
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Video> getAllVideosWithName(
			String a_name, boolean a_exact) throws SQLException{
		
		String query;
		if( a_exact ){
			//search for exactly this name
			query = "SELECT * FROM " + TableType.VIDEO.toString()
					+ " WHERE NAME = '" 
					+ a_name.replaceAll("'", "''") 
					+"'";
		}
		else{
			//search for names similar to this
			query = "SELECT * FROM " + TableType.VIDEO.toString()
					+ " WHERE NAME LIKE '%" 
					+ a_name.replaceAll("'", "''") 
					+"%'";
		}
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		return collectVideos( rs );
	}
	
	/**
	 * Get all videos with the designated genre from within the database
	 * 
	 * @param a_genre String, the genre to search against
	 * @param a_exact boolean, true for exact search
	 * @return Collection of videos with the given genre
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Video> getAllVideosWithGenre(
			String a_genre, boolean a_exact) throws SQLException{
		
		String query;
		if( a_exact ){
			//search for exactly this genre
			query = "SELECT * FROM " + TableType.VIDEO.toString()
					+ " WHERE GENRE = '" 
					+ a_genre.replaceAll("'", "''") 
					+"'";
		}
		else{
			//search for genre's similiar to this
			query = "SELECT * FROM " + TableType.VIDEO.toString()
					+ " WHERE GENRE LIKE '%" 
					+ a_genre.replaceAll("'", "''") 
					+"%'";
		}
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		return collectVideos( rs );
	}
	
	/**
	 * Get the names of all playlists
	 * 
	 * @return a Collection&lt;String&gt; object holding the names of every 
	 *  playlist(of size 0 if none exist)
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public Collection<String> getAllPlayListNames() throws SQLException
	{
		String query = "SELECT * FROM " + TableType.PLAYLIST.toString();
		ArrayList<String> playlists = new ArrayList<String>();
		

		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while(rs.next())
		{	
			playlists.add( rs.getString("PLNAME") );
		}

		return playlists;
	}
	
	/**
	 * Get the names of all music playlists
	 * 
	 * @return a Collection of Strings holding the names of 
	 *  music-based playlists(size 0 if none exist)
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public Collection<String> getAllMusicPlayListNames() throws SQLException
	{
		String query = "SELECT DISTINCT PLNAME FROM " + TableType.MUSICPL;
		ArrayList<String> playlists = new ArrayList<String>();
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		while( rs.next() )
		{
			playlists.add( rs.getString("PLNAME") );
		}
		
		return playlists;
	}
	
	/**
	 * Get the names of all video playlists
	 * 
	 * @return a Collection of Strings holding the names of video-based
	 *  playlists(size 0 if none exist)
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public Collection<String> getAllVideoPlayListNames() throws SQLException
	{
		String query = "SELECT DISTINCT PLNAME FROM " + TableType.VIDEOPL;
		ArrayList<String> playlists = new ArrayList<String>();
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery( query );
		
		while( rs.next() )
		{
			playlists.add( rs.getString("PLNAME") );
		}
		
		return playlists;
		
	}
	/**
	 * Get the data on all songs within the given playlist
	 * 
	 * @param a_PL String, the desired playlist
	 * @return a Collection&lt;Song&gt; object holding the data for every song 
	 *  within the given playlist, or an empty collection if none exist
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka 
	 */
	public Collection<Song> getSongsInPL(String a_PL) throws SQLException
	{
		String query = "SELECT MUSIC.* FROM " 
				+ TableType.MUSIC.toString() + " JOIN " 
				+ TableType.MUSICPL.toString() + " WHERE "
				+ "PLNAME = '" + a_PL.replaceAll("'", "''") 
				+ "' AND MUSIC.FILELOC = MUSICPL.FILELOC";
		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		return collectSongs( rs );
	}
	
	/**
	 * Get the data on all videos within the desired playlist
	 * 
	 * @param a_PL String, the desired playlist to search on
	 * @return Collection&lt;Video&gt, object holding the data for every video
	 *  within the given playlist, or an empty collection if none exist
	 * @throws SQLException if an error occurs when querying the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Video> getVideosInPL(String a_PL) throws SQLException
	{
		String query = "SELECT VIDEO.* FROM " 
				+ TableType.VIDEO.toString() + " JOIN " 
				+ TableType.VIDEOPL.toString() + " WHERE "
				+ "PLNAME = '" + a_PL.replaceAll("'", "''")
				+ "' AND VIDEO.FILELOC = VIDEOPL.FILELOC";

		
		Statement stmt = m_dbConn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		return collectVideos( rs );
	}
	
	// *********************************************************
	// ***************** Database Mutators *********************
	// *********************************************************
	/**
	 * Save the song data within the database
	 * 
	 * @param a_name String, the name of the song to save
	 * @param a_artist String, the artist name to save
	 * @param a_album String, the album name to save
	 * @param a_length long, the length to save
	 * @param a_genre String, the genre to save
	 * @param a_filePath String, the path to the file to save
	 * @return true if the save is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean saveSong(String a_name, String a_artist, String a_album,
			long a_length, String a_genre, String a_filePath) 
					throws SQLException{
	
		if( songExists( a_filePath ) ){
			//this song already exists in the database, update its info instead
			updateSongArtist(a_filePath, a_artist);
			updateSongAlbum(a_filePath, a_album);
			updateSongGenre(a_filePath, a_genre);

		}
		else{
			//new information, save it
			String insertString = "INSERT INTO " + TableType.MUSIC.toString() 
					+ " VALUES(?, ?, ?, ?, ?, ?)";
			
			PreparedStatement insertStmt = 
					m_dbConn.prepareStatement( insertString );
			insertStmt.setString( 1, a_name );
			insertStmt.setString( 2, a_artist );
			insertStmt.setString( 3, a_album );
			insertStmt.setLong( 4, a_length );
			insertStmt.setString( 5, a_genre );
			insertStmt.setString( 6, a_filePath );
			insertStmt.execute();
		}
		
		return true;
	}
	
	/**
	 * Update the given song's name within the database
	 * 
	 * @param a_name String, the name of the song to update
	 * @param a_filePath String, the path to the file
	 * @param a_newName String, the new name to save
	 * @return true if the save is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updateSongName(String a_name, 
			String a_filePath, String a_newName) throws SQLException{
		
		if( !songExists( a_filePath ) ){
			return false;
		}
		
		String updateString = "UPDATE " + TableType.MUSIC.toString() 
				+ " SET NAME = '" + a_newName.replaceAll("'", "''") 
				+"' WHERE NAME = '" + a_name.replaceAll("'", "''")
				+ "' AND FILELOC = '" 
				+ a_filePath.replaceAll("'", "''") + "'";
		Statement updateStmt = m_dbConn.createStatement();
		updateStmt.execute( updateString );
	
		return true;
	}
	
	/**
	 * Update the given song's artist within the database
	 * 
	 * @param a_filePath String, the path to the file to update
	 * @param a_newArtist String, the new artist name to save
	 * @return true if the save is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updateSongArtist(String a_filePath, String a_newArtist) 
			throws SQLException{
		
		if( !songExists( a_filePath ) ){
			return false;
		}
		
		String updateString = "UPDATE " + TableType.MUSIC.toString() 
				+ " SET ARTIST = '" + a_newArtist.replaceAll("'", "''") 
				+"' WHERE FILELOC = '" 
				+ a_filePath.replaceAll("'", "''") + "'";
		Statement updateStmt = m_dbConn.createStatement();
		updateStmt.execute( updateString );
		return true;
	}
	
	/**
	 * Update the given song's album within the database
	 * 
	 * @param a_filePath String, the path to the file to update
	 * @param a_newAlbum String, the new album to save
	 * @return true if the save is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updateSongAlbum(String a_filePath, String a_newAlbum) 
			throws SQLException{
		
		if( !songExists( a_filePath ) ){
			return false;
		}
		
		String updateString = "UPDATE " + TableType.MUSIC.toString() 
				+ " SET ALBUM = '" + a_newAlbum.replaceAll("'", "''") 
				+"' WHERE FILELOC = '" 
				+ a_filePath.replaceAll("'", "''") + "'";
		Statement updateStmt = m_dbConn.createStatement();
		updateStmt.execute( updateString );
		return true;
	}
	
	/**
	 * Update the given song's genre within the database
	 * 
	 * @param a_filePath String, the path to the file to update
	 * @param a_newGenre String, the new genre to save
	 * @return true if the save is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updateSongGenre(String a_filePath, String a_newGenre) 
			throws SQLException{
		
		if( !songExists( a_filePath ) ){
			return false;
		}
		
		String updateString = "UPDATE " + TableType.MUSIC.toString() 
				+ " SET GENRE = '" + a_newGenre.replaceAll("'", "''") 
				+"' WHERE FILELOC = '" 
				+ a_filePath.replaceAll("'", "''") + "'";
		Statement updateStmt = m_dbConn.createStatement();
		updateStmt.execute( updateString );
		return true;
	}
	
	/**
	 * Save the video data within the database
	 * 
	 * @param a_name String, the name of the video to save
	 * @param a_length long, the length to save
	 * @param a_genre String, the genre to save
	 * @param a_filePath String, the path to the file to save
	 * @return true if the save is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean saveVideo(String a_name, long a_length, String a_genre, 
			String a_filePath) throws SQLException{
	
		if( videoExists(a_filePath) ){
			//this song already exists in the database, update its info instead
			updateVideoGenre(a_name, a_filePath, a_genre);

		}
		else{
			//new information, save it
			String insertString = "INSERT INTO " + TableType.VIDEO.toString() 
					+ " VALUES(?, ?, ?, ?)";
			
			PreparedStatement insertStmt = 
					m_dbConn.prepareStatement( insertString );
			insertStmt.setString( 1, a_name );
			insertStmt.setLong( 2, a_length );
			insertStmt.setString( 3, a_genre );
			insertStmt.setString( 4, a_filePath );
			insertStmt.execute();
		}
		
		return true;
	}
	
	/**
	 * Update the given video's name within the database
	 * 
	 * @param a_name String, the name of the video to update
	 * @param a_filePath String, the path to the video to update
	 * @param a_newName String, the new name value
	 * @return true if the update is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updateVideoName(String a_name, 
			String a_filePath, String a_newName) throws SQLException{
		
		if( !songExists( a_filePath ) ){
			return false;
		}
		
		String updateString = "UPDATE " + TableType.VIDEO.toString() 
				+ " SET NAME = '" + a_newName.replaceAll("'", "''") 
				+ "' WHERE NAME = '" + a_name
				+ "' AND FILELOC = '" + a_filePath.replaceAll("'", "''") 
				+ "'";
		Statement updateStmt = m_dbConn.createStatement();
		updateStmt.execute( updateString );
		return true;
	}
	
	/**
	 * Update the given video's genre within the database
	 * 
	 * @param a_name String, the name of the video to update
	 * @param a_filePath String, the path to the video to update
	 * @param a_newGenre String, the new genre value
	 * @return true if the update is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updateVideoGenre(String a_name, String a_filePath, 
			String a_newGenre) throws SQLException{
		
		if( !videoExists( a_filePath ) ){
			return false;
		}
		
		String updateString = "UPDATE " + TableType.VIDEO.toString() 
				+ " SET GENRE = '" + a_newGenre.replaceAll("'", "''") 
				+ "' WHERE NAME = '" + a_name
				+ "' AND FILELOC = '" + a_filePath.replaceAll("'", "''") + "'";
		Statement updateStmt = m_dbConn.createStatement();
		updateStmt.execute( updateString );
		return true;
	}
	
	/**
	 * Save the given playlist within the database
	 * 
	 * @param a_name String, the playlist name
	 * @param a_type String, the playlist type(MUSIC, VIDEO)
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean savePlayListName(String a_name, String a_type) 
			throws SQLException{
	
		if( playlistExists(a_name) ){
			return false;

		}
		
		//new information, save it
		String insertString = "INSERT INTO " + TableType.PLAYLIST.toString() 
				+ " VALUES('" + a_name.replaceAll("'", "''") + "', '" 
				+ a_type + "')";
		
		Statement insertStmt = m_dbConn.createStatement();
		insertStmt.execute( insertString );
	
		return true;
	}
	
	/**
	 * Update the given playlist's name
	 * 
	 * @param a_name String, the playlist to update
	 * @param a_newName String, the new playlist name
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updatePlayListName(String a_name, String a_newName) 
			throws SQLException{
		
		if( !playlistExists( a_name ) ){
			return false;
		}
		
		String updateString = "UPDATE " + TableType.PLAYLIST.toString() 
				+ " SET PLNAME = '" + a_newName.replaceAll("'", "''") 
				+ "' WHERE PLNAME = '" 
				+ a_name.replaceAll("'", "''") + "'";
		Statement updateStmt = m_dbConn.createStatement();
		updateStmt.execute( updateString );
		return true;
	}	
	
	/**
	 * Helper Function. Add the given media to the provided 
	 *  playlist and database table.
	 *  
	 * @param a_playlistName String, the playlist to add to
	 * @param a_mediaName	String, the name of the media to add
	 * 	to the given playlist
	 * @param a_mediaPath String, the filepath to the media being
	 * 	added to the given playlist
	 * @param a_table TableType, the database table to add the media to
	 * @return true if the add is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	private boolean addMediaToPlayList(String a_playlistName, 
			String a_mediaName, String a_mediaPath, 
			TableType a_table) throws SQLException{
		
		if( !playlistExists( a_playlistName ) ){
			return false;
		}
		
		String insertString = "INSERT INTO " + a_table.toString()
				+ " VALUES('"+ a_playlistName.replaceAll("'", "''") 
				+ "', '" + a_mediaName.replaceAll("'", "''") +"', '"
				+ a_mediaPath.replaceAll("'", "''") + "')";
		
		Statement insertStmt = m_dbConn.createStatement();
		insertStmt.execute( insertString );
		
		return true;
	}
	
	/**
	 * Add the given song to the given playlist
	 * 
	 * @param a_playlistName String, the playlist to add to
	 * @param a_songName Song, the name of the song to add
	 * @param a_songPath String, the path to the song to add
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean addSongToPlayList(String a_playlistName, 
			String a_songName, String a_songPath) throws SQLException{
		if( songInPL(a_playlistName, a_songPath) ){
			return false;
		}
		
		return addMediaToPlayList( a_playlistName, a_songName, 
				a_songPath, TableType.MUSICPL );
	}
	
	/**
	 * Add the given video to the given playlist
	 * 
	 * @param a_playlistName String, the playlist to add to
	 * @param a_videoName String, the name of the video to add
	 * @param a_videoPath String, the path of the video to add
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean addVideoToPlayList(String a_playlistName, 
			String a_videoName, String a_videoPath) throws SQLException{
		if( videoInPL( a_playlistName, a_videoPath ) ){
			return false;
		}
		
		return addMediaToPlayList( a_playlistName, a_videoName, 
				a_videoPath, TableType.VIDEOPL );
	}
	
	// *********************************************************
	// ******************** Data Drop Functions ****************
	// *********************************************************
	/**
	 * Delete a song from the database
	 * 
	 * @param a_song Song, the song to delete
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropSong(Song a_song) throws SQLException{
		if( !songExists( a_song.getFilePath() ) ){
			return false;
		}
		
		String deleteString = "DELETE FROM " + TableType.MUSIC.toString()
				+ " WHERE FILELOC = ?";
		PreparedStatement deleteStmt = 
				m_dbConn.prepareStatement( deleteString );
		deleteStmt.setString(1, a_song.getFilePath() );
		return deleteStmt.execute();
	}
	
	/**
	 * Delete a video from the database
	 * 
	 * @param a_video Video, the video to delete	
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropVideo(Video a_video) throws SQLException{
		if( !videoExists( a_video.getFilePath() ) ){
			return false;
		}
		
		String deleteString = "DELETE FROM " + TableType.VIDEO.toString()
				+ " WHERE FILELOC = ?";
		PreparedStatement deleteStmt = 
				m_dbConn.prepareStatement( deleteString );
		deleteStmt.setString(1, a_video.getFilePath() );
		return deleteStmt.execute();
	}
	
	/**
	 * Delete a playlist from the database
	 * 
	 * @param a_playlistName String, the playlist to delete
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropPlaylist(String a_playlistName) throws SQLException{
		if( !playlistExists( a_playlistName ) ){
			return false;
		}
		
		String deleteString = "DELETE FROM " + TableType.PLAYLIST.toString()
				+ " WHERE PLNAME = ?";
		PreparedStatement deleteStmt = 
				m_dbConn.prepareStatement( deleteString );
		deleteStmt.setString(1, a_playlistName );
		return deleteStmt.execute();
	}
	
	/**
	 * Delete a song from a playlist
	 * 
	 * @param a_songFilePath String, the path to the song to delete
	 * @param a_playlist String, the playlist to delete from
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropSongFromPlaylist(
			String a_songFilePath, String a_playlist) throws SQLException{
		
		if( !playlistExists( a_playlist ) || !songExists( a_songFilePath ) ){
			return false;
		}
		
		String deleteString = "DELETE FROM " + TableType.MUSICPL.toString()
				+ " WHERE PLNAME = ? AND FILELOC = ?";
		
		PreparedStatement deleteStmt = 
				m_dbConn.prepareStatement( deleteString );
		deleteStmt.setString( 1, a_playlist );
		deleteStmt.setString( 2, a_songFilePath );
		return deleteStmt.execute();
	}
	
	/**
	 * Delete a video from a playlist
	 * 
	 * @param a_videoFilePath String, the path to the video to delete
	 * @param a_playlist String, the playlist to delete
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropVideoFromPlaylist(
			String a_videoFilePath, String a_playlist) throws SQLException{
		
		if( !playlistExists( a_playlist ) || 
				!videoExists( a_videoFilePath ) ){
			return false;
		}
		
		String deleteString = "DELETE FROM " + TableType.VIDEOPL.toString()
				+ " WHERE PLNAME = ? AND FILELOC = ?";
		
		PreparedStatement deleteStmt = 
				m_dbConn.prepareStatement( deleteString );
		deleteStmt.setString( 1, a_playlist );
		deleteStmt.setString( 2, a_videoFilePath );
		return deleteStmt.execute();
	}
}

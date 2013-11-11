package library.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.sun.jna.NativeLibrary;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

/**
 * The core class that manages user media objects, playlists, 
 * database interactions, and basic media player information.
 * 
 * @author Andrzej Brynczka
 */
public class Library {

	/**
	 * Enumeration specifying the type of media player currently
	 * being utilized by the library 
	 * @author Andrzej Brynczka
	 */
	public enum PlayerBase{
		/** The basic media player provided through JavaFX */
		DEFAULT,
		/** The VLC-based media player provided through interaction 
		 * with the VLC libraries.
		 */
		VLC;//VLC based media player
	};
	
	/**
	 * Enumeration specifying the type of playlist 
	 * to access or request
	 * @author Andrzej Brynczka
	 *
	 */
	public enum PLType{
		/** The main playlist containing all songs/videos being displayed */
		MEDIA_DISPLAY,
		/** The playlist showing all songs to be played
		 * or queued by the media player
		 */
		NOW_PLAYING_SONGS,
		
		/** The playlist showing all videos to be played or queued
		 * by the media player
		 */
		NOW_PLAYING_VIDEOS,
		
		/** The list holding media items for that are
		 * contained within a user-selected playlist
		 */
		PLAYLIST_CONTENTS;
	}
	
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************
	/** The maximum size of a user-created playlist */
	public static final int MAX_PLAYLIST_NAME_SIZE = 30;
	
	/** The name of the libvlc library file required to run the
	 * VLC-based media player
	 */
	public static final String FILENAME_LIBVLC = "libvlc.dll";
	
	/** The name of the libvlccore library file required to
	 * run the VLC-based media player
	 */
	public static final String FILENAME_LIBVLCCORE = "libvlccore.dll";
	
	/** The name of the file containing a user-specified path
	 * to the VLC library files required to run the VLC-based
	 * media player
	 */
	private static final String STORED_VLC_LIB_FILENAME = "vlcPath.txt";

	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** Reference to the database accessing object */
	private DataAccess m_database;
	
	/** Indicator of the type of media player being used */
	private SimpleObjectProperty<PlayerBase> m_playerBase;
	
	/** The path to the VLC library directory */
	private String m_vlcLibPath;
	
	/** Indicator of whether or not the library was able to successfully
	 * load the VLC-based media player from the user's given path.
	 */
	private boolean m_vlcPathSetSuccessfully;

	/** The "now playing" list of songs, to be played/queued by the
	 * media player
	 */
	private ObservablePlaylist<Song> m_nowPlayingSongList;
	 
	 /** The "now playing" list of videos, to be played/queued by the
	  * media player
	  */
	private ObservablePlaylist<Video> m_nowPlayingVideoList;
	
	/**
	 * The list holding all MediaItems that are contained within
	 * a user-selected playlist(allowing the playlist contents to be shown
	 * and held)
	 */
	private ObservablePlaylist m_plContentList;
	
	/**
	 * The list holding all currently displayed MediaItems
	 */
	private ObservablePlaylist m_mediaDisplayList;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * The constructor for the library object
	 */
	public Library(){
		m_database = new DataAccess();
		m_playerBase = 
				new SimpleObjectProperty<PlayerBase>(PlayerBase.DEFAULT);
		m_vlcLibPath = "";
		m_vlcPathSetSuccessfully = false;
		
		//attempt to load a VLC library path from a saved file
		loadVlcLibPath();
		
		//initialize the item lists
		m_nowPlayingSongList = new ObservablePlaylist("Now Playing");
		m_nowPlayingVideoList = new ObservablePlaylist("Now Playing");
		m_mediaDisplayList = new ObservablePlaylist("Display List");
		m_plContentList = new ObservablePlaylist("Playlist Contents");
		
		//fill the basic media display list from the database
		try {
			m_mediaDisplayList.set( getAllSongs() );
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to load songs on initial start.");
			System.out.println(e.getMessage());
		}	
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Determine if the VLC media player has yet to be successfully
	 * loaded.
	 * 
	 * @return true if the player was successfully loaded from the
	 *  provided VLC path, false if not
	 */
	public boolean getVlcPathSuccess(){
		return m_vlcPathSetSuccessfully;
	}
		
	/**
	 * Get the provided VLC library directory path
	 * @return String containing the path to the provided directory
	 */
	public String getVlcLibPath(){
		return m_vlcLibPath;
	}
	
	
	/**
	 * Get the SimpleObjectProperty for the PlayerBase value
	 * 
	 * @return the SimpleObjectProperty
	 */
	public SimpleObjectProperty<PlayerBase> getPlayerBaseProperty(){
		return m_playerBase;
	}
	
	/**
	 * Get the PlayerBase value indicating the media player currently
	 * 	being used
	 * 
	 * @return the PlayerBase value
	 */
	public PlayerBase getPlayerBase(){
		return m_playerBase.getValue();
	}
	
	/**
	 * Get the SizeProperty for the ObservableList containing all 
	 *  displayed media items
	 * @return the SimpleIntegerProperty containing the SizeProperty
	 */
	public SimpleIntegerProperty getDisplayedMediaListSizeProperty(){
		return m_mediaDisplayList.sizeProperty();
	}
	
	/**
	 * Get the ObservableList for the desired playlist
	 * 
	 * @param a_listType PLType, the type of playlist to get the
	 * 	ObservableList for
	 * @return the desired list's ObservableList, or null if 
	 * 	the provided list type is invalid
	 */
	public ObservableList getPlaylistObservableList( PLType a_listType ){
		switch( a_listType ){
		case NOW_PLAYING_SONGS:
			return m_nowPlayingSongList.getItemsAsObservable();
		case NOW_PLAYING_VIDEOS:
			return m_nowPlayingVideoList.getItemsAsObservable();
		case PLAYLIST_CONTENTS:
			return m_plContentList.getItemsAsObservable();
		case MEDIA_DISPLAY:
			return m_mediaDisplayList.getItemsAsObservable();		
		default:
			return null;
		}
	}
	
	/**
	 * Get the ObservablePlaylist for the desired playlist
	 * @param a_listType PLType, the type of playlist to get the
	 * 	ObservablePlaylist for
	 * @return the desired list's ObservableList, or null if
	 * 	the provided list type is invalid
	 */
	public ObservablePlaylist getPlaylist( PLType a_listType ){
		switch( a_listType ){
		case NOW_PLAYING_SONGS:
			return m_nowPlayingSongList;
		case NOW_PLAYING_VIDEOS:
			return m_nowPlayingVideoList;
		case PLAYLIST_CONTENTS:
			return m_plContentList;
		case MEDIA_DISPLAY:
			return m_mediaDisplayList;		
		default:
			return null;
		}
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the PlayerBase for the library, indicating the type of
	 *  mediaplayer is to be used
	 *  
	 * @param a_base PlayerBase, the PlayerBase to use
	 * @return true if the PlayerBase was set, false if it was null
	 * @author Andrzej Brynczka
	 */
	public boolean setPlayerBase(PlayerBase a_base){
		if( a_base == null ){
			return false;
		}
		
		m_playerBase.setValue( a_base );
		return true;
	}
	
	/**
	 * Set the success status of the library's attempt at loading
	 * the VLC media player from the user-provided m_vlcLibPath.
	 * 
	 * @param a_successful boolean, the success status
	 */
	public void setVlcPathSetSuccess(boolean a_successful){
		m_vlcPathSetSuccessfully = a_successful;
	}

	/**
	 * Set the directory to the VLC library and program files
	 * 
	 * @param a_path String, the path to the directory containing the files
	 * @return true if the path was set, false if the path was null
	 */
	public boolean setVlcLibPath(String a_path){
		if( a_path == null ){
			return false;
		}
		
		m_vlcLibPath = a_path;
		return true;
	}
	
	/**
	 * Add the Collection of Song objects to the "now playing" song
	 * 	list, to be played/queued by the media player.
	 * 
	 * @param a_collection Collection, the Songs to add to the list
	 * @return true if the Songs are added, false if the Collection
	 * 	was null
	 * @author Andrzej Brynczka
	 */
	public boolean addToNowPlayingSongList( Collection<Song> a_collection ){
		if( a_collection == null ){
			return false;
		}
		
		return m_nowPlayingSongList.addAll( a_collection );
	}
	
	/**
	 * Add the Collection of Video objects to the "now playing" video
	 * 	list, to be played/queued by the media player.
	 * 
	 * @param a_collection Collection, the Videos to add to the list
	 * @return true if the Videos are added, false if the Collection
	 * 	was null
	 * @author Andrzej Brynczka
	 */
	public boolean addToNowPlayingVideoList( Collection<Video> a_collection ){
		if( a_collection == null ){
			return false;
		}
		
		return m_nowPlayingVideoList.addAll( a_collection );
	}
	
	/**
	 * Add the given MediaItem to the main media display list
	 * 
	 * @param a_item MediaItem, the item to add to the display list
	 * @return true if the item was added, false if the item was null
	 * @author Andrzej Brynczka
	 */
	public boolean addToDisplayedMediaList( MediaItem a_item ){
		if( a_item == null ){
			return false;
		}
		
		return m_mediaDisplayList.add( a_item );
	}
	
	/**
	 * Add the given Collection of MediaItem objects to the main media
	 * 	display list
	 * 
	 * @param a_collection Collection, the items to add to the list
	 * @return true if the items were added, false if the Collection
	 * 	was null
	 * @author Andrzej Brynczka
	 */
	public boolean addToDisplayedMediaList( 
			Collection<? extends MediaItem> a_collection ){
		
		if( a_collection == null ){
			return false;
		}
		
		return m_mediaDisplayList.addAll( a_collection );
		
	}

	/**
	 * Set the given item to the "now playing" Song playlist
	 * 
	 * @param a_song Song, the Song to set
	 * @return true if the Song was set, false if the given
	 * 	Song was null
	 * @author Andrzej Brynczka
	 */
	public boolean setNowPlayingSongList( Song a_song ){
		if( a_song == null ){
			return false;
		}
		
		m_nowPlayingSongList.clear();
		return m_nowPlayingSongList.add( a_song );
	}
	
	/**
	 * Set the given Collection of Song objects to the "now playing"
	 * 	Song playlist
	 * 
	 * @param a_collection Collection, the Song objects to set to 
	 * 	the playlist
	 * @return true if the Songs were set, false if the given
	 * 	Collection was null
	 * @author Andrzej Brynczka
	 */
	public boolean setNowPlayingSongList( Collection<Song> a_collection ){
		if( a_collection == null ){
			return false;
		}
		
		return m_nowPlayingSongList.set( a_collection );
	}
	
	/**
	 * Set the given item to the "now playing" Video playlist
	 * 
	 * @param a_video Vong, the Vong to set
	 * @return true if the Video was set, false if the given
	 * 	Video was null
	 * @author Andrzej Brynczka
	 */
	public boolean setNowPlayingVideoList( Video a_video ){
		if( a_video == null ){
			return false;
		}
		
		m_nowPlayingVideoList.clear();
		return m_nowPlayingVideoList.add( a_video );
	}
	
	/**
	 * Set the given Collection of Video objects to the "now playing"
	 * 	Video playlist
	 * 
	 * @param a_collection Collection, the Video objects to set to 
	 * 	the playlist
	 * @return true if the Videos were set, false if the given
	 * 	Collection was null
	 * @author Andrzej Brynczka
	 */
	public boolean setNowPlayingVideoList( Collection<Video> a_collection ){
		if( a_collection == null ){
			return false;
		}
		
		return m_nowPlayingVideoList.set( a_collection );
	}
	
	/**
	 * Set the given MediaItem to the main media display list
	 * 
	 * @param a_item MediaItem, the item to set on the list
	 * @return true if the item was set, false if the item was null
	 * @author Andrzej Brynczka
	 */
	public boolean setDisplayedMediaList( MediaItem a_item ){
		if( a_item == null ){
			return false;
		}
		
		m_mediaDisplayList.clear();
		return m_mediaDisplayList.add( a_item );
	}

	/**
	 * Set the given Collection of media objects to the main media
	 * 	display list
	 * 
	 * @param a_collection Collection, the MediaItem objects to set to 
	 * 	the playlist
	 * @return true if the MediaItems were set, false if the given
	 * 	Collection was null
	 * @author Andrzej Brynczka
	 */
	public boolean setDisplayedMediaList( 
			Collection<? extends MediaItem> a_collection ){
		if( a_collection == null){
			return false;
		}
		
		return m_mediaDisplayList.set( a_collection );
	}
	
	/**
	 * Remove the given Song from the "now playing" song list
	 * 
	 * @param a_song Song, the song to remove from the list
	 * @return true if the song was removed, false if the song was
	 * 	not on the list or if it was null
	 * @author Andrzej Brynczka
	 */
	public boolean removeFromNowPlayingSongList( Song a_song ){
		if( a_song == null ){
			return false;
		}
		
		return m_nowPlayingSongList.remove( a_song );
	}
	
	/**
	 * Remove the given Collection of Song objects from the
	 * 	"now playing" song list
	 * 
	 * @param a_collection Collection, the songs to remove
	 * @return true if even one of the songs in the given
	 * 	Collection was removed, false if none were removed
	 * 	or the Collection was null
	 * @author Andrzej Brynczka
	 */
	public boolean removeFromNowPlayingSongList( 
			Collection<Song> a_collection ){
		
		if( a_collection == null ){
			return false;
		}
		
		return m_nowPlayingSongList.removeAll( a_collection );
	}
	
	/**
	 * Remove the given Video from the "now playing" video list
	 * 
	 * @param a_video Video, the video to remove from the list
	 * @return true if the video was removed, false if the video was
	 * 	not on the list or if it was null
	 * @author Andrzej Brynczka
	 */
	public boolean removeFromNowPlayingVideoList( Video a_video ){
		if( a_video == null ){
			return false;
		}
		
		return m_nowPlayingVideoList.remove( a_video );
	}
	
	/**
	 * Remove the given Collection of Video objects from the
	 * 	"now playing" video list
	 * 
	 * @param a_collection Collection, the videos to remove
	 * @return true if even one of the videos in the given
	 * 	Collection was removed, false if none were removed
	 * 	or the Collection was null
	 * @author Andrzej Brynczka
	 */
	public boolean removeFromNowPlayingVideoList( 
			Collection<Video> a_collection ){
		
		if( a_collection == null ){
			return false;
		}
		
		return m_nowPlayingVideoList.removeAll( a_collection );
	}
	
	/**
	 * Remove the given MediaItem from the main media display
	 * 	list
	 * 
	 * @param a_item MediaItem, the item to remove
	 * @return true if the item was removed, false if the item was 
	 * 	not found in the list or if it was null
	 * @author Andrzej Brynczka
	 */
	public boolean removeFromDisplayedMediaList( MediaItem a_item ){
		if( a_item == null ){
			return false;
		}
		
		return m_mediaDisplayList.remove( a_item );	
	}
	
	/**
	 * Remove the given Collection of MediaItem objects from the
	 * 	main media display list
	 * 
	 * @param a_collection Collection, the items to remove
	 * @return true if even one of the items in the given
	 * 	Collection was removed, false if none were removed
	 * 	or the Collection was null
	 * @author Andrzej Brynczka
	 */
	public boolean removeFromDisplayedMediaList( 
			Collection<MediaItem> a_collection ){
		
		if( a_collection == null ){
			return false;
		}
		
		m_mediaDisplayList.removeAll( a_collection );
		return true;
	}
	
	/**
	 * Clear the main media display list
	 */
	public void clearDisplayedMediaList(){
		m_mediaDisplayList.clear();
	}
	
	/**
	 * Clear the "now playing" video list
	 */
	public void clearNowPlayingVideoList(){
		m_nowPlayingVideoList.clear();
	}
	
	/**
	 * Clear the "now playing" song list
	 */
	public void clearNowPlayingSongList(){
		m_nowPlayingSongList.clear();
	}
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************

	/**
	 * Add the user-specified VLC library path to the
	 * NativeLibrary search path, allowing for an attempt to
	 * load the VLC media player.
	 * 
	 * @see {@link Library#setVlcLibPath(String)}, 
	 * {@link Library#getVlcLibPath()}
	 */
	public void addVlcLibToNativeSearch(){
		NativeLibrary.addSearchPath( "libvlc", m_vlcLibPath );
		System.out.println("Set libvlc to the native search path");
		
	}

	/**
	 * Save the contained path to the VLC program and library directory
	 * to a Library-designated file. This file will be loaded upon 
	 * future startup, allowing for this Library to maintain knowledge
	 * of a successful VLC directory.
	 * 
	 * </br></br>
	 * Set the VLC directory path with {@link #setVlcLibPath(String)}
	 * @return true if the path was saved for future use, false
	 * 	if otherwise
	 */
	public boolean saveVlcLibPath(){
		File libPathStoreFile = new File( STORED_VLC_LIB_FILENAME );
		
		try{
			if( !libPathStoreFile.exists() ){
				libPathStoreFile.createNewFile();
			}
			
			//open the file writer in a non-appendable mode
			BufferedWriter fileOut = new BufferedWriter( 
					new FileWriter( libPathStoreFile, false ) );

			//store the directory and close the file for future use
			fileOut.write( m_vlcLibPath );
			fileOut.flush();
			fileOut.close();
			
			System.out.println("Saved the vlc path to " 
					+ libPathStoreFile.getAbsolutePath() );
		}catch( IOException e){
			System.out.println("Error: unable to write location of libvlc.dll"
					+ " into file " + libPathStoreFile.getAbsolutePath() );
			return false;
		}
		return true;
	}
	
	/**
	 * Load the VLC library and program directory from the file
	 * 	designated by {@link #STORED_VLC_LIB_FILENAME }. This allows the
	 * 	loaded directory path to be used to attempt to open the 
	 * 	VLC-based media player after adding it to the NativeLibrary
	 * 	search path.
	 * 
	 * @return true if the VLC library directory path was loaded
	 * 	from the stored file, false if not
	 * @see {@link Library#addVlcLibToNativeSearch() }
	 */
	private boolean loadVlcLibPath(){
		File storedLibFile = new File(STORED_VLC_LIB_FILENAME);

		if( !storedLibFile.exists() ){
			System.out.println("Stored path to libvlc.dll not found");
			return false;
		}
		
		System.out.println("Stored path to libvlc.dll found in: " 
				+ storedLibFile.getAbsolutePath() );
		
		try{
			BufferedReader fileIn = new BufferedReader( 
					new FileReader( storedLibFile ) );
		
			m_vlcLibPath = fileIn.readLine();
			fileIn.close();
		}catch( IOException e ){
			System.out.println("Unable to read libvlc.dll path from "
					+ storedLibFile.getAbsolutePath() );
		}
		
		return true;
	}
	
	/**
	 * Determine if the given directory file path contains the
	 * 	necessary VLC library files
	 * 
	 * @param a_directoryPath
	 * @return true if the provided directory contains the 
	 * 	required files, false if the path provided is not a file
	 * 	directory or if it does not contain the required files
	 * @see {@link Library#FILENAME_LIBVLC} and 
	 * {@link Library#FILENAME_LIBVLCCORE }
	 */
	public boolean pathContainsVlcDll(String a_directoryPath){
		File directory = new File( a_directoryPath );
		if( !directory.exists() ){
			return false;
		}
		
		//search the directory for libvlc.dll and libvlccore.dll
		String[] fileNames = directory.list();
		boolean libVlcExists = false;
		boolean libVlcCoreExists = false;
		
		for(int fileIndex = 0; fileIndex < fileNames.length; fileIndex++ ){
			if( fileNames[fileIndex].equalsIgnoreCase( 
					Library.FILENAME_LIBVLC ) ){
				libVlcExists = true;
			}
			
			if( fileNames[fileIndex].equalsIgnoreCase(
					Library.FILENAME_LIBVLCCORE) ){
				libVlcCoreExists = true;
			}
			
			if( libVlcExists && libVlcCoreExists ){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Set the playlist-content list with items contained within
	 * 	the given playlist. This loads information from the 
	 * 	database's song-based playlist data.
	 * 
	 * @param a_playlistName String, the name of the playlist
	 * 	whose items are to be loaded from the database
	 * @return true if the items were loaded, false if the 
	 * 	given playlist name was null or if an error occured
	 * 	during the attempt to load the contents
	 * @author Andrzej Brynczka
	 */
	public boolean setSongPLContentList( String a_playlistName ){
		if( a_playlistName == null ){
			return false;
		}
		try {
			m_plContentList.set( getSongsInPlaylist( a_playlistName ) );
			m_plContentList.setName( a_playlistName );
		} catch (SQLException e) {
			e.printStackTrace();
			m_plContentList.clear();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Set the playlist-content list with items contained within
	 * 	the given playlist. This loads information from the 
	 * database's video-based playlist data.
	 * 
	 * @param a_playlistName String, the name of the playlist
	 * 	whose items are to be loaded from the database
	 * @return true if the items were loaded, false if the 
	 * 	given playlist name was null or if an error occured
	 * 	during the attempt to load the contents
	 * @author Andrzej Brynczka
	 */
	public boolean setVideoPLContentList( String a_playlistName ){
		try {
			m_plContentList.set( getVideosInPlaylist( a_playlistName ) );
			m_plContentList.setName( a_playlistName );
		} catch (SQLException e){
			e.printStackTrace();
			m_plContentList.clear();
			return false;
		}
		
		return true;
	}

	/**
	 * Move the item at the given index down within the desired list
	 * 	(further from the 0 index).
	 * @param a_type PLType, the type of playlist to modify
	 * @param a_index int, the index to move down
	 * @return true if the move succeeds, false if the index is out of bounds
	 * @author Andrzej Brynczka
	 */
	public boolean moveDownInList(PLType a_type, int a_index){
		
		switch( a_type ){
			case MEDIA_DISPLAY:
				return m_mediaDisplayList.moveDownInList(a_index);
			case NOW_PLAYING_SONGS:
				return m_nowPlayingSongList.moveDownInList(a_index);
			case NOW_PLAYING_VIDEOS:
				return m_nowPlayingVideoList.moveDownInList(a_index);
			case PLAYLIST_CONTENTS:
				return m_plContentList.moveDownInList(a_index);
			default:
				return false;
		}
	}

	/**
	 * Move the item at the given index up within the designated list
	 *  (closer to the 0 index)
	 * @param a_type PLType, the type of list to modify
	 * @param a_index int, the index to move up
	 * @return true if the move succeeds, false if the index is out of bounds
	 * @author Andrzej Brynczka
	 */
	public boolean moveUpInList(PLType a_type, int a_index){
		
		switch( a_type ){
			case MEDIA_DISPLAY:
				return m_mediaDisplayList.moveUpInList(a_index);
			case NOW_PLAYING_SONGS:
				return m_nowPlayingSongList.moveUpInList(a_index);
			case NOW_PLAYING_VIDEOS:
				return m_nowPlayingVideoList.moveUpInList(a_index);
			case PLAYLIST_CONTENTS:
				return m_plContentList.moveUpInList(a_index);
			default:
				return false;
		}
	}
	
	// *********************************************************
	// **************** Database Selectors *********************
	// *********************************************************
	
	/**
	 * Determine if the given playlist is a video-based playlist
	 * 
	 * @param a_playlistName String, the playlist to check against
	 * @return true if the playlist is video-based, false otherwise
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public boolean isVideoPlaylist(String a_playlistName) throws SQLException{
		if( a_playlistName == null ){
			return false;
		}
		return m_database.isVideoPlaylist(a_playlistName);
	}
	
	/**
	 * Determine if the given playlist is a song-based playlist
	 * 
	 * @param a_playlistName String, the playlist to check against
	 * @return true if the playlist is song-based, false otherwise
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public boolean isMusicPlaylist(String a_playlistName) throws SQLException{
		if( a_playlistName == null ){
			return false;
		}
		return m_database.isMusicPlaylist(a_playlistName);
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
		return m_database.getAllSongs();
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
		return m_database.getAllSongsWithName(a_name, a_exact);
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
		return m_database.getAllSongsWithArtist(a_artist, a_exact);
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
		return m_database.getAllSongsWithAlbum(a_album, a_exact);
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
		return m_database.getAllSongsWithGenre(a_genre, a_exact);
	}
	
	/**
	 * Get all of the videos contained within the database
	 * 
	 * @return the Collection of videos
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Video> getAllVideos() throws SQLException{
		return m_database.getAllVideos();
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
		return m_database.getAllVideosWithName(a_name, a_exact);
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
		return m_database.getAllVideosWithGenre(a_genre, a_exact);
	}
	

	/**
	 * Get the names of all playlists contained with the database
	 * 
	 * @return Collection of playlist names
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<String> getAllPlaylistNames() throws SQLException{
		return m_database.getAllPlayListNames();
	}
	
	

	/**
	 * Get all of the songs contained within the given playlist
	 * 
	 * @param a_playlist String, the playlist to search against
	 * @return Collection of songs contained within the playlist
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Song> getSongsInPlaylist( String a_playlist ) 
			throws SQLException{
		return m_database.getSongsInPL( a_playlist );
	}
	
	/**
	 * Get all of the videos contained within the given playlist
	 * 
	 * @param a_playlist String, the playlist to search against
	 * @return Collection of vidoes contained within the playlist
	 * @throws SQLException if an error occurs when querying 
	 * 	the database
	 * @author Andrzej Brynczka
	 */
	public Collection<Video> getVideosInPlaylist( String a_playlist ) 
			throws SQLException{
		return m_database.getVideosInPL( a_playlist );
	}
	// *********************************************************
	// **************** Database Mutators **********************
	// *********************************************************
	/**
	 * Save the given song data within the database
	 * 
	 * @param a_song Song, the song data to save
	 * @return true if the save is successful, false otherwise
	 * @throws SQLException if an error occurs during the
	 * 	database query
	 * @author Andrzej Brynczka
	 */
	public boolean saveSong( Song a_song ) throws SQLException{
		if( a_song == null ){
			return false;
		}
		
		return m_database.saveSong( a_song.getName(), a_song.getArtist(), 
				a_song.getAlbum(), a_song.getLengthAsLong(), 
				a_song.getGenre(), a_song.getFilePath()  );
	}
	
	/**
	 * Update the given song's name within the database
	 * 	(and file tags if supported)
	 * 
	 * @param a_song Song, the song to update
	 * @param a_newName String, the new name to give the song
	 * @return true if the update was successful
	 * @throws Exception if an error occurs during the update
	 * @author Andrzej Brynczka
	 */
	public boolean updateSongName( Song a_song, String a_newName ) 
			throws Exception{
		
		if( m_database.updateSongName( 
				a_song.getName(), a_song.getFilePath(), a_newName) == false){	
			return false;
		}
		
		//modify the file's tags after a successful database write
		MP3File musicFile = 
				(MP3File)AudioFileIO.read( new File( a_song.getFilePath() ) );
		
		//read meta data if it exists
		Tag metaTag = musicFile.getTagAndConvertOrCreateAndSetDefault();
		metaTag.setField( FieldKey.TITLE, a_newName );
		System.out.println("---new title value: " + a_newName);
		musicFile.setTag( metaTag );
		musicFile.save();
		System.out.println( a_song.getFilePath() );
		System.out.println( a_song.getName() );
		return true;
	}
	
	/**
	 * Update the given song's artist name within the database
	 * 	(and its file tag if supported)
	 * 
	 * @param a_song Song, the song to update
	 * @param a_newArtist String, the new artist value
	 * @return true if the update is succesfful
	 * @throws Exception if an error occurs during the update
	 * @author Andrzej Brynczka
	 */
	public boolean updateSongArtist( Song a_song, String a_newArtist ) 
			throws Exception{
		if( m_database.updateSongArtist(
				a_song.getFilePath(), a_newArtist) == false ){
			return false;
		}
		
		//update the song's Artist metatag
		MP3File musicFile = 
				(MP3File)AudioFileIO.read( new File( a_song.getFilePath() ) );
		
		//read meta data if it exists
		Tag metaTag = musicFile.getTagAndConvertOrCreateAndSetDefault();
		metaTag.setField( FieldKey.ARTIST, a_newArtist );
		
		musicFile.setTag( metaTag );
		musicFile.save();
		
		return true;
	}
	
	/**
	 * Update the given song's album in the database
	 *  (and its file tag if supported)
	 *  
	 * @param a_song Song, the song to update
	 * @param a_newAlbum String, the new album value
	 * @return true if update is succesfful
	 * @throws Exception if an error occurs during the update
	 * @author Andrzej Brynczka
	 */
	public boolean updateSongAlbum( Song a_song, String a_newAlbum ) 
			throws Exception {
		if( m_database.updateSongAlbum(
				a_song.getFilePath(), a_newAlbum) == false ){
			return false;
		}
		
		//update the song's Album metatag
		MP3File musicFile = 
				(MP3File)AudioFileIO.read( new File( a_song.getFilePath() ) );
		
		//read meta data if it exists
		Tag metaTag = musicFile.getTagAndConvertOrCreateAndSetDefault();
		metaTag.setField( FieldKey.ALBUM, a_newAlbum );
		
		musicFile.setTag( metaTag );
		musicFile.save();
		
		return true;
	}
	
	/**
	 * Update the given Song's genre tag in the database
	 *  (and its file tag if supported)
	 *  
	 * @param a_song Song, the song to update
	 * @param a_newGenre String, the new genre value
	 * @return true if update is successful
	 * @throws Exception if an error occurs during the update
	 * @author Andrzej Brynczka
	 */
	public boolean updateSongGenre( Song a_song, String a_newGenre ) 
			throws Exception {
		if( m_database.updateSongGenre(
				a_song.getFilePath(), a_newGenre) == false ){
			return false;
		}
		
		//update the song's Genre metatag
		MP3File musicFile = 
				(MP3File)AudioFileIO.read( new File( a_song.getFilePath() ) );
		
		//read meta data if it exists
		Tag metaTag = musicFile.getTagAndConvertOrCreateAndSetDefault();
		metaTag.setField( FieldKey.GENRE, a_newGenre );
		
		musicFile.setTag( metaTag );
		musicFile.save();
		
		return true;
	}

	/**
	 * Save the video data within the database
	 * 
	 * @param a_video Video, the data to save
	 * @return true if the save is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean saveVideo( Video a_video ) throws SQLException{
		if( a_video == null ){
			return false;
		}
		
		return m_database.saveVideo( 
				a_video.getName(), a_video.getLengthAsLong(), 
				a_video.getGenre(), a_video.getFilePath() );
	}


	/**
	 * Update the given video's name within the database
	 * 
	 * @param a_video Video, the video to update
	 * @param a_newName String, the new video name
	 * @return true if the update is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updateVideoName( Video a_video, String a_newName )
			throws SQLException{
		return m_database.updateVideoName(
				a_video.getName(), a_video.getFilePath(), a_newName);
	}
	
	/**
	 * Update the given video's genre within the database
	 * 
	 * @param a_video Video, the video to update
	 * @param a_newGenre String, the new genre value
	 * @return true if the update is successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updateVideoGenre( Video a_video, String a_newGenre ) 
			throws SQLException{
		return m_database.updateVideoGenre(
				a_video.getName(), a_video.getFilePath(), a_newGenre);
	}
	
	/**
	 * Save the given playlist within the database
	 * 
	 * @param a_playlistName String, the playlist name
	 * @param a_type String, the playlist type(MUSIC, VIDEO)
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean savePlayList( String a_playlistName, String a_type ) 
			throws SQLException{
		if( a_playlistName == null || a_type == null ){
			return false;
		}
		
		return m_database.savePlayListName( a_playlistName, a_type );
	}
	
	/**
	 * Update the given playlist's name
	 * 
	 * @param a_playlistName String, the playlist to update
	 * @param a_newName String, the new playlist name
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean updatePlayListName( String a_playlistName, String a_newName) 
			throws SQLException{
		return m_database.updatePlayListName( a_playlistName, a_newName);
	}
	
	/**
	 * Add the given song to the given playlist
	 * 
	 * @param a_playlistName String, the playlist to add to
	 * @param a_song Song, the song to add
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean addSongToPlaylist( String a_playlistName, Song a_song ) 
			throws SQLException{
		if( m_plContentList != null && 
				m_plContentList.getName().equals( a_playlistName ) &&
					!m_plContentList.contains( a_song ) ){
			
			m_plContentList.add( a_song );
		}
		
		return m_database.addSongToPlayList(
				a_playlistName, a_song.getName(), a_song.getFilePath());
	}
	
	/**
	 * Add the given video to the given playlist
	 * 
	 * @param a_playlistName String, the playlist to add to
	 * @param a_video Video, the video to add
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean addVideoToPlaylist( String a_playlistName, Video a_video ) 
			throws SQLException{
		if( m_plContentList != null && 
				m_plContentList.getName().equals( a_playlistName ) &&
					!m_plContentList.contains( a_video ) ){
			
			m_plContentList.add( a_video );
		}
		
		return m_database.addVideoToPlayList(
				a_playlistName, a_video.getName(), a_video.getFilePath());
	} 
	
	
	/**
	 * Delete a song from the database
	 * 
	 * @param a_song Song, the song to delete
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropSong(Song a_song) throws SQLException{
		if( m_plContentList != null ){
			m_plContentList.remove( a_song );
		}
		
		removeFromNowPlayingSongList( a_song );
		removeFromDisplayedMediaList( a_song );
		return m_database.dropSong(a_song);
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
		if( m_plContentList != null ){
			m_plContentList.remove( a_video );
		}
		
		removeFromNowPlayingVideoList( a_video );
		removeFromDisplayedMediaList( a_video );
		return m_database.dropVideo( a_video );
	}
	
	/**
	 * Delete a playlist from the database
	 * 
	 * @param a_name String, the playlist to delete
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropPlaylist(String a_name) throws SQLException{
		if( m_plContentList != null && 
				m_plContentList.getName().equals( a_name ) ){
			m_plContentList = null;
		}
		return m_database.dropPlaylist( a_name );
	}
	
	/**
	 * Delete a song from a playlist
	 * 
	 * @param a_song Song, the song to delete
	 * @param a_playlist String, the playlist to delete from
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropSongFromPlaylist(Song a_song, String a_playlist) 
			throws SQLException{
		if( m_plContentList != null && 
				m_plContentList.getName().equals( a_playlist ) ){
			
			m_plContentList.remove( a_song );

		}
		return m_database.dropSongFromPlaylist( 
				a_song.getFilePath(), a_playlist );
	}
	
	/**
	 * Delete a video from a playlist
	 * 
	 * @param a_video Video, the video to delete
	 * @param a_playlist String, the playlist to delete
	 * @return true if successful
	 * @throws SQLException if an error occurs during the query
	 * @author Andrzej Brynczka
	 */
	public boolean dropVideoFromPlaylist(Video a_video, String a_playlist) 
			throws SQLException{
		if( m_plContentList != null && 
				m_plContentList.getName().equals( a_playlist ) ){
			
			m_plContentList.remove( a_video );
		}
		return m_database.dropVideoFromPlaylist( 
				a_video.getFilePath(), a_playlist );
	}
}

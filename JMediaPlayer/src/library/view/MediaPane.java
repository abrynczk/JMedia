package library.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;



import library.model.Library;
import library.model.MediaItem;
import library.model.Song;
import library.model.Video;
import library.model.Library.PLType;
import library.view.MediaPlayerRegion.PlayerType;
import library.view.component.PlaylistTreeCell;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TreeView.EditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;

/**
 * Class meant to display media information to the user. Also contains a 
 * reference to the mediaplayer and its view, basing its display organization
 * based upon the mediaplayer's status.
 * 
 * @author Andrzej Brynczka
 *
 */
public class MediaPane extends BorderPane {
	
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************
	
	//UI component id information used for event handling and identification
	private static final String MENU_PLAY = "Play media";
	private static final String MENU_ADD_TO_NOW_PLAYING = "Add to now playing";
	private static final String MENU_REMOVE_FROM_PLAYLIST = 
			"Remove media from playlist";
	
	private static final String MENU_REMOVE_VIDEO = "Remove video from table";
	private static final String MENU_ADD_VIDEO_TO = "Add video to playlist";
	
	private static final String MENU_REMOVE_SONG = "Remove song from table";
	private static final String MENU_ADD_SONG_TO = "Add song to playlist";
	
	private static final String NOW_PLAYING_MOVE_UP = "Move up in playlist";
	private static final String NOW_PLAYING_MOVE_DOWN = "Move down in playlist";
	private static final String NOW_PLAYING_REMOVE = "Remove from playlist";
	private static final String TEXTFIELD_CREATE_PLAYLIST = 
			"Create new playlist";
	
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** Reference to the main library object */
	private Library m_library;
	
	/** Reference to the main library view object */
	private LibraryView m_libraryView;
	
	/** Reference to the main media player component */
	private MediaPlayerRegion m_mediaPlayerRegion;
	
	/** The type of media data that is to be displayed currently */
	private MediaPlayerRegion.PlayerType m_displayedMediaType;
	
	// *********************************************************
	// ******************** GUI Components *********************
	// *********************************************************
	/** The view used to display playlist names */
	private TreeView<String> m_playlistNameTree;

	/** The main table displaying Video-based data */
	private TableView<Video> m_videoTable;
	/** ContextMenu associated to the main video table */
	private ContextMenu m_videoTableConMenu;
	
	/** The main table displaying Song-based data */
	private TableView<Song> m_songTable;
	/** ContextMenu associated to the main song table */
	private ContextMenu m_songTableConMenu;

	/** Table used to display the contents of a selected video playlist */
	private TableView<Video> m_playlistVideosTable;
	/** ContextMenu associated to the video playlist table */
	private ContextMenu m_playlistVideosTableConMenu;
	
	/** Table used to display the contents of a selected music playlist */
	private TableView<Song> m_playlistSongsTable;	
	/** ContextMenu associated to the music playlist table */
	private ContextMenu m_playlistSongsTableConMenu;
	
	/** Table used to display the currently playing list of videos */
	private TableView<Video> m_nowPlayingVideoTable;
	/** ContextMenu associated with the currently playing video list table */
	private ContextMenu m_nowPlayingVideoTableConMenu;
	
	/** Table used to display the currently playing list of songs */
	private TableView<Song> m_nowPlayingSongTable;
	/** ContextMenu associated with the currently playing song list table */
	private ContextMenu m_nowPlayingSongTableConMenu;
	
	/** Choice selection box for the video search bar, containing several
	 * search options.
	 */
	private ChoiceBox<String> m_videoSearchTypeBox;
	
	/** Choice selection box for the song search bar, containing several
	 * search options.
	 */
	private ChoiceBox<String> m_songSearchTypeBox;
	
	/** Horizontal box containing the components that compose the search box */
	private HBox m_searchBox;
	
	/** Label containing the count of total media items in the active
	 * display table. Its value is bound to the Library's main data list.
	 */
	private Label m_totalCountLabel;
	
	
	/** Component holding the components displayed at the top of this pane */
	private SplitPane m_topTableSplitPane;
	
	/** Component holding the components displayed at the bottom of this pane*/
	private VBox m_bottomTableBox;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * Constructor for the MediaPane used to display library media data.
	 * 
	 * @param a_library Library, a reference to the main Library object
	 * @param a_libraryView LibraryView, a reference to the main library view
	 * @param a_mediaPlayerRegion MediaPlayerRegion, a reference to the main
	 * 	media player component
	 * @author Andrzej Brynczka
	 */
	public MediaPane(Library a_library, LibraryView a_libraryView, 
			MediaPlayerRegion a_mediaPlayerRegion){
		
		m_library = a_library;
		m_libraryView = a_libraryView;
		m_mediaPlayerRegion = a_mediaPlayerRegion;
		m_displayedMediaType = a_mediaPlayerRegion.getPlayerType();
		
		createPlayListNameTree();
		
		//create required context menus
		m_playlistSongsTableConMenu = 
				createAndGetPlaylistTableContextMenu( PlayerType.MUSIC );
		m_playlistVideosTableConMenu = 
				createAndGetPlaylistTableContextMenu( PlayerType.VIDEO );
		
		m_songTableConMenu = 
				createAndGetMainTableContextMenu( PlayerType.MUSIC );
		m_videoTableConMenu = 
				createAndGetMainTableContextMenu( PlayerType.VIDEO );
		
		//create the song-based tables
		m_playlistSongsTable = createAndGetSongTable( 
				FXCollections.<Song>observableArrayList(), 
				m_playlistSongsTableConMenu, true);
		m_songTable = createAndGetSongTable( 
				m_library.getPlaylistObservableList( PLType.MEDIA_DISPLAY ), 
				m_songTableConMenu, true );
		m_nowPlayingSongTable = createAndGetSongTable( 
				m_library.getPlaylistObservableList( PLType.NOW_PLAYING_SONGS ), 
				createAndGetNowPlayingTableContextMenu(), false );
		m_nowPlayingSongTable.setEditable( false );
		
		//create the video-based tables
		m_playlistVideosTable = createAndGetVideoTable( 
				FXCollections.<Video>observableArrayList(), 
				m_playlistVideosTableConMenu, true);
		m_videoTable = createAndGetVideoTable( 
				m_library.getPlaylistObservableList( PLType.MEDIA_DISPLAY ), 
				m_videoTableConMenu, true );
		m_nowPlayingVideoTable = createAndGetVideoTable( 
				m_library.getPlaylistObservableList( 
						PLType.NOW_PLAYING_VIDEOS ), 
				createAndGetNowPlayingTableContextMenu(), false );
		m_nowPlayingVideoTable.setEditable( false );
		
		//update the playlist tree
		setPlaylistTreeNamesFromDatabase();
		
		//create the display
		m_searchBox = createAndGetSearchBox();
		HBox paneSwitchBox = createAndGetMediaPaneSwitch();
		HBox totalCountBox = createAndGetTotalCountBox();
		
		BorderPane utilityComponents = new BorderPane();
		utilityComponents.setLeft( paneSwitchBox );
		utilityComponents.setCenter( totalCountBox );
		utilityComponents.setRight( m_searchBox );
		utilityComponents.setStyle("-fx-background-color: lightgray;");

		m_bottomTableBox = new VBox();
		m_bottomTableBox.setMinSize(0, 0);
		VBox.setVgrow(m_songTable, Priority.ALWAYS );
		VBox.setVgrow(m_videoTable, Priority.ALWAYS );
		
		VBox playlistTreeBox = new VBox();	
		playlistTreeBox.getChildren().addAll( createAndGetAddPlaylistField(), 
				m_playlistNameTree );
		
		m_topTableSplitPane = new SplitPane();
		m_topTableSplitPane.setOrientation(Orientation.HORIZONTAL);
		m_topTableSplitPane.setDividerPositions(.15, .425, .425);
		m_topTableSplitPane.setMinHeight(0);
		
		//finalize sizes
		if( m_displayedMediaType == PlayerType.MUSIC ){
			m_bottomTableBox.getChildren().addAll( 
					utilityComponents, m_songTable );
			m_topTableSplitPane.getItems().addAll( playlistTreeBox, 
					m_playlistSongsTable, m_nowPlayingSongTable);
			m_mediaPlayerRegion.setMaxHeight( 50 );
		}
		else{
			m_bottomTableBox.getChildren().addAll( 
					utilityComponents, m_videoTable );
			m_topTableSplitPane.getItems().addAll( playlistTreeBox, 
					m_playlistVideosTable, m_nowPlayingVideoTable );
		}
		m_playlistVideosTable.setMinSize(0, 0);
		m_nowPlayingVideoTable.setMinSize(0, 0);
		m_videoTable.setMinSize(0, 0);
		m_songTable.setMinSize(0, 0);
		m_nowPlayingSongTable.setMinSize(0, 0);
		m_playlistSongsTable.setMinSize(0, 0);
		
		playlistTreeBox.setMinSize(0, 0);
		playlistTreeBox.setMaxWidth(175);
		playlistTreeBox.prefHeightProperty().bind( 
				m_topTableSplitPane.heightProperty() );
		
		m_playlistNameTree.setMinSize(0,0);
		m_playlistNameTree.prefHeightProperty().bind( 
				m_topTableSplitPane.heightProperty() );
		
		//combine the final components
		SplitPane splitpane = new SplitPane();
		splitpane.setOrientation(Orientation.VERTICAL);
		splitpane.getItems().addAll( m_mediaPlayerRegion, 
				m_topTableSplitPane, m_bottomTableBox );
		splitpane.setDividerPositions(.45, .6);
		splitpane.setMinSize( 550, 450 );
		
		setCenter(splitpane);
		
		setPrefSize( 600, 500 );
		setOnKeyPressed( getKeyShortCutHandler() );
		requestFocus();
	}
	
	
	public void close(){
		m_mediaPlayerRegion.close();
	}
    // *******************************************************************
    // **************** PlaylistName Management Functions ****************
    // *******************************************************************
	/**
	 * Helper Function.
	 * Set the m_playlistNameTree with its list of playlist names acquired
	 * 	from the database. This updates the playlist names on all associated
	 * 	Song/Video context menus as well through 
	 * 	{@link #addNewPlayListNameToTree(String, PlayerType) }
	 * 
	 * @author Andrzej Brynczka
	 */
	private void setPlaylistTreeNamesFromDatabase(){
		//clear the musicRoot and videoRoot nodes children of their playlists
		m_playlistNameTree.getRoot().getChildren().get(0).getChildren().clear();
		m_playlistNameTree.getRoot().getChildren().get(1).getChildren().clear();
		
		try {
			Collection<String> playlistNames;
			playlistNames = m_library.getAllPlaylistNames();
			for(Iterator<String> it = playlistNames.iterator(); it.hasNext();){
				String playlist = it.next();
				if( m_library.isMusicPlaylist( playlist ) ){
					System.out.println( playlist + " is a music type");
					addNewPlayListNameToTree( playlist, PlayerType.MUSIC );
				}
				else{
					System.out.println( playlist + " is a video type");
					addNewPlayListNameToTree( playlist, PlayerType.VIDEO );
				}
				
			}
		} catch (SQLException e) {
			System.out.println("Error loading all playlist names");
			System.out.println( e.getMessage() );
		}
	}
	
	/**
	 * Add a newly created playlist name to the playlist display tree.
	 * 	(Also adds the playlist name to all relevant context menus' playlist
	 * 	lists)
	 * 
	 * @param a_plName String, the name of the playlist to add
	 * @param a_playlistType PlayerType, the playlists type(Music, Video)
	 * @see {@link #addPLNameToSongContextMenus(String)} and 
	 * 	{@link #addPLNameToVideoContextMenus(String)}
	 * @author Andrzej Brynczka
	 */
	public void addNewPlayListNameToTree( String a_plName, 
			PlayerType a_playlistType ){
		if( a_plName == null ){
			return;
		}
		
		TreeItem<String> newPlaylist = new TreeItem<String>( a_plName );
		
		if( a_playlistType == PlayerType.MUSIC ){
			//the playlist was created in the music media display,
			//so it is meant to be a music playlist
			
			//add the playlist to the music playlist root(item 0)
			m_playlistNameTree.getRoot().getChildren().get(0).getChildren().
				add( newPlaylist );
			
			//add the playlist name to all music table context menus
			addPLNameToSongContextMenus( a_plName );
		}
		else{
			//new video playlist
			
			//add to the video playlist root in the tree(item 1)
			m_playlistNameTree.getRoot().getChildren().get(1).getChildren().
				add( newPlaylist );
			
			//add the playlist name to all video table context menus
			addPLNameToVideoContextMenus( a_plName );	
		}	
	}
	
	/**
	 * Add the given playlist's name to all Song-based context menus,
	 * 	allowing songs to be added to the playlist.
	 * 
	 * @param a_playlistName String, the playlist to add to all menus
	 * @author Andrzej Brynczka
	 */
	protected void addPLNameToSongContextMenus( String a_playlistName ){
		
		//create a new context item for the playlist
		MenuItem playlistNameItem = new MenuItem( a_playlistName );
		playlistNameItem.setId( MENU_ADD_SONG_TO );
		playlistNameItem.setOnAction( getMainTableContextMenuHandler() );
		
		//add the item to the "Add to:" sub menu
		Menu plMenu = (Menu) m_songTableConMenu.getItems().get(3);
		plMenu.getItems().add( playlistNameItem );
		
		//repeat for the playlist songs table menu
		playlistNameItem = new MenuItem( a_playlistName );
		playlistNameItem.setId( MENU_ADD_SONG_TO );
		playlistNameItem.setOnAction( getPlaylistContextMenuHandler() );
		
		plMenu = (Menu) m_playlistSongsTableConMenu.getItems().get(3);
		plMenu.getItems().add( playlistNameItem );
	}
	
	/**
	 * Set the given playlist's name on all Song-based context menus. Useful
	 * 	for the editing of playlist names on all context menus rather than
	 * 	adding to the end of the lists of current playlist names.
	 * 
	 * @param a_index int, the point at which to set the playlist name
	 * @param a_name String, the playlist name to set
	 * @author Andrzej Brynczka
	 */
	protected void setPLNameOnSongContextMenus(int a_index, String a_name){
		if( a_index < 0 ){
			return;
		}
		
		//create the new context item for the playlist
		MenuItem playlistNameItem = new MenuItem( a_name );
		playlistNameItem.setId( MENU_ADD_SONG_TO );
		playlistNameItem.setOnAction( getMainTableContextMenuHandler() );
		
		//set the new item at the desired position
		Menu plMenu = (Menu) m_songTableConMenu.getItems().get(3);	
		if( a_index >= plMenu.getItems().size() ){
			return;
		}
		
		plMenu.getItems().set(a_index, playlistNameItem );
		
		//do the same for the playlist song table(can't add the exact same item
		//to multiple context menus)
		playlistNameItem = new MenuItem( a_name );
		playlistNameItem.setId( MENU_ADD_SONG_TO );
		playlistNameItem.setOnAction( getPlaylistContextMenuHandler() );
		
		plMenu = (Menu) m_playlistSongsTableConMenu.getItems().get(3);	
		if( a_index >= plMenu.getItems().size() ){
			return;
		}
		
		plMenu.getItems().set(a_index, playlistNameItem );
	}
	
	/**
	 * Add the given playlist's name to all Video-based context menus,
	 * 	allowing videos to be added to the playlist.
	 * 
	 * @param a_playlistName String, the playlist to add to all menus
	 * @author Andrzej Brynczka
	 */
	protected void addPLNameToVideoContextMenus( String a_playlistName ){
		
		//create a new context item for the playlist
		MenuItem playlistNameItem = new MenuItem( a_playlistName );
		playlistNameItem.setId( MENU_ADD_VIDEO_TO );
		playlistNameItem.setOnAction( getMainTableContextMenuHandler() );
		
		//add the item to the "Add to:" sub menu
		Menu plMenu = (Menu) m_videoTableConMenu.getItems().get(3);
		plMenu.getItems().add( playlistNameItem );
		
		//repeat for the playlist videos table menu
		playlistNameItem = new MenuItem( a_playlistName );
		playlistNameItem.setId( MENU_ADD_VIDEO_TO );
		playlistNameItem.setOnAction( getPlaylistContextMenuHandler() );
		
		plMenu = (Menu) m_playlistVideosTableConMenu.getItems().get(3);
		plMenu.getItems().add( playlistNameItem );
	}
	
	/**
	 * Set the given playlist's name on all Video-based context menus. Useful
	 * 	for the editing of playlist names on all context menus rather than
	 * 	adding to the end of the lists of current playlist names.
	 * 
	 * @param a_index int, the point at which to set the playlist name
	 * @param a_name String, the playlist name to set
	 * @author Andrzej Brynczka
	 */
	public void setPLNameOnVideoContextMenus(int a_index, String a_name){
		if( a_index < 0 ){
			return;
		}
		
		//create the new context item for the playlist
		MenuItem playlistNameItem = new MenuItem( a_name );
		playlistNameItem.setId( MENU_ADD_VIDEO_TO );
		playlistNameItem.setOnAction( getMainTableContextMenuHandler() );
		
		//set the new item at the desired position
		Menu plMenu = (Menu) m_videoTableConMenu.getItems().get(3);
		
		if( a_index >= plMenu.getItems().size() ){
			return;
		}
		plMenu.getItems().set(a_index, playlistNameItem );
		
		//do the same for the playlist video table(can't add the exact same 
		//item to multiple context menus)
		playlistNameItem = new MenuItem( a_name );
		playlistNameItem.setId( MENU_ADD_VIDEO_TO );
		playlistNameItem.setOnAction( getPlaylistContextMenuHandler() );
		
		plMenu = (Menu) m_playlistVideosTableConMenu.getItems().get(3);
		
		if( a_index >= plMenu.getItems().size() ){
			return;
		}
		plMenu.getItems().set(a_index, playlistNameItem );
	}
	
	/**
	 * Remove a playlist's name from all Song-based context menus at the given
	 * 	index.
	 * 
	 * @param a_index int, the point from which all playlist names are to be
	 * 	removed
	 * @author Andrzej Brynczka
	 */
	public void removePLNameFromSongContextMenus( int a_index ){	
		if( a_index < 0 ){
			return;
		}
		
		Menu plMenu = (Menu) m_songTableConMenu.getItems().get(3);
		if( a_index > plMenu.getItems().size() ){ return; }	
		plMenu.getItems().remove( a_index );
		
		plMenu = (Menu) m_playlistSongsTableConMenu.getItems().get(3);
		if( a_index > plMenu.getItems().size() ){ return; }	
		plMenu.getItems().remove( a_index );
	}
	
	/**
	 * Remove a playlist's name from all Song-based context menus at the given
	 * 	index.
	 * 
	 * @param a_index int, the point from which all playlist names are to be
	 * 	removed
	 * @author Andrzej Brynczka
	 */
	public void removePLNameFromVideoContextMenus( int a_index ){	
		if( a_index < 0 ){
			return;
		}
		
		Menu plMenu = (Menu) m_videoTableConMenu.getItems().get(3);
		if( a_index > plMenu.getItems().size() ){ return; }	
		plMenu.getItems().remove( a_index );
		
		plMenu = (Menu) m_playlistVideosTableConMenu.getItems().get(3);
		if( a_index > plMenu.getItems().size() ){ return; }		
		plMenu.getItems().remove( a_index );
	}
	
    // *******************************************************************
    // ******************** Component Creation Functions *****************
    // *******************************************************************
	/**
	 * Helper Function
	 * Creates a TextField that can be used to create new playlists and 
	 * 	sets its event handlers.
	 * @return the TextField
	 * @author Andrzej Brynczka
	 */
	private TextField createAndGetAddPlaylistField(){
		TextField plCreateTextField = new TextField("");
		plCreateTextField.setPromptText( TEXTFIELD_CREATE_PLAYLIST );
		plCreateTextField.setMinSize(50, 30);
		plCreateTextField.setPrefSize(50, 30);
		
		//create the playlist with the given name when the user presses
		//the ENTER key
		plCreateTextField.setOnAction( new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				TextField textField = (TextField) event.getSource();
				if( textField.getText().equals("") ){
					return;
				}
				
				String newPlaylistName = textField.getText();
				try{
				if( newPlaylistName.length() > Library.MAX_PLAYLIST_NAME_SIZE ||
						m_library.isMusicPlaylist( newPlaylistName ) ||
						m_library.isVideoPlaylist( newPlaylistName ) ){
					textField.clear();
					return;
				}
				} catch( SQLException e ){
					
				}
				
				try{
					
					if( m_displayedMediaType == PlayerType.MUSIC ){
						m_library.savePlayList( newPlaylistName, "MUSIC" );
						addNewPlayListNameToTree( newPlaylistName, 
								PlayerType.MUSIC );
					}
					else{
						m_library.savePlayList( newPlaylistName, "VIDEO" );
						addNewPlayListNameToTree( newPlaylistName, 
								PlayerType.VIDEO );
					}
				}
				catch( SQLException e ){
					System.out.println("ERROR: couldn't create new playlist ");
					System.out.println( e.getMessage() );
				}	
				
				textField.setText("");			
			}			
		});
		return plCreateTextField;
	}
	
	/**
	 * Helper Function.
	 * Create the search box that allows the user to search for Song/Video
	 * 	items based on specific criteria. This initializes the 
	 * 	m_songSearchTypeBox and m_videoSearchTypeBox components.
	 * 
	 * @return an HBox with the initialized components ready to be used for
	 * 	searches
	 */
	private HBox createAndGetSearchBox(){
		final TextField searchTextField = new TextField();
		searchTextField.setPromptText("Search for media");
		searchTextField.setPrefSize(150, 20);
	
		//search the database for the given search term
		searchTextField.setOnAction( new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				String searchText = searchTextField.getText();
				
				
				String selectType;//type of search to perform
				
				if( m_displayedMediaType == PlayerType.MUSIC ) {
					//song-based search
					selectType = (String) m_songSearchTypeBox
							.getSelectionModel().getSelectedItem();
					
					Collection<Song> searchResults = null;
					try{
						if( selectType.equals("Name") ){
							searchResults = 
								m_library.getAllSongsWithName( 
										searchText, false );
							System.out.println("Searched song name");
						}
						else if( selectType.equals("Artist") ){
							searchResults = 
								m_library.getAllSongsWithArtist( 
										searchText, false );
							System.out.println("Searched artist");
						}
						else if( selectType.equals( "Album" ) ){
							searchResults = 
								m_library.getAllSongsWithAlbum( 
										searchText, false);
							System.out.println("Searched album");
						}
						
						if( searchResults != null ){
							m_library.setDisplayedMediaList( searchResults );
						}
					}
					catch( SQLException e){
						System.out.println("Not able to search for " 
								+ searchText + " on " + selectType );
						System.out.println( e.getMessage() );
						return;
					}
				} else{
					//video-based search
					selectType = (String) m_videoSearchTypeBox
							.getSelectionModel().getSelectedItem();
					
					Collection<Video> searchResults = null;
					try{
						if( selectType.equals("Name") ){
							searchResults = 
								m_library.getAllVideosWithName( 
										searchText, false );
							System.out.println("Searched video name");
						}
						else if( selectType.equals("Genre") ){
							searchResults = 
								m_library.getAllVideosWithGenre( 
										searchText, false );
							System.out.println("Searched video genre");
						}
						
						if( searchResults != null ){
							m_library.setDisplayedMediaList( searchResults );
						}
					}
					catch( SQLException e){
						System.out.println("Not able to search for " 
								+ searchText + " on " + selectType );
						System.out.println( e.getMessage() );
					}					
				}			
			}	
		});
		
		m_songSearchTypeBox = new ChoiceBox<String>();
		m_songSearchTypeBox.getItems().addAll("Name", "Artist", "Album");
		m_songSearchTypeBox.getSelectionModel().selectFirst();
		m_songSearchTypeBox.setPrefHeight( 18 );
		
		m_videoSearchTypeBox = new ChoiceBox<String>();
		m_videoSearchTypeBox.getItems().addAll("Name", "Genre" );
		m_videoSearchTypeBox.getSelectionModel().selectFirst();
		m_videoSearchTypeBox.setPrefHeight( 18 );
		
		//display one type-box based on which media is being shown
		if( m_displayedMediaType == PlayerType.MUSIC ){
			m_videoSearchTypeBox.setVisible( false );
		}
		else{
			m_songSearchTypeBox.setVisible( false );
		}
		
		HBox searchBox = new HBox();
		if( m_displayedMediaType == PlayerType.MUSIC ){
			searchBox.getChildren().addAll( 
					searchTextField, m_songSearchTypeBox);
		}
		else{
			searchBox.getChildren().addAll( 
					searchTextField, m_videoSearchTypeBox );
		}
		searchBox.setStyle("-fx-background-color: lightgray");
		
		return searchBox;
		
	}
	
	/**
	 * Create a media switch button group, allowing the user to switch between
	 * 	a music/video player and table layout.
	 * @return an HBox with the initialized components ready to be used for
	 * 	media switches
	 * @author Andrzej Brynczka
	 */
	private HBox createAndGetMediaPaneSwitch(){
		//create the buttons for each pane
		final ToggleButton musicPaneButton = new ToggleButton("Music");
		musicPaneButton.setId("MusicSwitchButton");
		musicPaneButton.setOnAction( new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event){
				if( musicPaneButton.isSelected() ){
					m_displayedMediaType = PlayerType.MUSIC;
					m_mediaPlayerRegion.switchToMusic();
					
					m_songSearchTypeBox.setVisible( true );
					m_videoSearchTypeBox.setVisible( false );
					
					m_topTableSplitPane.getItems().set(1,m_playlistSongsTable);
					m_topTableSplitPane.getItems().set(2,m_nowPlayingSongTable);
					m_bottomTableBox.getChildren().set(1, m_songTable );
					m_searchBox.getChildren().set(1, m_songSearchTypeBox );
					
					try {
						m_library.setDisplayedMediaList( 
								m_library.getAllSongs() );
					} catch (SQLException e) {
						System.out.println("Couldn't display all songs"
								+ " after switch");
						System.out.println( e.getMessage() );
					}
				}
			}
		});

        final ToggleButton videoPaneButton = new ToggleButton("Videos");
        videoPaneButton.setId("VideoSwitchButton");
        videoPaneButton.setOnAction( new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent event){
        		if( videoPaneButton.isSelected() ){
	        		m_displayedMediaType = PlayerType.VIDEO;
	        		m_mediaPlayerRegion.switchToVideo();
	        		
	        		m_songSearchTypeBox.setVisible( false );
	        		m_videoSearchTypeBox.setVisible( true );
	        		
	        		m_topTableSplitPane.getItems().set(
	        				1, m_playlistVideosTable );
					m_topTableSplitPane.getItems().set(
							2, m_nowPlayingVideoTable );
					m_bottomTableBox.getChildren().set(1, m_videoTable );
					m_searchBox.getChildren().set(1, m_videoSearchTypeBox );
					
	        		try {
						m_library.setDisplayedMediaList( 
								m_library.getAllVideos() );
					} catch (SQLException e) {
						System.out.println("Couldn't display all videos" 
								+ " after switch");
						System.out.println( e.getMessage() );
					}
        		}
        		
        	}
        });

        //create the button group and add the buttons to it
        ToggleGroup paneGroup = new ToggleGroup();
        
        musicPaneButton.setToggleGroup(paneGroup);
        videoPaneButton.setToggleGroup(paneGroup);

        if( m_displayedMediaType == PlayerType.MUSIC ){
        	paneGroup.selectToggle( musicPaneButton );
        }
        else{
        	paneGroup.selectToggle( videoPaneButton );
        }

        //ensure that at least one button is always toggled
        paneGroup.selectedToggleProperty().addListener(
        		new ChangeListener<Toggle>() {
 
            public void changed(ObservableValue<? extends Toggle> a_observable,
                    Toggle a_oldToggle, Toggle a_newToggle) {
                
            	if (a_newToggle == null) {
            		a_oldToggle.setSelected(true);
                                     
                }
            }
        });
        
        //organize the buttons
        HBox groupBox = new HBox();
        groupBox.getChildren().addAll( musicPaneButton, videoPaneButton );

        return groupBox;
	}
	
	/**
	 * Helper Function.
	 * Create a component that displays the total number of items within the
	 * 	main media display table. Item count updates automatically through
	 * 	a binding to the library's main media list. 
	 * @return an HBox with the initialized count label, ready to be used
	 * 	for displaying of total media count
	 * @author Andrzej Brynczka
	 */
	private HBox createAndGetTotalCountBox(){
		//create the required labels used to display the total media count
		Label description = new Label("Total items displayed: ");

		description.setFont( javafx.scene.text.Font.font(null, 12.5 ));
		description.setTextFill( Color.SLATEGREY );
		
		m_totalCountLabel = new Label("Total items displayed: " + 
				m_library.getPlaylistObservableList( 
						PLType.MEDIA_DISPLAY ).size() );
		m_totalCountLabel.setTextFill( Color.SLATEGREY );
		m_totalCountLabel.setFont( javafx.scene.text.Font.font(null, 12.5 ));
		
		//bind the library's main media list to the displayed total-count
		//property, allowing immediate updates upon changes to the media list
		Bindings.bindBidirectional(m_totalCountLabel.textProperty(), 
				m_library.getDisplayedMediaListSizeProperty(), 
				new NumberStringConverter());
		
		
		//organize the labels into a horizontal box
		HBox box = new HBox();
		box.getChildren().addAll( description, m_totalCountLabel );
		box.setAlignment( Pos.BASELINE_CENTER );
		return box;
	}
	
	/**
	 * Create the playlist treeview containing the names of all playlists
	 * 
	 * @author Andrzej Brynczka
	 */
	private void createPlayListNameTree(){
		//create the tree's root nodes, each of which will hold a
		//list of its own playlist names
		final TreeItem<String> musicPLRoot = 
				new TreeItem<String>("Music Playlists");
		musicPLRoot.setExpanded( true );
		
		final TreeItem<String> videoPLRoot = 
				new TreeItem<String>("Video Playlists");
		videoPLRoot.setExpanded( true );
		
		//add the individual playlist roots to a singular tree node to be held
		//by the tree
		final TreeItem<String> treeRoot = new TreeItem<String>("Playlists");
		treeRoot.setExpanded( true );
	    treeRoot.getChildren().add( musicPLRoot );
	    treeRoot.getChildren().add( videoPLRoot );
	    
	    m_playlistNameTree = new TreeView<>();
	    m_playlistNameTree.setShowRoot( true );
	    m_playlistNameTree.setEditable( true );
	    m_playlistNameTree.getSelectionModel().setSelectionMode( 
	    		SelectionMode.SINGLE );
	    m_playlistNameTree.getSelectionModel().select(0);
	    m_playlistNameTree.setRoot( treeRoot );
	    
	    //set a factory for the tree's items, allowing each to handle their own
	    //modification and actions
	    final MediaPane thisPane = this;
	    m_playlistNameTree.setCellFactory( 
	    		new Callback<TreeView<String>, TreeCell<String>>(){
	    	@Override
	    	public TreeCell<String> call(TreeView<String> a_tree)
	    	{
	    		
	    		return new PlaylistTreeCell(m_library, thisPane, 
	    				m_mediaPlayerRegion, m_playlistNameTree);
	    	}
	    });
	    
	    m_playlistNameTree.setOnEditCommit( 
	    		new EventHandler<EditEvent<String>>(){
			@Override
			public void handle(EditEvent<String> event) 
			{
				String oldName = event.getOldValue();
				String newName = event.getNewValue();
				TreeItem<String> changedItem = event.getTreeItem();
				
				//determine if the item is a child of musicPLRoot or videoPLRoot
				//and get its position under its parent
				TreeItem<String> itemRoot = changedItem.getParent();
				int itemIndexInRoot = itemRoot.getChildren().indexOf( 
						changedItem );
				
				//use the item's position and root type to update its name
				//on the table context menus
				if( itemRoot == musicPLRoot ){
					setPLNameOnSongContextMenus( itemIndexInRoot, newName );
				}
				else {
					setPLNameOnVideoContextMenus( itemIndexInRoot, newName);
				}
				

				try {
					m_library.updatePlayListName(oldName, newName);
				} catch (SQLException e) {
					System.out.println("Cannot update playlist name for " 
							+ oldName + " to " + newName);
				}			
			}    	
	    });
	    
	    //set a listener on the selection of items on the tree,
	    //enabling a selection of a playlist name to display the playlist's
	    //contents on an associated table
	    m_playlistNameTree.getSelectionModel().selectedItemProperty().
	    	addListener(
    		new ChangeListener<TreeItem<String>>(){
				@Override
				public void changed(ObservableValue<? extends TreeItem<String>> 
					a_observable, TreeItem<String> a_oldValue, 
					TreeItem<String> a_newValue) 
				{
					
					if( a_newValue == null ){
						return;
					}
					
					String playlistName = (String) a_newValue.getValue();
					if( playlistName == null || 
							playlistName.equals( treeRoot.getValue() ) ||
							playlistName.equals( musicPLRoot.getValue() ) ||
							playlistName.equals( videoPLRoot.getValue() )){
						return;
					}

					//change the associated playlist table to hold information
					//on this playlist's media
					try{

					if( m_library.isMusicPlaylist( playlistName ) ){
	
						m_library.setSongPLContentList( playlistName );
						m_playlistSongsTable.setItems( 
								m_library.getPlaylistObservableList( 
										PLType.PLAYLIST_CONTENTS ) );
						
					}
					else if( m_library.isVideoPlaylist( playlistName ) ){

						m_library.setVideoPLContentList( playlistName );
						m_playlistVideosTable.setItems( 
								m_library.getPlaylistObservableList( 
										PLType.PLAYLIST_CONTENTS ) );
					}else{
						return;
					}
					}catch( SQLException e ){
						System.out.println( "ERROR: attempting to check " 
								+ "playlist type and load its contents.");
						System.out.println( e.getMessage() );
					}
					System.out.println("changed playlist display contents to: "
							+ playlistName);
				}    			
    		});
	}
	
	/**
	 * Create and get a TableView for the given list of songs.
	 * 
	 * @param a_songList ObservableList of Song objects, the list to attach
	 * 	to this table
	 * @param a_conMenu ContextMenu, the context menu to attach to this table
	 * 	(Set to null if no ContextMenu is needed)
	 * @param a_sortable boolean, indication of whether or not the columns
	 * 	within the table should be sortable
	 * @return the created TableView of Song objects 
	 * @author Andrzej Brynczka
	 */
	private TableView<Song> createAndGetSongTable(
			ObservableList<Song> a_songList, 
			ContextMenu a_conMenu, boolean a_sortable){
		
		//create the song name, artist, album, and genre columns
		TableColumn<Song, String> nameCol = new TableColumn<Song, String>();
		nameCol.setText("Name");
		nameCol.setMinWidth(200);
		nameCol.setPrefWidth(350);
		nameCol.setMaxWidth(600);
		nameCol.setCellValueFactory( 
				new PropertyValueFactory<Song, String>("Name") );
		nameCol.setEditable( true );
		nameCol.setSortable( a_sortable );
		nameCol.setCellFactory( TextFieldTableCell.<Song>forTableColumn() );		
			
		TableColumn<Song, String> artistCol = new TableColumn<Song, String>();
		artistCol.setText("Artist");
		artistCol.setMinWidth(125);
		artistCol.setPrefWidth(200);
		artistCol.setMaxWidth(200);
		artistCol.setCellValueFactory( 
				new PropertyValueFactory<Song, String>("Artist" ) );
		artistCol.setCellFactory( TextFieldTableCell.<Song>forTableColumn() );
		artistCol.setEditable( true );
		artistCol.setSortable( a_sortable );
		
		TableColumn<Song, String> albumCol = new TableColumn<Song, String>();
		albumCol.setText("Album");
		albumCol.setMinWidth(125);
		albumCol.setPrefWidth(200);
		albumCol.setMaxWidth(200);
		albumCol.setCellValueFactory( 
				new PropertyValueFactory<Song, String>("Album" ) );
		albumCol.setCellFactory( TextFieldTableCell.<Song>forTableColumn() );
		albumCol.setEditable( true );
		albumCol.setSortable( a_sortable );
		
		TableColumn<Song, String> genreCol = new TableColumn<Song, String>();
		genreCol.setText("Genre");
		genreCol.setMinWidth(75);
		genreCol.setMaxWidth(130);
		genreCol.setPrefWidth(100);
		genreCol.setCellValueFactory( 
				new PropertyValueFactory<Song, String>("Genre" ) );
		genreCol.setCellFactory( TextFieldTableCell.<Song>forTableColumn() );
		genreCol.setEditable( true );
		genreCol.setSortable( a_sortable );
		
		//set handlers for the table's cells, ensuring that all edits
		//are saved to the library's database
		setSongTableCellEditHandlers( nameCol, artistCol, albumCol, genreCol );
		
		//create the duration and location columns, as non-editable columns
		TableColumn<Song, String> lengthCol = new TableColumn<Song, String>();
		lengthCol.setText("Duration");
		lengthCol.setMinWidth(50);
		lengthCol.setMaxWidth(100);
		lengthCol.setPrefWidth(60);
		lengthCol.setCellValueFactory( 
				new PropertyValueFactory<Song, String>("Length" ) );
		lengthCol.setEditable( false );
		lengthCol.setSortable( a_sortable );
		
		TableColumn<Song, String> locationCol = new TableColumn<Song, String>();
		locationCol.setText("File Location");
		locationCol.setMinWidth(500);
		locationCol.setCellValueFactory( 
				new PropertyValueFactory<Song, String>("FilePath" ) );
		locationCol.setEditable( false );
		locationCol.setSortable( a_sortable );
		
		//create the tableview from the above columns
		TableView<Song> songTable = new TableView<Song>();
		songTable.setItems( a_songList );
		songTable.getColumns().addAll(nameCol, lengthCol, artistCol, albumCol, 
				genreCol, locationCol);
		songTable.getSelectionModel().setSelectionMode( 
        		SelectionMode.MULTIPLE );
		songTable.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY);
		songTable.setEditable( true );
		
		if( a_conMenu != null ){
			songTable.setContextMenu( a_conMenu );
		}
		return songTable;
	}

	/**
	 * Set EventHandlers on the provided Song based columns' edit actions, 
	 * 	ensuring that the modifications made to values in the columns 
	 * 	are saved to the database.
	 * 
	 * @param a_nameCol TableColumn, on which the Song object's "name" edit
	 * 	handler is set
	 * @param a_artistCol TableColumn, on which the Song object's "artist" edit
	 * 	handler is set
	 * @param a_albumCol TableColumn, on which the Song object's "album" edit
	 * 	handler is set
	 * @param a_genreCol TableColumn, on which the Song object's "genre" edit
	 * 	handler is set
	 * @author Andrzej Brynczka
	 */
	private void setSongTableCellEditHandlers(
			TableColumn<Song, String> a_nameCol, 
			TableColumn<Song, String> a_artistCol, 
			TableColumn<Song, String> a_albumCol, 
			TableColumn<Song, String> a_genreCol){

		//set the "Name" column's handler
		a_nameCol.setOnEditCommit( 
		    new EventHandler<CellEditEvent<Song, String>>() {
		        @Override
		        public void handle(CellEditEvent<Song, String> a_evt) {
		            
		            int cellPos = a_evt.getTablePosition().getRow();
		            Song editedSong = 
		            		a_evt.getTableView().getItems().get(cellPos);

		          //update the database
		    		try {
		    			m_library.updateSongName( 
		    					editedSong, a_evt.getNewValue() );
		    		} catch (SQLException e){
		    			System.out.println("ERROR: Couldn't update 'Name'" 
		    					+ "database information for " 
		    					+ editedSong.getFilePath() );
		    			System.out.println( e.getMessage() );
		    		} catch (Exception e) {
		    			System.out.println("ERROR: Couldn't update 'Title'" 
		    					+ "metatag information for " 
		    					+ editedSong.getFilePath() );
		    			System.out.println( e.getMessage() );
		    		}
		    						
		    		//set the song's new name
		    		editedSong.setName( a_evt.getNewValue() );		
		        }
		    }
		);
		
		//set the "Artist" column's handler
		a_artistCol.setOnEditCommit(
				new EventHandler< CellEditEvent<Song, String> >(){
			@Override
			public void handle(CellEditEvent<Song, String> a_editEvt) {
				//get the edited cell's position
				int cellPos = a_editEvt.getTablePosition().getRow();
				
				//get the edited song
				Song editedSong = 
						a_editEvt.getTableView().getItems().get( cellPos );
				
				//update the database
				try {
					m_library.updateSongArtist( 
							editedSong, a_editEvt.getNewValue() );
				} catch (SQLException e){
					System.out.println("ERROR: Couldn't update 'Artist'" 
							+ " database information for " 
							+ editedSong.getFilePath() );
					System.out.println( e.getMessage() );
				} catch (Exception e) {
					System.out.println("ERROR: Couldn't update 'Artist'" 
							+ " metatag information for " 
							+ editedSong.getFilePath() );
					System.out.println( e.getMessage() );
				}
				
				//set the song's new artist name
				editedSong.setArtist( a_editEvt.getNewValue() );
			}	
		});
		
		//set the "Album" column's handler
		a_albumCol.setOnEditCommit(
				new EventHandler< CellEditEvent<Song, String> >(){
			@Override
			public void handle(CellEditEvent<Song, String> a_editEvt) {
				//get the edited cell's position
				int cellPos = a_editEvt.getTablePosition().getRow();
				
				//get the edited song
				Song editedSong = 
						a_editEvt.getTableView().getItems().get( cellPos );
				
				//update the database
				try {
					m_library.updateSongAlbum( 
							editedSong, a_editEvt.getNewValue() );
				} catch (SQLException e){
					System.out.println("ERROR: Couldn't update 'Album'" 
							+ " database information for " 
							+ editedSong.getFilePath() );
					System.out.println( e.getMessage() );
				} catch (Exception e) {
					System.out.println("ERROR: Couldn't update 'Album'" 
							+ "metatag information for " 
							+ editedSong.getFilePath() );
					System.out.println( e.getMessage() );
				}
								
				//set the song's new Album name
				editedSong.setAlbum( a_editEvt.getNewValue() );
			}	
		});
		
		//set the "Genre" column's handler
		a_genreCol.setOnEditCommit(
				new EventHandler< CellEditEvent<Song, String> >(){
			@Override
			public void handle(CellEditEvent<Song, String> a_editEvt) {
				//get the edited cell's position
				int cellPos = a_editEvt.getTablePosition().getRow();
				
				//get the edited song
				Song editedSong = 
						a_editEvt.getTableView().getItems().get( cellPos );
				
				//update the database
				try {
					m_library.updateSongGenre( 
							editedSong, a_editEvt.getNewValue() );
				} catch (SQLException e){
					System.out.println("ERROR: Couldn't update 'Genre'" 
							+ " database information for " 
							+ editedSong.getFilePath() );
					System.out.println( e.getMessage() );
				} catch (Exception e) {
					System.out.println("ERROR: Couldn't update 'Genre' metatag" 
							+" information for " 
							+ editedSong.getFilePath() );
					System.out.println( e.getMessage() );
				}
				
				//set the song's new Genre name
				editedSong.setGenre( a_editEvt.getNewValue() );
			}	
		});				
	}

	/**
	 * Create and get a TableView for the given list of videos.
	 * 
	 * @param a_videoList ObservableList of Video objects, the list to attach
	 * 	to this table
	 * @param a_conMenu ContextMenu, the context menu to attach to this table
	 * 	(Set to null if no ContextMenu is needed)
	 * @param a_sortable boolean, indication of whether or not the columns
	 * 	within the table should be sortable
	 * @return the created TableView of Video objects
	 * 
	 * @author Andrzej Brynczka
	 */
	private TableView<Video> createAndGetVideoTable(
			ObservableList<Video> a_videoList, 
			ContextMenu a_conMenu, boolean a_sortable){

		//create the editable video name and genre columns
		TableColumn<Video, String> nameCol = new TableColumn<Video, String>();
		nameCol.setText("Name");
		nameCol.setMinWidth(200);
		nameCol.setPrefWidth(350);
		nameCol.setMaxWidth(600);
		nameCol.setCellValueFactory( 
				new PropertyValueFactory<Video, String>("Name") );
		nameCol.setEditable( true );
		nameCol.setSortable( a_sortable );
		nameCol.setCellFactory( TextFieldTableCell.<Video>forTableColumn() );		
			
		TableColumn<Video, String> genreCol = new TableColumn<Video, String>();
		genreCol.setText("Genre");
		genreCol.setMinWidth(75);
		genreCol.setMaxWidth(130);
		genreCol.setPrefWidth(100);
		genreCol.setCellValueFactory( 
				new PropertyValueFactory<Video, String>("Genre" ) );
		genreCol.setCellFactory( TextFieldTableCell.<Video>forTableColumn() );
		genreCol.setEditable( true );
		genreCol.setSortable( a_sortable );
		
		//add the handlers that ensure any changes are saved to the database
		setVideoTableCellEditHandlers( nameCol, genreCol );
		
		//create the non-editable duration and location columns
		TableColumn<Video, String> lengthCol = new TableColumn<Video, String>();
		lengthCol.setText("Duration");
		lengthCol.setMinWidth(50);
		lengthCol.setMaxWidth(100);
		lengthCol.setPrefWidth(60);
		lengthCol.setCellValueFactory( 
				new PropertyValueFactory<Video, String>("Length" ) );
		lengthCol.setEditable( false );
		lengthCol.setSortable( a_sortable );
		
		TableColumn<Video, String> locationCol = 
				new TableColumn<Video, String>();
		locationCol.setText("File Location");
		locationCol.setMinWidth(500);
		locationCol.setCellValueFactory( 
				new PropertyValueFactory<Video, String>("FilePath" ) );
		locationCol.setEditable( false );
		locationCol.setSortable( a_sortable );
		
		//create the table and add the above columns
		TableView<Video> videoTable = new TableView<Video>();
		videoTable.setItems( a_videoList );
		videoTable.getColumns().addAll( nameCol, lengthCol, 
				genreCol, locationCol );
		videoTable.getSelectionModel().setSelectionMode( 
        		SelectionMode.MULTIPLE );
		videoTable.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY);
		videoTable.setEditable( true );
		
		if( a_conMenu != null ){
			videoTable.setContextMenu( a_conMenu );
		}
		return videoTable;
	}

	/**
	 * Set EventHandlers on the provided Video based columns' edit actions, 
	 * 	ensuring that the modifications made to values in the columns 
	 * 	are saved to the database.
	 * 
	 * @param a_nameCol TableColumn, on which the Video object's "name" edit
	 * 	handler is set
	 * @param a_genreCol TableColumn, on which the Video object's "genre" edit
	 * 	handler is set
	 */
	private void setVideoTableCellEditHandlers(TableColumn<Video, 
			String> a_nameCol, TableColumn<Video, String> a_genreCol){

		//set the "Name" column's handler
		a_nameCol.setOnEditCommit( 
		    new EventHandler<CellEditEvent<Video, String>>() {
		        @Override
		        public void handle(CellEditEvent<Video, String> a_evt) {
		            
		            int cellPos = a_evt.getTablePosition().getRow();
		            Video editedVideo = 
		            		a_evt.getTableView().getItems().get(cellPos);

		          //update the database
		    		try {
		    			m_library.updateVideoName( 
		    					editedVideo, a_evt.getNewValue() );
		    		} catch (SQLException e){
		    			System.out.println("ERROR: Couldn't update 'Name'" 
		    					+ " database information for " 
		    					+ editedVideo.getFilePath() );
		    			System.out.println( e.getMessage() );
		    		} 
		    						
		    		//set the video's new name
		    		editedVideo.setName( a_evt.getNewValue() );		
		        }
		    }
		);
				
		//set the "Genre" column's handler
		a_genreCol.setOnEditCommit(
				new EventHandler< CellEditEvent<Video, String> >(){
			@Override
			public void handle(CellEditEvent<Video, String> a_editEvt) {

				int cellPos = a_editEvt.getTablePosition().getRow();
				Video editedVideo = 
						a_editEvt.getTableView().getItems().get( cellPos );
				
				//update the database
				try {
					m_library.updateVideoGenre( 
							editedVideo, a_editEvt.getNewValue() );
				} catch (SQLException e){
					System.out.println("ERROR: Couldn't update 'Genre'" 
							+ " database information for " 
							+ editedVideo.getFilePath() );
					System.out.println( e.getMessage() );
				} 
				
				//set the video's new Genre name
				editedVideo.setGenre( a_editEvt.getNewValue() );
			}	
		});				
	}
	
	
    // *******************************************************************
    // ******************** ContextMenu Functions ************************
    // *******************************************************************

	/**
	 * Get an ActionEvent EventHandler for the main media display tables'
	 * 	context menus.
	 * 
	 * @return EventHandler for the main media display table's context menu
	 * @author Andrzej Brynczka
	 */
	private EventHandler<ActionEvent> getMainTableContextMenuHandler(){
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				MenuItem item = (MenuItem) event.getSource();
				
				//the handled item's original context menu to be checked against
				ContextMenu parentConMenu = item.getParentPopup();
				
				//containers for use in several cases
				Collection<Song> selectedSongs;
				Collection<Video> selectedVideos;
				String playlistName;
				
				//determine course of action based on the item's id
				switch( item.getId() ){
				case MENU_PLAY:
					if( parentConMenu == m_songTableConMenu ){
						m_library.setNowPlayingSongList( 
								m_songTable.getSelectionModel()
									.getSelectedItems() );
						m_nowPlayingSongTable.getSelectionModel().select( 0 );				
					}
					else if( parentConMenu == m_videoTableConMenu ){
						m_library.setNowPlayingVideoList( 
								m_videoTable.getSelectionModel()
									.getSelectedItems() );
						m_nowPlayingVideoTable.getSelectionModel().select(0);
					}
					
					m_mediaPlayerRegion.updateActiveItem();
					break;
				case MENU_ADD_TO_NOW_PLAYING:
					if( parentConMenu == m_songTableConMenu ){
						m_library.addToNowPlayingSongList( 
								m_songTable.getSelectionModel()
									.getSelectedItems() );
					}
					else{
						m_library.addToNowPlayingVideoList( 
								m_videoTable.getSelectionModel()
									.getSelectedItems() );
					}
					m_mediaPlayerRegion.updateActiveItem();
					break;
				case MENU_REMOVE_SONG:
					selectedSongs = 
						m_songTable.getSelectionModel().getSelectedItems();
					
					for( Iterator<Song> it = 
							selectedSongs.iterator(); it.hasNext(); ){
						
						try {
							m_library.dropSong( it.next() );
						} catch (SQLException e) {
							System.out.println( e.getMessage() );
						}
					}		
					m_mediaPlayerRegion.updateActiveItem();
					break;
				case MENU_REMOVE_VIDEO:
					selectedVideos = 
						m_videoTable.getSelectionModel().getSelectedItems();
					
					for( Iterator<Video> it = 
								selectedVideos.iterator(); it.hasNext(); ){
						try{
							m_library.dropVideo( it.next() );
						} catch (SQLException e){
							System.out.println( e.getMessage() );
						}
					}
					break;
				case MENU_ADD_SONG_TO:
					//add the selected songs to this item's playlist
					playlistName = item.getText();
					selectedSongs = 
							m_songTable.getSelectionModel().getSelectedItems();
					
					//add the songs to the selected playlist
					for( Iterator<Song> it = 
								selectedSongs.iterator(); it.hasNext(); ){
						try {
							m_library.addSongToPlaylist(
										playlistName, it.next() );
						} catch (SQLException e) {
							System.out.println("ERROR: Couldn't add song to:" 
									+ playlistName);
							System.out.println( e.getMessage() );
						}
					}
					
					break;
				case MENU_ADD_VIDEO_TO:
					//add the selected videos to this item's playlist
					playlistName = item.getText();
					selectedVideos = m_videoTable.getSelectionModel()
							.getSelectedItems();
					
					//add the videos to the playlist			
					for( Iterator<Video> it = 
							selectedVideos.iterator(); it.hasNext(); ){
						try {
							m_library.addVideoToPlaylist(
									playlistName, it.next() );
						} catch (SQLException e) {
							System.out.println("ERROR: Couldn't add video to:" 
									+ playlistName);
							System.out.println( e.getMessage() );
						}
					}	
					break;
				default:
					break;
				}			
			}
		};
		return handler;
	}
	
	
	/**
	 * Create the ContextMenu for the main song/video table, based upon the
	 * 	given PlayerType value.(IE: a_mediaType == PlayerType.Music results
	 * 	in a creation of a contextmenu for m_songTable)
	 * 
	 * @param a_mediaType PlayerType, the type of context menu flavor to create
	 * @return a new ContextMenu for the desired type
	 * @author Andrzej Brynczka
	 */
	private ContextMenu createAndGetMainTableContextMenu( 
			PlayerType a_mediaType ){
		
		//create options with non-specific table ids
		MenuItem itemPlayItem = new MenuItem("Play");
		itemPlayItem.setId( MENU_PLAY );
		itemPlayItem.setOnAction( getMainTableContextMenuHandler() );
		
		MenuItem itemAddToNowPlaying = new MenuItem("Add to currently playing");
		itemAddToNowPlaying.setId( MENU_ADD_TO_NOW_PLAYING );
		itemAddToNowPlaying.setOnAction( getMainTableContextMenuHandler() );
		
		Menu subMenuAddItemTo = new Menu("Add to: ");		
		MenuItem itemRemoveMedia = new MenuItem("Remove from library");
		itemRemoveMedia.setOnAction( getMainTableContextMenuHandler() );	
		
		//add table-specific ids to required items
		if( a_mediaType == PlayerType.MUSIC ){
			subMenuAddItemTo.setId( MENU_ADD_SONG_TO );
			itemRemoveMedia.setId( MENU_REMOVE_SONG );
		}
		else if( a_mediaType == PlayerType.VIDEO ){
			subMenuAddItemTo.setId( MENU_ADD_VIDEO_TO );
			itemRemoveMedia.setId( MENU_REMOVE_VIDEO );
		}
		
		//add separator items for appearance
		SeparatorMenuItem divider1 = new SeparatorMenuItem();
		SeparatorMenuItem divider2 = new SeparatorMenuItem();
		
		//create the final menu
		ContextMenu conMenu = new ContextMenu();
		conMenu.getItems().addAll( itemPlayItem, divider1, 
				itemAddToNowPlaying, subMenuAddItemTo, 
				divider2, itemRemoveMedia );
		
		return conMenu;
	}
	
	/**
	 * Get an ActionEvent EventHandler for the playlist content display tables'
	 * 	context menus.
	 * 
	 * @return EventHandler for the playlist content display tables'  menu
	 * @author Andrzej Brynczka
	 */
	private EventHandler<ActionEvent> getPlaylistContextMenuHandler(){
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				MenuItem item = (MenuItem) event.getSource();
				ContextMenu parentConMenu = item.getParentPopup();
				
				//containers to be used in several cases
				Collection<Video> selectedVideos;
				Collection<Song> selectedSongs;
				String playlistName;
				
				switch( item.getId() ){
				case MENU_PLAY:
					if( parentConMenu == m_playlistVideosTableConMenu ){
						m_library.setNowPlayingVideoList( 
								m_playlistVideosTable.getSelectionModel()
									.getSelectedItems() );
						m_nowPlayingVideoTable.getSelectionModel().select( 0 );
					}
					else if( parentConMenu == m_playlistSongsTableConMenu ){
						m_library.setNowPlayingSongList( 
								m_playlistSongsTable.getSelectionModel()
									.getSelectedItems() );
						m_nowPlayingSongTable.getSelectionModel().select( 0 );
					}
					
					m_mediaPlayerRegion.updateActiveItem();
					break;
				case MENU_ADD_TO_NOW_PLAYING:
					if( parentConMenu == m_playlistVideosTableConMenu ){
						m_library.addToNowPlayingVideoList( 
								m_playlistVideosTable.getSelectionModel()
									.getSelectedItems() );
					}
					else if( parentConMenu == m_playlistSongsTableConMenu ){
						m_library.addToNowPlayingSongList( 
								m_playlistSongsTable.getSelectionModel()
									.getSelectedItems() );
					}
					
					m_mediaPlayerRegion.updateActiveItem();
					break;					
				case MENU_ADD_VIDEO_TO:
					//add all selected videos to the chosen playlist
					playlistName = item.getText();
					selectedVideos = m_playlistVideosTable.getSelectionModel()
							.getSelectedItems();
						
					for( Iterator<Video> it = 
								selectedVideos.iterator(); it.hasNext(); ){
						try {
							m_library.addVideoToPlaylist(
									playlistName, it.next() );
						} catch (SQLException e) {
							System.out.println("ERROR: Couldn't add video to:" 
									+ playlistName);
							System.out.println( e.getMessage() );
						}
					}
								
					break;
				case MENU_ADD_SONG_TO:
					//add all selected songs to the chosen playlist
					playlistName = item.getText();
					selectedSongs = m_playlistSongsTable.getSelectionModel()
							.getSelectedItems();
										
					for( Iterator<Song> it = 
								selectedSongs.iterator(); it.hasNext(); ){
						try {
							m_library.addSongToPlaylist(
										playlistName, it.next() );
						} catch (SQLException e) {
							System.out.println("ERROR: Couldn't add song to:" 
										+ playlistName);
							System.out.println( e.getMessage() );
						}
					}
					break;
				case MENU_REMOVE_FROM_PLAYLIST:
					if( parentConMenu == m_playlistVideosTableConMenu ){
						//get the video's playlist name from the playlist tree
						playlistName = 
								m_playlistNameTree.getSelectionModel()
									.getSelectedItem().getValue();
						selectedVideos = 
								m_playlistVideosTable.getSelectionModel()
									.getSelectedItems();
						
						for( Iterator<Video> it = 
								selectedVideos.iterator(); it.hasNext(); ){
							Video tempVideo = it.next();
							try{
								
								m_library.dropVideoFromPlaylist( 
											tempVideo, playlistName );
							}
							catch( SQLException e ){
								System.out.println("ERROR: Could not delete " 
										+ tempVideo.getFilePath() 
										+ " from list " + playlistName);
								System.out.println( e.getMessage() );
							}
						}		
					}	
					else if( parentConMenu == m_playlistSongsTableConMenu ){
						//get the song's playlist name from the playlist tree
						playlistName = 
									m_playlistNameTree.getSelectionModel()
										.getSelectedItem().getValue();
						selectedSongs = 
									m_playlistSongsTable.getSelectionModel()
										.getSelectedItems();
						
						for( Iterator<Song> it = 
								selectedSongs.iterator(); it.hasNext(); ){
							Song tempSong = it.next();
							try{
								
								m_library.dropSongFromPlaylist( 
										tempSong, playlistName );
							}
							catch( SQLException e ){
								System.out.println("ERROR: Could not delete " 
										+ tempSong.getFilePath() 
										+ " from list " + playlistName);
								System.out.println( e.getMessage() );
							}
						}	
					}
					break;
				default:
					break;
				}			
			}
		};
		return handler;
	}
	
	/**
	 * Create the ContextMenu for the playlist content table of the given
	 * 	type. (IE: PlayerType.MUSIC to create ContextMenu for a music table)
	 * 
	 * @param a_mediaType PlayerType, the type of context menu flavor to create
	 * @return a new ContextMenu for the desired type
	 * @author Andrzej Brynczka
	 */
	private ContextMenu createAndGetPlaylistTableContextMenu(
			PlayerType a_mediaType){
		
		MenuItem itemPlayItem = new MenuItem("Play");
		itemPlayItem.setId( MENU_PLAY );
		itemPlayItem.setOnAction( getPlaylistContextMenuHandler() );
		
		MenuItem itemAddToNowPlaying = new MenuItem("Add to currently playing");
		itemAddToNowPlaying.setId( MENU_ADD_TO_NOW_PLAYING );
		itemAddToNowPlaying.setOnAction( getPlaylistContextMenuHandler() );
		
		Menu subMenuAddItemTo = new Menu("Add to: ");
		
		if( a_mediaType == PlayerType.MUSIC ){
			subMenuAddItemTo.setId( MENU_ADD_SONG_TO );
		}
		else if( a_mediaType == PlayerType.VIDEO ){
			subMenuAddItemTo.setId( MENU_ADD_VIDEO_TO );
		}
		
		MenuItem itemRemoveMedia = new MenuItem("Remove from playlist");
		itemRemoveMedia.setId( MENU_REMOVE_FROM_PLAYLIST );
		itemRemoveMedia.setOnAction( getPlaylistContextMenuHandler() );	
		
		SeparatorMenuItem divider1 = new SeparatorMenuItem();
		SeparatorMenuItem divider2 = new SeparatorMenuItem();
		
		ContextMenu conMenu = new ContextMenu();
		conMenu.getItems().addAll( itemPlayItem, divider1,
				itemAddToNowPlaying, subMenuAddItemTo, 
				divider2, itemRemoveMedia );

		return conMenu;
	}
	
	/**
	 * Get the EventHandler for the "m_nowPlayingSongTable" and
	 * 	"m_nowPlayingVideoTable" TableView objects
	 * @return EventHandler for the "now playing" table's context menu
	 */
	private EventHandler<ActionEvent> getNowPlayingContextMenuHandler(){
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				MenuItem item = (MenuItem) event.getSource();
				
				MediaItem selectedItem;
				int indexInList;
				if( m_displayedMediaType == PlayerType.MUSIC ){
					selectedItem = 
							m_nowPlayingSongTable.getSelectionModel()
								.getSelectedItem();
					indexInList = 
							m_nowPlayingSongTable.getItems().indexOf( 
									(Song)selectedItem );
				}
				else{
					selectedItem = 
							m_nowPlayingVideoTable.getSelectionModel()
								.getSelectedItem();
					indexInList = m_nowPlayingVideoTable.getItems().indexOf( 
								(Video) selectedItem );
				}
				
				switch( item.getId() ){
				case MENU_PLAY:
					//set the first chosen song as the currently playing item
					m_mediaPlayerRegion.playMedia( indexInList );
					break;
				case NOW_PLAYING_MOVE_UP:
					//move the currently selected item in the queue
					if( m_displayedMediaType == PlayerType.MUSIC ){
						m_library.moveUpInList( 
								PLType.NOW_PLAYING_SONGS, indexInList );
						
						//select the item in its new position
						m_nowPlayingSongTable.getSelectionModel()
							.clearAndSelect( indexInList - 1 );
						m_nowPlayingSongTable.scrollTo( indexInList - 1 );
					}
					else{
						m_library.moveUpInList( 
								PLType.NOW_PLAYING_VIDEOS, indexInList );
						
						m_nowPlayingVideoTable.getSelectionModel()
							.clearAndSelect( indexInList - 1 );
						m_nowPlayingVideoTable.scrollTo( indexInList - 1 );
					}
					
					break;					
				case NOW_PLAYING_MOVE_DOWN:
					//move the currently selected item in the queue
					if( m_displayedMediaType == PlayerType.MUSIC ){
						m_library.moveDownInList( 
								PLType.NOW_PLAYING_SONGS, indexInList );
						
						//select the item in its new position
						m_nowPlayingSongTable.getSelectionModel()
							.clearAndSelect( indexInList + 1 );
						m_nowPlayingSongTable.scrollTo( indexInList + 1 );
					}
					else{
						m_library.moveDownInList( 
								PLType.NOW_PLAYING_VIDEOS, indexInList );
						
						//select the item in its new position
						m_nowPlayingVideoTable.getSelectionModel()
							.clearAndSelect( indexInList + 1 );
						m_nowPlayingVideoTable.scrollTo( indexInList + 1 );
					}
				
					break;
				case NOW_PLAYING_REMOVE:
					//remove the item from the now playing list
					if( m_displayedMediaType == PlayerType.MUSIC ){
						Collection<Song> songs = m_nowPlayingSongTable
									.getSelectionModel().getSelectedItems();
						m_library.removeFromNowPlayingSongList( 
									new ArrayList<Song>(songs) );
					}
					else{
						Collection<Video> videos = m_nowPlayingVideoTable
									.getSelectionModel().getSelectedItems();					
						m_library.removeFromNowPlayingVideoList( 
									new ArrayList<Video>(videos) );
					}
					
					m_mediaPlayerRegion.updateActiveItem();
					break;
				default:
					break;
				}			
			}
		};
		return handler;
	}
	
	/**
	 * Create the ContextMenu for the "now playing" tables.
	 * 
	 * @return the created ContextMenu
	 * @author Andrzej Brynczka
	 */
	private ContextMenu createAndGetNowPlayingTableContextMenu(){
		//create the basic menu options
		MenuItem playMediaOption = new MenuItem("Play");
		playMediaOption.setId( MENU_PLAY );
		playMediaOption.setOnAction( getNowPlayingContextMenuHandler() );
		
		MenuItem moveUpOption = new MenuItem("Move Up");
		moveUpOption.setId( NOW_PLAYING_MOVE_UP );
		moveUpOption.setOnAction( getNowPlayingContextMenuHandler() );
		
		MenuItem moveDownOption = new MenuItem("Move Down");
		moveDownOption.setId( NOW_PLAYING_MOVE_DOWN );
		moveDownOption.setOnAction( getNowPlayingContextMenuHandler() );
		
		MenuItem removeOption = new MenuItem("Remove from currently playing");
		removeOption.setId( NOW_PLAYING_REMOVE );
		removeOption.setOnAction( getNowPlayingContextMenuHandler() );
		
		//create separators for appearance
		SeparatorMenuItem divider1 = new SeparatorMenuItem();
		SeparatorMenuItem divider2 = new SeparatorMenuItem();
		
		//create the menu and add the options
		ContextMenu conMenu = new ContextMenu();
		conMenu.getItems().addAll( 
				playMediaOption, divider1, moveUpOption, moveDownOption, 
				divider2, removeOption );
		
		return conMenu;
	}
	
	/**
	 * Get a KeyEvent EventHandler that is meant to handle keyboard shortcuts.
	 * 	Meant to be set on the MediaPane itself.
	 * 
	 * @return the KeyEvent-based EventHandler
	 */
	private EventHandler<KeyEvent> getKeyShortCutHandler(){
		EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>(){
			@Override
			public void handle( KeyEvent a_event ){
				KeyCombination altS = KeyCombination.keyCombination("ALT+S");
				KeyCombination altR = KeyCombination.keyCombination("ALT+R");
				
				if( altS.match( a_event ) ){
					
					if( m_mediaPlayerRegion.isPlaying() ){
						m_mediaPlayerRegion.pause();
					}
					else if( m_mediaPlayerRegion.isPaused() ){
						m_mediaPlayerRegion.play();
					}
					
				}
				else if( altR.match( a_event ) ){
					m_mediaPlayerRegion.toggleRepeat();
				}
				else if( a_event.getCode() == KeyCode.RIGHT ){
					m_mediaPlayerRegion.playNextMedia();
				}
				else if( a_event.getCode() == KeyCode.LEFT ){
					m_mediaPlayerRegion.playPreviousMedia();
				}
				else if( a_event.getCode() == KeyCode.UP ){
					m_mediaPlayerRegion.volumeUp( 0.1 );
				}
				else if( a_event.getCode() == KeyCode.DOWN ){
					m_mediaPlayerRegion.volumeDown( 0.1 );
				}
				
			}
		};
		
		return handler;
	}
}

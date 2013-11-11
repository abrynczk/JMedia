package library.view;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import library.model.Library;
import library.model.Song;
import library.model.Video;
import library.model.Library.PLType;
import library.model.Library.PlayerBase;
import library.view.MediaPlayerRegion.PlayerType;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import client.ClientDriver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.RadioMenuItemBuilder;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

/**
 * The view class for the media library
 * @author Andrzej Brynczka
 *
 */
public class LibraryView extends Application {

	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************
	private final String MENU_FILE_ADD_NEW_FILE = "Add new file";
	private final String MENU_FILE_EXIT = "Close the library";
	
	private final String MENU_CHAT_OPEN = "Open the chat window";
	
	private final String MENU_OPTIONS_USE_DEFAULT_PLAYER = 
										"Use default player";
	private final String MENU_OPTIONS_USE_VLC_PLAYER = "Use VLC player";
	private final String MENU_OPTIONS_CONFIG_VLC_LIB = 
										"Set VLC library location";
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	private ClientDriver m_client;
	
	private Stage m_primaryStage;
	private Library m_library;
	private MenuBar m_systemMenuBar;
	
	private MediaPlayerRegion m_mediaPlayerRegion;
	private MediaPane m_mediaPane;
		
	@Override
	public void start(Stage primaryStage) throws Exception {
		m_primaryStage = primaryStage;
		m_client = null;
		m_library = new Library();
		m_mediaPlayerRegion = new MediaPlayerRegion(m_library, 
				m_library.getPlaylist(PLType.NOW_PLAYING_SONGS), 
				PlayerType.MUSIC);
		
		m_mediaPane = new MediaPane(m_library, this, m_mediaPlayerRegion);
		createMenuBar();
		
		
		//listen for changes to the media player's base
		m_library.getPlayerBaseProperty().addListener( 
				new ChangeListener<PlayerBase>(){
			@Override
			public void changed(
					ObservableValue<? extends PlayerBase> a_observableBase,
					PlayerBase a_oldBase, PlayerBase a_newBase) {
				
				//determine if the vlc player was enabled
				if( a_newBase.equals( PlayerBase.VLC ) ){
					if( m_mediaPlayerRegion.enableVLCPlayer( true ) ){
						//enabled successfully, save the proper path
						//to the libvlc.dll file and disable configure options
						//to prevent unnecessary changes
						if( !m_library.getVlcPathSuccess() ){
							m_library.saveVlcLibPath();
							m_library.setVlcPathSetSuccess( true );
							disableVlcLibConfigMenuItem( true );
						}
						
					}
					else{
						//unsuccessful attempt at enabling the VLC player
						m_library.setPlayerBase( PlayerBase.DEFAULT );
						showInvalidVlcLibDialog();
						
						//modify the selected option in the menu
						Menu optionMenu = m_systemMenuBar.getMenus().get(1);
						Menu playerMenu = (Menu) optionMenu.getItems().get(0);	
						RadioMenuItem defaultOption = 
								(RadioMenuItem)playerMenu.getItems().get(0);
						defaultOption.setSelected( true );
					}
				}
				else{
					m_mediaPlayerRegion.enableVLCPlayer( false );
				}
				
			}
			
		});
		

		m_mediaPane.setTop( m_systemMenuBar );
		m_primaryStage.setScene( new Scene( m_mediaPane ) );
		m_primaryStage.setTitle("JMediaLibrary");
		m_primaryStage.setMinHeight(550);
		m_primaryStage.setMinWidth(590);
		m_primaryStage.setWidth(750);
		m_primaryStage.setHeight(660);
		m_primaryStage.show();
		
		m_primaryStage.setOnCloseRequest( new EventHandler<WindowEvent>(){

			@Override
			public void handle(WindowEvent event) {
				stop();	
			}
			
		});
	}

	/**
	 * Close the mediaPane and the mediaPlayerRegion
	 * and exit the application
	 */
	@Override
	public void stop(){
		m_mediaPane.close();
		m_mediaPlayerRegion.close();
		System.out.println("closed the application");
		Platform.exit();
	}
	
	/**
	 * Initialize and create the main menu bar
	 */
	private void createMenuBar(){
		//create the "File" menu
		MenuItem addFileItem = MenuItemBuilder.create()
				.text("Add file to library")
				.id(MENU_FILE_ADD_NEW_FILE)
				.build();
		addFileItem.setOnAction( getMenuBarHandler() );
		
		MenuItem exitLibraryItem = MenuItemBuilder.create()
				.text("Close the library")
				.id( MENU_FILE_EXIT )
				.build();
		exitLibraryItem.setOnAction( getMenuBarHandler() );
		
		SeparatorMenuItem divider = new SeparatorMenuItem();
		
		Menu fileMenu = MenuBuilder.create()
				.text("File")
				.items(addFileItem, divider, exitLibraryItem)
				.build();
		
		//create the "Options" menu
		ToggleGroup mediaPlayerChoiceGroup = new ToggleGroup();
		
		RadioMenuItem useDefaultPlayerItem = RadioMenuItemBuilder.create()
				.text("Use default player")
				.id( MENU_OPTIONS_USE_DEFAULT_PLAYER )
				.selected( true )
				.toggleGroup( mediaPlayerChoiceGroup)
				.build();
		useDefaultPlayerItem.setOnAction( getMenuBarHandler() );
		
		RadioMenuItem useVLCPlayerItem = RadioMenuItemBuilder.create()
				.text("Use VLC player")
				.id( MENU_OPTIONS_USE_VLC_PLAYER )
				.selected( false )
				.toggleGroup( mediaPlayerChoiceGroup )
				.build();
		useVLCPlayerItem.setOnAction( getMenuBarHandler() );
		
		MenuItem configureVLCLibSettingsItem = MenuItemBuilder.create()
				.text("Set VLC Library location")
				.id( MENU_OPTIONS_CONFIG_VLC_LIB )
				.build();
		configureVLCLibSettingsItem.setOnAction( getMenuBarHandler() );
		
		Menu mediaPlayerOptionMenu = new Menu("Media Player");
		mediaPlayerOptionMenu.getItems().addAll( useDefaultPlayerItem, 
				useVLCPlayerItem, new SeparatorMenuItem(), 
				configureVLCLibSettingsItem);
		
		Menu optionMenu = new Menu("Options");
		optionMenu.getItems().add( mediaPlayerOptionMenu );
		
		//create the "Chat" menu
		MenuItem openChatItem = MenuItemBuilder.create()
				.text("Open chat window")
				.id( MENU_CHAT_OPEN )
				.build();
		openChatItem.setOnAction( getMenuBarHandler() );
		
		
		Menu chatMenu = MenuBuilder.create()
				.text("Chat")
				.items( openChatItem )
				.build();
		
		//create the final menu bar
		m_systemMenuBar = new MenuBar();
		m_systemMenuBar.getMenus().addAll( fileMenu, optionMenu, 
				chatMenu);
		m_systemMenuBar.setMaxHeight(25);
		//m_systemMenuBar.setPrefSize(500, 25);
		m_systemMenuBar.setMinHeight(25);
	}
	/**
	 * Show the dialog box that asks the user to provided the VLC lib
	 * directory path.
	 */
	private void showConfigVlcLibDialog(){
		
		final TextField pathField = new TextField("");
		pathField.setEditable( false );
		pathField.setText( m_library.getVlcLibPath() );
		
		Label pathLabel = new Label("Path: ");
		Button setPathButton = new Button("Find directory");
		setPathButton.setOnAction( new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Find the VLC program file directory");
				
				File directory = null;
				if( pathField.getText() != null ){
					directory = new File( pathField.getText() );
				}

				
				if( directory != null && directory.exists() ){
					directoryChooser.setInitialDirectory( directory );
				}
				
				File chosenFile = directoryChooser.showDialog( m_primaryStage );
				if( chosenFile == null ){
					return;
				}
				pathField.setText( chosenFile.getAbsolutePath() );
			}
		});
		
		GridPane contentGrid = new GridPane();
		contentGrid.add(pathLabel, 0, 0);
		contentGrid.add(pathField, 1, 0);
		contentGrid.add(setPathButton, 1, 1);
		GridPane.setColumnSpan(pathField, 3);
		
		String masthead = "Set the path to the VLC program file directory," 
				+ " required for the use of the VLC-based media"
				+ " player. \n\n"
				+ " This directory chosen should contain the files \""
				+ Library.FILENAME_LIBVLC + "\" and \"" 
				+ Library.FILENAME_LIBVLCCORE + "\" \n\n"
				+ "Ex: \"c:/program files/videolan/vlc\"";
		
		DialogResponse response = Dialogs.showCustomDialog(m_primaryStage, 
				contentGrid, masthead, "Find your VLC program directory", 
				DialogOptions.OK_CANCEL, null);
				
		if( response == DialogResponse.OK ){
			System.out.println("Provided path: " + pathField.getText());
			m_library.setVlcLibPath( pathField.getText() );
			
			if( !m_library.pathContainsVlcDll( pathField.getText() ) ){
				showVlcLibFilesNotFoundDialog();
			}
		}
				
	}
	
	/**
	 * Show the dialog box that tells the user that the path provided
	 *does not continan the required VLC library items
	 */
	private void showVlcLibFilesNotFoundDialog(){
		String title = "VLC Error";
		String masthead = "Error with VLC-based media player";
		String information = "The VLC library path provided: \n\"" 
				+ m_library.getVlcLibPath() + "\" \n was not a valid path to" 
				+ " the \"" + Library.FILENAME_LIBVLC + "\" and \""
				+ Library.FILENAME_LIBVLCCORE + "\" files. \n\n"
				+ "Please set a new path to the complete VLC program directory" 
				+ ", created upon the installation of the standalone VLC media"
				+ " player, before attempting to use the VLC player option " 
				+ "again.";
		DialogResponse response = Dialogs.showErrorDialog( 
				m_primaryStage, information, masthead, title );
		
		if( response == DialogResponse.OK ){
			showConfigVlcLibDialog();
		}
	}
	
	/**
	 * Show the dialog box that warns the user of an invalid
	 *  VLC library directory path
	 */
	private void showInvalidVlcLibDialog(){
		String title = "VLC Error";
		String masthead = "Error with VLC-based media player";
		String information = "The VLC library path provided: \n\"" 
				+ m_library.getVlcLibPath() + "\" \n was not a path to a valid" 
				+ " VLC program directory. Though the required .dll files"
				+ " maybe be contained within, other required files(such as" 
				+ " necessary plugins) are missing. \n\n"
				+ "Please set a new path to the complete VLC program directory" 
				+ ", created upon the installation of the standalone VLC media"
				+ " player, before attempting to use the VLC player option " 
				+ "again.";
		DialogResponse response = Dialogs.showErrorDialog( 
				m_primaryStage, information, masthead, title );
		
		if( response == DialogResponse.OK ){
			showConfigVlcLibDialog();
		}
	}
	
	/**
	 * Determine whether the VLC library configuration item menu should
	 * be disabled
	 * @param a_disable true to disable the configuration button, false
	 *  otherwise
	 */
	private void disableVlcLibConfigMenuItem(boolean a_disable){
		if( m_systemMenuBar == null ){
			return;
		}
		
		if( a_disable ){
			Menu optionMenu = m_systemMenuBar.getMenus().get(1);
			Menu playerMenu = (Menu) optionMenu.getItems().get(0);	
			playerMenu.getItems().get(3).setDisable( true );		
		}
		else{
			Menu optionMenu = m_systemMenuBar.getMenus().get(1);
			Menu playerMenu = (Menu) optionMenu.getItems().get(0);	
			playerMenu.getItems().get(3).setDisable( false );
		}
	}
		
	
	private void promptUserForFile(){
		FileChooser fileSelector = new FileChooser();
		fileSelector.setTitle("Choose a file to send to add to the library");
		
		
		List<String> defaultExtensions = new ArrayList<String>();
		defaultExtensions.add("*.mp3");
		defaultExtensions.add("*.wav");
		defaultExtensions.add("*.flv");
		defaultExtensions.add("*.mp4");
		ExtensionFilter filter1 = 
				new FileChooser.ExtensionFilter("Default", defaultExtensions);
		
		List<String> allExtensions = new ArrayList<String>();
		allExtensions.add("*.avi");
		allExtensions.add("*.m4v");
		allExtensions.add("*.mov");
		allExtensions.add("*.mp4");
		allExtensions.add("*.mp3");
		allExtensions.add("*.wav");
		allExtensions.add("*.wmv");
		allExtensions.add("*.wma");
		allExtensions.add("*.3GP");
		allExtensions.add("*.flv");
		allExtensions.add("*.ogg");
		ExtensionFilter filter2 = 
				new FileChooser.ExtensionFilter("All", allExtensions);
		fileSelector.getExtensionFilters().addAll( filter1, filter2 );
		
		List<File> desiredFiles = 
				fileSelector.showOpenMultipleDialog( m_primaryStage );
		
		if( desiredFiles == null ){
			return;
		}
		
		m_library.clearDisplayedMediaList();
		
		//add each file to the database and update the display
		for( Iterator<File> it = desiredFiles.iterator(); it.hasNext(); ){
			final File currentFile = it.next();
			
			System.out.println( currentFile.getName()+": " 
					+ currentFile.getAbsolutePath() );
			if( currentFile.getName().toLowerCase().endsWith(".mp3") ){
				//a music file, add to music table	
				
					//read the file's metadata
					MP3File musicFile;
					try {
						musicFile = (MP3File)AudioFileIO.read( currentFile );
						System.out.println("reading name");
						String name = MP3File.getBaseFilename( currentFile );
						
						
						
						//read meta data if it exists
						Tag metaTag = musicFile.getTag();
						String artist;
						String album;
						String genre;
						if( metaTag != null ){
							String title = musicFile.getTag().getFirst(FieldKey.TITLE);
							artist = musicFile.getTag().getFirst( FieldKey.ARTIST );
							album = musicFile.getTag().getFirst( FieldKey.ALBUM );
							genre = musicFile.getTag().getFirst( FieldKey.GENRE );
							
							//if the song has a valid title, 
							//set that as its official name
							if( !title.equals("") ){
								name = title;
							}
							
						}
						else{
							artist = "";
							album = "";
							genre = "";
						}
						
						//get the files duration
						double lengthSeconds = 
								musicFile.getMP3AudioHeader().getPreciseTrackLength();

						
						//get the file path
						String path = currentFile.getAbsolutePath();

	
						
						//create the new song
						Song newSong;
						newSong = new Song(name, artist, album, 
								(long)lengthSeconds, genre, path);
						
						//add the song to the library
						m_library.saveSong(newSong);
						
						//add the song to the display table
						m_library.addToDisplayedMediaList( newSong );
					} catch (CannotReadException | IOException | TagException
							| ReadOnlyFileException
							| InvalidAudioFrameException | SQLException e) {
						System.out.println("Unable to add " 
								+ currentFile.getAbsolutePath() 
								+ " to the database.");
						System.out.println( e.getMessage() );
					}
					
				 	
			}
			
			String currentName = currentFile.getName().toLowerCase();
			if( currentName.endsWith(".wav") || currentName.endsWith(".wma") 
					|| currentName.endsWith(".ogg") ){

				
				//add the song to the library
				try {
					
					
					Song newSong;
					newSong = new Song(currentFile.getName(), "", "", 
							0, "", currentFile.getAbsolutePath());
					m_library.saveSong(newSong);
					
					//add the song to the display table
					m_library.addToDisplayedMediaList( newSong );
				} catch (SQLException e) {
					System.out.println("Unable to add " 
							+ currentFile.getAbsolutePath() 
							+ " to the database.");
					System.out.println( e.getMessage() );
				}
				
	
			}
			else{//video file
				Media videoMedia = null;
				try{
					videoMedia = new Media( currentFile.toURI().toString() );
				}
				catch(MediaException | NullPointerException | 
						IllegalArgumentException | UnsupportedOperationException e){
					System.out.println("Can't add the video file: ");
					System.out.println( e.getMessage() );
					return;
				}
				
				ObservableMap<String, Object> metadata = videoMedia.getMetadata();
				System.out.println( metadata.size() );
				int indexOfExt = currentFile.getName().indexOf(".");
				
				final String name = currentFile.getName().substring(0, indexOfExt);
				final String genre;
				Object tempData = null;

				if(  (tempData = metadata.get( "genre" )) != null ){
					genre = tempData.toString();
				}
				else{
					genre = "";
				}
				
				Video newVideo = new Video(name, 0, genre, 
						currentFile.getAbsolutePath() );
				
				//add the video to the library
				try {
					m_library.saveVideo( newVideo );
					
					//add the video to the video display table
					m_library.addToDisplayedMediaList( newVideo );
				} catch (SQLException e) {
					System.out.println("Unable to add " 
							+ currentFile.getAbsolutePath() 
							+ " to the database.");
				}
				

			
					

				
			}
		}
	}
	
	private EventHandler<ActionEvent> getMenuBarHandler(){
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
			public void handle( ActionEvent e ){
				Object eventSource = e.getSource();
				String itemID;
				System.out.println("button clicked");
				if( eventSource instanceof RadioMenuItem ){
					System.out.println("instance of radio item");
					RadioMenuItem radioMenuItem = (RadioMenuItem)eventSource;
					itemID = radioMenuItem.getId();
					
					switch( itemID ){
					case MENU_OPTIONS_USE_DEFAULT_PLAYER:
						if( radioMenuItem.isSelected() ){
							m_library.setPlayerBase(Library.PlayerBase.DEFAULT);
						}
						break;
					case MENU_OPTIONS_USE_VLC_PLAYER:
						System.out.println("use vlc clicked");
						if( radioMenuItem.isSelected() ){
							System.out.println("use vlc was selected");
							m_library.addVlcLibToNativeSearch(); 
							m_library.setPlayerBase(Library.PlayerBase.VLC);
						}
						break;
					default:
							break;
					}				
				}
				else if( eventSource instanceof MenuItem ){
					MenuItem item = (MenuItem) eventSource;
					itemID = item.getId();
					

					//handle the option
					switch( itemID ){
					case MENU_FILE_ADD_NEW_FILE:
						promptUserForFile();
						break;
					case MENU_FILE_EXIT:
						stop();
						break;
					case MENU_OPTIONS_CONFIG_VLC_LIB:
						showConfigVlcLibDialog();		
						break;
					case MENU_CHAT_OPEN:
						if( m_client == null || m_client.isShowing() == false ){
							m_client = new ClientDriver();
							m_client.initOwner( m_primaryStage );
							m_client.show();
							System.out.println("opened chat.");
						}
						else{
							System.out.println("didn't open chat");
							m_client.close();
						}
						break;
					default:
						break;			
					}				
				}
			}
		};
		
		return handler;
	}
	
	public static void main(String argv[]){
		launch(argv);
	}
}

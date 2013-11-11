package library.view;


import java.io.File;
import java.nio.ByteBuffer;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import com.sun.jna.Memory;


import library.model.Library;
import library.model.MediaItem;
import library.model.ObservablePlaylist;
import library.model.Song;
import library.model.Library.PLType;
import library.model.Library.PlayerBase;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ParallelTransitionBuilder;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.CheckMenuItemBuilder;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.RadioMenuItemBuilder;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPaneBuilder;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.PixelFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBuilder;
import javafx.util.Duration;

/**
 * Combined class containing the media player and its associated view, which
 * is contained within this region as a child
 * .
 * It handles both the default media player and its VLC counterpart.
 * @author Andrzej Brynczka
 *
 */
public class MediaPlayerRegion extends Region {

	/**
	 * Enumeration meant to detail the type of media that the media player is
	 * currently playing.
	 * @author Andrzej Brynczka
	 *
	 */
	public enum PlayerType{
		MUSIC,
		VIDEO;
	}
	
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************
	//ids used for component modification and identification
	private final String BUTTON_REPEAT = "PlayerRepeatButton";
	private final String BUTTON_PLAY_PAUSE = "PlayerPlayPauseButton";
	private final String BUTTON_BACK = "PlayerBackButton";
	private final String BUTTON_FORWARD = "PlayerForwardButton";
	
	//media player size settings
	private final double MIN_WIDTH = 320;
	private final double VIDEO_MIN_HEIGHT = 200;
	private final double MUSIC_HEIGHT = 50;
	private final double MAX_HEIGHT = 3500;
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************

	/** Reference to the main library object */
	private Library m_library;
	
	/** The default media player's view, used for videos */
	private MediaView m_mediaPlayerView;
	
	/** The default media player */
	private MediaPlayer m_mediaPlayer;
	
	/** The media player component used for the VLC player */
	private DirectMediaPlayerComponent m_vlcPlayer;
	
	/** The duration of the current media item, used by the default player */
	private Duration m_currentMediaDuration;
	
	/** The list of items queued by the media player */
	private ObservablePlaylist<MediaItem> m_playlist;
	
	/** The position within the playlist of the currently playing item */
	private int m_currentActiveItemPos;
	
	/** The currently playing item */
	private MediaItem m_currentActiveItem;
	
	/** The current type of media file the media player is playing */
	private PlayerType m_playerType;
	
	/** Determination of whether or not to resize the video player upon the 
	 * selection of a varying video dimension
	 */
	private boolean m_autoResizeToVideoHeight;
	
	/** Determination of whether or not to resize video content to a preset
	 * size.
	 */
	private boolean m_stretchVideoToPresetSize;
	
	/** Determination of whether or not to stretch a video to the current
	 * window size.
	 */
	private boolean m_stretchVideoToWindowSize;
	
	/** Whether or not to use a video's default dimensions */
	private boolean m_useDefaultVideoSize;
	
	/** A set preset width dimension, to be used for video resizing */
	private double m_presetWidth;
	
	/** A preset height dimension, to be used for video resizing */
	private double m_presetHeight;
	
	/** Whether or not to repeat the current media after it finishes playing */
	private boolean m_repeat;

	// *********************************************************
	// ******************** GUI Components *********************
	// *********************************************************
	private Canvas m_vlcDisplayCanvas;
	private ToolBar m_mainBar;
	private ToolBar m_timeBar;
	
	protected Slider m_volumeSlider;
	protected Slider m_timeSlider;
	protected Label m_playTimeLabel;
	
	private Button m_playPauseButton;
	private Button m_repeatButton;
	private ContextMenu m_playerContextMenu;

	private Text m_scrollingMediaName;
	private TranslateTransition m_mediaNameTransition;
	private ParallelTransition m_transitionFadeOut;
	private ParallelTransition m_transitionFadeIn;
	
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * Constructor of the MediaPlayerRegion.
	 * 
	 * @param a_library Library, reference to the main library object
	 * @param a_playlist ObservablePlaylist, reference to the "now playing"
	 * 	playlist to be used by the media player
	 * @param a_playerType PlayerType, the type of media to be played first
	 * 	(the type provided in the playlist,if the list has anything inside)
	 */
	public MediaPlayerRegion(Library a_library,
			ObservablePlaylist<MediaItem> a_playlist, 
			PlayerType a_playerType){
		m_library = a_library;
		m_playlist = a_playlist;
		m_currentActiveItem = null;
		m_currentActiveItemPos = -1;
		m_currentMediaDuration = null;

		m_playerType = a_playerType;
		
		m_mediaPlayer = null;
		m_mediaPlayerView = new MediaView();
		m_mediaPlayerView.setPreserveRatio( false );
		m_vlcPlayer = null;
		
		
		m_autoResizeToVideoHeight = false;
		m_stretchVideoToPresetSize = false;
		m_stretchVideoToWindowSize = false;
		m_useDefaultVideoSize = true;
		m_presetWidth = 320;
		m_presetHeight = 180;
		
		m_repeat = false;

		m_vlcDisplayCanvas = new Canvas();
		m_mainBar = new ToolBar();
		m_timeBar = new ToolBar();
		
		m_volumeSlider = null;
		m_timeSlider = null;
		m_playTimeLabel = null;
		
		m_playPauseButton = null;
		m_repeatButton = null;
		
		m_scrollingMediaName = null;
		m_mediaNameTransition = null;
		m_transitionFadeOut = null;
		m_transitionFadeIn = null;
		
		createMainBar();
		createTimeBar();
		createTransitions();
		setRegionEventHandlers();
		m_playerContextMenu = createAndGetPlayerContextMenu();
		
		if( m_playerType == PlayerType.MUSIC ){
			setBarStylesForMusic();
			setMinSize( MIN_WIDTH, MUSIC_HEIGHT );
			setHeight( MUSIC_HEIGHT );
			setMaxSize( 2500, MUSIC_HEIGHT );
		}
		else{
			setBarStylesForVideo();
			setMinSize( MIN_WIDTH, VIDEO_MIN_HEIGHT );
		}
		
		
		setStyle("-fx-background-color: black;");
		getChildren().addAll( m_mediaPlayerView, m_mainBar, m_timeBar);
	}
	
	// *********************************************************
	// ******************** Getter Functions *******************
	// *********************************************************
	
	/**
	 * Get the media type that the mediaplayer is currently playing.
	 * @return the type of media being played by the player, as a PlayerType
	 * 	object
	 * @author Andrzej Brynczka
	 */
	public PlayerType getPlayerType(){
		return m_playerType;
	}
	
	/**
	 * Get the media player's playing status.
	 * @return true if the media player is currently playing a media item, 
	 * 	false otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean isPlaying(){
		if( m_library.getPlayerBase() == PlayerBase.VLC ){
			if( m_vlcPlayer.getMediaPlayer().isPlaying() ){
				return true;
			}
			else{
				return false;
			}
		}
		else if( m_mediaPlayer == null || 
				m_mediaPlayer.getStatus() != Status.PLAYING ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get the media player's paused status.
	 * @return true if the media player is paused, false otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean isPaused(){
		if( m_library.getPlayerBase() == PlayerBase.VLC ){
			if( !m_vlcPlayer.getMediaPlayer().isPlaying() ){
				return true;
			}
			else{
				return false;
			}
		}
		
		if( m_mediaPlayer == null ||
				m_mediaPlayer.getStatus() != Status.PAUSED ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Helper Function. Determine if the media player exists or is active.
	 * @return true if the current player to be used is active, false otherwise
	 * @author Andrzej Brynczka
	 */
	private boolean playerExists(){
		if( ( m_library.getPlayerBase() == PlayerBase.VLC && 
				m_vlcPlayer == null ) || 
				( m_library.getPlayerBase() == PlayerBase.DEFAULT && 
				m_mediaPlayer == null ) ){
			return false;
		}
		
		return true;
	}
	// *********************************************************
	// ******************** Setter Functions *******************
	// *********************************************************
	/**
	 * Toggle the media player's repeat value, either enabling or disabling it.
	 * @author Andrzej Brynczka
	 */
	public void toggleRepeat(){
		
		if( m_repeat == true ){
			m_repeat = false;
			
			//modify the associated button displaying the change
			m_repeatButton.setText("Repeat: OFF");
			m_repeatButton.setTextFill( Color.DARKRED );
			
			if( m_vlcPlayer != null ){
				m_vlcPlayer.getMediaPlayer().setRepeat( false );
			}
			
			if( m_mediaPlayer != null ){
				m_mediaPlayer.setCycleCount( 1 );
			}
		}
		else{
			m_repeat = true;
			//modify the associated button displaying the change
			
			m_repeatButton.setText("Repeat: ON");
			m_repeatButton.setTextFill( Color.DARKBLUE );
			
			if( m_vlcPlayer != null ){
				m_vlcPlayer.getMediaPlayer().setRepeat( true );
			}
			
			if( m_mediaPlayer != null ){
				m_mediaPlayer.setCycleCount( MediaPlayer.INDEFINITE );
			}
		}
	}
	
	// *********************************************************
	// ****************** General Utility Functions ************
	// *********************************************************
	
	/**
	 * Close the media player, releasing its data.
	 * @author Andrzej Brynczka
	 */
	public void close(){
		if( m_mediaPlayer != null ){
			m_mediaPlayer.stop();
			m_mediaPlayer = null;
			m_mediaPlayerView = null;
		}
		
		if( m_vlcPlayer != null ){
			m_vlcPlayer.getMediaPlayer().release();
			m_vlcPlayer.getMediaPlayerFactory().release();
			m_vlcPlayer.release();
			m_vlcPlayer = null;
		}
	}
	
	/**
	 * Update the active item to be played by the media player based upon
	 * the playlist's changes.
	 * @author Andrzej Brynczka
	 */
	public void updateActiveItem(){
		if( m_playlist.size() == 0 ){
			m_currentActiveItemPos = -1;
			resetMediaPlayer();
			return;
		}
		
		//set the active item to the only remaining item in the list
		if( m_playlist.size() == 1 || m_currentActiveItem == null ){
			m_currentActiveItemPos = 0;
			m_currentActiveItem = m_playlist.getFirst();
			playCurrentMedia();
			return;
		}
		
		//check that the active item has not been removed from the list
		if( !m_playlist.contains( m_currentActiveItem ) ){
			//active item was removed, set a new item based on its old position	
			if( m_playlist.size() > m_currentActiveItemPos ){
				m_currentActiveItem = m_playlist.get( m_currentActiveItemPos );
				playCurrentMedia();
			}
			else{
				m_currentActiveItem = m_playlist.getFirst();
				m_currentActiveItemPos = 0;
				playCurrentMedia();
			}
		}
		else{
			//active item was NOT removed, but its position needs to be updated
			m_currentActiveItemPos = m_playlist.indexOf( m_currentActiveItem );
		}	
	}

	/**
	 * Choose to enable the VLC-based media player, switching from the
	 * default built-in JavaFX player.
	 * @param a_enable boolean, whether or not to enable the player
	 * @return true if the player was enabled/disabled as desired, false
	 * 	if the desired switch was not made
	 */
	public boolean enableVLCPlayer(boolean a_enable){
		if( a_enable == true ){
			if( createVLCMediaPlayer() == false ){
				return false;
			}
			
			if( m_mediaPlayer != null ){
				m_mediaPlayer.stop();
				m_mediaPlayer = null;
			}
			
			//switch this region's display from the default media player's
			//view to the VLC's display canvas
			getChildren().set(0, m_vlcDisplayCanvas);
			
			//turn off any options that may not work properly with the
			//VLC player
			disableContextMenuOptionsForVLC();
			
			//start the player
			playCurrentMedia();
			System.out.println("Enabled the vlc player and created it.");
		}
		else{
			if( m_vlcPlayer == null ){
				//none existant, already disabled
				return true;
			}
			
			//set the region's display to the default media player's view
			//from the prior VLC's m_
			getChildren().set(0, m_mediaPlayerView);
			
			//free the vlc player of its data
			m_vlcPlayer.getMediaPlayer().release();
			m_vlcPlayer.getMediaPlayerFactory().release();
			m_vlcPlayer.release();
			m_vlcPlayer = null;
			
			//renable all default options and start the player
			enableContextMenuOptionsAfterVLC();
			playCurrentMedia();
			System.out.println("Disabled the vlc player, reverted to default.");
		}
		
		layoutChildren();
		return true;
	}
	
	/**
	 * Switch the media player to its Music playing state, updating its 
	 * display as well.
	 */
	public void switchToMusic(){
		//modify its display height
		setMinSize( MIN_WIDTH, MUSIC_HEIGHT );
		setHeight( MUSIC_HEIGHT );
		setMaxSize( 2500, MUSIC_HEIGHT );
		
		//reset it to end its current songs and allow for a clean
		//play of the next
		resetMediaPlayer();
		
		//get the playlist of songs
		m_playerType = PlayerType.MUSIC;
		m_playlist = m_library.getPlaylist( PLType.NOW_PLAYING_SONGS );
		playMedia(0);
		
		//modify the display
		setBarStylesForMusic();
		layoutChildren();
	}
	
	/**
	 * Switch the media player to its Video playing state, updating its display
	 * as well.
	 */
	public void switchToVideo(){
		setMinSize( MIN_WIDTH, VIDEO_MIN_HEIGHT );
		setMaxSize( 2500, 2500);
		
		//reset it to end its current songs and allow for a clean
		//play of the next
		resetMediaPlayer();
		
		//get the playlist of videos
		m_playerType = PlayerType.VIDEO;
		m_playlist = m_library.getPlaylist( PLType.NOW_PLAYING_VIDEOS );
		
		//modify the display
		setBarStylesForVideo();
		layoutChildren();
	}
	
	@Override
	protected void layoutChildren(){
		if( m_playerType == PlayerType.MUSIC ){
			layoutForMusicPlayer();
		}
		else{
			layoutForVideoPlayer();
		}
		
	}
	
	/**
	 * Helper Function, to be used in layoutChildren().
	 * Layout the player's toolbars for its music display.
	 * @author Andrzej Brynczka
	 */
	private void layoutForMusicPlayer(){
		double currentHeight = this.getHeight();

		double mainBarHeight = currentHeight - 40;
		double timeBarHeight = currentHeight - 50;
		
		m_mainBar.resizeRelocate(0, mainBarHeight, this.getWidth(), 40);
		m_timeBar.resizeRelocate(0, timeBarHeight, this.getWidth(), 20);
	}
	
	/**
	 * Helper Function, to be used in layoutChildren().
	 * Layout the player's toolbars for its video display.
	 * @author Andrzej Brynczka
	 */
	private void layoutForVideoPlayer(){
		double currentHeight = this.getHeight();
		double currentWidth = this.getWidth();
		
		if( m_mediaPlayer != null && m_mediaPlayer.getMedia() != null ){
			
			//variables determining the image outputs relocation
			//to the center of the canvas, if necessary
			double relocH = 0;
			double relocW = 0;
			setMinHeight( VIDEO_MIN_HEIGHT );
			setMaxHeight( MAX_HEIGHT );
			
			if( m_stretchVideoToWindowSize ){
				m_mediaPlayerView.relocate( relocW, relocH );
				m_mediaPlayerView.setFitHeight( currentHeight );
				m_mediaPlayerView.setFitWidth( currentWidth );
			}
			else if( m_stretchVideoToPresetSize ){
				m_mediaPlayerView.setFitHeight( m_presetHeight );
				m_mediaPlayerView.setFitWidth( m_presetWidth );
				
				//center the image in the drawn region
				if( m_presetHeight < currentHeight ){
					relocH = (currentHeight - m_presetHeight) / 2;
				}
				
				if( m_presetWidth < currentWidth ){
					relocW = (currentWidth - m_presetWidth) / 2;
				}
				
				//move the view to the center of the canvas
				m_mediaPlayerView.relocate( relocW, relocH );
				
				if( m_autoResizeToVideoHeight ){
					setMinHeight( m_presetHeight );
					setMaxHeight( m_presetHeight );
				}
			}
			else {
				//draw the video frames based on video's own dimensions
				double mediaHeight = m_mediaPlayer.getMedia().getHeight();
				double mediaWidth = m_mediaPlayer.getMedia().getWidth();
				
				m_mediaPlayerView.setFitHeight( mediaHeight );
				m_mediaPlayerView.setFitWidth( mediaWidth );
				
				//center the image in the drawn region
				if( mediaHeight < currentHeight ){
					relocH = (currentHeight - mediaHeight) / 2;
				}
				
				if( mediaWidth < currentWidth ){
					relocW = (currentWidth - mediaWidth) / 2;
				}
				
				//move the view to the center of the canvas
				m_mediaPlayerView.relocate( relocW, relocH );
				
				if( m_autoResizeToVideoHeight ){
					setMinHeight( mediaHeight );
					setMaxHeight( mediaHeight );
				}
			}
			
		}
				
		double mainBarHeight = 0;
		double timeBarHeight = 15;
		if( currentHeight - 40 > 0 ){
			mainBarHeight = currentHeight - 40;
		}
		
		if( currentHeight - 55 > 0 ){
			timeBarHeight = currentHeight - 55;
		}
		m_mainBar.resizeRelocate(0, mainBarHeight, this.getWidth(), 40);
		m_timeBar.resizeRelocate(0, timeBarHeight, this.getWidth(), 20);
	}



	/**
	 * Stop the media player.
	 * @return true if the player exists and was stopped, false otherwise
	 */
	public boolean stop(){
		System.out.println("In player stopped function");
		if( !playerExists() ){
			return false;
		}
		
		if( m_library.getPlayerBase() == PlayerBase.VLC ){
			System.out.println("stopped vlc");
			m_vlcPlayer.getMediaPlayer().stop();
		}
		else{
			System.out.println("stopped default");
			m_mediaPlayer.stop();
		}
		
		m_playPauseButton.setText("Play");
		return true;
	}
	
	/**
	 * Pause the media player.
	 * @return true if the player exists and was paused, false otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean pause(){
		System.out.println("in pause");
		if( !playerExists() ){
			return false;
		}
		
		if( m_library.getPlayerBase() == PlayerBase.VLC ){
			System.out.println("pause vlc");
			m_vlcPlayer.getMediaPlayer().pause();
		}
		else {
			System.out.println("pause default");
			m_mediaPlayer.pause();
		}

		m_playPauseButton.setText("Play");
		return true;
	}
	
	/**
	 * Play the media player.
	 * @return true if the player exists and was played, false otherwise
	 * @author Andrzej Brynczka
	 */
	public boolean play(){
		System.out.println("in play");
		if( !playerExists() ){
			return false;
		}
		
		System.out.println("passed play condition");
		if( m_library.getPlayerBase() == PlayerBase.VLC ){
			System.out.println("play vlc");
			m_vlcPlayer.getMediaPlayer().play();
		}
		else{
			System.out.println("play default");
			m_mediaPlayer.play();
		}
		
		m_playPauseButton.setText("Pause");
		return true;
	}
	
	/**
	 * Increase the media player's volume by the given increment.
	 * Volume ranges from 0.0 - 1.0.
	 * Any increment above 1(100%) or below 0 is ignored.
	 * 
	 * @param a_increment double, the amount to raise the volume by
	 * @author Andrzej Brynczka
	 */
	public void volumeUp( double a_increment ){
		if( !playerExists() || a_increment > 1.0 || a_increment < 0 ){
			return;
		}

		if( m_library.getPlayerBase() == PlayerBase.VLC ){
			if( m_vlcPlayer.getMediaPlayer().getVolume() >= 200 ){
				return;
			}
			else{
				m_vlcPlayer.getMediaPlayer().setVolume(
						m_vlcPlayer.getMediaPlayer().getVolume() 
						+ (int)(a_increment*100) );
			}
		}
		else{
			if( m_mediaPlayer.getVolume() >= 1.0 ){
				return;
			}
			
			m_mediaPlayer.setVolume( m_mediaPlayer.getVolume() + a_increment );
		}
	}
	
	/**
	 * Decrease the media player's volume by the given amount.
	 * Volume ranges from 0.0 - 1.0; any amount greater than 1 or lower than 0 
	 * is ignored.
	 * 
	 * @param a_decrementValue double, the amount to decrease by
	 * @author Andrzej Brynczka
	 */
	public void volumeDown(double a_decrementValue ){
		if( !playerExists() || a_decrementValue > 1.0 || a_decrementValue < 0){
			return;
		}
		
		if( m_library.getPlayerBase() == PlayerBase.VLC ){
			if( m_vlcPlayer.getMediaPlayer().getVolume() <= 0 ){
				return;
			}
			else{
				m_vlcPlayer.getMediaPlayer().setVolume( 
						m_vlcPlayer.getMediaPlayer().getVolume() 
						- (int)(a_decrementValue*100) );
			}
		}
		
		if( m_mediaPlayer.getVolume() <= 0.0 ){
			return;
		}
		
		m_mediaPlayer.setVolume( m_mediaPlayer.getVolume() - a_decrementValue );
		
	}
	
	/**
	 * Play the next media item queued up on the playlist.
	 * 
	 * @return true if the next item is played, false otherwise
	 * @author Andrzej Brynczka
	 */
	protected boolean playNextMedia(){
		if( m_currentActiveItem == null ){
			resetMediaPlayer();
			return false;
		}
		
		//update the current active item to reflect the new media to be played
		m_currentActiveItem = m_playlist.getItemFollowing( 
				m_currentActiveItemPos );
		m_currentActiveItemPos++;
		
		if( m_currentActiveItem == null ){
			//if there is no further media, reset the player
			if( (m_currentActiveItem = m_playlist.getFirst() ) == null ){
				m_currentActiveItemPos = -1;
				resetMediaPlayer();
				return false;
			}
			else{
				//play the first item if the next could not be found
				m_currentActiveItemPos = 0;
			}
		}
		
		return playCurrentMedia();
	}
	
	/**
	 * Play the previous item on the playlist.
	 * @return true if the previous item is played, false otherwise
	 * @author Andrzej Brynczka
	 */
	protected boolean playPreviousMedia(){
		if( m_currentActiveItem == null ){
			resetMediaPlayer();
			return false;
		}
		
		//update the current item to reflect the new media to be played
		m_currentActiveItem = m_playlist.getItemPreceeding( 
				m_currentActiveItemPos );
		m_currentActiveItemPos--;
		
		if( m_currentActiveItem == null ){
			//play the last media if the previous can't be found
			if( (m_currentActiveItem = m_playlist.getLast()) == null ){
				m_currentActiveItemPos = -1;
				return false;
			}
			else{
				//get the last if the previous wasn't available
				m_currentActiveItemPos = m_playlist.size() - 1;
			}
		}
		
		return playCurrentMedia();
	}
	
	/**
	 * Play the currently selected media item.
	 * @return true if their is an item selected and it is played, 
	 * 	false otherwise
	 * @author Andrzej Brynczka
	 */
	protected boolean playCurrentMedia(){
		if( m_currentActiveItem == null ){
			resetMediaPlayer();
			return false;
		}
		
		if( setMediaItem( m_currentActiveItem ) == false ){
			return false;
		}
		
		startMediaNameTransitionForCurrentActive();
		m_playPauseButton.setText("Pause");
		return true;
	}
	
	/**
	 * Play the media item at the given location on the queued playlist.
	 * @param a_index int, the index of the desired item to play
	 * @return true if the index was valid and the item was played, false
	 * 	otherwise
	 * @author Andrzej Brynczka
	 */
	protected boolean playMedia( int a_index ){
		MediaItem mediaItem = m_playlist.get( a_index );
		
		if( mediaItem == null ){
			return false;
		}
		
		m_currentActiveItem = mediaItem;
		m_currentActiveItemPos = a_index;
		
		return playCurrentMedia();
	}
		

	/**
	 * Play the given media item.
	 * 
	 * @param a_item MediaItem, the item to be played
	 * @return true if the item's file location is valid and the item can be
	 * 	played, false otherwise
	 * @author Andrzej Brynczka
	 */
	protected boolean setMediaItem(MediaItem a_item){
		String filePath = a_item.getFilePath();
		File mediaFile = new File( filePath );
		
		if( !mediaFile.exists() ){
			System.out.println("ERROR: Setting media, file not found: "
					+ filePath);
			return false;	
		}
		
		//variable to hold the player's volume level, to save and re-set
		//for the next item
		double volume = 1;
		
		//recreate or stop the media player if necessary
		if( m_library.getPlayerBase() == PlayerBase.VLC ){
			System.out.println("Setting vlcplayer media");
			if( m_vlcPlayer != null ){
				volume = m_vlcPlayer.getMediaPlayer().getVolume();
				m_vlcPlayer.getMediaPlayer().stop();
			}
			else{
				volume = 100;
				createVLCMediaPlayer();
				System.out.println("had to create new player");
			}

			//set the media file and the player's handlers
			m_vlcPlayer.getMediaPlayer().prepareMedia( mediaFile.getPath() );
			m_vlcPlayer.getMediaPlayer().play();
			m_vlcPlayer.getMediaPlayer().setVolume( (int)volume );
			setVLCPlayerEventHandlers();
			
			m_currentMediaDuration = new Duration(
					m_vlcPlayer.getMediaPlayer().getLength());
		}
		else{
			System.out.println("Setting regular player media");
			
			//create the required JavaFX media file used in the player
			Media newMedia = new Media( mediaFile.toURI().toString() );
			if( newMedia.getError() != null ){
				System.out.println("ERROR: Setting media: " + filePath 
						+ newMedia.getError().getMessage() );
				return false;
			}
			
			if( m_mediaPlayer != null ){
				//if a player is active, stop it and preserve the 
				//volume for the next item
				volume = m_mediaPlayer.getVolume();
				m_mediaPlayer.stop();
			}
			
			//re-initialize the media player for the new file and play
			m_mediaPlayer = new MediaPlayer( newMedia );	
			m_currentMediaDuration = newMedia.getDuration();
			m_mediaPlayerView.setMediaPlayer( m_mediaPlayer );
			m_mediaPlayer.setVolume( volume );
			setMediaPlayerEventHandlers();		
			m_mediaPlayer.play();
		}
		
		return true;
	}
	
	/**
	 * Reset the media player, freeing it of its data.
	 * @author Andrzej Brynczka
	 */
	protected void resetMediaPlayer(){
		if( m_library.getPlayerBase() == PlayerBase.VLC && m_vlcPlayer != null ){
			m_vlcPlayer.getMediaPlayer().stop();
			m_vlcPlayer.getMediaPlayer().prepareMedia("");
		}
		else if( m_mediaPlayer != null ){
			m_mediaPlayer.stop();
			m_mediaPlayer = null;
		}

		//reset associated variables dependent upon the player
		m_currentMediaDuration = new Duration(0);
		m_currentActiveItem = null;
		m_currentActiveItemPos = -1;
		
		m_mediaNameTransition.stop();
		m_scrollingMediaName.setText("");
	}
	
	/**
	 * Force the VLC Player to resize by stopping and starting it, activating
	 * its callback.
	 * @author Andrzej Brynczka
	 * 
	 */
	private void resizeVLCPlayer(){
		//save the current time to allow the video to restart at the
		//same moment
		long time = m_vlcPlayer.getMediaPlayer().getTime();
		m_vlcPlayer.getMediaPlayer().stop();
		m_vlcPlayer.getMediaPlayer().play();
		m_vlcPlayer.getMediaPlayer().setTime( time );

	}

	
	/**
	 * Start the scrolling media name transition, overlayed over the media
	 * 	player's toolbars to aware the user of the currently playing item.
	 * @author Andrzej Brynczka
	 */
	private void startMediaNameTransitionForCurrentActive(){
		if( m_currentActiveItem == null ){
			return;
		}
		
		m_mediaNameTransition.stop();	
		m_scrollingMediaName.setText( m_currentActiveItem.getName() );
		
		//add the item's artist if possible
		if( m_currentActiveItem instanceof Song && 
				!((Song)m_currentActiveItem).getArtist().equals("") ){
			m_scrollingMediaName.setText( m_scrollingMediaName.getText() 
					+ " - " + ((Song) m_currentActiveItem).getArtist() );
		}
		
		//have the name scroll from one end of its starting point to the next
		m_mediaNameTransition.setToX( 
					- m_scrollingMediaName.getText().length() * 4);
		m_mediaNameTransition.setFromX( 
					m_scrollingMediaName.getText().length() * 2);
		m_mediaNameTransition.playFromStart();
	}
	// *********************************************************
	// *************** Component Creation Functions ************
	// *********************************************************
	
	/**
	 * Helper Function. 
	 * Create the main information bar for the media player's
	 * 	view. Creates the m_volumeSlider, m_repeatButton, m_playPauseButton,
	 * 	m_scrollingMediaName, and m_mediaNameTransition components
	 * 	and their eventhandlers. Adds the finished components to the m_mainBar
	 * 	component's children.
	 * @author Andrzej Brynczka
	 */
	private void createMainBar(){
		
		//create volume components
		Label volumeLabel = LabelBuilder.create()
				.text("Volume")
				.textFill( Color.WHITE )
				.minWidth( Control.USE_PREF_SIZE )
				.build();
		m_volumeSlider = SliderBuilder.create()
				.id("PlayerVolumeSlider")
				.prefWidth( 120 )
				.minWidth( 30 )
				.value(100)
				.build();
		m_volumeSlider.setMin(0);
		m_volumeSlider.setMax(100);
		
		//set a change listener to modify the media player's volume based on
		//the slider's position
		m_volumeSlider.valueProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed( ObservableValue<? extends Number> a_obsValue, 
					Number oldValue, Number newValue){
				
				if( m_volumeSlider.isValueChanging() ){
					
					if( m_library.getPlayerBase() == PlayerBase.VLC 
							&& m_vlcPlayer != null ){
						
						m_vlcPlayer.getMediaPlayer().setVolume( 
								(int) m_volumeSlider.getValue() );
					}
					else if( m_mediaPlayer != null ){
						m_mediaPlayer.setVolume( 
								m_volumeSlider.getValue() / 100.0 );
					}
				}
			}
		});
		
		//create the main player buttons
		m_repeatButton = ButtonBuilder.create()
				.id( BUTTON_REPEAT )
				.text( "Repeat: OFF" )
				.textFill( Color.DARKRED )
				.onAction( getButtonHandler() )
				.build();
		
		m_playPauseButton = ButtonBuilder.create()
				.id( BUTTON_PLAY_PAUSE )
				.text( "Play" )
				.onAction( getButtonHandler() )
				.build();
		
		Button backButton = ButtonBuilder.create()
				.id( BUTTON_BACK )
				.text( "Back" )
				.onAction( getButtonHandler() )
				.build();
		
		Button forwardButton = ButtonBuilder.create()
				.id( BUTTON_FORWARD )
				.text( "Forward" )
				.onAction( getButtonHandler() )
				.build();
		
		
		//create a scrolling text section, to display the
		//currently playing media name
		m_scrollingMediaName = TextBuilder.create()
				.textOrigin( VPos.CENTER )
				.textAlignment( TextAlignment.JUSTIFY )
				.text( "" )
				.font( Font.font("SansSerif", FontWeight.NORMAL, 12) )
				.build();

		//add a dropshadow to the scrolling text to make it easier to 
		//read against the black background
		if( m_playerType == PlayerType.VIDEO ){
			DropShadow ds = new DropShadow();
			ds.setColor( Color.BLACK);
			ds.setOffsetY(0.5);
			ds.setOffsetX(0.5);
			ds.setRadius(2.5);
			ds.setSpread(.50);
			
			m_scrollingMediaName.setEffect( ds );
			m_scrollingMediaName.setFill( Color.WHITE );
		}
		
	     // Provide the animated scrolling behavior for the text
	     m_mediaNameTransition = TranslateTransitionBuilder.create()
	    		 .duration( new Duration(15000) )
	    		 .node( m_scrollingMediaName )
	    		 .toX( -200 )
	    		 .fromX( 200 )
	    		 .toY( 10 )
	    		 .fromY( 10 )
	    		 .interpolator( Interpolator.EASE_BOTH )
	    		 .cycleCount( Timeline.INDEFINITE )
	    		 .build();
		      
	     ScrollPane scrollingTextPane = ScrollPaneBuilder.create()
	    		 .minWidth(125)
	    		 .maxWidth(this.getWidth()/3)//125
	    		 .maxHeight( 30 )
	    		 .hbarPolicy( ScrollBarPolicy.NEVER )
	    		 .vbarPolicy( ScrollBarPolicy.NEVER )
	    		 .content( m_scrollingMediaName )
	    		 .style("-fx-background-color: transparent;")
	    		 .build();
	     
		//organize the layout
		Separator vertSeparator = new Separator();
		vertSeparator.setOrientation( Orientation.VERTICAL );
		vertSeparator.setMinWidth(10);
		vertSeparator.setMinHeight(10);
		
		Separator vertSeparator2 = new Separator();
		vertSeparator2.setOrientation( Orientation.VERTICAL );
		vertSeparator2.setMinWidth(10);
		vertSeparator2.setMinHeight(10);

		//create a horizontal box to hold the main player
		//buttons and information
		HBox mainBarHBox = HBoxBuilder.create()
				.spacing(0)
				.alignment( Pos.CENTER )
				.opacity( 1.0 )
				.build();

		mainBarHBox.getChildren().addAll( scrollingTextPane, backButton,  
				m_playPauseButton, forwardButton, vertSeparator, 
				m_repeatButton, vertSeparator2, volumeLabel, m_volumeSlider);
		
		//finalize the main tool bar
		m_mainBar.getItems().add( mainBarHBox );		
	}
	
	/**
	 * Helper Function.
	 * Create the toolbar containing information on the current media's
	 * time/duration played. Initializes the m_timeSlider and m_playTimeLabel
	 * variables and sets their event handlers().  Adds the finished component
	 * to the m_timeBar toolbar's children.
	 * @author Andrzej Brynczka
	 */
	private void createTimeBar(){				
		//create the time label and slider
		Label timeLabel = LabelBuilder.create()
				.text("Time")
				.minWidth( 25 )
				.textFill( Color.WHITE )
				.build();
		
		m_timeSlider = SliderBuilder.create()
				.id("PlayerTimeSlider")
				.minWidth( 200 )
				.maxWidth(1020)
				.disable( true )
				.build();
		
		//set a listener to the slider, ensuring that it's change
		//modifies the media player's seek position
		m_timeSlider.valueProperty().addListener( new InvalidationListener(){
		
			@Override
			public void invalidated(Observable a_value){
				if( m_timeSlider.isValueChanging() ){
					
					//update the media player's seek position based on
					//the changing slider position
					if( m_currentMediaDuration != null ){		
						if( m_library.getPlayerBase() == PlayerBase.VLC && 
								m_vlcPlayer != null ){
							
							m_vlcPlayer.getMediaPlayer().setTime( 
									(long)m_currentMediaDuration.multiply( 
											m_timeSlider.getValue() / 100.0 )
												.toMillis() );
						}
						else if( m_mediaPlayer != null ){
							m_mediaPlayer.seek( 
									m_currentMediaDuration.multiply( 
											m_timeSlider.getValue() / 100.0 ) );
						}
					}

					updateDisplayValues();
				}
			}
		} );
		
		m_timeSlider.valueProperty().addListener( new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> a_observable,
					Number a_oldValue, Number a_newValue) {
				
				if( m_timeSlider.isValueChanging() ){
					if( m_currentMediaDuration != null ){
						if( m_library.getPlayerBase() == PlayerBase.VLC && 
								m_vlcPlayer != null ){
							
							m_vlcPlayer.getMediaPlayer().setTime( 
									(long)m_currentMediaDuration.multiply( 
											m_timeSlider.getValue() / 100.0 )
												.toMillis() );
						}
						else if( m_mediaPlayer != null ){
							m_mediaPlayer.seek( 
									m_currentMediaDuration.multiply( 
											m_timeSlider.getValue() / 100.0 ) );
						}
					}
				}
				
				updateDisplayValues();		
			}		
		} );
		
		//create a label to hold the current time information
		m_playTimeLabel = LabelBuilder.create()
				.prefWidth( 125 )
				.minWidth( 125 )
				.textFill( Color.WHITE )
				.build();
		
		
		//create a horizontal box to hold the time information
		//in a single row 
		HBox timeBarHBox = HBoxBuilder.create()
				.padding( new Insets(5, 10, 5, 10) )
				.alignment( Pos.CENTER )
				.opacity( 1.0 )
				.build();
		timeBarHBox.getChildren().addAll(timeLabel, m_timeSlider, 
				m_playTimeLabel);
		
		//finalize the time tool bar
		m_timeBar.getItems().add( timeBarHBox );
	}
	
	/**
	 * Create transitions for the main information and time tool bars, allowing
	 * 	them to fade in and out over the video player's display.  Initializes
	 * 	the m_transitionFadeIn and m_transitionFadeOut transition variables.
	 * @author Andrzej Brynczka
	 */
	private void createTransitions(){
		m_transitionFadeIn = ParallelTransitionBuilder.create()
				.children(
						FadeTransitionBuilder.create()
							.node( m_mainBar )
							.toValue( 1.0 )
							.duration( Duration.millis( 200 ) )
							.interpolator( Interpolator.EASE_OUT )
							.build(),
						
						FadeTransitionBuilder.create()
							.node( m_timeBar )
							.toValue( 1.0 )
							.duration( Duration.millis( 200 ) )
							.interpolator( Interpolator.EASE_OUT )
							.build()
						)
				.build();
		
		m_transitionFadeOut = ParallelTransitionBuilder.create()
				.children(
						FadeTransitionBuilder.create()
							.node( m_mainBar )
							.toValue( 0.0 )
							.duration( Duration.millis( 800 ) )
							.interpolator( Interpolator.EASE_OUT )
							.build(),
						
						FadeTransitionBuilder.create()
							.node( m_timeBar )
							.toValue( 0.0 )
							.duration( Duration.millis( 800 ) )
							.interpolator( Interpolator.EASE_OUT )
							.build()
						)
				.build();
	}
	
	/**
	 * Create the media player's context menu with options for resizing
	 * 	video output.
	 * @return the created ContextMenu
	 */
	private ContextMenu createAndGetPlayerContextMenu(){
		//create an item for auto-resizing player to the video size
		final CheckMenuItem autoResizeOption = CheckMenuItemBuilder.create()
				.text("Auto resize player to video height")
				.selected( false )
				.build();
		autoResizeOption.setOnAction( new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent a_evt) {
				if( autoResizeOption.isSelected() ){
					m_autoResizeToVideoHeight = true;		
				}
				else{
					m_autoResizeToVideoHeight = false;
					setMinSize( MIN_WIDTH, VIDEO_MIN_HEIGHT );
				}
				
				layoutChildren();
			}	
		});
		
		//create a mediaplayer menu for selecting video sizes
		Menu videoSizeMenu = new Menu("Video Size");
		ToggleGroup videoSizeGroup = new ToggleGroup();
		
		//create an option for using the video's default size 
		final RadioMenuItem defaultToVideoDimOption = 
				RadioMenuItemBuilder.create()
					.text("Use file's default dimensions")
					.toggleGroup( videoSizeGroup )
					.selected( true )
					.build();	
		defaultToVideoDimOption.setOnAction( new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent a_evt){
				if( defaultToVideoDimOption.isSelected() ){
					m_useDefaultVideoSize = true;
					m_stretchVideoToPresetSize = false;
					m_stretchVideoToWindowSize = false;
				}
				else{
					m_useDefaultVideoSize = false;
				}
				
				//resize the VLC player explicitly due to issues with
				//automatic resizing
				if( m_library.getPlayerBase() == PlayerBase.VLC ){
					resizeVLCPlayer();
				}

				layoutChildren();
				
			}
		});
		videoSizeMenu.getItems().addAll( 
				defaultToVideoDimOption, new SeparatorMenuItem() );
		
		//create an option for stretching to the media player's size
		final RadioMenuItem stretchToPlayerOption = 
				RadioMenuItemBuilder.create()
					.text("Stretch to player screen")
					.toggleGroup( videoSizeGroup )
					.selected( false )
					.build();
		stretchToPlayerOption.setOnAction( new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent a_evt) {
				if( stretchToPlayerOption.isSelected() ){
					m_stretchVideoToWindowSize = true;
					m_stretchVideoToPresetSize = false;
					m_useDefaultVideoSize = false;
				}
				else{
					m_stretchVideoToWindowSize = false;
				}
				
				//resize the VLC player explicitly due to issues with
				//automatic resizing
				if( m_library.getPlayerBase() == PlayerBase.VLC ){
					resizeVLCPlayer();
				}
				layoutChildren();
			}
		});
		videoSizeMenu.getItems().addAll( 
				stretchToPlayerOption, new SeparatorMenuItem() );
		
		//create numerous video preset size options
		RadioMenuItem size1 = getVideoSizeMenuItem( 320, 180 );
		size1.setToggleGroup( videoSizeGroup );
		RadioMenuItem size2 = getVideoSizeMenuItem( 480, 360 );
		size2.setToggleGroup( videoSizeGroup );
		RadioMenuItem size3 = getVideoSizeMenuItem( 640, 360 );
		size3.setToggleGroup( videoSizeGroup );
		RadioMenuItem size4 = getVideoSizeMenuItem( 720, 480 );
		size4.setToggleGroup( videoSizeGroup );
		RadioMenuItem size5 = getVideoSizeMenuItem( 720, 560 );
		size5.setToggleGroup( videoSizeGroup );
		RadioMenuItem size6 = getVideoSizeMenuItem( 800, 600 );
		size6.setToggleGroup( videoSizeGroup );
		RadioMenuItem size7 = getVideoSizeMenuItem( 960, 720 );
		size7.setToggleGroup( videoSizeGroup );
		RadioMenuItem size8 = getVideoSizeMenuItem( 1024, 640 );
		size8.setToggleGroup( videoSizeGroup );
		RadioMenuItem size9 = getVideoSizeMenuItem( 1024, 768 );
		size9.setToggleGroup( videoSizeGroup );
		RadioMenuItem size10 = getVideoSizeMenuItem( 1280, 720 );
		size10.setToggleGroup( videoSizeGroup );
		RadioMenuItem size11 = getVideoSizeMenuItem( 1440, 1080 );
		size11.setToggleGroup( videoSizeGroup );
		RadioMenuItem size12 = getVideoSizeMenuItem( 1600, 1200 );
		size12.setToggleGroup( videoSizeGroup );
		RadioMenuItem size13 = getVideoSizeMenuItem( 1920, 1080 );
		size13.setToggleGroup( videoSizeGroup );
		RadioMenuItem size14 = getVideoSizeMenuItem( 1920, 1200 );
		size14.setToggleGroup( videoSizeGroup );
		
		videoSizeMenu.getItems().addAll(size1, size2, size3, size4, size5, 
				size6, size7, size8, size9, size10, size11, size12, 
				size13, size14 );
		
		//return the final menu
		ContextMenu conMenu = new ContextMenu();
		conMenu.getItems().addAll( autoResizeOption, videoSizeMenu );
		return conMenu;
	}
	
	/**
	 * Get a RadioMenuItem used in the creation of the media player context
	 * 	menu to utilize preset video sizes.
	 * @param a_width int, the preset width to associate with this menu item
	 * @param a_height int, the preset height to associate with this menu item
	 * @return the RadioMenuItem to be used within a context menu
	 * @author Andrzej Brynczka
	 */
	private RadioMenuItem getVideoSizeMenuItem(
			final int a_width, final int a_height){
		
		final RadioMenuItem sizeMenuItem = RadioMenuItemBuilder.create()
				.text(a_width + " x " + a_height)
				.selected( false )
				.build();
		sizeMenuItem.setOnAction( new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent a_evt) {
				if( sizeMenuItem.isSelected() ){
					m_stretchVideoToPresetSize = true;
					m_stretchVideoToWindowSize = false;
					m_useDefaultVideoSize = false;
					m_presetWidth = a_width;
					m_presetHeight = a_height;
				}
				else{
					m_stretchVideoToPresetSize = false;
				}
				
				//resize the VLC player explicitly due to issues with
				//automatic resizing
				if( m_library.getPlayerBase() == PlayerBase.VLC ){
					resizeVLCPlayer();
				}
				layoutChildren();
			}
		});
		
		return sizeMenuItem;
	}
	
	/**
	 * Get the BufferFormatCallback for the VLC player creation.
	 * @return the BufferFormatCallback updated for the current player state
	 * @author Andrzej Brynczka
	 */
	private BufferFormatCallback getVLCBufferFormatCallback(){
		BufferFormatCallback bufFormCallback = new BufferFormatCallback() {

			@Override
			public BufferFormat getBufferFormat(int sourceWidth,
					int sourceHeight) {
				//base the buffer format on the current desired size dimensions
				return new RV32BufferFormat( 
						(int)m_presetWidth, (int)m_presetHeight );
			}
			  
		  };
		  
		  return bufFormCallback;
	}
	
	/**
	 * Create the VLC-based player component and override its display function
	 * 	to allow for updated size control.
	 * @return true if the player was created successfully, false otherwise
	 * @author Andrzej Brynczka
	 */
	private boolean createVLCMediaPlayer(){
		try{
		  m_vlcPlayer = new DirectMediaPlayerComponent( 
				  getVLCBufferFormatCallback() ) {
        
			  public void display(DirectMediaPlayer a_mediaPlayer, 
					  Memory[] a_nativeBuffers, BufferFormat a_bufferformat){
				  
				  	double currentHeight = getHeight();
					double currentWidth = getWidth();
					
					//values used to determine where the video display
					//should start relative to the canvas it appears on
					double relocH = 0;
					double relocW = 0;

					m_vlcDisplayCanvas.setHeight( m_presetHeight );
					m_vlcDisplayCanvas.setWidth( m_presetWidth );
					
					//center the image in the drawn region
					if( m_presetHeight < currentHeight ){
						relocH = (currentHeight - m_presetHeight) / 2;
					}
					
					if( m_presetWidth < currentWidth ){
						relocW = (currentWidth - m_presetWidth) / 2;
					}
					
					//move the drawn canvas to the relocation points within
					//this region
					m_vlcDisplayCanvas.relocate( relocW, relocH );
					
					//force the region to take the size of the video output
					//if desired by the user
					if( m_autoResizeToVideoHeight ){
						setMinHeight( m_presetHeight );
						setMaxHeight( m_presetHeight );
					}
					
					//draw the video output image to the display canvas
				  	Memory nativeBuffer = a_nativeBuffers[0];
				  	ByteBuffer byteBuffer = 
				  			nativeBuffer.getByteBuffer(0, nativeBuffer.size());

				  	m_vlcDisplayCanvas.getGraphicsContext2D()
				  		.getPixelWriter().setPixels(0, 0, (int)m_presetWidth, 
				  				(int)m_presetHeight, 
				  				PixelFormat.getByteBgraPreInstance(), 
				  				byteBuffer, (int)m_presetWidth*4);

			  }
		  };
		}catch( Exception e ){
			System.out.println("Incorrect libvlc.dll path provided");
			return false;
		}catch( Throwable t ){
			System.out.println("Incorrect VLC program directory provided.");
			return false;
		}

		  //set the current player settings
		  m_vlcPlayer.getMediaPlayer().setRepeat( m_repeat );
		  return true;
	}
	
	// *********************************************************
	// ************* Component Modification Functions **********
	// *********************************************************
	/**
	 * Disable options on the video context menu that create issues with the
	 * VLC-based player.
	 * @author Andrzej Brynczka
	 */
	private void disableContextMenuOptionsForVLC(){
		//disable the ability to stretch the video to the player's size
		//and the ability to use the video file's default size
		Menu videoSizeMenu = (Menu) m_playerContextMenu.getItems().get(1) ;
		RadioMenuItem item1 = (RadioMenuItem) videoSizeMenu.getItems().get(0);
		RadioMenuItem item2 = (RadioMenuItem) videoSizeMenu.getItems().get(2);
		
		item1.setDisable( true );
		item2.setDisable( true );	
		
		//deactivate any disallowed options if necessary
		if( item1.isSelected() || item2.isSelected() ){
			RadioMenuItem temp = 
					(RadioMenuItem) videoSizeMenu.getItems().get(4);
			temp.setSelected( true );

			m_stretchVideoToPresetSize = true;
			m_stretchVideoToWindowSize = false;
			m_useDefaultVideoSize = false;
			m_presetWidth = 320;
			m_presetHeight = 180;
		}	
	}
	
	/**
	 * Enable options on the video context menu that were previously
	 * disabled due to VLC compatibility issues.
	 * @author Andrzej Brynczka
	 */
	private void enableContextMenuOptionsAfterVLC(){
		//enable option to stretch video dimensions to media player size
		//and the option to use the video file's default dimensions
		Menu videoSizeMenu = (Menu) m_playerContextMenu.getItems().get(1) ;
		videoSizeMenu.getItems().get(0).setDisable( true );
		videoSizeMenu.getItems().get(2).setDisable( true );
	}
	
	/**
	 * Modify the main and time toolbars' visual appearance, allowing
	 * them to appear less intrusive during video playback.
	 * @author Andrzej Brynczka
	 */
	private void setBarStylesForVideo(){
		//add a dropshadow to scrolling file name text to ensure it is
		//easier to see over dark video backrounds
		DropShadow ds = new DropShadow();
		ds.setColor( Color.BLACK);
		ds.setOffsetY(0.5);
		ds.setOffsetX(0.5);
		ds.setRadius(3.5);
		ds.setSpread(.75);
		
		m_scrollingMediaName.setEffect( ds );
		m_scrollingMediaName.setFill( Color.WHITE );

		//make the toolbars transparent
		m_mainBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
		m_mainBar.setOpacity( 0.0 );
		m_mainBar.getItems().get(0).setStyle(
					"-fx-background-color: rgba(0, 0, 0, 0.0);");
		
		m_timeBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
		m_timeBar.setOpacity( 0.0 );
		m_timeBar.getItems().get(0).setStyle(
					"-fx-background-color: rgba(0, 0, 0, 0.0);");
	}
	
	/**
	 * Modify the main and time toolbars' visual appearance, allowing them
	 * to be cover the dark canvas used during video playback.
	 * @author Andrzej Brynczka
	 */
	private void setBarStylesForMusic(){
		//remove the dropshadow from scrolling text added during video playback
		m_scrollingMediaName.setEffect( null );
		m_scrollingMediaName.setFill( Color.BLACK );
		
		//remove toolbar transparency, providing the bars with a common color
		m_mainBar.setStyle("-fx-background-color: darkgray;");
		m_mainBar.setOpacity( 1.0 );
		
		m_timeBar.setStyle("-fx-background-color: darkgray;");
		m_timeBar.setOpacity( 1.0 );
	}

	/**
	 * Format the time values presented on the toolbars during media playback.
	 * 
	 * This code was retrieved from javaFX "Ensemble" sample code and modified
	 * slightly to work here.
	 * Ensemble can be found at the JavaFX sample code directory:
	 * http://www.oracle.com/technetwork/java/javase/downloads/
	 * 	jdk7-downloads-1880260.html
	 * 
	 * @param a_current Duration, the current playback time for the media
	 * @param a_total Duration, the current playing media's total duration
	 * @return the current and total durations formatted in a String as
	 * 	"hh:mm:ss / hh:mm:ss" 
	 */
	protected String formatTime(Duration a_current, 
			Duration a_total){

        int intCurrent = (int)Math.floor(a_current.toSeconds());
        int elapsedHours = intCurrent / (60 * 60);

        if (elapsedHours > 0) {
        	intCurrent -= elapsedHours * 60 * 60;
        }

        int elapsedMinutes = intCurrent / 60;
        int elapsedSeconds = intCurrent - (elapsedMinutes * 60);

        if (a_total.greaterThan(Duration.ZERO)) {

            int intTotal = (int)Math.floor(a_total.toSeconds());
            int durationHours = intTotal / (60 * 60);

            if (durationHours > 0) {
            	intTotal -= (durationHours * 60 * 60);
            }

            int durationMinutes = intTotal / 60;
            int durationSeconds = intTotal - (durationMinutes * 60);

            
            if (durationHours > 0) {
                
            	return String.format("%d:%02d:%02d/%d:%02d:%02d",
            			elapsedHours, elapsedMinutes, elapsedSeconds,
            			durationHours, durationMinutes, durationSeconds);
            } else {
               
            	return String.format("%02d:%02d/%02d:%02d",
                                     elapsedMinutes, elapsedSeconds,
                                     durationMinutes, durationSeconds);
            }

        } else {
        		
        	return new String("00:00:00");
        }

    }

	/**
	 * Update the time and volume display values for the media player, alongside
	 * their respective sliders, as the media player plays.
	 * 
	 * This code was retrieved from javaFX "Ensemble" sample code and modified
	 * slightly to work here.
	 * Ensemble can be found at the JavaFX sample code directory:
	 * http://www.oracle.com/technetwork/java/javase/downloads/
	 * 	jdk7-downloads-1880260.html
	 * 
	 */
	protected void updateDisplayValues(){
        if (m_playTimeLabel != null && m_timeSlider != null && 
        		m_volumeSlider != null && m_currentMediaDuration != null) {

            Platform.runLater(new Runnable() {

                public void run() {
                	
            		if( !playerExists() ){            		
                		m_playTimeLabel.setText( "00:00:00/00:00:00" );
                		m_timeSlider.setDisable( true );
                		return;
                	}

                	Duration currentTime;
                	if( m_library.getPlayerBase() == PlayerBase.VLC ){
                		currentTime = new Duration( 
                				m_vlcPlayer.getMediaPlayer().getTime() ) ;  
                	}
                	else{
                		currentTime = m_mediaPlayer.getCurrentTime();
                	}
                	
                    m_playTimeLabel.setText(
                    		formatTime(currentTime, m_currentMediaDuration) );
                   
                    m_timeSlider.setDisable(m_currentMediaDuration.isUnknown());

                    if (!m_timeSlider.isDisabled() && 
                    		m_currentMediaDuration.greaterThan(Duration.ZERO) && 
                    		!m_timeSlider.isValueChanging()) {

                        m_timeSlider.setValue(
                        		currentTime.divide( 
                        				m_currentMediaDuration.toMillis() )
                        					.toMillis() * 100.0);

                    }

                    if (!m_volumeSlider.isValueChanging()) {

                    	if( m_library.getPlayerBase() == PlayerBase.VLC ){
                    		m_volumeSlider.setValue( 
                    				(int)Math.round( 
                    						m_vlcPlayer.getMediaPlayer().
                    							getVolume() ) );
                    	}
                    	else{
                        m_volumeSlider.setValue((int) 
                        		Math.round(m_mediaPlayer.getVolume() * 100));
                    	}

                    }

                }

            });
        }
	}

	// *********************************************************
	// **************** EventHandler Functions *****************
	// *********************************************************
	/**
	 * Get the EventHandler for the media player's buttons, including
	 * 	the repeat button, play/pause button, forward button, and back button.
	 * @return the ActionEvent EventHandler specializing in button actions
	 * @author Andrzej Brynczka
	 */
	private EventHandler<ActionEvent> getButtonHandler(){
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				Object source = event.getSource();
				
				if( source instanceof Button ){		
					Button currentButton = (Button) source;
					
					switch( currentButton.getId() ){
					case BUTTON_REPEAT:
						toggleRepeat();
						break;
					case BUTTON_PLAY_PAUSE:
						System.out.println("play/pause clicked...");
						if( m_library.getPlayerBase() == PlayerBase.VLC ){
							System.out.println("in vlc");
							if( m_vlcPlayer != null && 
									m_vlcPlayer.getMediaPlayer().isPlaying() ){
								pause();
							}
							else{
								play();
							}
						}
						else{
							System.out.println("in default");
							if( m_mediaPlayer != null && 
								m_mediaPlayer.getStatus() == Status.PLAYING ){
								pause();
							}
							else{
								play();
							}
						}
						break;
					case BUTTON_BACK:
						if( m_playlist.size() > 1 ){
							playPreviousMedia(); 
						}
						break;
					case BUTTON_FORWARD:
						if( m_playlist.size() > 1 ){
							playNextMedia();
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
	
	/**
	 * Set the event handlers for the default media player, focusing on the
	 * change of its time property to update its displayed values and its
	 * endOfMedia event to play another item on the playlist.
	 * @author Andrzej Brynczka
	 */
	private void setMediaPlayerEventHandlers(){
		if( m_mediaPlayer == null ){
			return;
		}
		
		m_mediaPlayer.setCycleCount( m_repeat ? MediaPlayer.INDEFINITE : 1);
		
		m_mediaPlayer.currentTimeProperty().addListener( 
				new ChangeListener<Duration>(){
			@Override
			public void changed(ObservableValue<? extends Duration> a_obsVal, 
					Duration oldValue, Duration newValue){
				updateDisplayValues();
			}
		});
		
		m_mediaPlayer.setOnReady( new Runnable(){
			public void run(){
				m_currentMediaDuration = m_mediaPlayer.getMedia().getDuration();
				updateDisplayValues();
			}
		});
		
		m_mediaPlayer.setOnEndOfMedia( new Runnable() {
			public void run(){
				
				if( m_repeat ){
					m_mediaPlayer.seek( Duration.ZERO );
					m_mediaPlayer.play();
				}
				else{					
					m_mediaPlayer.stop();
					m_playPauseButton.setText("Play");
					
					if( m_playlist.size() > 1 ){
						playNextMedia();
					}
				}
			}
		});
	}
	
	/**
	 * Set EventHandlers for the VLC player, allowing for the update of its
	 * 	time and volume values, as well as the playing of new media upon
	 * 	the end of a prior.
	 * @author Andrzej Brynczka
	 */
	private void setVLCPlayerEventHandlers(){
		if( m_vlcPlayer == null ){
			return;
		}
		
		m_vlcPlayer.getMediaPlayer().addMediaPlayerEventListener(
				new MediaPlayerEventAdapter(){
							
			@Override
	        public void timeChanged( 
	        		uk.co.caprica.vlcj.player.MediaPlayer a_player, 
	        		long a_newTime){
				
	        	updateDisplayValues();
	        }
	        
	        @Override
	        public void finished(
	        		uk.co.caprica.vlcj.player.MediaPlayer a_player ){
	        	
	        	if( m_repeat ){
	        		//issues with the player's built-in repeat require this
	        		//workaround
	        		m_vlcPlayer.getMediaPlayer().stop();
					m_vlcPlayer.getMediaPlayer().setPosition(0);
					m_vlcPlayer.getMediaPlayer().play();
				}
				else{
					m_vlcPlayer.getMediaPlayer().stop();

					//Modify the relevant button of the player's end.
					//Due to the this funcion launching in a non-javafx thread,
					//the platform must be used to run a thread capable of
					//modifying the javaFX button component
					Platform.runLater( new Runnable(){
						@Override
						public void run(){
							m_playPauseButton.setText("Play");
							System.out.println("can change button");
							
							if( m_playlist.size() > 1 ){
								playNextMedia();
							}
						}
					});

				}	        	
	        }
	        
	        @Override
	        public void playing( 
	        		uk.co.caprica.vlcj.player.MediaPlayer a_player ){
	        	
	        	m_currentMediaDuration = new Duration( 
	        				m_vlcPlayer.getMediaPlayer().getLength() );
				updateDisplayValues();
	        }
		});
	}
	
	/**
	 * Set EventHandlers for this display region, allowing the fadein/out
	 * transitions to play upon mouse over and for mouseclicks to start/pause
	 * the video player.
	 * @author Andrzej Brynczka
	 */
	private void setRegionEventHandlers(){

		this.setOnMouseEntered( new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				if( m_playerType == PlayerType.VIDEO ){
					m_transitionFadeIn.stop();
					m_transitionFadeIn.play();
				}
			}
		});
		
		this.setOnMouseExited( new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event){
				if( m_playerType == PlayerType.VIDEO ){
					m_transitionFadeOut.stop();		
					m_transitionFadeOut.play();
				}
			}
		});
		
		final MediaPlayerRegion thisRegion = this;
		this.setOnMouseClicked( new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent a_evt){
				if( MouseButton.SECONDARY.equals( a_evt.getButton() ) ){
					//show the context menu over this region at the location
					//of the player's click
					m_playerContextMenu.show( 
							thisRegion, a_evt.getScreenX(), a_evt.getScreenY());
				}

				//play/pause the video if the user clicks the region
				if( MouseButton.PRIMARY.equals( a_evt.getButton() ) ){
					if( m_playerContextMenu.isShowing() ){
						m_playerContextMenu.hide();
					}
					else if( !playerExists() ){
						return;
					}
					else if( m_library.getPlayerBase() == PlayerBase.VLC ){
						
						if( m_vlcPlayer.getMediaPlayer().isPlaying() ){
							pause();
						}
						else{
							play();
						}
						
					}
					else{
						if(m_mediaPlayer.getStatus().equals( Status.PLAYING )){
							pause();
						}
						else {
							play();
						}
					}
			
				}
			}
		});	
	}	
}

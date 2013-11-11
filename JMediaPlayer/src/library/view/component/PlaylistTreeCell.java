package library.view.component;

import java.sql.SQLException;
import java.util.Collection;


import library.model.Library;
import library.model.Song;
import library.model.Video;
import library.view.MediaPane;
import library.view.MediaPlayerRegion;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Class used as a component for a TreeView, specialized towards the display
 * of media information within the MediaPane's TreeView object containing
 * playlist names.
 * 
 * @author Andrzej Brynczka
 *
 */
public class PlaylistTreeCell extends TreeCell<String> {

	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** Reference to the main library object */
	private Library m_library;
	
	/** Reference to the containing MediaPane */
	private MediaPane m_mediaPane;
	
	/** Reference to the mediaplayer within the containing MediaPane */
	private MediaPlayerRegion a_mediaPlayerRegion;
	
	/** Reference to the tree that owns this cell */
	private TreeView<String> m_ownerTree;

	// *********************************************************
	// ******************** GUI Components *********************
	// *********************************************************
	/** Component containing the data to be presented */
	private TextField m_displayedTextField;
	
	/** ContextMenu utilized by this individual tree cell */
	private ContextMenu m_contextMenu;
	
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * Constructor for the PlaylistTreeCell.
	 * 
	 * @param a_library Library, Reference to the main library object
	 * @param a_mediaPane MediaPane, Reference to the containing MediaPane
	 * @param a_mediaPlayer MediaPlayerRegion, Reference to the mediaplayer 
	 * 	within the containing MediaPane
	 * @param a_treeView TreeView, Reference to the tree that owns this cell
	 */
	public PlaylistTreeCell(Library a_library, MediaPane a_mediaPane, 
			MediaPlayerRegion a_mediaPlayer,
		    TreeView<String> a_treeView ) {
		
	    createDisplayedTextField();
	    createContextMenu();
	    
	    m_library = a_library;
	    m_ownerTree = a_treeView;
	    m_mediaPane = a_mediaPane;
	    a_mediaPlayerRegion = a_mediaPlayer;
   }
	
	/**
	 * Determine if the current item is a root item that should
	 * be ignored for most actions.(As roots are indicators of
	 * playlist types, but not playlists themselves.)
	 * 
	 * @return true if the item is a root item, false otherwise
	 */
	private boolean isItemRoot(){
		TreeItem<String> currentItem = getTreeItem();
		
		//item 0 after main root = music playlist root
		//item 1 after main root = video playlist root
		if( currentItem == m_ownerTree.getRoot() ||
				currentItem == m_ownerTree.getRoot().getChildren().get(0) ||
				currentItem == m_ownerTree.getRoot().getChildren().get(1) ){
			return true;
		}
		
		return false;
	}

   /**
    * Get the text value associated with this cell.
    * @return String, the text value
    */
   private String getAssociatedText() {
	   if( getItem() == null ){
		   return getText();
	   }
	   
	   String text = getItem().toString();
	   if( text == null || text.equals("") ){
		   text = getText();
	   }
       return text;
   }
	   
	/**
	 * Begin the editing process of this cell's value.
	 */
   @Override
   public void startEdit() {
		//don't allow the root items be edited as they act as indicators
		//and not playlist names
		if( isItemRoot() ){
			return;
		}
				   
		super.startEdit();
				
		m_displayedTextField.setText( getAssociatedText() );    
		setText( null );
		setGraphic( m_displayedTextField );
		m_displayedTextField.selectAll();
		m_displayedTextField.requestFocus();
   }
		

   /**
    * Cancel the editing of this cell's value. Make the changes necessary to
    * 	ensure that the proper value is maintained.
    */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		
		setText( getAssociatedText() );
		setGraphic( getTreeItem().getGraphic() );
	}


   @Override
   public void updateItem(String item, boolean empty) {
       super.updateItem(item, empty);
       
       
       if ( empty ) {
           setText( null );
           setGraphic( null );
       } 
       else if ( isEditing() ) {
               if ( m_displayedTextField != null ) {
            	   m_displayedTextField.setText( getAssociatedText() );
               }
               
               setText( m_displayedTextField.getText() );
               setGraphic( m_displayedTextField );
       }
       else {
               setText( getAssociatedText() );
               setGraphic( getTreeItem().getGraphic() );
               
               if( isItemRoot() ){
            	   return;
               }
               
               //set and update the context menu's first item to 
               //display the playlist's name
               m_contextMenu.getItems().get(0).setText( 
            		   getAssociatedText() + ": " );
               setContextMenu( m_contextMenu );

       }      
   }
   
	// *********************************************************
	// ************* Component Creation Functions **************
	// *********************************************************
   /**
    * Helper Function.
    * Create the context menu used by this cell. This function initializes
    * the cell's m_contextMenu member variable and sets its action/event
    * handlers.
    */
   private void createContextMenu(){
	   
	   MenuItem nameOption = new MenuItem("");
	   nameOption.setId("Playlist Name");
	   nameOption.setStyle("-fx-font-weight: bold;");
	   
	   MenuItem deleteOption = new MenuItem("Delete" );
	   deleteOption.setId("Delete PlayList");
	   deleteOption.setOnAction( new EventHandler<ActionEvent>(){
	   
		@Override
		public void handle(ActionEvent event) {
			String playlistName = getText();
			try {
				
				if( isItemRoot() ){
					return;
				}
				
				if( m_library.isVideoPlaylist( playlistName ) ){
					//root item for music playlists, under the main tree root
					TreeItem<String> musicRoot = 
							m_ownerTree.getRoot().getChildren().get(0);	
					
					//root item for video playlists, under the main tree root
					TreeItem<String> videoRoot = 
							m_ownerTree.getRoot().getChildren().get(1);
					
					//the cell's index is based upon all elements in the tree...
					//it must therefore account for all items prior to it
					//including the 3 root items
					int indexUnderVideoRoot = getIndex() - 
							musicRoot.getChildren().size() - 3;
					
					videoRoot.getChildren().remove( indexUnderVideoRoot );			
					m_mediaPane.
						removePLNameFromVideoContextMenus( indexUnderVideoRoot );
				}
				else{
					
					//subtract 2 to account for the main tree and music roots
					m_ownerTree.getRoot().getChildren().get(0)
						.getChildren().remove( getIndex() - 2 );
					
					m_mediaPane.
						removePLNameFromSongContextMenus( getIndex() - 2 );
				}
				
				m_library.dropPlaylist( playlistName );
				System.out.println("deleted playlist: " + playlistName );
			} catch (SQLException e) {
				System.out.println("Couldn't delete playlist: " 
						+ playlistName );
				System.out.println( e.getMessage() );
			}
			
		}	   
	   });
	   
	   MenuItem playOption = new MenuItem("Play");
	   playOption.setId("Play Playlist");
	   playOption.setOnAction( new EventHandler<ActionEvent>(){

		@Override
		public void handle(ActionEvent event) {
			String playlistName = getAssociatedText();
			
			try{		
				if( m_library.isVideoPlaylist( playlistName ) ){
					Collection<Video> videos = 
							m_library.getVideosInPlaylist( playlistName );
					m_library.setNowPlayingVideoList( videos );
				}
				else{
					Collection<Song> songs = 
							m_library.getSongsInPlaylist( playlistName );
					m_library.setNowPlayingSongList( songs );
				}
				
				a_mediaPlayerRegion.updateActiveItem();
			}
			catch( SQLException e ){
				System.out.println("Couldn't play " + playlistName);
				System.out.println( e.getMessage() );
			}			
		}		   
	   });
	   
	   SeparatorMenuItem divider = new SeparatorMenuItem();
	   
   	   m_contextMenu = new ContextMenu();
	   m_contextMenu.getItems().addAll( nameOption, divider, 
			   playOption,  deleteOption );
   }
   
   /**
    * Helper Function.
    * This function creates the textfield that is to be displayed upon the
    * cell's editing process, initializing the cell's m_displayedTextField
    * variable and setting its required event handler.
    */
   private void createDisplayedTextField() {
	   m_displayedTextField = new TextField( );
	   m_displayedTextField.setText( getAssociatedText() );
	   
	   m_displayedTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
           @Override
           public void handle(KeyEvent t) {
               if (t.getCode() == KeyCode.ENTER) {      
            	   commitEdit( m_displayedTextField.getText() );          	   
               } else if (t.getCode() == KeyCode.ESCAPE) {
                   cancelEdit();
               }
           }
       });         
   }

   
}

package client.view.component;


import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

/**
 * This extension to the basic ListCell component incorporates a
 * contextMenu and overwrites the menu's topmost item with the 
 * textual value of the current cell.
 * 
 * This is useful for displaying the value of a ListView's item
 * within the ListView's own associated menus. 
 * 
 * @author Andrzej Brynczka

 */
public class UserListCell<T> extends ListCell<T> {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	private ContextMenu m_menu;//context menu for the cell
	private ListView<T> m_ownerList;//the list that owns this cell

	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	public UserListCell(ContextMenu a_menu, ListView<T> a_listView){
		m_menu = a_menu;
		m_ownerList = a_listView;
	
	}
	
	private EventHandler<MouseEvent> getMouseEventHandler(){	

		EventHandler<MouseEvent> handler = new EventHandler<MouseEvent>(){
			public void handle( MouseEvent e ){
				//deal with basic mouse clicks
				
				Object source = e.getSource();

				if( e.getEventType() == MouseEvent.MOUSE_MOVED){
					//when mouse is moved over this cell:
					
					//have this cell get focus
					m_ownerList.getSelectionModel().select( getIndex() );
					
					//set the top most item for this user's context menu
					//hold this user's name(with bold font and underline)
					MenuItem menuItem = m_menu.getItems().get(0);
					menuItem.setText( getText() + ":" );
					menuItem.setStyle(
							"-fx-font-weight: bold; -fx-underline: true");
					
				}
			}
		};
		return handler;
	}
	
	@Override public void updateItem(T item, boolean empty){
		super.updateItem(item, empty);
		
		if( empty ){
			setText( null );
		}
		else {
			//ensure that only cells with information can access the
			//context menu and have their data handled
			
			setText( item.toString() );	
			setContextMenu( m_menu );	
			setOnMouseMoved( getMouseEventHandler() );
		}
	}
}

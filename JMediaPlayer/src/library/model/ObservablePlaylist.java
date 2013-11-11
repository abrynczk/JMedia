package library.model;

import java.util.Collection;
import java.util.ListIterator;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Container class for lists of media items, allowing for 
 * organized management of groups of items. Utilizes an
 * <code>ObservableList<code>, to hold items and automatically
 * track changes to list size and contents through bindable properties.
 * 
 * @author Andrzej Brynczka
 *
 */
public class ObservablePlaylist<T extends MediaItem> {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/** The name of the playlist */
	private String m_name;
	
	/** The list of MediaItems */
	private ObservableList<T> m_items;
	
	/** The size of the list, as an observable property */
	private SimpleIntegerProperty m_sizeProperty;
	
	// *********************************************************
	// ******************** Constructors ***********************
	// *********************************************************
	/**
	 * The basic constructor for the playlist. Accepts a name
	 * and initializes its list to zero elements.
	 * 
	 * @param a_name String, the name of the playlist
	 */
	public ObservablePlaylist(String a_name){
		m_name = a_name;
		m_items = FXCollections.observableArrayList();
		m_sizeProperty = new SimpleIntegerProperty();
		m_sizeProperty.setValue( 0 );
	}
	
	// *********************************************************
	// ******************** Selectors **************************
	// *********************************************************
	/**
	 * Get the size property of the ObservableList containing 
	 * the media items
	 * 
	 * @return the list's size as a SimpleIntegerProperty 
	 */
	public SimpleIntegerProperty sizeProperty(){
		return m_sizeProperty;
	}
		
	/**
	 * Get the list's name
	 * 
	 * @return String containing the list's name
	 */
	public String getName(){
		return m_name;
	}
	
	/**
	 * Get the ObservableList containing the media items
	 * 
	 * @return the list as an ObservableList
	 */
	public ObservableList<T> getItemsAsObservable(){
		return m_items;
	}
	
	/**
	 * Get the size of the playlist
	 * @return int, the size of the playlist
	 */
	public int size(){
		return m_items.size();
	}
	
	/**
	 * Get the item located at the given index
	 * 
	 * @param a_index int, the index of the desired item
	 * @return the desired object, or null if the index
	 * was out of bounds
	 */
	public T get(int a_index){
		if( a_index >= m_items.size() || a_index < 0 ){
			return null;
		}
		
		return m_items.get(a_index);
	}
	
	/**
	 * Get the item located at the front of the playlist
	 * @return the first item in the playlist, or null if the 
	 * list is empty
	 */
	public T getFirst(){
		if( m_items.size() == 0 ){
			return null;
		}
		
		return m_items.get(0);
	}
	
	/**
	 * Get the item located at the back of the playlist
	 * @return the item at the end of the playlist, or null if the 
	 * list is empty
	 */
	public T getLast(){
		int size = m_items.size();
		if( size == 0 ){
			return null;
		}
		
		if( size == 1 ){
			return m_items.get(0);
		}

		return m_items.get( size-1 );
	}
	
	/**
	 * Get the item located immediately after the given index
	 * 
	 * @param a_index int, the index to search against
	 * @return the item located in position a_index + 1, 
	 * 	or null if the provided index was out of bounds
	 */
	public T getItemFollowing(int a_index){
		//nothing to return if index surpasses the range
		if( a_index < 0 || a_index >= m_items.size()-1 ){
			return null;
		}		
		
		return m_items.get( a_index + 1 );
	}
	
	/**
	 * Get the item located immediately before the given index
	 * 
	 * @param a_index int, the index to search against
	 * @return the item located in position a_index - 1,
	 * or null if the provided index was out of bounds
	 */
	public T getItemPreceeding( int a_index ){
		
		//nothing to return if index surpasses the range
		if( a_index < 1 || a_index > m_items.size() ){
			return null;
		}		
		
		return m_items.get( a_index - 1 );
	}
	
	/**
	 * Get an iterator to the list of items
	 * @return the ListIterator for the list of items
	 */
	public ListIterator<T> getListIterator(){
		return m_items.listIterator();
	}
	
	/**
	 * Get the list of items as a collection
	 * @return the Collection of items held in the playlist
	 */
	public Collection<T> getItemsAsCollection(){
		return m_items;
	}
	
	// *********************************************************
	// ******************** Mutators ***************************
	// *********************************************************
	/**
	 * Set the name of the playlist
	 * @param a_name String, the name to set
	 */
	public void setName(String a_name){
		m_name = a_name;
	}
	
	/**
	 * Add the given item to the playlist
	 * 
	 * @param a_mediaItem T(extends MediaItem), the item to add
	 * @return true if the item was added, false if the item was null
	 */
	public boolean add(T a_mediaItem){
		if( a_mediaItem == null ){
			return false;
		}
		
		m_items.add( a_mediaItem );
		m_sizeProperty.setValue( m_items.size() );
		return true;
	}
	
	/**
	 * Add the given collection of items to the playlist
	 * 
	 * @param a_mediaCollection Collection, the items to add to the list
	 * @return true if the Collection was added, false if the collection
	 * 	was null
	 */
	public boolean addAll(Collection<T> a_mediaCollection){
		if( a_mediaCollection == null ){
			return false;
		}
		
		m_items.addAll( a_mediaCollection );
		m_sizeProperty.setValue( m_items.size() );
		return true;
	}
	
	/**
	 * Remove the item located at the given position
	 * 
	 * @param a_index int, the index to remove an item from
	 * @return true if the item at the index was removed, false
	 * 	if the given index was out of bounds
	 */
	public boolean remove(int a_index){
		if( a_index >= m_items.size() || a_index < 0 ){
			return false;
		}
		
		m_items.remove( a_index );
		m_sizeProperty.setValue( m_items.size() );
		return true;
	}
	
	/**
	 * Remove the given item from the playlist
	 * 
	 * @param a_mediaItem T(extends MediaItem), the item to remove
	 * @return true if the item was removed from the list, false if the
	 * 	the given item was null or the item was not on the list
	 */
	public boolean remove(T a_mediaItem ){
		if( a_mediaItem == null ){
			return false;
		}
		
		if( !m_items.remove( a_mediaItem ) ){
			return false;
		}
		
		m_sizeProperty.setValue( m_sizeProperty.getValue() - 1 );
		return true;
	}
	
	/**
	 * Remove all of the given items from the playlist
	 * 
	 * @param a_mediaList Collection&ltT&gt, the items to remove from
	 * 	the playlist
	 * @return true if even one of the items was removed from the list,
	 * 	false if none were removed or the Collection provided was null
	 */
	public boolean removeAll(Collection<T> a_mediaList){
		if( a_mediaList == null ){
			return false;
		}
		
		if( !m_items.removeAll( a_mediaList ) ){
			return false;
		}
		
		m_sizeProperty.setValue( m_items.size() );
		return true;
	}
	
	/**
	 * Clear the playlist of all of its items.
	 */
	public void clear(){
		m_items.clear();
		m_sizeProperty.setValue( 0 );
	}
	
	/**
	 * Set the given list of items into the playlist,
	 * clearing the current items completely.
	 * 
	 * @param a_playlist ObservablePlaylist, the list to set
	 * @return true if the list was set, false if the list was null
	 */
	public boolean set(ObservablePlaylist<T> a_playlist){
		return set( a_playlist.getItemsAsCollection() );
	}
	
	/**
	 * Set the given list of items into the playlist,
	 * clearing the current items completely.
	 * 
	 * @param a_mediaCollection Collection, the list to set
	 * @return true if the list was set, false if the list was null
	 */
	public boolean set(Collection<T> a_mediaCollection){
		if( a_mediaCollection == null ){
			return false;
		}
		
		m_items.setAll( a_mediaCollection );
		m_sizeProperty.setValue( m_items.size() );
		return true;
	}
	
	// *********************************************************
	// ******************** Utility Methods ********************
	// *********************************************************
	/**
	 * Determine if the playlist contains the given item
	 * 
	 * @param a_item T(extends MediaItem), the item to check
	 * @return true if the item is contained within the list,
	 * 	false if not or if the item was null
	 */
	public boolean contains(T a_item){
		if( a_item == null ){
			return false;
		}
		
		return m_items.contains( a_item );
	}
	
	/**
	 * Find the index of the given item within the list
	 * @param a_item T(extends MediaItem), the item to look for
	 * @return int, the position of the item within the list or 
	 * 	-1 if not found
	 */
	public int indexOf(T a_item){		
		return m_items.indexOf( a_item );
	}
	
	/**
	 * Move the given item up within the list(closer to the 0 index)
	 * 
	 * @param a_mediaItem T(extends MediaItem), the item to move up
	 * @return true if the item was moved up, false if the item
	 *  was not found on the list
	 */
	public boolean moveUpInList( T a_mediaItem ){
		int index = m_items.indexOf( a_mediaItem );
		if( index <= 0 ){
			return false;
		}
		
		//get the item above the selected
		T itemInFront = m_items.get( index - 1 );
		
		
		//switch the two in the list
		m_items.set( index - 1, a_mediaItem );
		m_items.set( index, itemInFront );
		
		return true;
	}

	/**
	 * Move the item at the given index up within 
	 * the list(closer to the 0 index)
	 * 
	 * @param a_index int, the index to move up
	 * @return true if the item was moved up, false if the item
	 *  was not found on the list
	 */
	public boolean moveUpInList( int a_index ){
		if( a_index >= m_items.size() || a_index < 1 ){
			return false;
		}
		
		//get the items to switch
		T itemToMoveUp = m_items.get( a_index );
		T itemInFront = m_items.get( a_index - 1 );
		
		
		//switch the items, moving the desired up by 1
		m_items.set(a_index - 1, itemToMoveUp);
		m_items.set(a_index, itemInFront);
		
		return true;
	}
	
	/**
	 * Move the given item down within the list(further from the 0 index)
	 * 
	 * @param a_mediaItem T(extends MediaItem), the item to move down
	 * @return true if the item was moved down, false if the item
	 *  was not found on the list
	 */
	public boolean moveDownInList( T a_mediaItem ){
		int index = m_items.indexOf( a_mediaItem );
		if( index == -1 || index == (m_items.size() - 1) ){
			return false;
		}
		
		//get the item below the selected
		T itemUnder = m_items.get( index + 1 );
		
		
		//switch the two in the list
		m_items.set( index + 1, a_mediaItem );
		m_items.set( index, itemUnder );
				
		return true;
	}
	
	/**
	 * Move the item at the given index down within 
	 * the list(further from the 0 index)
	 * 
	 * @param a_index int, index to move down
	 * @return true if the item was moved down, false if the item
	 *  was not found on the list
	 */
	public boolean moveDownInList( int a_index ){
		if( a_index >= (m_items.size() - 1) || a_index < 0 ){
			return false;
		}
		
		//get the items to switch
		T itemToMoveDown = m_items.get( a_index );
		T itemUnder = m_items.get( a_index + 1 );
		
		//switch the items, moving the desired down by 1
		m_items.set( a_index + 1, itemToMoveDown );
		m_items.set( a_index, itemUnder);
		
		return true;
	}
}

package client.view.component;


/**
 * This class serves as an extension to the basic TableCell
 * used within a TableView, incorporating a progress bar within.
 * 
 * It is bound to an item with a DoubleProperty through
 * a TableView's cellFactory, and presents a progress bar
 * in place of its associated item.
 */
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;


public class ProgressBarTableCell<S, T> extends TableCell<S, T> {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
    private final ProgressBar progressBar;
 	
 	// *********************************************************
 	// ******************** Constructor ************************
 	// *********************************************************
    public ProgressBarTableCell() {
        this.progressBar = new ProgressBar();
        progressBar.setPrefHeight(15);
        
        setAlignment(Pos.CENTER);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
		if( item == null){            
			setGraphic(null);
			setText(null);
		}
		else{

			//bind an item's double property to the progress bar,
			//allowing the bar to get updated upon the item's
			//change of property
			
			//EX: When used with FileTransferTicket in FileManagerView, 
			//progressBar binds to FileTransferTicket.getProgress() which 
			//returns the progress DoubleProperty
			progressBar.progressProperty().bind( ((DoubleProperty)item) );		
			setGraphic( progressBar );
			
		}
    }
}

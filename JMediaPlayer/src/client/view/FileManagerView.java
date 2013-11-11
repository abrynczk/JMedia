package client.view;

import client.Client;
import client.FileTransferTicket;
import client.messages.FileTransferMessage.TransferStage;
import client.messages.Message.MessageResponse;
import client.view.component.ProgressBarTableCell;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * The view class that visually displays the transfer of files
 * between the client and other users.
 * @author Andrzej Brynczka
 *
 */
public class FileManagerView extends Stage {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************
	private final String MENU_CLEAR_TICKET = "Clear this file";
	private final String MENU_CANCEL_TICKET = "Cancel this transfer";
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	/**
	 * Reference to the main client object
	 */
	private Client m_client;
	
	/**
	 * Reference to the main clientView 
	 */
	private ClientView m_clientView;
		
	/**
	 * List of FileTransferTickets that contain data on a file being
	 *  sent to another use
	 */
	private TableView<FileTransferTicket> m_sentTable;
	
	/**
	 * List of FileTransferTickets that contain data on a file being
	 *  sent to this client
	 */
	private TableView<FileTransferTicket> m_receivedTable;
	
	/**
	 * Contextmenu for the table that displays outgoing files
	 */
	private ContextMenu m_sentTableContextMenu;
	
	/**
	 * ContextMenu for the table that displays incoming files
	 */
	private ContextMenu m_receivedTableContextMenu;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * Construct the FileManagerView Stage, containing an empty
	 *  list of outgoing and incoming file transfers
	 *  
	 * @param a_client Client, reference to the main client object
	 * @param a_clientView ClientView, reference to the main
	 *  clientview
	 */
	public FileManagerView(Client a_client, ClientView a_clientView){
		m_client = a_client;
		m_clientView = a_clientView;
		
		createSentContextMenu();
		createReceivedContextMenu();
		createFileManager();
		
		initOwner( m_clientView.getDriver() );
		setTitle("File Manager");
	}

	/**
	 * Initialize the context menu for the outgoing files display table
	 */
	private void createSentContextMenu(){
		m_sentTableContextMenu = new ContextMenu();
		
		MenuItem optionClearFileInfo = 
				new MenuItem("Clear this file information");
		optionClearFileInfo.setId( MENU_CLEAR_TICKET );
		optionClearFileInfo.setOnAction( getSentContextMenuHandler() );
		
		MenuItem optionCancelTransfer = 
				new MenuItem("Cancel this transfer");
		optionCancelTransfer.setId( MENU_CANCEL_TICKET );
		optionCancelTransfer.setOnAction( getSentContextMenuHandler() );
		
		m_sentTableContextMenu.getItems().addAll(
				optionClearFileInfo, optionCancelTransfer );
	}
	
	/**
	 * Initialize the context menu for the incoming files display table
	 */
	private void createReceivedContextMenu(){
		m_receivedTableContextMenu = new ContextMenu();
		
		MenuItem optionClearFileInfo = 
				new MenuItem("Clear this file information");
		optionClearFileInfo.setId( MENU_CLEAR_TICKET );
		optionClearFileInfo.setOnAction( getReceivedContextMenuHandler() );
		
		MenuItem optionCancelTransfer = 
				new MenuItem("Cancel this transfer");
		optionCancelTransfer.setId( MENU_CANCEL_TICKET );
		optionCancelTransfer.setOnAction( getReceivedContextMenuHandler() );
		
		m_receivedTableContextMenu.getItems().addAll(
				optionClearFileInfo, optionCancelTransfer );
	}
	
	/**
	 * Initialize the sent and receive file transfer display tables
	 * and place them into this stage's scene to be displayed
	 */
	private void createFileManager(){		
		
		//***************************************************
		//create the sent-ticket table and tab
		//***************************************************	
		TableColumn fileNameCol = new TableColumn();
		fileNameCol.setText("File Name");
		fileNameCol.setMinWidth(100);
		fileNameCol.setCellValueFactory(
        		new PropertyValueFactory("FileName") );
		
		TableColumn receiverNameCol = new TableColumn();
		receiverNameCol.setText("Receiver");
		receiverNameCol.setMinWidth(60);
		receiverNameCol.setCellValueFactory( 
				new PropertyValueFactory("ReceiverName") );
		
		TableColumn progressCol = new TableColumn();
		progressCol.setText("Progress");
		progressCol.setMinWidth(80);
		progressCol.setCellValueFactory( 
				new PropertyValueFactory("Progress") );
		progressCol.setCellFactory( 
				new Callback< TableColumn<FileTransferTicket, Double>, 
					TableCell<FileTransferTicket, Double>>(){
			
			@Override
			public TableCell call( final TableColumn param ){
				final ProgressBarTableCell cell = new ProgressBarTableCell();
				return cell;
			}
		} );
		
		TableColumn statusCol = new TableColumn();
		statusCol.setText("Status");
		statusCol.setMinWidth(80);
		statusCol.setCellValueFactory( 
				new PropertyValueFactory("Status" ) );
		
		TableColumn filePathCol = new TableColumn();
		filePathCol.setText("File Location");
		filePathCol.setMinWidth(150);
		filePathCol.setCellValueFactory( 
				new PropertyValueFactory("FilePath") );

		
		m_sentTable = new TableView();
		m_sentTable.setItems( m_client.getSentTicketsList() );
		m_sentTable.getColumns().addAll( progressCol, statusCol, 
				receiverNameCol, fileNameCol, filePathCol);
		m_sentTable.getSelectionModel().setSelectionMode( 
        		SelectionMode.MULTIPLE );
		m_sentTable.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY);
		m_sentTable.setContextMenu( m_sentTableContextMenu );
		
		Tab sentFileTab = new Tab("Sent");
		sentFileTab.setContent( m_sentTable );
		sentFileTab.setClosable( false );
		
		//***************************************************
		//create the received table and tab
		//***************************************************		
		TableColumn fileNameCol2 = new TableColumn();
		fileNameCol2.setText("File Name");
		fileNameCol2.setMinWidth(100);
		fileNameCol2.setCellValueFactory(
        		new PropertyValueFactory("FileName") );
		
		TableColumn senderNameCol = new TableColumn();
		senderNameCol.setText("Sender");
		senderNameCol.setMinWidth(60);
		senderNameCol.setCellValueFactory( 
				new PropertyValueFactory("SenderName") );
		
		TableColumn progressCol2 = new TableColumn();
		progressCol2.setText("Progress");
		progressCol2.setMinWidth(80);
		progressCol2.setCellValueFactory( 
				new PropertyValueFactory("Progress") );
		progressCol2.setCellFactory( 
				new Callback< TableColumn<FileTransferTicket, Double>, 
					TableCell<FileTransferTicket, Double>>(){
			
			@Override
			public TableCell call( final TableColumn param ){
				final ProgressBarTableCell cell = new ProgressBarTableCell();
				return cell;
			}
		} );
		
		TableColumn statusCol2 = new TableColumn();
		statusCol2.setText("Status");
		statusCol2.setMinWidth(80);
		statusCol2.setCellValueFactory( 
				new PropertyValueFactory("Status" ) );
		
		TableColumn filePathCol2 = new TableColumn();
		filePathCol2.setText("File Location");
		filePathCol2.setMinWidth(150);
		filePathCol2.setCellValueFactory( 
				new PropertyValueFactory("FilePath") );
		
		
		m_receivedTable = new TableView();
		m_receivedTable.setItems( m_client.getReceivedTicketsList() );
		m_receivedTable.getColumns().addAll( progressCol2, statusCol2, 
				senderNameCol, fileNameCol2, filePathCol2);
		m_receivedTable.getSelectionModel().setSelectionMode( 
        		SelectionMode.MULTIPLE );
		m_receivedTable.setColumnResizePolicy( 
				TableView.CONSTRAINED_RESIZE_POLICY);
		m_receivedTable.setContextMenu( m_receivedTableContextMenu );
	
		Tab receivedFileTab = new Tab("Received");
		receivedFileTab.setContent( m_receivedTable );
		receivedFileTab.setClosable( false );
		
		//create the dual chat pane, for regular chat and private chat
		TabPane fileManagerTabPane = new TabPane();
		fileManagerTabPane.setSide( Side.TOP );
		fileManagerTabPane.setTabClosingPolicy( TabClosingPolicy.UNAVAILABLE );
		fileManagerTabPane.getTabs().addAll(sentFileTab, receivedFileTab);
		fileManagerTabPane.setMinSize(300, 200);
		fileManagerTabPane.setPrefSize(650, 300);

		this.setScene( new Scene( fileManagerTabPane ) );	
	}
	
	/**
	 * Get an ActionEvent EventHandler for the context menu
	 * used in the outgoing file display table, allowing for the 
	 * cancellation and clearing of transfers.
	 * 
	 * @return the EventHandler for the menu
	 */
	private EventHandler<ActionEvent> getSentContextMenuHandler(){

		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
			public void handle( ActionEvent e ){
				Object eventSource = e.getSource();
				

				if( eventSource instanceof MenuItem ){
					MenuItem item = (MenuItem) eventSource;
					String itemName = item.getId();
					
					FileTransferTicket ticket = 
							m_sentTable.getSelectionModel().getSelectedItem();
					if( ticket == null ){
						return;
					}
					
					switch( itemName ){
					case MENU_CLEAR_TICKET:
						if( ticket.getTransferStage() == 
							TransferStage.STAGE5_Done ||
						ticket.getTransferStage() == TransferStage.ERROR ){
							
							m_client.deleteFileTicketFromTransferTable( 
									ticket.getTransferID() );
							m_client.removeFromSentTicketsList( ticket );
						}
						break;
					case MENU_CANCEL_TICKET:
						TransferStage stage = ticket.getTransferStage();	
						if( stage == TransferStage.STAGE4_TransEndResponse 
								|| stage == TransferStage.STAGE5_Done
								|| stage == TransferStage.ERROR ){
							return;
						}
						
						ticket.setTransferStage(
								TransferStage.STAGE4_TransEndResponse);
						
						m_client.killFileDataSend( ticket.getTransferID() );
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
	 * Get an ActionEvent EventHandler for the context menu used in the
	 *  table that display incoming files
	 *  
	 * @return the ActionEvent based EventHandler
	 */
	private EventHandler<ActionEvent> getReceivedContextMenuHandler(){

		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
			public void handle( ActionEvent e ){
				Object eventSource = e.getSource();
				

				if( eventSource instanceof MenuItem ){
					MenuItem item = (MenuItem) eventSource;
					String itemName = item.getId();
					
					FileTransferTicket ticket = m_receivedTable
							.getSelectionModel().getSelectedItem();
					
					switch( itemName ){
					case MENU_CLEAR_TICKET:
						System.out.println( ticket.getTransferID() );
						if( ( ticket.getTransferStage() == 
								TransferStage.STAGE5_Done ) ||
								( ticket.getTransferStage() 
								== TransferStage.ERROR ) ){
							m_client.deleteFileTicketFromTransferTable( 
									ticket.getTransferID() );
							m_client.removeFromReceivedTicketsList( ticket );
						}

						break;
					case MENU_CANCEL_TICKET:
						TransferStage stage = ticket.getTransferStage();
						if( stage == TransferStage.STAGE4_TransEndResponse 
								|| stage == TransferStage.STAGE5_Done 
								|| stage == TransferStage.ERROR  ){
							return;
						}
							
						ticket.setTransferStage(
								TransferStage.STAGE4_TransEndResponse);
						
						//send a warning to the other user
						m_client.sendFileResponse(ticket, 
									MessageResponse.Failure);
						
					default:
						break;
					}
				}
			}
		};
	
		return handler;
	}
}

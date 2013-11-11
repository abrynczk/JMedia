package client.view;

import java.io.File;
import java.text.DecimalFormat;



import client.Client;
import client.FileTransferTicket;
import client.messages.ChatMessage;
import client.messages.Message;
import client.messages.PrivateChatMessage;
import client.messages.PunishmentInfo.Punishment;
import client.messages.Message.MessageHeader;
import client.messages.Message.MessageResponse;
import client.view.component.MessageListCell;
import client.view.component.UserListCell;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 * The view containing the main chat components presented to the user
 * during use of the chat program.
 * 
 * @author Andrzej Brynczka
 *
 */
public class MainChatPane extends BorderPane {
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************		
	private final String CONTEXT_ITEM_SEND_PM = "Send Private Message";
	private final String CONTEXT_ITEM_SEND_FILE = "Send File Request";
	private final String CONTEXT_ITEM_IGNORE = "Ignore user";
	private final String CONTEXT_ITEM_REMOVE_IGNORE = 
			"Remove user from ignore list";
	private final String CONTEXT_ITEM_MUTE = "Mute";
	private final String CONTEXT_ITEM_KICK = "Kick";
	private final String CONTEXT_ITEM_BAN = "Ban";
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	private Client m_client;
	private ClientView m_clientView;
	
	//initial directory to present to use user when choosing a file
	//to send or when choosing a directory to save a receiving file
	private File m_initialDirectory;
	
	private ContextMenu m_userListContextMenu;
	private ContextMenu m_ignoreListContextMenu;
	
	private TextField m_inputField;
	
	private ListView<String> m_userListView;
	private ListView<String> m_ignoreListView;

	private ListView<Message> m_chatMessageView;
	private ListView<PrivateChatMessage> m_privateMessageView;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	MainChatPane(Client a_client, ClientView a_clientView){
		m_client = a_client;
		m_clientView = a_clientView;
		
		m_initialDirectory = null;
		
		TabPane chatDisplay = createChatDisplay();
		TabPane userList = createUserList();
		createChatInput();
		
		//************************************************************
		//Finalize the bottom input display
		//************************************************************
		//create a username label to appear near the input box
		Label userNameLabel = new Label( m_client.getUserName()+":" );
		userNameLabel.setMinSize( 30 , m_inputField.getMinHeight() );
		
		//create a gridpane to combine the username label and input box
		GridPane inputDisplay = new GridPane();
		GridPane.setHgrow( userNameLabel, Priority.ALWAYS );
		GridPane.setHgrow( m_inputField, Priority.ALWAYS );
		
		//set some column constraints for the label and box
		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setMaxWidth(7 * m_client.getUserName().length() + 1 );
		labelCol.setMinWidth(30);
		ColumnConstraints inputCol = new ColumnConstraints();
		inputCol.setFillWidth( true );
		
		//add the label and box
		inputDisplay.getColumnConstraints().addAll(labelCol, inputCol);
		inputDisplay.add( userNameLabel, 0, 0);
		inputDisplay.add( m_inputField, 1, 0);
		
		//************************************************************
		//Finalize the center chat and user display
		//************************************************************
		GridPane centerDisplay = new GridPane();
		GridPane.setHgrow( chatDisplay, Priority.ALWAYS);
		GridPane.setHgrow( userList, Priority.ALWAYS );
		
		//modify the display row and column constraints to ensure that
		//the components resize and scale appropriately
		ColumnConstraints chatDisplayCol = new ColumnConstraints();
		chatDisplayCol.setPercentWidth(70);
		
		ColumnConstraints userListCol = new ColumnConstraints();
		userListCol.setPercentWidth(30);
		
		RowConstraints rowCon = new RowConstraints();
		rowCon.setPercentHeight(100);
		centerDisplay.getRowConstraints().add(rowCon);
		centerDisplay.getColumnConstraints().addAll(
				chatDisplayCol, userListCol);
		
		centerDisplay.add(chatDisplay, 0, 0);
		centerDisplay.add(userList, 1, 0);
		

		//************************************************************
		//Combine the display grids
		//************************************************************
		//this.setTop( m_systemMenuBar ); leave room for the menu bar
		setCenter( centerDisplay );
		setBottom( inputDisplay );
		BorderPane.setMargin( inputDisplay, new Insets(8,0,0,0));
		setMinSize(400, 500);
		setMaxSize(1000, 700);
		setPrefSize(500, 500);
		setOnKeyPressed( rootInputHandler() );
		
	}

	public void enableAdminOptionsInContextMenu(boolean a_enable){
		if( a_enable ){
			//set the MUTE/KICK/BAN context menu items on 
			m_userListContextMenu.getItems().get(5).setVisible( true );
			m_userListContextMenu.getItems().get(6).setVisible( true );
			m_userListContextMenu.getItems().get(7).setVisible( true );
			
			m_ignoreListContextMenu.getItems().get(3).setVisible( true );
			m_ignoreListContextMenu.getItems().get(4).setVisible( true );
			m_ignoreListContextMenu.getItems().get(5).setVisible( true );
		}
		else{
			m_userListContextMenu.getItems().get(5).setVisible( false );
			m_userListContextMenu.getItems().get(6).setVisible( false );
			m_userListContextMenu.getItems().get(7).setVisible( false );
			
			m_ignoreListContextMenu.getItems().get(3).setVisible( false );
			m_ignoreListContextMenu.getItems().get(4).setVisible( false );
			m_ignoreListContextMenu.getItems().get(5).setVisible( false );
		}
	}

	public void promptFileRequestResponse(final FileTransferTicket a_ticket){
		
		DecimalFormat decimalFormat = new DecimalFormat("#.###");
		String fileSize = decimalFormat.format( 
								a_ticket.getFileSize() / (1024.0 * 1024.0) ); 

		String mastHead = "User \"" + a_ticket.getSenderName()
				+ "\" wants to send you " + a_ticket.getFileName() 
				+ "(" + fileSize + "mb) \n"
				+ "Do you wish to download the file?";
		
		DialogResponse response = Dialogs.showConfirmDialog( 
				m_clientView.getDriver(), mastHead, "Download the file?", 
				"Transfer Request Prompt", DialogOptions.YES_NO );
		
		if( response == DialogResponse.NO ){
			m_client.sendFileResponse(a_ticket, MessageResponse.Failure);
			return;
		}
		
		System.out.println(a_ticket.getReceiverName() + " accepted "
				+ a_ticket.getFileName() + " request from "
				+ a_ticket.getSenderName() );
		System.out.println("directory: " + a_ticket.getFilePath());

		promptFileSavePath( a_ticket );		
	}
	
	private void promptFileSavePath(FileTransferTicket a_ticket){
		//prompt the user for a directory in which to save the
		//file that will be sent
		DirectoryChooser directorySelector = new DirectoryChooser();
		directorySelector.setTitle("Choose where to save " 
				+ a_ticket.getFileName());
		directorySelector.setInitialDirectory( m_initialDirectory );
		final File desiredDirectory = 
				directorySelector.showDialog( m_clientView.getDriver() );

		m_initialDirectory = desiredDirectory;
		
		//check that a directory was provided 
		if( desiredDirectory == null ){
			//canceled request at the location selection
			m_client.sendFileResponse(a_ticket, MessageResponse.Failure);
			return;
		}
		
		System.out.println("Saving in:" + desiredDirectory.getAbsolutePath());
		//file request was accepted and a valid path provided...
		
		//update the path and send the successful response
		a_ticket.setFilePath( desiredDirectory.getAbsolutePath() );
		m_client.sendFileResponse( a_ticket, MessageResponse.Success );
	}
	private TabPane createChatDisplay(){
		
		m_chatMessageView = new ListView<Message>();
		m_chatMessageView.setItems( m_client.getChatMessageList() );
		m_chatMessageView.getSelectionModel()
			.setSelectionMode( SelectionMode.SINGLE );
		m_chatMessageView.setEditable( false );
		m_chatMessageView.setMinSize(100, 100);
		m_chatMessageView.setPrefSize(250, 300);
		
		
		//modify the list's cells, making them instances of MessageCell instead
		final Callback<ListView<Message>, ListCell<Message>> messageCellFactory 
										= m_chatMessageView.getCellFactory();
		
		m_chatMessageView.setCellFactory( 
				new Callback < ListView<Message>, ListCell<Message> >(){
				@Override 
				public ListCell<Message> call( ListView<Message> a_listView){
					ListCell<Message> cell = 
							(ListCell<Message>) messageCellFactory;
					
					if( cell == null ){
						cell = new MessageListCell<Message>( m_clientView );
					}
					else{
						cell = messageCellFactory.call(a_listView);
					}
					return cell;
					
				}
			});

		//create the private chat message list
		m_privateMessageView = new ListView<PrivateChatMessage>();
		m_privateMessageView.setItems( m_client.getPrivateChatMessageList() );
		m_privateMessageView.getSelectionModel()
			.setSelectionMode( SelectionMode.SINGLE );
		m_privateMessageView.setEditable( false );
		m_privateMessageView.setMinSize(100, 100);
		m_privateMessageView.setPrefSize(250, 300);
		
		//modify the list's cells, making them instances of MessageCell instead
		final Callback<ListView
					<PrivateChatMessage>, ListCell<PrivateChatMessage>> 
						pmCellFactory = m_privateMessageView.getCellFactory();
		m_privateMessageView.setCellFactory( 
				new Callback 
				<ListView<PrivateChatMessage>, ListCell<PrivateChatMessage>>(){
					
			@Override 
			public ListCell<PrivateChatMessage> call( 
									ListView<PrivateChatMessage> a_listView){
				
				ListCell<PrivateChatMessage> cell = 
						(ListCell<PrivateChatMessage>) pmCellFactory;
				if( cell == null ){
					cell = new MessageListCell<PrivateChatMessage>( m_clientView );
				}
				else{
					cell = pmCellFactory.call(a_listView);
				}
				return cell;
				
			}
		});
		
		//create chat tabs for each chat message type
		Tab regularChatTab = new Tab("Chat");
		regularChatTab.setContent( m_chatMessageView );
		regularChatTab.setClosable( false );

		Tab privateChatTab = new Tab("Private Messages");
		privateChatTab.setContent( m_privateMessageView );
		privateChatTab.setClosable( false );
		
		//create the dual chat pane, for regular chat and private chat
		TabPane chatDisplayPane = new TabPane();
		chatDisplayPane.setSide( Side.TOP );
		chatDisplayPane.setTabClosingPolicy( TabClosingPolicy.UNAVAILABLE );
		chatDisplayPane.getTabs().addAll(regularChatTab, privateChatTab);
		chatDisplayPane.setMinSize(100, 100);
		chatDisplayPane.setPrefSize(250, 300);
		
		return chatDisplayPane;
	}

	private TabPane createUserList(){	
		//create a context menu and its menu options
		MenuItem targetedUser = new MenuItem("");
		targetedUser.setId("Targeted User");
		
		MenuItem userOptionSendPM = new MenuItem("Send Message");
		userOptionSendPM.setId(CONTEXT_ITEM_SEND_PM);
		userOptionSendPM.setOnAction( getContextMenuHandler() );
		
		MenuItem userOptionSendFile = new MenuItem("Send File");
		userOptionSendFile.setId(CONTEXT_ITEM_SEND_FILE);
		userOptionSendFile.setOnAction( getContextMenuHandler() );
		
		MenuItem userOptionIgnore = new MenuItem("Ignore");
		userOptionIgnore.setId(CONTEXT_ITEM_IGNORE);
		userOptionIgnore.setOnAction( getContextMenuHandler() );
		
		MenuItem userOptionMute = new MenuItem("Mute");
		userOptionMute.setId(CONTEXT_ITEM_MUTE);
		userOptionMute.setOnAction( getContextMenuHandler() );
		userOptionMute.setVisible(false);
		
		MenuItem userOptionKick = new MenuItem("Kick");
		userOptionKick.setId(CONTEXT_ITEM_KICK);
		userOptionKick.setOnAction( getContextMenuHandler() );
		userOptionKick.setVisible(false);
		
		MenuItem userOptionBan = new MenuItem("Ban");
		userOptionBan.setId(CONTEXT_ITEM_BAN);
		userOptionBan.setOnAction( getContextMenuHandler() );
		userOptionBan.setVisible(false);
		
		//add the menu options to the menu 
		m_userListContextMenu = new ContextMenu();
		m_userListContextMenu.getItems().addAll(targetedUser, 
				userOptionSendPM, userOptionSendFile,
				userOptionIgnore, new SeparatorMenuItem(), 
				userOptionMute, userOptionKick, userOptionBan);	
		
		MenuItem ignoreOptionRemoveFromIgnore = 
				new MenuItem("Remove from ignore list");
		ignoreOptionRemoveFromIgnore.setId(CONTEXT_ITEM_REMOVE_IGNORE);
		ignoreOptionRemoveFromIgnore.setOnAction( getContextMenuHandler() );
		
		MenuItem ignoreOptionMute = new MenuItem("Mute");
		ignoreOptionMute.setId(CONTEXT_ITEM_MUTE);
		ignoreOptionMute.setOnAction( getContextMenuHandler() );
		ignoreOptionMute.setVisible(false);
		
		MenuItem ignoreOptionKick = new MenuItem("Kick");
		ignoreOptionKick.setId(CONTEXT_ITEM_KICK);
		ignoreOptionKick.setOnAction( getContextMenuHandler() );
		ignoreOptionKick.setVisible(false);
		
		MenuItem ignoreOptionBan = new MenuItem("Ban");
		ignoreOptionBan.setId(CONTEXT_ITEM_BAN);
		ignoreOptionBan.setOnAction( getContextMenuHandler() );
		ignoreOptionBan.setVisible(false);
		
		m_ignoreListContextMenu = new ContextMenu();
		m_ignoreListContextMenu.getItems().addAll(targetedUser, 
				ignoreOptionRemoveFromIgnore, 
				new SeparatorMenuItem(), ignoreOptionMute, 
				ignoreOptionKick, ignoreOptionBan);
		
		//create the view for the list of users
		m_userListView = new ListView<String>();
		m_userListView.setItems( m_client.getUserList() );
		m_userListView.getSelectionModel().setSelectionMode( 
														SelectionMode.SINGLE);
		m_userListView.setMinSize(100, 100);
		m_userListView.setPrefSize(250, 300);

		
		//modify the list's cells, making them instances of UserListCell instead
		final Callback<ListView<String>, ListCell<String>> cellFactory = 
												m_userListView.getCellFactory();
		m_userListView.setCellFactory( 
				new Callback <ListView<String>, ListCell<String> >(){
					
			@Override 
			public ListCell<String> call( ListView<String> a_listView){
				ListCell<String> cell = (ListCell<String>) cellFactory;
				if( cell == null ){
					cell = new UserListCell<String>( 
									m_userListContextMenu, m_userListView );
				}
				else{
					cell = cellFactory.call(a_listView);
				}
				return cell;
				
			}
		});
		
		m_ignoreListView = new ListView<String>();
		m_ignoreListView.setItems( m_client.getIgnoreList() );
		m_ignoreListView.getSelectionModel().setSelectionMode( 
														SelectionMode.SINGLE );
		m_ignoreListView.setMinSize(100, 100);
		m_ignoreListView.setPrefSize(250, 300);
	
		final Callback<ListView<String>, ListCell<String>> ignoreListCellFactory 
											= m_ignoreListView.getCellFactory();
		m_ignoreListView.setCellFactory( 
				new Callback <ListView<String>, ListCell<String> >(){
			@Override 
			public ListCell<String> call( ListView<String> a_listView){
				
				ListCell<String> cell = 
						(ListCell<String>) ignoreListCellFactory;
				if( cell == null ){
					cell = new UserListCell<String>( 
								m_ignoreListContextMenu, m_ignoreListView );
				}
				else{
					cell = cellFactory.call(a_listView);
				}
				return cell;
				
			}
		});
		

		//create the userlist and ignore list panes
		Tab userListTab = new Tab("User List");
		userListTab.setContent( m_userListView );
		userListTab.setClosable( false );
		
		Tab ignoreListTab = new Tab("Ignore List");
		ignoreListTab.setContent( m_ignoreListView );
		ignoreListTab.setClosable( false );
		
		//create the dual chat pane, for lists
		TabPane userListPane = new TabPane();
		userListPane.setSide( Side.TOP );
		userListPane.setTabClosingPolicy( TabClosingPolicy.UNAVAILABLE );
		userListPane.getTabs().addAll( userListTab, ignoreListTab );
		userListPane.setMinSize(100, 100);
		userListPane.setPrefSize(250, 300);

		return userListPane;
		
	}
	
	private TextField createChatInput(){
		m_inputField = new TextField();
		m_inputField.setPromptText("Enter text here");
		m_inputField.setEditable( true );
		m_inputField.prefWidth(200);
		m_inputField.prefHeight(30);
		m_inputField.setMinSize(200, 30);
		m_inputField.setMaxHeight(200);
		m_inputField.setFocusTraversable( false );
		m_inputField.requestFocus();
		
		m_inputField.setOnAction( new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent e){
				//send the user's input to the server
				TextField currentInputField = (TextField) e.getSource();		
				m_client.sendChatMessageToServer( currentInputField.getText() );
				
				currentInputField.setText("");
			}

		});
		
		//set a handler for cycling of recent private messages
		m_inputField.setOnKeyPressed( new EventHandler<KeyEvent>(){
			KeyCombination altR = KeyCombination.keyCombination("ALT+R");
			@Override
			public void handle(KeyEvent event) {
				if( altR.match(event) ){
					
					if( m_client.getMostRecentPMSender() != null ){
						
						//set the current input field to allow for a quick
						//private message to the user that most recently
						//sent this client a message
						String recentPm = "@" + m_client.getMostRecentPMSender() 
								+ " ";
						
						m_inputField.setText( recentPm );
						m_inputField.positionCaret( recentPm.length() );
					}
				}		
			}	
		});
		

		return m_inputField;
	}

	private void promptFileRequestFile( final String a_userToSendTo ){
		//prompt the user for a file to send
		FileChooser fileSelector = new FileChooser();
		fileSelector.setTitle("Choose a file to send to " + a_userToSendTo);
		fileSelector.setInitialDirectory( m_initialDirectory );
		final File desiredFile = 
				fileSelector.showOpenDialog(m_clientView.getDriver());
		
		if( desiredFile == null ){
			return;
		}
		
		//save the file's directory, allowing the user to start from 
		//that point on the next prompt
		m_initialDirectory = desiredFile.getParentFile();
		
		//create a dialog pop-up to confirm the user's file transfer
		String mastHead = "Send \"" + desiredFile.getName() + "\"("
				+ new DecimalFormat("#.###").format( 
						desiredFile.length() / (1024.0 * 1024.0) ) 
				+ "mb) to user \""
				+ a_userToSendTo + "\"?";
		
		DialogResponse response = Dialogs.showConfirmDialog( 
				m_clientView.getDriver(), mastHead, "Send the file?",
				"Transfer Request Prompt", DialogOptions.YES_NO );
		
		
		if( response == DialogResponse.NO ){
			System.out.println("File transfer declined");
			return;
		}
		
		//send the transfer file request to the desired user
		String fileName = desiredFile.getName();
		String filePath = desiredFile.getAbsolutePath();
		long fileSize = desiredFile.length();
		
		System.out.println("Sending request for " 
				+ desiredFile.getName() +" to " + a_userToSendTo);

		m_client.requestFileTransfer(filePath, fileName, 
				fileSize, a_userToSendTo);		
		
	}

	//a handler ensure that all text entered on the main screen
	//is entered into the chat bar
	private EventHandler<KeyEvent> rootInputHandler(){
        
		EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>(){
			@Override
	        public void handle(final KeyEvent ke) {
				m_inputField.requestFocus();
	        }
		};
		
		return handler;
	}
	
	
	private EventHandler<ActionEvent> getContextMenuHandler(){

		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
			public void handle( ActionEvent e ){
				Object eventSource = e.getSource();
				

				if( eventSource instanceof MenuItem ){
					MenuItem item = (MenuItem) eventSource;
					String itemName = item.getId();
					ContextMenu itemConMenu = item.getParentPopup();
					
					//handle the option
					switch( itemName ){
					case CONTEXT_ITEM_SEND_PM:
						//send a private message to the selected user
						m_inputField.requestFocus();
						m_inputField.setText("@"
								+ m_userListView.getSelectionModel()
												.getSelectedItem()
								+ " ");
						m_inputField.positionCaret( 
								m_inputField.getText().length() );

						break;
					case CONTEXT_ITEM_SEND_FILE:
						if( m_client.getNumOfCurrentFileSends() >= 
										m_client.MAX_CONCURRENT_FILE_SENDS ){

							m_client.addChatMessageToList( new ChatMessage(
									MessageHeader.REG_SendChatMess,
									"You can only send a total of " +
									m_client.MAX_CONCURRENT_FILE_SENDS +
									" files at a time. Please wait until a " +
									" file finishes before attempting to " +
									" send another.", "Server" ) 
							);
							
							break;
						}
						
						String selectedUser = m_userListView.
								getSelectionModel().getSelectedItem();
						promptFileRequestFile(selectedUser);
						break;
					case CONTEXT_ITEM_IGNORE:
						m_client.addToIgnoreList( 
								m_userListView.getSelectionModel()
														.getSelectedItem() );
						
						System.out.println("Ignoring user: " 
								+ m_userListView.getSelectionModel()
														.getSelectedItem() );
						break;
						
					case CONTEXT_ITEM_REMOVE_IGNORE:
						m_client.removeFromIgnoreList( 
								m_ignoreListView.getSelectionModel()
														.getSelectedItem() );
						
						System.out.println("Removed ignore from user " 
								+  m_ignoreListView.getSelectionModel()
														.getSelectedItem() );
						break;
					case CONTEXT_ITEM_MUTE:
						if( itemConMenu == m_userListContextMenu ){
							System.out.println("muting from userlist");
							m_client.sendAdminPunishMessage( 
									m_userListView.getSelectionModel()
										.getSelectedItem(), Punishment.MUTE);
						}
						else if( itemConMenu == m_ignoreListContextMenu ){
							System.out.println("muting from ignore list");
							m_client.sendAdminPunishMessage( 
									m_ignoreListView.getSelectionModel()
									.getSelectedItem(), Punishment.MUTE);
						}
						break;
					case CONTEXT_ITEM_KICK:
						if( itemConMenu == m_userListContextMenu ){
							m_client.sendAdminPunishMessage(
									m_userListView.getSelectionModel()
										.getSelectedItem(), Punishment.KICK);
						}
						else if( itemConMenu == m_ignoreListContextMenu ){
							m_client.sendAdminPunishMessage(
									m_ignoreListView.getSelectionModel()
										.getSelectedItem(), Punishment.KICK);
						}
						break;
					case CONTEXT_ITEM_BAN:
						if( itemConMenu == m_userListContextMenu ){
							m_client.sendAdminPunishMessage(
									m_userListView.getSelectionModel()
										.getSelectedItem(), Punishment.BAN);
						}
						else if( itemConMenu == m_ignoreListContextMenu ){
							m_client.sendAdminPunishMessage(
									m_ignoreListView.getSelectionModel()
										.getSelectedItem(), Punishment.BAN);
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
}

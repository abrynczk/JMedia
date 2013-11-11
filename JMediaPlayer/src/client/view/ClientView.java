package client.view;



import client.Client;
import client.ClientDriver;
import client.FileTransferTicket;

import client.messages.PunishmentInfo;
import client.messages.Message.MessageResponse;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.CheckMenuItemBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuBuilder;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * The view class for the client, used to display the messages 
 * that move between the server and the client.
 * 
 * @author Andrzej Brynczka
 *
 */
public class ClientView extends Scene{
	// *********************************************************
	// ******************** Class Constants ********************
	// *********************************************************	
	//menu bar fields
	private final String MENU_ADMIN_LOGIN = "Admin Login";
	private final String MENU_ADMIN_PUNISHMENT_VIEW = "Open Punishment List";
	
	private final String MENU_OPTIONS_IGNORE_PM = "Ignore PMs";
	private final String MENU_OPTIONS_IGNORE_FILES = "Ignore File Requests";
	
	private final String MENU_FILE_OPEN_FILE_MGR = "Open File Manager";
	private final String MENU_FILE_BACK_TO_LOGIN = "Display Login Screen";
	private final String MENU_FILE_EXIT = "Close Chat";
	
	//admin login fields
	private final String TEXTFIELD_ADMIN_PASSWORD = "Enter admin password";
	private final String BUTTON_ADMIN_LOGIN = "Admin Login";
	
	//admin punishment fields
	private final String BUTTON_ADMIN_REMOVE_PUN = "Admin Remove Punishment";
	private final String BUTTON_ADMIN_PUN_LIST_EXIT = "Admin Exit Pun List";
	
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	private Client m_client;
	private ClientDriver m_driver;
	
	private Stage m_adminLoginStage;
	private Label m_adminLoginFeedback;
	private TextField m_adminPassTF;
	
	private MenuBar m_systemMenuBar;
	private LoginPane m_loginPane;
	private MainChatPane m_mainChatPane;

	private FileManagerView m_fileManagerView;
	private TableView<PunishmentInfo> m_punishmentTable;
	private Stage m_punishmentTableStage;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	/**
	 * 
	 * Construct a ClientView object, which listens for the client's
	 * loggedInProperty, adminLoginSuccessProperty, ResponseRequestTicket list,
	 * SentTicket list, and Received ticket list
	 * @param a_client Client, the main Client that this view is to act on
	 * @param a_driver ClientDriver, the stage that started this class
	 */
	public ClientView(Client a_client, ClientDriver a_driver){
		super( new Group() );
		m_driver = a_driver;
		m_client = a_client;
		m_fileManagerView = new FileManagerView( m_client, this);
		
		createMenuBar();

		//listen for a successful login into the server
		m_client.loggedInProperty().addListener(new ChangeListener<Boolean>() {
		        @Override
		        public void changed(ObservableValue<? extends Boolean> observable, 
		        		Boolean oldValue, Boolean newValue) {
		        	
		            if( newValue == true ){
		            	System.out.println("Logged into server.");
		            	displayChatScene();
		           	
		            	//begin listening for a successful admin login attempts
		            	m_client.adminLoginSuccessProperty().addListener(
		            			new ChangeListener<MessageResponse>()
		            			{
		        					
					        			@Override
					        			public void changed(
					        					ObservableValue<? extends MessageResponse> 
					        					observable,	MessageResponse oldValue, 
					        					MessageResponse newValue) 
					        			{
					        				if( newValue == MessageResponse.Success ){
					        					
					        					enableAdminPowerInView( true );
					        					m_adminLoginFeedback.setText("");
					        					m_adminLoginStage.close();
					
					        					
					        					//create the punishment-list window only 
					        					//after a successful login
					        					createAdminPunishmentStage();
					        				}
					        				else if( newValue == MessageResponse.Failure )
					        				{
				        						//aware the user of the invalid attempt
				                    		m_adminLoginFeedback.setText(
				                    				"Invalid password provided.");
				  
				
				            			}
				            			else if( newValue == MessageResponse.INVALID )
				            			{
				            					//turn off admin powers
				            					enableAdminPowerInView( false );
				            			}
					        			}		
		        		});
            		
            		//begin listening for file transfer requests from other users
            		m_client.getResponseRequestTicketList().addListener( 
            				new ListChangeListener<FileTransferTicket>()
            		{

							@Override
							public void onChanged(
									javafx.collections.ListChangeListener
									.Change<? extends FileTransferTicket> a_changeToList) 
							{
	
								while( a_changeToList.next() )
								{
									if( a_changeToList.wasAdded() ){
										//get the added ticket
										//and prompt the user for a response
										
										FileTransferTicket ticket = 
												m_client.getResponseRequestTicket(0);
										
										m_client.removeResponseRequestTicketFromList(0);
										m_mainChatPane.promptFileRequestResponse(ticket);
									}
								}
	
							}   			
            		});
            		
            		m_client.getSentTicketsList().addListener( 
            				new ListChangeListener<FileTransferTicket>()
            		{

							@Override
							public void onChanged(
									javafx.collections.ListChangeListener
									.Change<? extends FileTransferTicket> a_changeToList) 
							{
		
								while( a_changeToList.next() )
								{
									if( a_changeToList.wasAdded() && 
											!m_fileManagerView.isShowing() )
									{
										m_fileManagerView.show();
									}
								}
		
							}   			
            		});
            		
            		m_client.getReceivedTicketsList().addListener( 
            				new ListChangeListener<FileTransferTicket>()
            		{

							@Override
							public void onChanged(
									javafx.collections.ListChangeListener
									.Change<? extends FileTransferTicket> a_changeToList) 
							{
	
								while( a_changeToList.next() )
								{
									if( a_changeToList.wasAdded() && 
											!m_fileManagerView.isShowing() ){
										m_fileManagerView.show();
									}
								}
	
							}   			
            		});
                }
            }
        });
			
		//start with the login scene
		displayLoginScene();
		
	}
	
	/**
	 * Get the ClientDriver, the main stage for the client
	 * @return the ClientDriver for this client
	 */
	public ClientDriver getDriver(){
		return m_driver;
	}
	
	/**
	 * Get the client's username
	 * @return String containing the name
	 */
	public String getUserName(){
		return m_client.getUserName();
	}
	
	/**
	 * Initialize and display the LoginPane that is to be presented
	 * to the user.
	 * Also hides the admin punishment menu from the main system bar,
	 * if it is showing
	 */
	public void displayLoginScene(){
		//remove the admin punishment menu option, if applicable
		m_systemMenuBar.getMenus().get(2).getItems().get(1).setVisible( false );
		m_systemMenuBar.getMenus().get(2).getItems().get(1).setDisable( true );
		
		//create the login pane and set it
		m_loginPane = new LoginPane( m_client );
		
		m_loginPane.setTop( m_systemMenuBar );
		//setScene( new Scene( m_loginPane ) );
		setRoot( m_loginPane );
	}
	/**
	 * Initialize and display the MainChatPane that is to be displayed.
	 * Also enables the "admin login" option within the system menu bar
	 */
	public void displayChatScene(){
		//turn the admin login option back on within the system menu
		m_systemMenuBar.getMenus().get(2).getItems().get(0).setDisable( false );
		m_systemMenuBar.getMenus().get(2).getItems().get(0).setVisible( true );
		
		//add the menu to the main display
		m_mainChatPane = new MainChatPane( m_client, this );
		m_mainChatPane.setTop( m_systemMenuBar );
		
		//set the new display
		//setScene( new Scene( m_mainChatPane ) );
		setRoot( m_mainChatPane );
	}
	
	/**
	 * Enables the admin right-click powers, makes visible the admin
	 *  punishment list, and hides the admin login option if true is provided.
	 *  Does the opposite if false is given
	 *  
	 * @param a_enable boolean, whether or not to enable the admin power
	 *  options within the view
	 */
	public void enableAdminPowerInView( boolean a_enable ){
		
		if( a_enable ){
			//allow the user to request punishments by right-clicking others
			m_mainChatPane.enableAdminOptionsInContextMenu( true );
			
			//allow the user to view the admin punishment list
			m_systemMenuBar.getMenus().get(2).getItems().get(1).setVisible( true );
			m_systemMenuBar.getMenus().get(2).getItems().get(1).setDisable( false);
		
			//turn off visibility of the admin login option
			m_systemMenuBar.getMenus().get(2).getItems().get(0).setVisible( false);
			m_systemMenuBar.getMenus().get(2).getItems().get(0).setDisable( true );
		
		}
		else{
			//disable right-click punishment options in the chat pane
			m_mainChatPane.enableAdminOptionsInContextMenu( false );
			
			//remove the punishment list option
			m_systemMenuBar.getMenus().get(2).getItems().get(1).setVisible(false);
			m_systemMenuBar.getMenus().get(2).getItems().get(1).setDisable(true);
		
			//present the admin login option
			m_systemMenuBar.getMenus().get(2).getItems().get(0).setVisible(true);
			m_systemMenuBar.getMenus().get(2).getItems().get(0).setDisable(false);
		}
	}

	/**
	 * Initialize and fully create the menu bar to be placed at the top
	 * of the view
	 */
	private void createMenuBar(){
		
		//create the "File" menu items
		MenuItem openFileManagerItem = MenuItemBuilder.create()
				.text("Open File Manager")
				.id(MENU_FILE_OPEN_FILE_MGR)
				.build();
		openFileManagerItem.setOnAction( getMenuBarHandler() );
		
		MenuItem backToLoginItem = MenuItemBuilder.create()
				.text("Go back to login screen")
				.id(MENU_FILE_BACK_TO_LOGIN)
				.build();
		backToLoginItem.setOnAction( getMenuBarHandler() );
		
		MenuItem closeClientItem = MenuItemBuilder.create()
				.text("Close Client")
				.id(MENU_FILE_EXIT)
				.build();
		closeClientItem.setOnAction( getMenuBarHandler() );
		
		//create the "Options" menu items
		CheckMenuItem ignorePMsItem = CheckMenuItemBuilder.create()
				.text("Ignore Private Messages")
				.id(MENU_OPTIONS_IGNORE_PM)
				.selected(false)
				.build();
		ignorePMsItem.setOnAction( getMenuBarHandler() );
		
		CheckMenuItem ignoreFileRequestsItem = CheckMenuItemBuilder.create()
				.text("Ignore File Requests")
				.id(MENU_OPTIONS_IGNORE_FILES)
				.selected(false)
				.build();
		ignoreFileRequestsItem.setOnAction( getMenuBarHandler() );

		//create the "Admin" menu items
		MenuItem adminLoginItem = MenuItemBuilder.create()
				.text("Login as Admin")
				.id(MENU_ADMIN_LOGIN)
				.visible( false )
				.disable( true )//enable after logging in
				.build();
		adminLoginItem.setOnAction( getMenuBarHandler() );
		
		MenuItem punishmentListItem = MenuItemBuilder.create()
				.text("See Punished Users List")
				.id(MENU_ADMIN_PUNISHMENT_VIEW)
				.visible( false )
				.disable( true )//enable after logging in as admin
				.build();
		punishmentListItem.setOnAction( getMenuBarHandler() );
		
		//create the file and options menus
		Menu fileMenu = MenuBuilder.create()
				.text("File")
				.items(openFileManagerItem, backToLoginItem, closeClientItem)
				.build();
		Menu optionsMenu = MenuBuilder.create()
				.text("Options")
				.items(ignorePMsItem, ignoreFileRequestsItem)
				.build();
		
		Menu adminMenu = MenuBuilder.create()
				.text("Admin")
				.items( adminLoginItem, punishmentListItem )
				.build();
		
		//create the final menu bar
		m_systemMenuBar = new MenuBar();
		m_systemMenuBar.getMenus().addAll( fileMenu, optionsMenu, adminMenu );
		m_systemMenuBar.setMaxHeight(30);
		m_systemMenuBar.setPrefSize(500, 25);

	}
	
	/**
	 * Initialize the admin login stage and display it
	 */
	private void showAdminLogin(){
		//add labels for the description and password field
		Label adminDisplayLabel = new Label("Login as an admin");
		Label adminPassLabel = new Label("Password:");
		m_adminPassTF = new TextField("");
		m_adminPassTF.setId( TEXTFIELD_ADMIN_PASSWORD );
		m_adminPassTF.setEditable( true );
		m_adminPassTF.setMaxWidth( 150 );
		
		//add a login button
		Button loginButton = new Button("Login");
		loginButton.setId( BUTTON_ADMIN_LOGIN );
		loginButton.setOnAction( getButtonHandler() );
		
		//add a feedback label to display errors provided after
		//attempting to login
		m_adminLoginFeedback = new Label("");
		
		//create a grid pane to store the information in
		GridPane loginGrid = new GridPane();
		

		GridPane.setColumnSpan(adminDisplayLabel, 3);
		GridPane.setColumnSpan(m_adminLoginFeedback, 3);
		GridPane.setRowSpan( m_adminLoginFeedback, 3);
		GridPane.setColumnSpan( loginButton, 2);
		GridPane.setColumnSpan( adminPassLabel, 2);
		GridPane.setMargin( adminPassLabel, new Insets(16, 0, 0, 0) );
		GridPane.setMargin( m_adminPassTF, new Insets(16, 0, 0, 0) );
		GridPane.setMargin( loginButton, new Insets(16, 0, 0, 125) );
		GridPane.setMargin( adminDisplayLabel, new Insets(16, 0, 0, 0) );
		GridPane.setMargin( m_adminLoginFeedback, new Insets(16, 0, 0, 0) );
	
		//add the components to the grid
		loginGrid.add(adminDisplayLabel, 0, 0);
		loginGrid.add(adminPassLabel, 0, 1);
		loginGrid.add(m_adminPassTF, 2, 1);
		loginGrid.add(loginButton, 1, 2);
		loginGrid.add( m_adminLoginFeedback, 0, 3);
		
		Scene adminLoginScene = new Scene( loginGrid, 255, 200 );
		
		m_adminLoginStage = new Stage();
		m_adminLoginStage.setScene( adminLoginScene );
		m_adminLoginStage.setTitle("Login as an administrator");
		//m_adminLoginStage.sizeToScene();
		m_adminLoginStage.setMaxHeight(225);
		m_adminLoginStage.setMinHeight(225);
		m_adminLoginStage.setMaxWidth(270);
		m_adminLoginStage.setMinWidth(270);
		m_adminLoginStage.initOwner( m_driver );
		m_adminLoginStage.show();
		
	}
	
	/**
	 * Create the stage and tableview for the list of punished users displayed
	 * when an admin chooses the "Display Admin Punish List" option
	 */
	private void createAdminPunishmentStage( ){
		//a table from them with checkboxes and a button that sends
		//the list of punishmentInfo objects that are checked to be
		//un-punished
        TableColumn userNameCol = new TableColumn();
        userNameCol.setText("Username");
        userNameCol.setMinWidth(60);
        userNameCol.setCellValueFactory(
        		new PropertyValueFactory("TargetName") );

        TableColumn ipCol = new TableColumn();
        ipCol.setText("IP");
        ipCol.setMinWidth( 150 );
        ipCol.setCellValueFactory( new PropertyValueFactory("TargetIP") );
        
        
        TableColumn punishmentCol = new TableColumn();
        punishmentCol.setText("Punishment");
        punishmentCol.setMinWidth(60);
        punishmentCol.setCellValueFactory(
        		new PropertyValueFactory("Punishment") );

        m_punishmentTable = new TableView();
        m_punishmentTable.setItems( m_client.getPunishmentInfoList() );
        m_punishmentTable.getColumns().addAll(userNameCol, 
      		  punishmentCol, ipCol);
        m_punishmentTable.getSelectionModel().setSelectionMode( 
        		SelectionMode.MULTIPLE );
        m_punishmentTable.setColumnResizePolicy( 
        		TableView.CONSTRAINED_RESIZE_POLICY );

        //add buttons to remove punishments and exit
        Button removePunButton = new Button("Remove Punishment");
        removePunButton.setId( BUTTON_ADMIN_REMOVE_PUN );
        removePunButton.setOnAction( getButtonHandler() );
        
        Button exitButton = new Button("Exit");
        exitButton.setId( BUTTON_ADMIN_PUN_LIST_EXIT );
        exitButton.setOnAction( getButtonHandler() );
        
        //create the layout...
        //create a horizontal box to hold the buttons
        HBox buttonRow = new HBox();
        HBox.setHgrow(removePunButton, Priority.ALWAYS);
        HBox.setHgrow(exitButton, Priority.ALWAYS);
        buttonRow.getChildren().addAll( removePunButton, exitButton );
        
        //order the buttons and table in a border layout
        BorderPane borderLayout = new BorderPane();
        BorderPane.setMargin( buttonRow, new Insets(5, 0, 0, 15) );
        borderLayout.setCenter( m_punishmentTable );
        borderLayout.setBottom( buttonRow );

        //create the table's stage
		m_punishmentTableStage = new Stage();
		m_punishmentTableStage.setScene( new Scene( borderLayout ) );
	}

	/**
	 * Get the ActionEvent EventHandler for the buttons created
	 * in this class.
	 * @return the EventHandler
	 */
	private EventHandler<ActionEvent> getButtonHandler(){
		
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				Button source = (Button) event.getSource();
				
				//handle the button's event based on ID
				switch( source.getId() ){
				case BUTTON_ADMIN_LOGIN:
					m_client.sendAdminLoginMessage( m_adminPassTF.getText() );
					break;
				case BUTTON_ADMIN_REMOVE_PUN:
					ObservableList<PunishmentInfo> selectedUsers;
					selectedUsers = m_punishmentTable.getSelectionModel()
							.getSelectedItems();

					m_client.sendAdminPunishRemoveMessage( selectedUsers );				
					break;
				case BUTTON_ADMIN_PUN_LIST_EXIT:
					m_punishmentTableStage.close();
					break;
				default:
					break;
				}
			}
		};
		
		return handler;
	}
	
	/**
	 * Get the ActionEvent EventHandler for the menu bar displayed at
	 * the top of this view
	 * @return
	 */
	private EventHandler<ActionEvent> getMenuBarHandler(){
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>(){
			public void handle( ActionEvent e ){
				Object eventSource = e.getSource();
				
				if( eventSource instanceof CheckMenuItem ){
					CheckMenuItem item = (CheckMenuItem) eventSource;
					String itemID = item.getId();
					
					switch( itemID ){
					case MENU_OPTIONS_IGNORE_PM:
						if( item.isSelected() ){
							//was selected, start ignoring
							System.out.println("Private messages being ignored.");
							m_client.setIgnorePM( true );
						}
						else{
							//was disabled, stop ignoring
							System.out.println("Private messages being accepted.");
							m_client.setIgnorePM( false );
						}
						break;
					case MENU_OPTIONS_IGNORE_FILES:
						if( item.isSelected() ){
							//was selected, enable file ignoring
							System.out.println("File requests being ignored.");
							m_client.setIgnoreFileRequests( true );
						}
						else{
							//was disabled, disable file ignoring
							System.out.println("File requests being accepted.");
							m_client.setIgnoreFileRequests( false );
						}
						break;
					}
				}

				if( eventSource instanceof MenuItem ){
					MenuItem item = (MenuItem) eventSource;
					String itemID = item.getId();

					//handle the option
					switch( itemID ){
					case MENU_FILE_OPEN_FILE_MGR:
						m_fileManagerView.show();
						break;
					case MENU_FILE_BACK_TO_LOGIN:
						m_client.terminateConnection();
						displayLoginScene();
						break;
					case MENU_FILE_EXIT:
						m_client.terminateConnection();
						m_driver.close();
						break;
					case MENU_ADMIN_LOGIN:
						showAdminLogin();
						break;
					case MENU_ADMIN_PUNISHMENT_VIEW:
						m_client.sendAdminPunishListRequest();
						m_punishmentTableStage.show();
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
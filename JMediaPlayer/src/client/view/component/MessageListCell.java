package client.view.component;

import client.messages.AdminLoginMessage;
import client.messages.AdminPunishMessage;
import client.messages.ChatMessage;
import client.messages.FileTransResponseMessage;
import client.messages.FileTransferMessage.TransferStage;
import client.messages.LoginMessage;
import client.messages.Message;
import client.messages.PrivateChatMessage;
import client.messages.PunishmentInfo.Punishment;
import client.messages.ServerMessage;
import client.messages.LoginMessage.LoginCondition;
import client.messages.Message.MessageResponse;
import client.view.ClientView;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * This class is a cell component made to be used within a
 * larger ListView. It is specialized to display messages to
 * the client differently based upon the type of Message it
 * currently represents.
 * 
 * @author Andrzej Brynczka
 *
 */
public class MessageListCell<T> extends ListCell<T> {
	// *********************************************************
	// ******************** Class Variables ********************
	// *********************************************************
	private ClientView m_clientView;
	
	// *********************************************************
	// ******************** Constructor ************************
	// *********************************************************
	public MessageListCell(ClientView a_clientView){
		m_clientView = a_clientView;
	}
	
	@Override 
	public void updateItem(T item, boolean empty){
		super.updateItem(item, empty);
		
		if( empty ){
			setText( null );
		}
		else {
			//change the color of the message based on its type
			String textMsg = "";
			setEditable( false );
			Text displayedText = null;
			
			
			switch( ((Message)item).getHeader() ){
			case REG_SendChatMess:
				ChatMessage message = (ChatMessage) item;
				textMsg =  message.getSenderName() 
								+ " > " + message.getMessage();
				
				displayedText = new Text(textMsg);
				displayedText.setFill( Color.DARKBLUE );
				setGraphic(displayedText);
				
				break;
			case PRIV_SendChatMess:
				PrivateChatMessage pMessage = (PrivateChatMessage) item;
				String sender = pMessage.getSenderName();
				String receiver = pMessage.getReceiverName();
				if( sender.equals( m_clientView.getUserName() ) ){
					textMsg = "To " + receiver + " > " + pMessage.getMessage();
				}
				else{
					textMsg = "From " + sender +" > " + pMessage.getMessage();
				}

				
				displayedText = new Text( textMsg );
				displayedText.setFill( Color.DARKVIOLET );
				setGraphic( displayedText );
				//TODO create message types for other messages
				break;
			case FILE_Transfer:
				//only response messages get displayed in the chat screen
				FileTransResponseMessage ftrMsg = 
					(FileTransResponseMessage) item;
				
				String fileName = ftrMsg.getFileName();
				
				if( ftrMsg.getTransferStage() == 
									TransferStage.STAGE2_ResponseToRequest ){
					textMsg = "Server > " + ftrMsg.getSenderName() + " has " 
							+ (ftrMsg.getResponse() == MessageResponse.Success 
								? "accepted" : "denied") 
							+ " your request to transfer " + fileName;
				}
				else{
					
					if( ftrMsg.getSenderName().equals( 
							m_clientView.getUserName() ) ){
						
						textMsg = "Server > The transfer of " + fileName
								+ " with " + ftrMsg.getReceiverName() + " was "+
								(ftrMsg.getResponse() == MessageResponse.Success 
								? "completed." : "canceled."); 
					}
					else{
						
						textMsg = "Server > The transfer of " + fileName 
								+ " with " + ftrMsg.getSenderName() + " was " +
								(ftrMsg.getResponse() == MessageResponse.Success 
								? "completed successfully." : "canceled."); 
					}
				}
				
				displayedText = new Text( textMsg );
				displayedText.setFill( Color.CRIMSON );
				setGraphic( displayedText );
				break;
			case SERVER_Error:
			case SERVER_Kicked:
				ServerMessage sMessage = (ServerMessage) item;
				textMsg = "Server > " + sMessage.getMessage();
				
				displayedText = new Text( textMsg );
				displayedText.setFill( Color.DARKGREEN );
				setGraphic( displayedText );
				break;
			case ADMIN_Login:
				AdminLoginMessage alMsg = (AdminLoginMessage) item;
				textMsg = "Server > Successfully logged in as an admin.";
				
				displayedText = new Text( textMsg );
				displayedText.setFill( Color.DARKBLUE );
				setGraphic( displayedText );
				break;
			case ADMIN_PunishUser:
				AdminPunishMessage apMsg = (AdminPunishMessage) item;
				if( apMsg.getServerResponse() == MessageResponse.Success ){
					
					//modify the message slightly for a mute
					if( apMsg.getPunishment() == Punishment.MUTE ){
						textMsg = "Server > " + apMsg.getAdminName() 
								+ " has MUTED " + apMsg.getTargetName();
					}
					else{
						textMsg = "Server > " + apMsg.getAdminName()
								+ " has " + apMsg.getPunishment().toString()
								+ "ED " + apMsg.getTargetName();
					}
				}
				else{
					textMsg = "Server > Unable to " 
							+ apMsg.getPunishment().toString() + " " 
							+ apMsg.getTargetName();
				}
				
				displayedText = new Text( textMsg );
				displayedText.setFill( Color.DARKGREEN );
				setGraphic( displayedText );
				break;
			case ADMIN_RemovePunishment:
				AdminPunishMessage apRmvMsg = (AdminPunishMessage) item;
				if( apRmvMsg.getServerResponse() == MessageResponse.Success ){
					
					textMsg = "Server > " + apRmvMsg.getAdminName()
							+ " has removed " + apRmvMsg.getTargetName()
							+ "'s " + apRmvMsg.getPunishment().toString();

				}
				else{
					textMsg = "Server > Unable to remove the " 
							+ apRmvMsg.getPunishment().toString() 
							+ " from " + apRmvMsg.getTargetName();
				}
				
				displayedText = new Text( textMsg );
				displayedText.setFill( Color.DARKGREEN );
				setGraphic( displayedText );
				break;
			case LOGIN:
				LoginMessage loginMsg = (LoginMessage) item;
				if( loginMsg.getCondition() == LoginCondition.SUCCESS ){
					textMsg = "Server > Logged into server.";
				}
				else{
					textMsg = "Server > Logged into server. Your IP is muted.";
				}
				
				displayedText = new Text( textMsg );
				displayedText.setFill( Color.DARKBLUE );
				setGraphic( displayedText );
				break;
			default:
					break;
			}
			displayedText.setWrappingWidth( 
					m_clientView.widthProperty().doubleValue() * .55  );
		}
	}
}

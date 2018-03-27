package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * ServerConnection class for Multi-Threaded Chat Application
 * Created by Collin Brey, Nnenna Aneke, Rafael Carter and Caleb Farara
 * 
 * @version CS4225 Spring 2018
 */
public class ServerConnection extends Thread {
	
	private Socket socket;
	private Server server;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	private boolean activeConnection;
	private HashMap<String, String> usernames;
	private ArrayList<String> activeUsernames;
	
	/**
	 * Creates a ServerConnection object consisting of a Socket, Server, DataInputStream and DataOutputStream
	 * 
	 * @precondition socket != null, server != null, ipAddress != null, usernames != null, activeUsernames != null
	 * @postcondition this.getName() = "ServerConnectionThread"
	 * 					this.socket = socket
	 * 					this.server = server
	 * 					this.activeConnection = true
	 * 					this.dataInputStream = this.socket.getInputStream()
	 * 					this.dataOutputStream = this.socket.getOutputStream()
	 * 					this.usernames = usernames
	 * 					this.activeUsernames = activeUsernames
	 * @param socket
	 * @param server
	 */
	public ServerConnection(Socket socket, Server server, String ipAddress, HashMap<String, String> usernames, ArrayList<String> activeUsernames) {
		super(ipAddress);
		try {
			this.socket = socket;
			this.server = server;
			this.activeConnection = true;
			this.dataInputStream = new DataInputStream(this.socket.getInputStream());
			this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
			this.usernames = usernames;
			this.activeUsernames = activeUsernames;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Listens for messages sent by any of the client's connected and sends found messages to all Clients
	 * 
	 * @precondition none
	 * @postcondition none
	 */
	public void run() {
		try {
			while (this.activeConnection) {
				while (this.dataInputStreamUnavailable()) {
					this.waitOneSecond();
				}
				
				String message = this.dataInputStream.readUTF();
				
				if (userIsJoiningChat(message)) {
					this.activeUsernames.add(message.substring(1));
				}
				else if (userIsLeavingChat(message)) {
					boolean usernameFound = false;
					
					for (int i = 0; i < this.activeUsernames.size(); i++) {
						if (this.activeUsernames.get(i).equals(message.substring(1))) {
							this.activeUsernames.remove(i);
							usernameFound = true;
						}
						if (usernameFound) break;
					}
				}
				else if (!messageContainsIpAddress(message)) {
					this.sendMessageToAllClients(message);
				}
				else {
					String[] information = message.split("\\s+");
					this.usernames.put(information[0], information[1]);
				}
				this.sendActiveUsersToClients();
			}
			this.closeServerConnection();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a message to specific client
	 * 
	 * @precondition message != null
	 * @postcondition this.dataOutputStream.flush()
	 * 
	 * @param message Message being sent
	 */
	public void sendMessageToClient(String message) {
		try {
			this.dataOutputStream.writeUTF(message);
			this.dataOutputStream.flush();
		} catch (IOException e) { }
	}
	
	private void sendMessageToAllClients(String message) {
		for (ServerConnection serverConnection : this.server.getServerConnections()) {
			if (serverConnection.activeConnection) {
				serverConnection.sendMessageToClient(message);
			}
		}
	}
	
	private void sendActiveUsersToClients() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("u");
		for (String username : this.activeUsernames) {
			stringBuilder.append(username + "\n");
		}
		this.sendMessageToAllClients(stringBuilder.toString());
	}

	private boolean messageContainsIpAddress(String message) {
		return message.charAt(0) == '/';
	}

	private boolean userIsLeavingChat(String message) {
		return message.charAt(0) == 'i';
	}

	private boolean userIsJoiningChat(String message) {
		return message.charAt(0) == 'a';
	}
	
	private void closeServerConnection() {
		try {
			this.dataInputStream.close();
			this.dataOutputStream.close();
			this.socket.close();
			this.activeConnection = false;
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void waitOneSecond() {
		try {
			Thread.sleep(1);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private boolean dataInputStreamUnavailable() { 
		boolean inputStreamAvailable = false;
		try {
			inputStreamAvailable = this.dataInputStream.available() == 0;
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return inputStreamAvailable;
	}
}

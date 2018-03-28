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
		
		if (socket == null) {
			throw new IllegalArgumentException("Socket cannot be null");
		}
		if (server == null) {
			throw new IllegalArgumentException("Server cannot be null");
		}
		if (ipAddress == null) {
			throw new IllegalArgumentException("Ip Address cannot be null");
		}
		if (usernames == null) {
			throw new IllegalArgumentException("Map of Ip Addresses and usernames cannot be null");
		}
		if (activeUsernames == null) {
			throw new IllegalArgumentException("Active usernames cannot be null");
		}
		
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
					if (information[0] != null && information[1] != null) {
						this.usernames.put(information[0], information[1]);
					}
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
		if (message == null) {
			throw new IllegalArgumentException("Must be a valid message to send to Client");
		}
		try {
			this.dataOutputStream.writeUTF(message);
			this.dataOutputStream.flush();
		} catch (IOException e) { }
	}
	
	private void sendMessageToAllClients(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Must be a valid message to send to the Clients");
		}
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
		if (message == null) {
			throw new IllegalArgumentException("Message is null");
		}
		return message.charAt(0) == '/';
	}

	private boolean userIsLeavingChat(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Message is null");
		}
		return message.charAt(0) == 'i';
	}

	private boolean userIsJoiningChat(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Message is null");
		}
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

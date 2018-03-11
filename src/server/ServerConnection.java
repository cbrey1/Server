package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
	
	/**
	 * Creates a ServerConnection object consisting of a Socket, Server, DataInputStream and DataOutputStream
	 * 
	 * @precondition socket != null, server != null
	 * @postcondition this.getName() = "ServerConnectionThread"
	 * 					this.socket = socket
	 * 					this.server = server
	 * 					this.activeConnection = true
	 * 					this.dataInputStream = this.socket.getInputStream()
	 * 					this.dataOutputStream = this.socket.getOutputStream()
	 * @param socket
	 * @param server
	 */
	public ServerConnection(Socket socket, Server server) {
		super("ServerConnectionThread");
		try {
			this.socket = socket;
			this.server = server;
			this.activeConnection = true;
			this.dataInputStream = new DataInputStream(this.socket.getInputStream());
			this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
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
				this.sendMessageToAllClients(message);
			}
			this.closeServerConnection();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessageToClient(String message) {
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

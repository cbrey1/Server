package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * Server class for Multi-Threaded Chat Application
 * Created by Collin Brey, Nnenna Aneke, Rafael Carter and Caleb Farara
 * 
 * @version CS4225 Spring 2018
 */
public class Server {

	private ServerSocket serverSocket;
	private ArrayList<ServerConnection> serverConnections;
	private boolean serverRunning;
	
	/**
	 * Creates a Server object to allow Clients to connect to and chat
	 * 
	 * @precondition none
	 * @postcondition this.serverSocket = new ServerSocket(6066)
	 * 				this.serverConnections = new ArrayList<ServerConnection>
	 * 				this.serverRunning = true
	 */
	public Server() {	
		try {
			this.serverSocket = new ServerSocket(6066);
			this.serverConnections = new ArrayList<ServerConnection>();
			this.serverRunning = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the Server Object creating new ServerConnections for each Client that connects
	 * 
	 * @precondition none
	 * @postcondition this.serverConnections.Count() += 1
	 */
	public void start() {
		try {
			while(this.serverRunning) {
				Socket socket = this.serverSocket.accept();
				ServerConnection serverConnection = new ServerConnection(socket, this);
				serverConnection.start();
				this.serverConnections.add(serverConnection);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the ArrayList of ServerConnections
	 * 
	 * @precondition none
	 * @postcondition = none
	 * 
	 * @return this.serverConnections
	 */
	public ArrayList<ServerConnection> getServerConnections() {
		return this.serverConnections;
	}
}

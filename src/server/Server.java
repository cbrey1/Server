package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Server class for Multi-Threaded Chat Application
 * Created by Collin Brey, Nnenna Aneke, Rafael Carter and Caleb Farara
 * 
 * @version CS4225 Spring 2018
 */
public class Server {

	private ServerSocket serverSocket;
	private ArrayList<ServerConnection> serverConnections;
	private ArrayList<String> activeUsernames;
	private boolean serverRunning;
	private HashMap<String, String> usernames;

	/**
	 * Creates a Server object to allow Clients to connect to and chat
	 * 
	 * @precondition none
	 * @postcondition this.serverSocket = new ServerSocket(6066)
	 *                this.serverConnections = new ArrayList<ServerConnection>
	 *                this.serverRunning = true this.activeUsernames = new
	 *                ArrayList<String>() this.usernames = new HashMap<String,
	 *                String>()
	 */
	public Server() {
		try {
			this.serverSocket = new ServerSocket(4225);
			this.serverConnections = new ArrayList<ServerConnection>();
			this.activeUsernames = new ArrayList<String>();
			this.serverRunning = true;
			this.usernames = new HashMap<String, String>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts the Server Object creating new ServerConnections for each Client that
	 * connects
	 * 
	 * @precondition none
	 * @postcondition this.serverConnections.Count() += 1
	 */
	public void start() {
		try {
			while (this.serverRunning) {
				Socket socket = this.serverSocket.accept();
				String result = "";
				boolean ipFound = false;

				for (ServerConnection conn : this.serverConnections) {
					if (conn.getName().equals(socket.getInetAddress().toString())) {
						if (this.usernames.get(socket.getInetAddress().toString()) == null) {
							result = "username is null";
						} else {
							ipFound = true;
						}
					}
				}
				
				if (ipFound) {
					result = "_" + this.usernames.get(socket.getInetAddress().toString());
				} else {
					result = "n" + this.getAllUsernames();
				}

				ServerConnection serverConnection = new ServerConnection(socket, this,
						socket.getInetAddress().toString(), this.usernames, this.activeUsernames);
				serverConnection.start();
				this.serverConnections.add(serverConnection);
				serverConnection.sendMessageToClient(result);

			}
		} catch (IOException e) {
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

	private ArrayList<String> getAllUsernames() {
		ArrayList<String> allUsernames = new ArrayList<String>();

		for (String value : this.usernames.values()) {
			allUsernames.add(value + " ");
		}

		return allUsernames;
	}
}

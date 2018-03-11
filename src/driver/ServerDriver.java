package driver;

import server.Server;

/*
 * ServerDriver class for Multi-Threaded Chat Application
 * Created by Collin Brey, Nnenna Aneke, Rafael Carter and Caleb Farara
 * 
 * @version CS4225 Spring 2018
 */
public class ServerDriver {
	
	/**
	 * Entry point for Server Application
	 * @param args
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
}

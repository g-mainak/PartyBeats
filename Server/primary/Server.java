package primary;

/* 
 *
 * CS434/534 hw3 server
 * To compile (need the json lib):
 *   javac -cp json-simple-1.1.1.jar *.java
 *
 * To run (make sure you have the json lib):
 *   java -cp '.:json-simple-1.1.1.jar' Server
 * 
 * To test the output
 *   
 */
import java.io.*;
import java.util.*;
import java.net.*;

public class Server {

    private ServerSocket welcomeSocket;

    public final static int THREAD_COUNT = 3;
    public final static int PORT = 10000;
    
    private ServiceThread[] threads;
    private Map<String, Party> partyTable;
    private Map<String, Party> deviceTable;
	
    /* Constructor: starting all threads at once */
    public Server(int serverPort) {

	try {
	    // create server socket
	    welcomeSocket = new ServerSocket(serverPort);
	    System.out.println("WBServer at" + welcomeSocket);
	    
	    // initialize party table
	    partyTable = new HashMap<String, Party>();
	    deviceTable = new HashMap<String, Party>();
	    
	    // create thread pool
	    threads = new ServiceThread[THREAD_COUNT];
	    
	    // start all threads
	    for (int i = 0; i < threads.length; i++) {
		threads[i] = new ServiceThread(welcomeSocket, partyTable, deviceTable);
		threads[i].start();
	    }
	} catch (Exception e) {
	    System.out.println("WBServer construction failed.");
	}
	
    } // end of Server
    
    public static void main(String[] args) {
	// see if we do not use default server port
	int serverPort = PORT;
	if (args.length >= 1)
	    serverPort = Integer.parseInt(args[0]);
	
	Server server = new Server(serverPort);
	server.run();
	
    } // end of main

    // Infinite loop to process each connection
    public void run() {

	try {
	    for (int i = 0; i < threads.length; i++) {
		threads[i].join();
	    }
	    System.out.println("All threads finished. Exit");
	    
	} catch (Exception e) {
	    System.out.println("Join errors");
	}
	
    } // end of run
    
} // end of class
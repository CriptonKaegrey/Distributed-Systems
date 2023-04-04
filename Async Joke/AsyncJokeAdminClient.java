/*--------------------------------------------------------

1. Name / Date: Vatsal Parikh, May 28, 2022

2. Java version used (java -version), if not the official version for the class:

java 18.0.1.1

3. Precise command-line compilation examples / instructions:

> javac AsyncJokeServer.java
> javac AsyncJokeClient.java
> javac AsyncJokeAdminClient.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java AsyncJokeServer
> java AsyncJokeClient
> java AsyncJokeAdminClient

All acceptable commands are displayed below:

> java AsyncJokeServer secondary
> java AsyncJokeClient
> java AsyncJokeClient localhost
> java AsyncJokeClient localhost localhost
> java AsyncJokeClient 127.0.0.1 localhost
> java AsyncJokeAdminClient
> java AsyncJokeAdminClient localhost
> java AsyncJokeAdminClient localhost localhost

5. List of files needed for running the program.

 a. AsyncJokeServer.java
 b. AsyncJokeClient.java
 c. AsyncJokeAdminClient.java

5. Notes:

It is a multithreaded UDP server/client with a secondary server for both Client and Admin.

I didn't transfer objects between servers and clients instead I stored only UUID
at client side and passed it as string between server and client adding more security
but also complexity at server side.

I used hashtables for storing state as it is easier than custom classes to iterate over.

Only the shuffle of Joke IDs are stored as state for users and not all jokes/proverbs. 
There is only one copy of jokes/proverbs.

In the worker thread, once a request has been received, sleeping for 40 seconds.

After sending the asynchronous request for Joke/Proverb the client displays a prompt
on the client console that asks the user to enter two numbers and returning sum to show 
work on server side.

----------------------------------------------------------*/


// Importing Java Input Output and networking libraries
import java.io.*;
import java.net.*;

// AsyncJokeAdminClient class
public class AsyncJokeAdminClient{
// Main method
    public static void main (String args[]) {
// Defining servernames and port numbers
        String serverName1 = "localhost";
        String serverName2 = "localhost";
        String currentServer = serverName1;
        int port_server1 = 5050;
        int port_server2 = 5051;
        int currentPort = port_server1;
// Switcher will be used to check if user provided multiple arguments(ie multi server)
        boolean switcher = false;

// Based on arguments servername will be fetched 
        if(args.length == 1) {
            serverName1 = args[0];
        }
      
        System.out.println("Vatsal Parikh's Admin Client\n");
        System.out.println("Server one: " + serverName1 + ", Port: 5050"); 

        if (args.length == 2){
            serverName2 = args[1];
            System.out.println("Server two: " + serverName1 + ", Port: 5051");  
            System.out.println("\n**(s) to change servers**\n");
            switcher = true;
        }

        try {
// Defining I/O streams fromServer/toServer and in
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader fromServer;
            PrintStream toServer;
// Trying to get user inputs until user passes quit as input 
            do {
                Socket sock;
                System.out.println("Press enter to change modes");
// Waiting for user input and it won't execute further until some kind of input is given
                String temp = in.readLine();
// Trying to make a connection with the server name and port number
                try{
                    sock = new Socket(currentServer, currentPort);
// Assigning fromServer/toServer to socket's(server) Input/Output stream
                    fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    toServer = new PrintStream(sock.getOutputStream());
                    String mode="joke";
// If the input is quit and breaking the loop and exiting the program
                    if(temp.toString().contains("quit")){
                        break;
                    }
// Changing servers when the input is s and printing accordingly on admin side
                    if(temp.equals("s")){
// Changing boolean value
                        if(switcher){
                            if(currentPort==port_server1){
                                currentServer = serverName2;
                                System.out.println("Now communicating with: localhost, port 5051\n");
                                currentPort = port_server2;
                            }
                            else if(currentPort==port_server2){
                                currentServer = serverName1;
                                System.out.println("Now communicating with: localhost, port 5050\n");
                                currentPort = port_server1;
                            }
                            continue;
                        }
// If no secondary server is provided it will provide this output
                        else{
                            System.out.println("No secondary server being used");
                            continue;
                        }
                    }
// Server sends the current mode to admin
                    mode = fromServer.readLine();
// Based on the current mode admin will change the mode and send that that to server
                    if (mode.equals("joke")){
                        mode = "proverb";
                    }
                    else if(mode.equals("proverb")){
                        mode = "joke";
                    }
                    toServer.println(mode);
                    toServer.flush();
// Closing socket
                    sock.close();
                } catch (Exception x) {
                    System.out.println ("Socket error.");
                    x.printStackTrace (); 
                }
            } while(true);
        } catch (IOException x) {x.printStackTrace();}
    }
}
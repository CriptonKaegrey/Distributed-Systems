/*--------------------------------------------------------

1. Name / Date: Vatsal Parikh, April 16, 2022

2. Java version used (java -version), if not the official version for the class:

java 11.0.12

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed below:

> java JokeServer secondary
> java JokeClient
> java JokeClient localhost
> java JokeClient localhost localhost
> java JokeClient 127.0.0.1 localhost
> java JokeClientAdmin
> java JokeClientAdmin localhost
> java JokeClientAdmin localhost localhost

5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

5. Notes:

It is a multithreaded server with a secondary server for both Client and Admin.

I didn't transfer objects between servers and clients instead I stored only UUID
at client side and passed it as string between server client adding more security
but also complexity at server side.

I used hashtables for storing state as it is easier than custom classes to iterate over.

Only the shuffle of Joke IDs are stored as state for users and not all jokes/proverbs. 
There is only one copy of jokes/proverbs.

I tried adding InetAddress.getByName() in JokeClientAdmin socket but 140.192.1.9 was always 
giving error: Cannot assign requested address: NET_Bind. So except this everything works.

----------------------------------------------------------*/


// Importing Java Input Output and networking libraries
import java.io.*;
import java.net.*;
import java.util.*;

// JokeClient class
public class JokeClient{
// UUID with respective name is stored in name_uuid
    static Hashtable<String, String> name_uuid = new Hashtable<String, String>();
    static String name;
// Main method
    public static void main (String args[]) {
// Defining servernames and port numbers
        String serverName1 = "localhost";
        String serverName2 = "localhost";
        String currentServer = serverName1;
        int port_server1 = 4545;
        int port_server2 = 4546;
        int currentPort = port_server1; 
// Switcher will be used to check if user provided multiple arguments(ie multi server)
        boolean switcher = false;

// Based on arguments servername will be fetched 
        if(args.length == 1) {
            serverName1 = args[0];
        }
        
        System.out.println("Vatsal Parikh's Joke Client\n");
        System.out.println("Server one: " + serverName1 + ", Port: 4545");    

        if (args.length == 2){
            serverName2 = args[1];
            System.out.println("Server two: " + serverName1 + ", Port: 4546");  
            System.out.println("\n**(s) to change servers**");
            switcher = true;
        }

// Taking input from the user using InputStreamReader
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
// Trying to get user inputs until user passes quit as input 
        try {
            do {
// Fetching user input
                System.out.print("\nEnter your name or just press enter(if you are the previous user), \n(quit) to end: ");
                System.out.flush ();
                String temp = in.readLine();
// Changing servers when the input is s and printing accordingly on admin side
                if(temp.equals("s")){
// Changing boolean value
                    if(switcher){
                        if(currentPort==port_server1){
                            currentServer = serverName2;
                            System.out.println("Now communicating with: localhost, port 4546");
                            currentPort = port_server2;
                        }
                        else if(currentPort==port_server2){
                            currentServer = serverName1;
                            System.out.println("Now communicating with: localhost, port 4545");
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
                if(!temp.isEmpty()){
                    name = temp;
                }
// If user input is valid, getJoke method is called with user input and server name
                if (name.indexOf("quit") < 0){
                    if(!name_uuid.keySet().contains(name)){
                        name_uuid.put(name, UUID.randomUUID().toString());
                    }
                    getJoke(name, name_uuid.get(name), currentServer, currentPort);
                }
                    
            } while (name.indexOf("quit") < 0);
            System.out.println ("Cancelled by user request.");
        } catch (Exception x) {System.out.println(x);}
    }

// This static method will fetch the output from server side with the help of fromServer
    static void getJoke (String name, String uuid, String serverName, int currentPort){
        
// sock contains the current client connection
        Socket sock;
// Defining I/O streams fromServer/toServer to null
        BufferedReader fromServer;
        PrintStream toServer;
        String textFromServer;
 
// Trying to make a connection with the server name and port number
        try{
            sock = new Socket(serverName, currentPort);
 
// Assigning fromServer/toServer to socket's(server) Input/Output stream
            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            toServer = new PrintStream(sock.getOutputStream());
// Sending name, uuid and cuurent port to server
            toServer.println(name+"\n"+uuid+"\n"+currentPort);
            toServer.flush();
 
// Reading the first 2-3 lines from the server
            for (int i = 1; i <=3; i++){
                textFromServer = fromServer.readLine();
// If the line read is not null, it is printed on client side
                if (textFromServer != null) System.out.println(textFromServer);
            }
            sock.close();
        } catch (IOException x) {
            System.out.println ("Socket error.");
            x.printStackTrace ();
        }
    }
}
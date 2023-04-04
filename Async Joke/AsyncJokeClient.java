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
import java.util.*;

// AsyncJokeClient class
public class AsyncJokeClient{
// UUID with respective name is stored in name_uuid
    static Hashtable<String, String> name_uuid = new Hashtable<String, String>();
    static String name;
    static String textFromServer = null;
// Main method
    public static void main (String args[]) {
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
        
        System.out.println("Vatsal Parikh's Async Joke Client\n");
        System.out.println("Server one: " + serverName1 + ", Port: 4545");    
// If second hostname is given then option is given to change servers
        if (args.length == 2){
            serverName2 = args[1];
            System.out.println("Server two: " + serverName1 + ", Port: 4546");  
            System.out.println("\n**(s) to change servers**");
            switcher = true;
        }

// Taking input from the user using InputStreamReader
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

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
                textFromServer = null;    
            } while (name.indexOf("quit") < 0);
            System.out.println ("Cancelled by user request.");
        } catch (Exception x) {System.out.println(x);}
    }

// This static method will send the name, uuid and currentPort to server and shpwing some work done
    static void getJoke (String name, String uuid, String serverName, int currentPort) throws Exception{
// Defining InetAddress, socket and toServer
        InetAddress address = InetAddress.getByName(serverName);
        DatagramSocket socket = new DatagramSocket();
        byte[] toServer = null;
// Trying to send the request to user and returning error if not possible
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
// Concatenating the reponse text
            String temp = name + "," + uuid + "," + currentPort;
// Converting String to byte[]
            toServer = temp.getBytes();
// Connecting to server with default port            
            DatagramPacket request = new DatagramPacket(toServer, toServer.length, address, 4545);
// Sending the request to server
            socket.send(request);
// Starting a thread to listen for Server response
            new ServerResponse(socket).start();
// While we receive the response simulating work using basic sum calculating
            while(textFromServer==null){
                try{
                    System.out.print("\nEnter numbers seperated by space to sum: ");
                    String[] nums = in.readLine().split(" ");
                    int num1 = Integer.parseInt(nums[0]);
                    int num2 = Integer.parseInt(nums[1]);
                    System.out.println("Your sum is: " + (num1 + num2));
                }   
                catch(Exception e){
                    System.out.println("Try again!");
                }
            }
// Printing out the response
            System.out.println(textFromServer);
        } catch (IOException x) {
            System.out.println ("Socket error.");
            x.printStackTrace ();
        }

    }

// ServerReponse class 
    static class ServerResponse extends Worker{
        DatagramSocket socket;
        byte[] fromServer = new byte[100];
        DatagramPacket response;
// Intializing socket
        ServerResponse(DatagramSocket socket){
            this.socket = socket;
        }
// start() method 
        public void run(){
            try{
// Waiting for the server response
                response = new DatagramPacket(fromServer, fromServer.length);
                socket.receive(response);
                textFromServer = new String(fromServer, 0, response.getLength());
            }
            catch(Exception e){System.out.println(e);}
            
        }
    }
}
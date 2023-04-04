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

// This will act as the parent class whose variable can be used in both AdminWorker and ServerWorker
class Worker extends Thread{
// mode_admin and mode_client will be used keep track of the current mode and both servers
    static Hashtable<Integer, String> mode_admin = new Hashtable<Integer, String>();
    static Hashtable<Integer, String> mode_client = new Hashtable<Integer, String>();
}

// AdminWorker class
class AdminWorker extends Worker {
    ServerSocket servsock;
    Socket sock;
// Passsing the serversocket instead of socket as argument in constructor
    AdminWorker(ServerSocket servsock) {
        this.servsock = servsock;
// Inserting default values
        mode_admin.putIfAbsent(5050, "joke");
        mode_admin.putIfAbsent(5051, "joke");
        mode_client.putIfAbsent(4545, "joke");
        mode_client.putIfAbsent(4546, "joke");
    }

// start() method 
    public void run() {
        while(true){
            try{
// Connecting to admin client
                sock = servsock.accept();
                int admin_port = servsock.getLocalPort();
                PrintStream out = null;
                BufferedReader in = null;
// Trying to assign in/out to socket's(client) Input/Output stream and returning error if not possible
                try {
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    out = new PrintStream(sock.getOutputStream());
 // Trying to read client request and if it cannot be read returning an error
                    try {
// Sending the current mode to admin
                        out.println(mode_admin.get(admin_port));
// Reading the mode returned by admin, changing the old value in mode_admin and printing it on server side
                        mode_admin.replace(admin_port, in.readLine());
                        System.out.println("Changed mode for " + admin_port +" to " + mode_admin.get(admin_port));
// For the current port when mode if changed it will be reflected on admin_port for server worker to use
                        if(admin_port==5050){
                            mode_client.replace(4545, mode_admin.get(admin_port));
                        }
                        else if(admin_port==5051){
                            mode_client.replace(4546, mode_admin.get(admin_port));
                        }
                    } catch (Exception x) {}
// Closing the connection with client, though server is still running
                    sock.close();
                } catch (IOException ioe) {System.out.println(ioe);}
            } catch(Exception e){
            System.out.println(e);
            }
        }
    }

    static String toText (byte ip[]) {
        StringBuffer result = new StringBuffer ();
        for (int i = 0; i < ip.length; ++ i) {
            if (i > 0) result.append (".");
            result.append (0xff & ip[i]);
        }
        return result.toString ();
    }
}


// ServerWorker class
class ServerWorker extends Worker {
// buffer will contain the response received by client
byte[] buffer;
// request and ds contains client information
DatagramPacket request;
DatagramSocket ds;
// Defining jokes and proverbs
    static Hashtable<String, String> jokes = new Hashtable<String, String>();
    static Hashtable<String, String> proverbs = new Hashtable<String, String>();
// According to server used and user accessing the server, user's joke/proverb sequence will be stored here
    static Hashtable<Integer, Hashtable<String, ArrayList<String>>> server_jokes = new Hashtable<Integer, Hashtable<String, ArrayList<String>>>();
    static Hashtable<Integer, Hashtable<String, ArrayList<String>>> server_proverbs = new Hashtable<Integer, Hashtable<String, ArrayList<String>>>();
// This will contain the shuffle used to randomize the joke sequence
    ArrayList<String> shuffle_jokes;
    ArrayList<String> shuffle_proverbs;
    static String out = "";
// Worker Constructor
    ServerWorker (DatagramSocket ds, DatagramPacket request, byte[] buffer) {
// Putting jokes and proverbs in static jokes and proverbs hashtable with respective keys
        jokes.put("JA", "A clean house is the sign of a broken computer.");
        jokes.put("JB", "CAPS LOCK has been preventing login since 1980.");
        jokes.put("JC", "What do you call 8 hobbits? A hobbyte");
        jokes.put("JD", "I love the F5 key. It is so refreshing.");
        proverbs.put("PA", "I do not fear computers. I fear lack of them.");
        proverbs.put("PB", "The computer was born to solve problems that did not exist before.");
        proverbs.put("PC", "Computing is not about computers any more. It is about living.");
        proverbs.put("PD", "Man is still the most extraordinary computer of all.");
        mode_client.putIfAbsent(4545, "joke");
        mode_client.putIfAbsent(4546, "joke");
// initializing ds, buffer and request variables
        this.ds = ds;
        this.buffer = buffer;
        this.request = request;
    }
// Run method will run when start() is called
    public void run(){
// response variable contain the DatagramPacket
        DatagramPacket response = null;
// Converting byte[] to String
        String concat = (new String(buffer)); 
// Splitting and formatting the string to get new_name, uuid and port
        String new_name = concat.split(",")[0];
        String uuid = concat.split(",")[1];
        int port = Integer.parseInt(concat.split(",")[2].substring(0,4));
// Retrieving clietn_port from request variable
        int client_port = request.getPort();
        server_jokes.putIfAbsent(port, new Hashtable<>());
        server_proverbs.putIfAbsent(port, new Hashtable<>());
// Trying to send the Packet and returning error if not possible
        try {
// Simulating work by sleeping the worker thread
            Thread.sleep(40000);
// Requesting address
            InetAddress address = request.getAddress();
// Checking if the mode is joke and adding the shuffled list if empty to respective user to respective server
            if(mode_client.get(port).equals("joke")){
                shuffle_jokes = Collections.list(jokes.keys());
                Collections.shuffle(shuffle_jokes);
                server_jokes.get(port).putIfAbsent(uuid, shuffle_jokes);

                if(server_jokes.get(port).get(uuid).isEmpty()){
// When the shuffled list is empty message is sent to client
                    if(port==4546) out = "<S2>";
                    out += "\nJOKE CYCLE COMPLETED\n";
                    server_jokes.get(port).put(uuid, shuffle_jokes);
                }
// Calling the printstate function
                printState(new_name, port, server_jokes.get(port).get(uuid).remove(0));
// Creating the Datagram packet to sent to client
                response = new DatagramPacket(out.getBytes(), out.getBytes().length, address, client_port);
// Sending the response
                ds.send(response);
                out = "";
            }
// Checking if the mode is joke and adding the shuffled list if empty to respective user to respective server
            else if(mode_client.get(port).equals("proverb")){
                shuffle_proverbs = Collections.list(proverbs.keys());
                Collections.shuffle(shuffle_proverbs);
                server_proverbs.get(port).putIfAbsent(uuid, shuffle_proverbs);
                    
                if(server_proverbs.get(port).get(uuid).isEmpty()){
// When the shuffled list is empty message is sent to client
                    if(port==4546) out = "<S2>";
                    out += "\nPROVERB CYCLE COMPLETED\n";
                    server_proverbs.get(port).put(uuid, shuffle_proverbs);
                }
// Calling the printstate function
                printState(new_name, port, server_proverbs.get(port).get(uuid).remove(0));
// Creating the Datagram packet to sent to client
                response = new DatagramPacket(out.getBytes(), out.getBytes().length, address, client_port);
// Sending the response
                ds.send(response);
                out = "";
            }
// Catching the exception
        } catch (Exception ioe) {System.out.println(ioe);}
    }

// This static method will update the out variable which will be later sent to client
    static void printState(String name, int client_port, String shuffle) {
        try {
// When second server is connected <S2> is added
            if(client_port==4546) out = "<S2> ";
                out += shuffle + " " + name + ": ";
            if(mode_client.get(client_port).equals("joke")){
                out += jokes.get(shuffle);
            }
            else if(mode_client.get(client_port).equals("proverb")){
                out += proverbs.get(shuffle);
            }
        } catch(Exception ex) {
            out += "Failed!" + name;
        }
    }
}

// Serverlooper class
class ServerLooper implements Runnable {
// If this boolean value is true we can switch the server
    public static boolean ServerSwitch = true;
// RuNning the Admin listen loop
    public void run(){
        int q_len = 6; 
// buffer will contain the response received by client
        byte[] buffer = new byte[100];
// request contains client information
        DatagramPacket request = null;
  
        try{
            DatagramSocket ds = new DatagramSocket(4546);
            ServerSocket servsock_admin = new ServerSocket(5051, q_len);
            new AdminWorker(servsock_admin).start();
            while (ServerSwitch) {
// wait for the next ADMIN client connection:
                request = new DatagramPacket(buffer, buffer.length);
                ds.receive(request);
                new ServerWorker(ds, request, buffer).start();
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
}


// AsyncJokeServer class
public class AsyncJokeServer {
// Main method
    public static void main(String args[]) throws Exception {
// Socket used to send UDP packet
        DatagramSocket ds = new DatagramSocket(4545);
// buffer will contain the response received by client
        byte[] buffer = new byte[100];
        int q_len = 6;  
        System.out.println("Vatsal Parikh's Async Joke Server is starting up, listening at port 4545.\n");
        DatagramPacket request = null;
// If the argument "secondary" is added then Serverlooper is called 
        if(args.length>0){
            if(args[0].contains("secondary")){
                System.out.println("Vatsal Parikh's Async Joke Server 2 is starting up, listening at port 4546.\n");
                ServerLooper SL = new ServerLooper();
                Thread t = new Thread(SL);
                t.start();              
            }          
        }           
// Calling AdminWorker with server socket
        ServerSocket servsock_admin = new ServerSocket(5050, q_len);
        new AdminWorker(servsock_admin).start();
            while(true){
// Accepting requests from clients
                request = new DatagramPacket(buffer, buffer.length);
                ds.receive(request);
                new ServerWorker(ds, request, buffer).start();
            } 
    }
}
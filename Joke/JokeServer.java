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
// sock contains the current client connection
    Socket sock;
// Defining jokes and proverbs
    static Hashtable<String, String> jokes = new Hashtable<String, String>();
    static Hashtable<String, String> proverbs = new Hashtable<String, String>();
// According to server used and user accessing the server, user's joke/proverb sequence will be stored here
    static Hashtable<Integer, Hashtable<String, ArrayList<String>>> server_jokes = new Hashtable<Integer, Hashtable<String, ArrayList<String>>>();
    static Hashtable<Integer, Hashtable<String, ArrayList<String>>> server_proverbs = new Hashtable<Integer, Hashtable<String, ArrayList<String>>>();
// This will contain the shuffle used to randomize the joke sequence
    ArrayList<String> shuffle_jokes;
    ArrayList<String> shuffle_proverbs;
// Worker Constructor
    ServerWorker (Socket s) {
// Putting jokes and proverbs in static jokes and proverbs hashtable with respective keys
        jokes.put("JA", "A clean house is the sign of a broken computer.");
        jokes.put("JB", "CAPS LOCK has been preventing login since 1980.");
        jokes.put("JC", "What do you call 8 hobbits? A hobbyte");
        jokes.put("JD", "I love the F5 key. It is so refreshing.");
        proverbs.put("PA", "I do not fear computers. I fear lack of them.");
        proverbs.put("PB", "The computer was born to solve problems that did not exist before.");
        proverbs.put("PC", "Computing is not about computers any more. It is about living.");
        proverbs.put("PD", "Man is still the most extraordinary computer of all.");
        sock = s;
        mode_client.putIfAbsent(4545, "joke");
        mode_client.putIfAbsent(4546, "joke");
    }
// Run method will run when start() is called
    public void run(){
        int client_port = sock.getLocalPort();
        server_jokes.putIfAbsent(client_port, new Hashtable<>());
        server_proverbs.putIfAbsent(client_port, new Hashtable<>());
// Defining I/O streams in/out to null
        PrintStream out = null;
        BufferedReader in = null;
// Trying to assign in/out to socket's(client) Input/Output stream and returning error if not possible
        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());
 // Trying to read client request and if it cannot be read returning an error
            try {
// Reading client request with the help of in
                String new_name = in.readLine(); 
                String uuid = in.readLine();
// Checking if the mode is joke and adding the shuffled list if empty to respective user to respective server
                if(mode_client.get(client_port).equals("joke")){
                    shuffle_jokes = Collections.list(jokes.keys());
                    Collections.shuffle(shuffle_jokes);
                    server_jokes.get(client_port).putIfAbsent(uuid, shuffle_jokes);

                    if(server_jokes.get(client_port).get(uuid).isEmpty()){
// When the shuffled list is empty message is sent to client
                        if(client_port==4546) out.print("<S2>");
                        out.println("JOKE CYCLE COMPLETED");
                        server_jokes.get(client_port).put(uuid, shuffle_jokes);
                    }
// Calling the printstate function
                    printState(new_name, out, client_port, server_jokes.get(client_port).get(uuid).remove(0));
                }
// Checking if the mode is joke and adding the shuffled list if empty to respective user to respective server
                else if(mode_client.get(client_port).equals("proverb")){
                    shuffle_proverbs = Collections.list(proverbs.keys());
                    Collections.shuffle(shuffle_proverbs);
                    server_proverbs.get(client_port).putIfAbsent(uuid, shuffle_proverbs);
                    
                    if(server_proverbs.get(client_port).get(uuid).isEmpty()){
// When the shuffled list is empty message is sent to client
                        if(client_port==4546) out.print("<S2>");
                        out.println("PROVERB CYCLE COMPLETED");
                        server_proverbs.get(client_port).put(uuid, shuffle_proverbs);
                    }
// Calling the printstate function
                    printState(new_name, out, client_port, server_proverbs.get(client_port).get(uuid).remove(0));
                }
                
            } catch (IOException x) {
                System.out.println("Server read error");
                x.printStackTrace ();
            }
// Closing the connection with client, though server is still running
            sock.close();
        } catch (IOException ioe) {System.out.println(ioe);}
    }
// This static method will print the output on client side with the help of out
    static void printState(String name, PrintStream out, int client_port, String shuffle) {
        try {
// When second server is connected <S2> is added
            if(client_port==4546) out.print("<S2> ");
                out.print(shuffle + " " + name + ": ");
            if(mode_client.get(client_port).equals("joke")){
                out.println(jokes.get(shuffle));
            }
            else if(mode_client.get(client_port).equals("proverb")){
                out.println(proverbs.get(shuffle));
            }
        } catch(Exception ex) {
            out.println ("Failed!" + name);
        }
    }
}


class ServerLooper implements Runnable {
    public static boolean ServerSwitch = true;

    public void run(){ // RUNning the Admin listen loop
      
        int q_len = 6; 
        int port = 4546;  
        Socket sock;
  
        try{
            ServerSocket servsock_admin = new ServerSocket(5051, q_len);
            new AdminWorker(servsock_admin).start();
            ServerSocket servsock_client = new ServerSocket(port, q_len);
            while (ServerSwitch) {
      // wait for the next ADMIN client connection:
                sock = servsock_client.accept();
                 new ServerWorker(sock).start(); 
            }
        }catch (IOException ioe){
            System.out.println(ioe);
        }
    }
}


// Server class
public class JokeServer {
// Main method
    public static void main(String args[]) throws IOException {
// Number of request at a time is stored in a queue of length 6
        int q_len = 6;
        Socket sock;
// Creating a server socket
        InetAddress bindAddress = InetAddress.getByName("127.0.0.1");
        ServerSocket servsock_client = new ServerSocket(4545, q_len, bindAddress);
        System.out.println("Vatsal Parikh's Joke Server is starting up, listening at port 4545.\n");
// If the argument "secondary" is added then Serverlooper is called 
        if(args.length>0){
            if(args[0].contains("secondary")){
                System.out.println("Vatsal Parikh's Joke Server 2 is starting up, listening at port 4546.\n");
                ServerLooper SL = new ServerLooper();
                Thread t = new Thread(SL);
                t.start();              
            }          
        }
// Calling AdminWorker with server socket
        ServerSocket servsock_admin = new ServerSocket(5050, q_len, bindAddress);
        new AdminWorker(servsock_admin).start();
// Waiting for a client connection
        while (true) {
// Accepting requests from clients
            sock = servsock_client.accept();
            new ServerWorker(sock).start();
        }
    }
}
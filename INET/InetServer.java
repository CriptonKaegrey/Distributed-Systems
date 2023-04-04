// Importing Java Input Output and networking libraries
import java.io.*;
import java.net.*;

// Worker class
class Worker extends Thread {
// sock contains the current client connection
    Socket sock;
// Worker Constructor
    Worker (Socket s) {sock = s;}
// Run method will run when start() is called
    public void run(){
// Defining I/O streams in/out to null
    PrintStream out = null;
    BufferedReader in = null;
// Trying to assign in/out to socket's(client) Input/Output stream and returning error if not possible
        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());
 // Trying to read client request and if it cannot be read returning an error
            try {
                String name;
// Reading client request with the help of in
                name = in.readLine ();
                System.out.println("Looking up " + name);
                printRemoteAddress(name, out);
            } catch (IOException x) {
                System.out.println("Server read error");
                x.printStackTrace ();
            }
// Closing the connection with client, though server is still running
            sock.close();
        } catch (IOException ioe) {System.out.println(ioe);}
    }
// This static method will print the output on client side with the help of out
    static void printRemoteAddress (String name, PrintStream out) {
        try {
            out.println("Looking up " + name + "...");
            InetAddress machine = InetAddress.getByName (name);
            out.println("Host name : " + machine.getHostName ());
            out.println("Host IP : " + toText (machine.getAddress ()));
        } catch(UnknownHostException ex) {
            out.println ("Failed in atempt to look up " + name);
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

// Server class
public class InetServer {
// Main method
    public static void main(String a[]) throws IOException {
// Number of request at a time is stored in a queue of length 6
        int q_len = 6;
        int port = 1565;
        Socket sock;
// Creating a server socket
        ServerSocket servsock = new ServerSocket(port, q_len);
 
        System.out.println("Vatsal Parikh's Inet Server is starting up, listening at port 1565.\n");
// Waiting for a client connection
        while (true) {
// sock will contain the client connection
            sock = servsock.accept();
// Starting the thread(worker) to handle the request
            new Worker(sock).start();
        }
    }
}
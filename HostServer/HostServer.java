// Importing I/O and networking libraries
  import java.io.BufferedReader;
  import java.io.IOException;
  import java.io.InputStreamReader;
  import java.io.PrintStream;
  import java.net.ServerSocket;
  import java.net.Socket;

// AgentWorker
  class AgentWorker extends Thread {
// sock contains the current client connection
    Socket sock;
// The state of the agent is maintained using parentAgentHolder
    agentHolder parentAgentHolder;
// The port used for connection
    int localPort;
  
// AgentWorker constructor used to initialize Socket, Port and AgentHolder
    AgentWorker (Socket s, int prt, agentHolder ah) {
      sock = s;
      localPort = prt;
      parentAgentHolder = ah;
    }

// run method is called when thread is initiaized by Worker
    public void run() {
// Defining I/O streams in/out to null
      PrintStream out = null;
      BufferedReader in = null;
// server name is stored in NewHost
      String NewHost = "localhost";
// This port is used by the worker
      int NewHostMainPort = 4242;	
// Used to store string sent by server	
      String buf = "";
// newPort is used to store the new port sent by server
      int newPort;
// clientSock is used to store the Socket info
      Socket clientSock;
// Initializing fromHostServer and toHostServer to print and read with server
      BufferedReader fromHostServer;
      PrintStream toHostServer;
// Try block      
      try {
// Trying to assign in/out to socket's(client) Input/Output stream and returning error if not possible
        out = new PrintStream(sock.getOutputStream());
        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//Storing the line read by client to inLine for further processing
        String inLine = in.readLine();
// Building the html response to determine the proper length in htmlString variable
        StringBuilder htmlString = new StringBuilder();
// Printing out the html response
        System.out.println();
        System.out.println("Request line: " + inLine);
// Checking if the reponse contains migrate in it   
        if(inLine.indexOf("migrate") > -1) {
// Switching the user if the response contains migrate and changing it to a new port
      
// This clientSock will be used with the main server
      clientSock = new Socket(NewHost, NewHostMainPort);
// Initializing fromHostServer to receive response from Server
      fromHostServer = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
// toHostServer will be used to send request to Server
      toHostServer = new PrintStream(clientSock.getOutputStream());
// Sending the request to server to find the next available port
      toHostServer.println("Please host me. Send my port! [State=" + parentAgentHolder.agentState + "]");
      toHostServer.flush();
      
// Waiting for the server response and then storing it in buf variable
      for(;;) {
// Storing the response in buf
        buf = fromHostServer.readLine();
// Checking if the response contains the new port
        if(buf.indexOf("[Port=") > -1) {
          break;
        }
      }
      
// Manipulating the response to extract the port number in string format
      String tempbuf = buf.substring( buf.indexOf("[Port=")+6, buf.indexOf("]", buf.indexOf("[Port=")) );
// Parsing the string to int
      newPort = Integer.parseInt(tempbuf);
// Printing out the new port number to console
      System.out.println("newPort is: " + newPort);
      
// Preparing the reponse in html form to send to the client
      htmlString.append(AgentListener.sendHTMLheader(newPort, NewHost, inLine));
// In the form of html reponse letting the client know about migrating
      htmlString.append("<h3>We are migrating to host " + newPort + "</h3> \n");
      htmlString.append("<h3>View the source of this page to see how the client is informed of the new location.</h3> \n");
// Ending the html
      htmlString.append(AgentListener.sendHTMLsubmit());
// Printing that we're killing the server at port
      System.out.println("Killing parent listening loop.");
// The old port contains socket, trying to grab it
      ServerSocket ss = parentAgentHolder.sock;
// Closing port
      ss.close();
// Checking if the response contains person
      } else if(inLine.indexOf("person") > -1) {
// If it does then increasing the state number stored in agentState variable
      parentAgentHolder.agentState++;
// Sending back the html in header to client which contains updated values of agentState and form
      htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
// Letting the client know state number
      htmlString.append("<h3>We are having a conversation with state   " + parentAgentHolder.agentState + "</h3>\n");
// Ending the html
      htmlString.append(AgentListener.sendHTMLsubmit());
// If the input does not contains a valid reponse it goes in else block      
        } else {
// Letting the user know about the invalid request in html format
      htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
      htmlString.append("You have not entered a valid request!\n");
// Ending the html
      htmlString.append(AgentListener.sendHTMLsubmit());		
        }
// Sending the html to client
        AgentListener.sendHTMLtoStream(htmlString.toString(), out);
// Closing the socket
        sock.close();
// Catching any exception
      } catch (IOException ioe) {
// Printing out the exception
        System.out.println(ioe);
      }
    }   
  }
  
// agentHolder class
// It holds the socket and agent state information
  class agentHolder {
// serversocket variable for storing the socket of server
    ServerSocket sock;
// agentState varible to store the state of agent
    int agentState;
    
// Constructor for agentHolder class
    agentHolder(ServerSocket s) { sock = s;}
  }

// AgentListener class
// It maintains conversation with every ports and Hostserver uses this class when new requests are made
  class AgentListener extends Thread {
// sock contains the current client connection
    Socket sock;
// localPort contains the port number of the current object
    int localPort;
    
// Constructor for AgentListener class
    AgentListener(Socket As, int prt) {
      sock = As;
      localPort = prt;
    }

// The default agent state will always be 0 and is stored in agentState
    int agentState = 0;
    
// run method is called when thread is initiaized by Worker
    public void run() {
// Defining I/O streams in/out to null
    PrintStream out = null;
    BufferedReader in = null;
// server name is stored in NewHost
    String NewHost = "localhost";
// Printing out acknowledgement of inside the thread
      System.out.println("In AgentListener Thread");		
// Try block
      try {
        String buf;
// Trying to assign in/out to socket's(client) Input/Output stream and returning error if not possible
        out = new PrintStream(sock.getOutputStream());
        in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));
// Used to store string sent by server	
        buf = in.readLine();    
// Checking if buf variable contains the state
        if(buf != null && buf.indexOf("[State=") > -1) {
// Manipulating the response to extract the state number in string format
      String tempbuf = buf.substring(buf.indexOf("[State=")+7, buf.indexOf("]", buf.indexOf("[State=")));
// Parsing the string to int
      agentState = Integer.parseInt(tempbuf);
// Printing out the state number to console
      System.out.println("agentState is: " + agentState);
        }
// Printing out the response sent by server        
        System.out.println(buf);
// Building the html response to determine the proper length in htmlString variable
        StringBuilder htmlResponse = new StringBuilder();
// sending the response to client in html form
// Letting the client about being in AgentListener class and port number in html form
        htmlResponse.append(sendHTMLheader(localPort, NewHost, buf));
        htmlResponse.append("Now in Agent Looper starting Agent Listening Loop\n<br />\n");
        htmlResponse.append("[Port="+localPort+"]<br/>\n");
// Ending the html
        htmlResponse.append(sendHTMLsubmit());
// Sending the html to client
        sendHTMLtoStream(htmlResponse.toString(), out);     

// Starting the connection at localPort
        ServerSocket servsock = new ServerSocket(localPort,2);
// Creating agentholder object and storing socket and agent state in instance variable
        agentHolder agenthold = new agentHolder(servsock);
        agenthold.agentState = agentState;
        
// Waiting for a client connection
      while(true) {
// sock will contain the client connection
      sock = servsock.accept();
// Acknoledging about receiving a connection request
      System.out.println("Got a connection to agent at port " + localPort);
// Starting the thread(worker) to handle the request
      new AgentWorker(sock, localPort, agenthold).start();
        }
// Catching any exceptions while connecting or switching ports
      } catch(IOException ioe) {
// Printing it out on the console to let us know the port and exact exception
        System.out.println("Either connection failed, or just killed listener loop for agent at port " + localPort);
        System.out.println(ioe);
      }
    }

// sendHTMLHeader method
// Used to create the header of response in html form
    static String sendHTMLheader(int localPort, String NewHost, String inLine) {
// Creating an empty StringBuilder
      StringBuilder htmlString = new StringBuilder();
// Creating the html header reponse using localPort, NewHost and inLine variables passed in arguments
      htmlString.append("<html><head> </head><body>\n");
      htmlString.append("<h2>This is for submission to PORT " + localPort + " on " + NewHost + "</h2>\n");
      htmlString.append("<h3>You sent: "+ inLine + "</h3>");
      htmlString.append("\n<form method=\"GET\" action=\"http://" + NewHost +":" + localPort + "\">\n");
      htmlString.append("Enter text or <i>migrate</i>:");
      htmlString.append("\n<input type=\"text\" name=\"person\" size=\"20\" value=\"YourTextInput\" /> <p>\n");
// Returning the htmlString variable
      return htmlString.toString();
    }

// sendHTMLsubmit mwthod
// Used to finish the html response
    static String sendHTMLsubmit() {
      return "<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n";
    }
    
// sendHTMLtoStream method
// Used to send the html response with proper length so it doesn't create any issues    
    static void sendHTMLtoStream(String html, PrintStream out) {
// Sending the reponse to client with html response length
      out.println("HTTP/1.1 200 OK");
      out.println("Content-Length: " + html.length());
      out.println("Content-Type: text/html");
      out.println("");		
      out.println(html);
    }
  }

// HostServer class
// It is used to listen to port 4242 and incrementing NextPort variable for each request
// and starts new listener for each port. It also assumes that all the ports above 3000 are free
  public class HostServer {
// Listening at port 3000
    public static int NextPort = 3000;
// Main method    
    public static void main(String[] a) throws IOException {
// Number of request at a time is stored in a queue of length 6
      int q_len = 6;
// Port number and socket intialization
      int port = 4242;
      Socket sock;
// Creating a server socket using port number and q_len      
      ServerSocket servsock = new ServerSocket(port, q_len);
      System.out.println("Elliott/Reagan DIA Master receiver started at port 4242.");
      System.out.println("Connect from 1 to 3 browsers using \"http:\\\\localhost:4242\"\n");
// Waiting for a client connection or migrating requests
      while(true) {
//incrementing port number with each connection
        NextPort = NextPort + 1;
// Accepting requests from clients
        sock = servsock.accept();
// Printing out to console
        System.out.println("Starting AgentListener at port " + NextPort);
// Starting the thread(worker) to handle the request
        new AgentListener(sock, NextPort).start();
      }  
    }
  }
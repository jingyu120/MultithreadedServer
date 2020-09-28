/*--------------------------------------------------------

1. Name / Date:
Justin Zhang 4/19/2020

2. Java version used, if not the official version for the class:

12.0.2

3. Precise command-line compilation examples / instructions:

navigate to directory containing this java file then run the following command to compile
> javac JokeServer.java


4. Precise examples / instructions to run this program:

navigate to directory containing this java file
enter the following command to run primary server
> java JokeServer

in a separate shell, enter the following command to run secondary server
> java JokeServer secondary

5. List of files needed for running the program.

 JokeServer.java
 JokeClient.java
 JokeClientAdmin.java

6. Notes:
Hashtable for jokes and proverbs will not clear even if user exits
Only way for it to clear is to restart server.
----------------------------------------------------------*/

/**
 * These statements below import
 * all modules/libraries/files required
 */
import java.io.*;
import java.net.*;
import java.util.*;


class AdminLooper implements Runnable {
    public static boolean adminControlSwitch = true;
    boolean primaryServers;
    AdminLooper(boolean primaryServer){
        /**
         * constructor setting boolean variable for primaryServer
         * if primaryServer is true AdminLooper will connect to primary server
         * if primaryServer is false AdminLooper will connect to secondary server
         */
        primaryServers = primaryServer;
    }

    public void run() {         //implementing run method from runnable interface
        System.out.println("In the admin looper thread");

        int q_len = 6;      /* maximum number of requests in the queue */
        int port;
        if (primaryServers){
            port = 5050;        //this is the port for primary server
        }
        else {
            port = 5051;        //this is the port for secondary server
        }

        Socket sock;        //instantiation socket object

        try {
            ServerSocket servsock = new ServerSocket(port, q_len);  //creating server socket from given port and queue
            while (adminControlSwitch) {        //infinite while loop does not turn off unless shell is closed
                // wait for the next ADMIN client connection:
                sock = servsock.accept();       //waiting for admin connection
                new AdminWorker(sock).start();      //once connected, run AdminWorker class
            }
        } catch (IOException ioe) {     //catching IO errors if there are any
            System.out.println(ioe);    //print out what the error is
        }
    }
}

class AdminWorker extends Thread {
    /**
     * AdminWorker is an extension of a thread
     * that is in the foreground and processes
     * non-primary work
     */
    Socket sock;            //instantiating socket object

    AdminWorker (Socket socket) {       //AdminWorker constructor with socket that has port number
        sock = socket;
    }

    public void run() {
        PrintStream out = null;     //this would establish connection to JokeClientAdmin
        BufferedReader in = null;   //input stream, this retrieves data from JokeClientAdmin

        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());

            try {
                String inputState = in.readLine();  //receiving input from JokeAdminClient and stored in variable

                if (inputState.indexOf("quit") < 0) {   //entering anything other than quit will toggle mode
                    JokeServer.mode = !JokeServer.mode;

                    if (JokeServer.mode){
                        out.println ("You're in joke mode");    //output to JokeClientAdmin
                        System.out.println("Enabled Joke Mode");    //print this text on JokeServer
                    }
                    else {
                        out.println ("You're in proverb mode");     //output to JokeClientAdmin
                        System.out.println("Enabled Proverb Mode"); //print this text on JokeServer
                    }
                }
                else if (inputState.indexOf("quit") > -1) { //input of quit is detected
                    out.println("exiting");     //output to JokeClientAdmin
                    System.out.println("Admin exited");     //print this on JokeServer
                }
                else {
                    out.println("Please enter valid input");    //just in case, probably won't happen
                }
            }
            catch (IOException x) {     //in case there's error
                System.out.println(x);  //print what the error is
            }
        }
        catch (IOException x) {         //in case there's error
            System.out.println(x);      //print what the error is
        }
    }
}

class Worker extends Thread {
    /**
     * Worker is an extension of a thread
     * that is in the foreground and processes
     * non-primary work
     */

    Socket sock;     // Create socket for allowing server use
    static String jCount;
    static String pCount;       //
    static String modeName;     //current mode (joke or proverb), formatting purpose
    static String userID;
    static int jokeCount;
    static int jokeIndex;
    static int proverbCount;
    static int proverbIndex;

    Worker(Socket s) {     // constructor that requires socket
        sock = s;
        if (JokeServer.mode) modeName = "joke";
        else modeName = "proverb";
    }

    public void run(){
        /**
         * Creates print stream and buffered reader
         * that utilizes socket to retrieve
         * I/O. If input, joke/proverb is sent to client.
         * Else, logs error and stack trace.
         */
        PrintStream out = null;
        BufferedReader in = null;

        try {
            in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream(), true);

            try {
                jCount = in.readLine();     //reading joke count from client, initially 0
                pCount = in.readLine();     //reading proverb count from client, initially 0
                userID = in.readLine();     //reading UUID from client

                System.out.println("Returning " + modeName);

                if (!JokeServer.jokeData.containsKey(userID)){
                    //if userID not in hashtable for jokes, create new entry for user
                    JokeServer.jokeData.put(userID, JokeServer.joke);
                }
                if (!JokeServer.proverbData.containsKey(userID)){
                    //if userID not in hashtable for proverbs, create new entry for user
                    JokeServer.proverbData.put(userID, JokeServer.proverb);
                }

                returnJoke(out, userID);        //invoking method and return joke to client

            } catch (IOException x) { // When reader does not have correct input, exception thrown
                System.out.println("Server read error");
                x.printStackTrace (); // Show cascading trail of what lead to error from stack
            }
            sock.close(); // close this connection, but not the server;
        } catch (IOException ioe) {System.out.println(ioe);} // if any errors thrown, print to console
    }

    static void returnJoke(PrintStream out, String userID) {
        /**
         * When name matches in machine, print details to log.
         * If host not found, error logged instead.
         */
        String jokeText = "";        //string containing retrieved joke
        String proverbText = "";     //string containing retrieved proverb

        out.println("Looking up " + modeName + "...");

        if (JokeServer.mode){
            jokeCount = Integer.parseInt(jCount);   //parsing String jCount from client into integer
            jokeIndex = jokeCount % JokeServer.sizeJoke;    //calculating index using modulus

            if (jokeIndex == 0 & jokeCount != 0) {  //shuffling jokes after all jokes are looped through

                //last joke before shuffling
                String oldJoke = JokeServer.jokeData.get(userID).get(JokeServer.sizeJoke-1);
                //the first joke after shuffling
                String newJoke;
                do {
                    //shuffling jokes in hashtable by userID
                    Collections.shuffle(JokeServer.jokeData.get(userID));
                    newJoke = JokeServer.jokeData.get(userID).get(0);   //retrieving first joke after shuffle
                }
                while (oldJoke == newJoke); //shuffle again if last joke prior to shuffle is the same as
                                            //the first joke after shuffle. so we don't get the same joke in a row

            }

            //depending on if server is on primary or secondary, joke string is formatted differently
            if (JokeServer.primaryServer){
                jokeText =  JokeServer.jokeData.get(userID).get(jokeIndex);
            }
            else {
                jokeText = "<S2> " + JokeServer.jokeData.get(userID).get(jokeIndex);
            }

            out.println(jokeText);  //output joke to client

            jokeCount += 1;     //increase joke count by 1
            jCount = Integer.toString(jokeCount);   //new joke count, will be sent to client
        }

        else {
            proverbCount = Integer.parseInt(pCount);    //parsing String jCount from client into integer
            proverbIndex = proverbCount % JokeServer.sizeProverb;  //calculating index using modulus


            if (proverbIndex == 0 & proverbCount != 0) {    //shuffling proverbs after all proverbs are looped through

                //last proverb before shuffling
                String oldProverb = JokeServer.proverbData.get(userID).get(JokeServer.sizeProverb - 1);
                //the first proverb after shuffling
                String newProverb;
                do {
                    //shuffling proverbs in hashtable by userID
                    Collections.shuffle(JokeServer.proverbData.get(userID));
                    newProverb = JokeServer.proverbData.get(userID).get(0);     //retrieving first proverb after shuffle
                }
                while (oldProverb == newProverb);   //shuffle again if last proverb prior to shuffle is the same as
                //the first proverb after shuffle. so we don't get the same proverb in a row
            }
            //depending on if server is on primary or secondary, proverb string is formatted differently
            if (JokeServer.primaryServer) {
                proverbText = JokeServer.proverbData.get(userID).get(proverbIndex);

            } else {
                proverbText = "<S2> " + JokeServer.proverbData.get(userID).get(proverbIndex);
            }
            out.println(proverbText);   //output proverb to client

            proverbCount += 1;      //increase proverb count by 1
            pCount = Integer.toString(proverbCount);    //new proverb count, will be sent to client
        }
        out.println(jCount);    //sending new joke count to client
        out.println(pCount);    //sending new proverb count to client
    }
}
public class JokeServer {
    /**
     * Utilizes Inet to create server by
     * hosting socket. Output's start-up message
     * and is available on local host of port 1565
     * @throws IOException if input incorrect, exception thrown
     */
    static boolean mode = true;     //true is joke mode, false is proverb mode. Default is joke mode
    static boolean primaryServer = true;    //when false the server is in secondary mode
    static HashMap<String, List<String>> jokeData = new HashMap<>();    //hashtable containing all jokes by userID
    static HashMap<String, List<String>> proverbData = new HashMap<>(); //hashtable containing all proverbs by userID

    //list of jokes to be added to jokeData when new user connected
    static List<String> joke = new ArrayList<>(List.of(
            "JA %s: What’s the best thing about Switzerland? I don’t know, but the flag is a big plus.",
            "JB %s: Did you hear about the mathematician who’s afraid of negative numbers? He’ll stop at nothing to avoid them.",
            "JC %s: Why do we tell actors to “break a leg? Because every play has a cast.",
            "JD %s: Helvetica and Times New Roman walk into a bar.     “Get out of here!”shouts the bartender. “We don’t serve your type.”")
    );

    //list of proverbs to be added to proverbData when new user connected
    static List<String> proverb = new ArrayList<>(List.of("PA %s: The pen is mightier than the sword.",
            "PB %s: When in Rome, do as the Romans.",
            "PC %s: Fortune favors the bold.",
            "PD %s: People who live in glass houses should not throw stones.")
    );

    static int sizeJoke = joke.size();          //size of joke list
    static int sizeProverb = proverb.size();    //size of proverb list

    public static void main(String args[]) throws IOException {
        if (args.length<1){     //no arguments given, default connect to primary server
            JokeServer.primaryServer = true;

            int q_len = 6;      // Maximum requests for OpSys to queue
            int port = 4545;    // Setting primary server port number
            Socket sock;        // instantiate variable for socket with no initial assignment

            //creating AdminLooper object for JokeClientAdmin
            AdminLooper adminLooper = new AdminLooper(true);
            Thread t = new Thread(adminLooper);
            t.start();

            ServerSocket servsock = new ServerSocket(port, q_len);      //Instantiate ServerSocket object
                                                        //Listens to and establishes connection with client object

            System.out.println
                    ("Justin Zhang's JokeServer starting up, listening at port 4545.\n");
            while (true) {      //Infinite while loop
                sock = servsock.accept();       // Waiting for client connection to server with given port number
                new Worker(sock).start();       // Allow worker to begin process of thread given connection request
            }
        }

        //when one of the argument contains secondary, connect to secondary server
        else if (args[0].indexOf("secondary") > -1){
            JokeServer.primaryServer = false;

            int q_len = 6;      // Maximum requests for OpSys to queue
            int port = 4546;    // Setting secondary server port number
            Socket sock;        // instantiate variable for socket with no initla assignment

            //creating AdminLooper object for JokeClientAdmin
            AdminLooper adminLooper = new AdminLooper(false);
            Thread t = new Thread(adminLooper);
            t.start();

            ServerSocket servsock = new ServerSocket(port, q_len);      //Instantiate ServerSocket object
                                                        //Listens to and establishes connection with client object

            System.out.println
                    ("Justin Zhang's Inet server 1.8 starting up, listening at port 4546.\n");
            while (true) {      //Infinite while loop
                sock = servsock.accept();       // Waiting for client connection to server with given port number
                new Worker(sock).start();       // Allow worker to begin process of thread given connection request
            }
        }


        else {      //if entered an argument without secondary in it
            System.out.println("enter valid argument");
        }

    }
}


/*--------------------------------------------------------

1. Name / Date:
Justin Zhang 4/19/2020

2. Java version used, if not the official version for the class:

12.0.2

3. Precise command-line compilation examples / instructions:

navigate to directory containing this java file then run the following command to compile
> javac JokeClient.java


4. Precise examples / instructions to run this program:

navigate to directory containing this java file
a. Enter the following command to enter default JokeClient
    default server name is "localhost" and port 4545
> java JokeClient

b. Enter the following command to enter JokeClient that can run across machines
    For local machine running at 192.168.86.246 you would type 192.168.86.246
    in place of <IPAddress>
> java JokeClient <IPAddress>

c. Enter the following command to enter JokeClient that can toggle to secondary server
    and run across machines. For local machine running at 192.168.86.246
    you would type 192.168.86.246 in place of <IPAddress>
> java JokeClient <IPAddress> <IPAddress>

After entering your name, pressing Enter to request for joke/proverb.
    Enter "s" to switch to toggle between primary and secondary server for (c).
    Enter "quit" to exit client.

5. List of files needed for running the program.

 JokeServer.java
 JokeClient.java
 JokeClientAdmin.java

6. Notes:
when toggling


----------------------------------------------------------*/
/**
 * These statements below import
 * all modules/libraries/files required
 */

import java.io.*;
import java.net.*;
import java.util.UUID;

public class JokeClient {
    /**
     *
     */
    static String jokeCount = "0";     //used in server side to index for jokes
    static String proverbCount = "0";   //used in server side to index for proverbs
    static String userID = UUID.randomUUID().toString();    //unique client ID, used as hashtable key
                                                            // to retrieve client state


    public static void main (String args[]) {
        String serverName = "";     //server name for primary server
        String serverName2 = "";    //server name for secondary server
        boolean primaryServer = true;   //if true, connect to primary server, else connect to secondary
        boolean multipleServer;     //indicate if client can connect to multiple server or not

        if (args.length < 1){   //when no parameter entered
            multipleServer = false;     //cannot connect to multiple server
            serverName = "localhost";  //Using local host as default when starting client

            System.out.println("Justin Zhang's Joke Client.\n");
            System.out.println("Using Server: " + serverName + ", Port: 4545");
        }
        else if (args.length == 1){     //when one argument entered
            multipleServer = false;     //cannot connect to multiple server
            serverName = args[0];       //Starting client with name or IP address of server machine

            System.out.println("Justin Zhang's Joke Client.\n");
            System.out.println("Using Server: " + serverName + ", Port: 4545");
            }
        else if (args.length == 2){     //when two arguments entered
            multipleServer = true;      //able to connect to secondary server as well as primary server
            serverName = args[0];       //Starting client with name or IP address of server machine
            serverName2 = args[1];      //Starting client with name or IP address of server machine

            System.out.println("Justin Zhang's Joke Client.\n");
            System.out.println("Using Server One: " + serverName + ", Port: 4545");
            System.out.println("Using Server Two: " + serverName2 + ", Port: 4546");
        }
        else {      // when more than two arguments entered
            System.out.println("Invalid input");
            return;
        }


        System.out.println("UUID: " + userID + "\n");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // initialization of buffer reader



        try { // try to establish connection with server
            String clientName;  //the name of client
            String text;        //entered text when prompted

            System.out.print("Enter your name: ");
            System.out.flush();
            clientName = in.readLine();     //prompting client to enter name

            do { // continuous execution of program until quit command executed
                System.out.print("Press Enter for a joke or proverb, type quit to exit: ");
                text = in.readLine();   //asking client for input

                if (multipleServer){    //executed if client can connect to secondary server
                    if (text.indexOf("s") == 0){    //when "s" entered, switch client from primary to secondary
                                                    //or from secondary to primary server
                        primaryServer = !primaryServer;     //toggling

                        String server;
                        String port;

                        if (primaryServer) {
                            server = serverName;    //primary server IP address or name
                            port = "4545";          //primary server port number
                        }
                        else {
                            server = serverName2;   //secondary server IP address or name
                            port = "4546";          //secondary server port number
                        }

                        System.out.println("Now communicating with: " + server + ", port " + port);
                    }
                    if (text.indexOf("quit") < 0) // retrive joke or proverb since "quit" not executed
                        retrieveResponse(clientName, serverName, primaryServer, serverName2);
                }
                else{       //executed if client cannot connect to secondary server
                    if (text.indexOf("quit") < 0) // retrieve joke or proverb since "quit" not executed
                        retrieveResponse(clientName, serverName);
                }
            } while (text.indexOf("quit") < 0); // quit command not found, repeat
            System.out.println ("Cancelled by user request."); // cancellation due to exit command
        } catch (IOException x) {x.printStackTrace ();} // errors caught and stack printed
    }

    static void retrieveResponse(String clientName, String serverName, boolean primaryServer, String serverName2){
        /**
         * Method utilizing connection with server to retrieve
         * joke or proverb from server. Allow client to retrieve joke
         * or proverb from primary or secondary server.
         */

        // initialization of variables for use
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String headerFromServer;
        String textFromServer;

        try{
            /* Open our connection to server port*/
            if (primaryServer){     //creating socket for connection with primary server
                sock = new Socket(serverName, 4545);
            }
            else {                  //creating socket for connection with secondary server
                sock = new Socket(serverName2, 4546);
            }

            /* Create buffered reader to retrieve output response from server */
            fromServer =
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Create stream for input request auto flush previous history and cache to prevent errors */
            toServer = new PrintStream(sock.getOutputStream(), true);

            toServer.println(jokeCount);        //sending current joke count to server
            toServer.println(proverbCount);     //sending current proverb count to server
            toServer.println(userID);           //sending UUID to server

            headerFromServer = fromServer.readLine();   //signal from server that it's looking up joke or proverb
            textFromServer = fromServer.readLine();     //retrieving joke or proverb text from server
            jokeCount = fromServer.readLine();          //retrieving from server and updating joke count
            proverbCount = fromServer.readLine();       //retrieving from server and updating proverb count

            System.out.println(headerFromServer);
            System.out.printf(textFromServer, clientName);
            System.out.println();;

            sock.close(); // close socket to finish response
        } catch (IOException x) { // catch errors if socket creates unexpected behavior
            System.out.println ("Socket error.");
            x.printStackTrace ();
        }
    }

    static void retrieveResponse(String clientName, String serverName){
        /**
         * Method utilizing connection with server to retrieve
         * joke or proverb from server. Only allows client
         * retrieve jokes or proverbs from primary server
         */

        // initialization of variables for use
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;
        String headerFromServer;
        String textFromServer;

        try{
            /* Open our connection to server port, constant int added for educational purposes */
            sock = new Socket(serverName, 4545);

            /* Create buffered reader to retrieve output response from server */
            fromServer =
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Create stream for input request auto flush previous history and cache to prevent errors */
            toServer = new PrintStream(sock.getOutputStream(), true);

            toServer.println(jokeCount);        //sending current joke count to server
            toServer.println(proverbCount);     //sending current proverb count to server
            toServer.println(userID);           //sending UUID to server

            headerFromServer = fromServer.readLine();   //signal from server that it's looking up joke or proverb
            textFromServer = fromServer.readLine();     //retrieving joke or proverb text from server
            jokeCount = fromServer.readLine();          //retrieving from server and updating joke count
            proverbCount = fromServer.readLine();       //retrieving from server and updating proverb count

            System.out.println(headerFromServer);
            System.out.printf(textFromServer, clientName);
            System.out.println();;

            sock.close(); // close socket to finish response
        } catch (IOException x) { // catch errors if socket creates unexpected behavior
            System.out.println ("Socket error.");
            x.printStackTrace ();
        }
    }
}
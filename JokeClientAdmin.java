/*--------------------------------------------------------

1. Name / Date:
Justin Zhang 4/19/2020

2. Java version used, if not the official version for the class:
12.0.2

3. Precise command-line compilation examples / instructions:
navigate to directory containing this java file then run the following command to compile
> javac JokeClientAdmin.java

4. Precise examples / instructions to run this program:

navigate to directory containing this java file
a. Enter the following command to enter default JokeClientAdmin
    default server name is "localhost" and port 5050
> java JokeClientAdmin

b. Enter the following command to enter JokeClientAdmin that can run across machines
    For local machine running at 192.168.86.246 you would type 192.168.86.246
    in place of <IPAddress>
> java JokeClientAdmin <IPAddress>

c. Enter the following command to enter JokeClientAdmin that can toggle to secondary server
    and run across machines. For local machine running at 192.168.86.246
    you would type 192.168.86.246 in place of <IPAddress>
> java JokeClientAdmin <IPAddress> <IPAddress>

Press enter to toggle server from joke to proverb or proverb to joke mode for (a) and (b).
    Enter "s" to switch to toggle between primary and secondary server for (c).
    Enter "quit" to exit client admin.

5. List of files needed for running the program.

 JokeServer.java
 JokeClient.java
 JokeClientAdmin.java

6. Notes:

----------------------------------------------------------*/


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class JokeClientAdmin
{

    public static void main (String [] args){
        String serverName = "";     //server name for primary server
        String serverName2 = "";    //server name for secondary server
        boolean primaryServer = true;   //if true, connect to primary server, else connect to secondary
        boolean multipleServer;     //indicate if client can connect to multiple server or not

        if (args.length < 1){   //when no parameter entered
            multipleServer = false;     //cannot connect to multiple server
            serverName = "localhost";  //Using local host as default when starting client

            System.out.println("This is Joke Client Admin.");
            System.out.println("Using server " + serverName + "at port 5050");
        }
        else if (args.length == 1){     //when one argument entered
            multipleServer = false;     //cannot connect to multiple server
            serverName = args[0];       //Starting client with name or IP address of server machine

            System.out.println("This is Joke Client Admin.");
            System.out.println("Using server " + serverName + "at port 5050");
        }
        else if (args.length == 2){     //when two arguments entered
            multipleServer = true;      //able to connect to secondary server as well as primary server
            serverName = args[0];       //Starting client with name or IP address of server machine
            serverName2 = args[1];      //Starting client with name or IP address of server machine

            System.out.println("Using Server One: " + serverName + ", Port: 5050");
            System.out.println("Using Server Two: " + serverName2 + ", Port: 5051");
        }
        else {  //when more than two arguments entered
            System.out.println("Invalid input");
            return;
        }


        BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // initialization of buffer reader


        try { // try to establish connection with server
            String inputMode;   //text entered when prompted. can be empty string
            do { // continuous execution of program until quit command executed
                System.out.print
                        ("Switch between joke and proverb mode by pressing enter, enter quit to exit: ");
                System.out.flush ();
                inputMode = in.readLine ();     //prompting admin to enter direction

                if (multipleServer){    //executed if client can connect to secondary server
                    if (inputMode.indexOf("s") == 0){    //when "s" entered, switch admin from primary to secondary
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

                    if (inputMode.indexOf("quit") < 0) //switch mode from proverb to joke or joke to proverb
                                                        //when "quit" not executed. primary and secondary server
                        getMode(inputMode, serverName, primaryServer, serverName2);
                }
                else{
                    if (inputMode.indexOf("quit") < 0) // locate remote address since "quit" not executed
                        getMode(inputMode, serverName);//switch mode from proverb to joke or joke to proverb
                                                        //when "quit" not executed. only for primary server
                }
            } while (inputMode.indexOf("quit") < 0); // quit command not found, repeat
            System.out.println ("Cancelled by user request."); // cancellation due to exit command
        } catch (IOException x) {x.printStackTrace ();} // errors caught and stack printed
    }
    static void getMode(String inputMode, String serverName, boolean primaryServer, String serverName2){
        /**
         * Method utilizing connection with server to toggle
         * primary server from joke to proverb or proverb to joke
         */

        // initialization of variables for use
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;

        try{
            if (primaryServer) {    //creating socket for connection with primary server
                sock = new Socket(serverName, 5050);
            }
            else {                  //creating socket for connection with secondary server
                sock = new Socket(serverName2, 5051);
            }


            /* Create buffered reader to retrieve output response from server */
            fromServer =
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Create stream for input request auto flush previous history and cache to prevent errors */
            toServer = new PrintStream(sock.getOutputStream(), true);
            /* Print name on server, flush previous history and cache to prevent errors */

            toServer.println(inputMode);    //tell server to switch mode or switch server
            System.out.println(fromServer.readLine());  //print text from server to indicate what mode we are in

            sock.close(); // close socket to finish response
        } catch (IOException x) { // catch errors if socket creates unexpected behavior
            System.out.println ("Socket errorsssss.");
            x.printStackTrace ();
        }
    }

    static void getMode(String inputMode, String serverName){
        /**
         * Method utilizing connection with server to toggle
         * primary or secondary server, from joke to proverb
         * or from proverb to joke
         */

        // initialization of variables for use
        Socket sock;
        BufferedReader fromServer;
        PrintStream toServer;

        try{
            sock = new Socket(serverName, 5050);    //creating socket for connection with primary server


            /* Create buffered reader to retrieve output response from server */
            fromServer =
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Create stream for input request auto flush previous history and cache to prevent errors */
            toServer = new PrintStream(sock.getOutputStream(), true);
            /* Print name on server, flush previous history and cache to prevent errors */

            toServer.println(inputMode);    //tell server to switch mode
            System.out.println(fromServer.readLine());  //print text from server to indicate what mode we are in

            sock.close(); // close socket to finish response
        } catch (IOException x) { // catch errors if socket creates unexpected behavior
            System.out.println ("Socket errorsssss.");
            x.printStackTrace ();
        }
    }

}

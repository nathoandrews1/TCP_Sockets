package TCP_PRACTICE;
import java.net.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author Nathan
 */
public class Server {
    private static ServerSocket serverSock;
    private static final int port = 1234;
    private static int clientConnections = 0;
    private static Map<String, ArrayList<String>> eventList = new HashMap<String,ArrayList<String>>();
    

    public static void main(String args[])
    {
        try
        {
            serverSock = new ServerSocket(port);
        }
        catch(IOException e)
        {
            System.out.println("Unable to start server!");
            System.exit(0);
        }

        //Added dates below, Notice the order of time is random input. But it will order dates by lowest to greatest time.
        //Check ServerActions class for logic

        //Use of static method so no need to instance
        ServerActions.add("add;7 October 2021;11 am;Meeting at work, Office;",eventList);
        ServerActions.add("add;7 October 2021;8 am;Meeting at work, Office;",eventList);
        ServerActions.add("add;7 October 2021;8 pm;Dance in town, Dublin;",eventList);
        ServerActions.add("add;7 October 2021;11 pm;Dance in town, Dublin;",eventList);
        ServerActions.add("add;7 October 2021;1 am;Dance in town, Dublin;",eventList);
        ServerActions.add("add;8 October 2021;10 am;Dance Cancelled in town, Dublin;",eventList);
        ServerActions.add("add;8 October 2021;10 am;Dance in town, Dublin;",eventList);
        ServerActions.add("add;8 October 2021;8 am;Go to work, Dublin;",eventList);
        ServerActions.add("add;8 October 2021;12 pm;Go to work, Dublin;",eventList);
        ServerActions.add("add;8 October 2021;12 am;Go to work, Dublin;",eventList);
        ServerActions.add("add;8 October 2021;8 pm;Go to work, Dublin;",eventList);
        ServerActions.add("add;8 October 2021;9 pm;Go to work, Dublin;",eventList);
        ServerActions.add("add;8 October 2021;11 pm;Go to work, Dublin;",eventList);
        ServerActions.add("add;8 october 2021;11 am;Go to work, Dublin;",eventList);
        do{
            run();
        }while(true);
    }

    private static void run()
    {
        System.out.println("Server Running");
        Socket link = null;
        try{
            link = serverSock.accept();
            if(link.isConnected())
            {
                System.out.println("\nConnection Accepted");
                
                clientConnections++;
                int clientId = clientConnections;

                //Creating thread below with our own ConnectionHandler class and passing that worker to that thread
                Runnable clientRun = new ConnectionHandler(link, clientId, eventList);
                Thread t = new Thread(clientRun);
                t.start();
            }
        }
        catch(IOException e)
        {
            try{
                System.out.println("Closing Connection");
                clientConnections--;
                System.out.println("Clients Connected " + clientConnections);
                link.close();
            }
            catch(IOException e2)
            {
                System.out.println("Error trying to close link");
                System.exit(0);
            }
        }
    }
}

package TCP_PRACTICE;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.*;


/**
 *
 * @author Nathan
 */
public class Client {
    private static int port = 1234;
    private static InetAddress ip;
    private static Socket link;
    public static void main(String args[])
    {
        try
        {
            ip = InetAddress.getLocalHost();           
        }
        catch(IOException e)
        {
            System.out.println("Error getting ip!");
            System.exit(1);
        }
        run();
    }

    private static void run()
    {
        try{
            link = new Socket(ip,port);

            if(link.isConnected())
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
                BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(link.getOutputStream(),true);

                String clientMsg = "";
                String response = "";

                //function made below to receive incoming transmissions.
                //Receives the greeting then moves onto echo output and detecting keywrods
                ReceiveTransmission(clientMsg,response,in,out);
                
                do{
                    clientMsg = userIn.readLine();
                    if(clientMsg.equalsIgnoreCase("stop"))
                    {
                        System.out.println("Stopped Connection to server");
                        out.println(clientMsg);
                        response = in.readLine();
                        System.out.println(response);
                        try{    
                            link.close();
                        }
                        catch(SocketException e)
                        {
                            System.out.println("Error closing, server closed connection already");
                        }

                        System.exit(0);
                    }
                    if(clientMsg.equalsIgnoreCase("list"))
                    {
                        out.println(clientMsg);
                        do{
                            response = in.readLine();
                            if(response.equalsIgnoreCase("break"))
                            {
                                System.out.println("\nEnd of List\n\nPlease enter a message to send to the server.");
                                break;
                            }
                            System.out.println(response);
                        }
                        while(response != null);
                    }
                    else {
                    //if no keyword is found then expect echo
                    out.println(clientMsg);
                    response = in.readLine();
                    System.out.println(response);
                    }
                }
                while(true);

            }
        }
        catch(IOException e)
        {

        }
    }

    private static void ReceiveTransmission(String message, String response, BufferedReader in, PrintWriter out)
    {
        do{
            try
            {
            response = in.readLine();

            if(response.equalsIgnoreCase("break"))
            {
                System.out.println("\nEnd of Server Message\n");
                System.out.println("Please type a message to send to the server");
                break;
            }
            System.out.println(response);
            }
            catch(IOException e)
            {
                System.out.println("Error receiving transmission from server!");
            }
            
        }while(response != null);
    }
}

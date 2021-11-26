package TCP_PRACTICE;
import java.net.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author Nathan
 */
public class ServerActions {
    
    private Socket link = null;
    private BufferedReader clientInput;
    private PrintWriter clientOut;
    private static Map<String, ArrayList<String>> eventList = new HashMap<String,ArrayList<String>>();
    private int id;
    
    public ServerActions(Socket link, BufferedReader in, PrintWriter out, int inId)  {
        
        getLink(link, in, out, inId);
    }

    //This function accepts a HashMap that accepts a String as Key and ArrayList then for the value.
    //That way we can index the dates with the hashmap AND also use ArrayList. Online it was referenced as a Triple Index Hash
    public void eventListener(String input, Map<String, ArrayList<String>> inputMap)
    {
        //Doing study online I found some good level guys where recommending doing this if with return, as else if is strict control
        //and if and return allows this top down approach and I feel is more natural
        if(input.equalsIgnoreCase("list"))
        {
            try{
                listEvents(inputMap);
                return;
            }
            catch(RuntimeException e)
            {
                throw new IncorrectActionException(e, "Error in list action!");
            }
        }
        if(input.equalsIgnoreCase("stop"))
        {
            try{    
                System.out.println("Client stopped connection, shutting down");
                clientOut.println("SERVER MESSAGE: TERMINATED CONNECTION");
                clientOut.close();
                link.close();
                return;
            }
            catch(IOException e)
            {
                System.out.println("Could not close connection!");
                System.exit(1);
                return;
            }
        }
        if(input.equalsIgnoreCase("empty"))
        {
            try{
                clientOut.println("Emptied the list");
                inputMap.clear();
                return;
            }
            catch(RuntimeException e)
            {
                throw new IncorrectActionException(e, "Error emptying list ServerAction!");
            }
        }
        ArrayList<String> clientInput = new ArrayList<>(Arrays.asList(input.split(";")));

        //If input not equal to, keyword,date,time,description return
        if(clientInput.size() != 4)
        {
            System.out.println("Message from Client " + id + " " + input);
            clientOut.println("Echo Message " + input);
        }
        else if(clientInput.size() == 4){
            //Else check 
            String keyword = clientInput.get(0).toLowerCase();
            if(keyword.equals("add"))
            {
                addEvent(input, inputMap);
            }
            else if(keyword.equals("remove"))
            {
                removeEvent(input, inputMap);
            }
        }
        
    }
    private void removeEvent(String input, Map<String, ArrayList<String>> inputMap)
    {
        ArrayList<String> clientInput = new ArrayList<>(Arrays.asList(input.split(";")));
        clientInput.remove(0); //Remove keyword 
        String inDate = clientInput.get(0).toUpperCase();
        String inDescript = clientInput.get(2);
        int inputTime = parseTime(clientInput.get(1));
        clientInput.clear();
        
        if(inputMap.containsKey(inDate))
        {            
            ArrayList<String> value = inputMap.get(inDate);

                for(int i = 0; i < value.size(); i+=2)
                {
                    int currentTime = parseTime(value.get(i));
                    String currentDescript = value.get((i + 1));

                    if(currentTime == inputTime && currentDescript.equals(inDescript))
                    {
                        
                        inputMap.get(inDate).remove(i);
                        inputMap.get(inDate).remove(i);
                        clientOut.println("[DATE REMOVED] Type Another Message below, events listed: " + inDate + " " + inputMap.get(inDate));
                        break;
                    }
                }
        }
        else
        {
            clientOut.println("No date found");
        }
    }

    //Add event for server, adding from lowest to greatest so easy to sort.
    private void addEvent(String input, Map<String,ArrayList<String>> inputMap){
        ArrayList<String> clientInput = new ArrayList<>(Arrays.asList(input.split(";")));

        clientInput.remove(0); //Remove add keyword 
        //ArrayList index is now date, currentTime, description
        String date = clientInput.get(0);
        date = date.toUpperCase();
        //Removing date as we have it stored instead of using multple currentTimes below
        clientInput.remove(0);
        //Parse for currentTime that was input
        int inputTime = parseTime(clientInput.get(0));
        
        if(inputMap.containsKey(date))
        {            

            //Here we grab the ArrayList with the date and increment over it's values. i+=2 to next time value as we order
            ArrayList<String> value = inputMap.get(date);
                
                //Since values are sorted on the way in, we can always use the check ahead and check end to continue sortnng.
                    for(int i = 0; i<value.size();i+=2)
                    {
                        try
                        {
                            //This function sorts the time in 24 hour mode to compare.
                            int currentTime = parseTime(value.get(i));

                            //-2 because description is at usual -1
                            int endTime = parseTime(value.get((value.size() - 2)));

                            //If input > parsedTime which is 1 to 24 and array has more than 1 value
                            if(inputTime > currentTime && value.size() > 2)
                            {
                                //Get the value ahead and currentTime, +2 because we skip the description of the previous.
                                int aheadIndex = parseTime(value.get(i+2));

                                //Check if inputTime is greaater than the value ahead of i AND less than the value at the end 
                                if(inputTime > endTime)
                                {
                                    //Adding Time and Description to the end.
                                    inputMap.get(date).add((value.size() - 1), clientInput.get(0));
                                    inputMap.get(date).add((value.size() - 2), clientInput.get(1));
                                    clientOut.println(date + " " + inputMap.get(date));
                                    break;
                                }
                                //Because we use less than, times that are the same will stack accordingly
                                if(inputTime < aheadIndex)
                                {
                                    inputMap.get(date).add((i+2), clientInput.get(0));
                                    inputMap.get(date).add((i+3), clientInput.get(1));
                                    clientOut.println(date + " " + inputMap.get(date));
                                    break;
                                }
                            }
                            else 
                            {
                                inputMap.get(date).add(i, clientInput.get(0));
                                inputMap.get(date).add((i+1), clientInput.get(1));
                                clientOut.println(date + " " + inputMap.get(date));
                                break;
                            }
                        }
                        catch(ArrayIndexOutOfBoundsException | NumberFormatException e)
                        {
                            clientOut.println("Error parsing for currentTime, OR String was NaN");
                            break;
                        }
                    }
        }
        else{
            //If not already contained, create an empty data set with the date and add the append the values as like above indexing currentTime
            inputMap.put(date, new ArrayList<>());
            inputMap.get(date).add(clientInput.get(0));
            inputMap.get(date).add(clientInput.get(1));
            clientOut.println(date + " " + inputMap.get(date)+ " ||New Unique Date Added, Type new message below!||");
        }
    }
    

    //Create function to parse currentTime, if PM add 12 to make 24 hours. This way we can then compare integer value for currentTime
    private static int parseTime(String inputTime){
        inputTime = inputTime.toLowerCase();
        if(inputTime.contains("pm"))
        {
            //Using regex here \s is whitesapce [pm] character set, so splits 6, " pm" used for adding to eventList and split
            //to int for time operation
            try 
            {
                int sortedTime = (int)Integer.parseInt(inputTime.split("\\s[pm]")[0]);
                //This is to change 12pm to correct value as would be 24 which is 12am. Vice versa below
                if(sortedTime == 12)
                {
                    return sortedTime;
                }
                else {
                    return sortedTime += 12;
                }
            }
            catch(NumberFormatException e)
            {
                System.out.println("Error parsing time");
                return 0;
            }
        }
        //Vice versa of top
        else if(inputTime.contains("am"))
        {
            int sortedTime = (int)Integer.parseInt(inputTime.split("\\s[am]")[0]);

            if(sortedTime == 12)
            {
                sortedTime = 24;
            }
            return sortedTime;
        }
        else
        {
            return 0;
        }
    }


    //Not needed but I was experimenting with receiving multiple inputs for just one side
    private void listEvents(Map<String, ArrayList<String>> inputMap)
    {
        if(inputMap.size() == 0)
        {
            clientOut.println("List empty, please add to your list.");
            endComm();
            return;
        }
        for(Map.Entry<String, ArrayList<String>> key : inputMap.entrySet())
        {   
                ArrayList values = key.getValue();
                for(int i = 0; i < values.size() - 1; i+=2)
                {
                    if(i == 0)
                    {
                        clientOut.println("\nShowing events for: " + key.getKey() + "\n");

                    }
                    clientOut.println(values.get(i) + " " + values.get(i + 1));
                }
                clientOut.println("<<END OF EVENTS FOR THIS DATE>>\n");
        }
        endComm();
    }

    //Not used
    private void listAll(Map<String, ArrayList<String>> input){
        for(Map.Entry key : input.entrySet())
        {
            System.out.println(key.getKey() + " " + key.getValue());
        }
    }

    //Kept this method as was using this class like a static class at first. So just called this in constructor
    public void getLink(Socket input, BufferedReader in, PrintWriter out, int inId){
        if(link == null)
        {
            try
            {
                link = input;
                clientInput = in;
                clientOut = out;
                this.id = inId;
                if(link.isConnected())
                {
                    System.out.println("Connected to client Via Server Actions");
                }
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        else {
            return;
        }
    }

    //Better to create little functions like this instead of writing the same thing over and over again and better for readability
    private void endComm()
    {
        clientOut.println("break");
    }

    public void sayHello()
    {
        clientOut.println("Welcome to the event server.");
        clientOut.println("A list of keywords are [add] [list] [remove] [empty]\n");
        clientOut.println("[list] [empty] work as single words\n");
        clientOut.println("[add] = add;10 October 2021;10 pm;Any Descrption you like, Place");
        clientOut.println("[remove] = remove;10 Octover 2021;10 pm;Any Descripton you like, Place");
        clientOut.println("Make sure to use ; to separate the satements\nExample keyword;date;time;description");
        endComm();
    }



    //IGNORE ONLY FOR ADDING DATA TO LIST BEFORE SERVER
    public static void add(String input, Map<String,ArrayList<String>> inputMap){
        ArrayList<String> clientInput = new ArrayList<>(Arrays.asList(input.split(";")));

        clientInput.remove(0); //Remove add keyword 
        //ArrayList index is now date, currentTime, description
        String date = clientInput.get(0);
        date = date.toUpperCase();
        //Removing date as we have it stored instead of using multple currentTimes below
        clientInput.remove(0);
        //Parse for currentTime that was input
        int inputTime = parseTime(clientInput.get(0));
        
        if(inputMap.containsKey(date))
        {            

            //Here we grab the ArrayList with the date and increment over it's values. i+=2 to next time value as we order
            ArrayList<String> value = inputMap.get(date);
                
                //Since values are sorted on the way in, we can always use the check ahead and check end to continue sortnng.
                    for(int i = 0; i<value.size();i+=2)
                    {
                        try
                        {
                            //This function sorts the time in 24 hour mode to compare.
                            int currentTime = parseTime(value.get(i));

                            //-2 because description is at usual -1
                            int endTime = parseTime(value.get((value.size() - 2)));

                            //If input > parsedTime which is 1 to 24 and array has more than 1 value
                            if(inputTime > currentTime && value.size() > 2)
                            {
                                //Get the value ahead and currentTime, +2 because we skip the description of the previous.
                                int aheadIndex = parseTime(value.get(i+2));

                                //Check if inputTime is greaater than the value ahead of i AND less than the value at the end 
                                if(inputTime > endTime)
                                {
                                    //Adding Time and Description to the end.
                                    inputMap.get(date).add((value.size() - 1), clientInput.get(0));
                                    inputMap.get(date).add((value.size() - 2), clientInput.get(1));
                                    break;
                                }
                                //Because we use less than, times that are the same will stack accordingly
                                if(inputTime < aheadIndex)
                                {
                                    inputMap.get(date).add((i+2), clientInput.get(0));
                                    inputMap.get(date).add((i+3), clientInput.get(1));
                                    break;
                                }
                            }
                            else 
                            {
                                inputMap.get(date).add(i, clientInput.get(0));
                                inputMap.get(date).add((i+1), clientInput.get(1));
                                break;
                            }
                        }
                        catch(ArrayIndexOutOfBoundsException | NumberFormatException e)
                        {
                            break;
                        }
                    }
        }
        else{
            //If not already contained, create an empty data set with the date and add the append the values as like above indexing currentTime
            inputMap.put(date, new ArrayList<>());
            inputMap.get(date).add(clientInput.get(0));
            inputMap.get(date).add(clientInput.get(1));
        }
    }
}

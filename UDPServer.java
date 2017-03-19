/*
 Name: 			CIS427 Project2
 Created by:	Yanzhen Wang
 Created at:	1/28/2017
 Description:	This project is to demonstrate how TCP works. The client console sends request to the server console, and the
 				server console send response back. In this project, the server console reads input from a text file, and calculate
 				the number of the character in a sentence then plus the sequence number. In client console, the user types "query",
 				then it will display the result line by line of the text document it's reading from.
 				
download: 
server-
1. sever sends segment to client
	- server reads in lines of data from file and stores in Segment.Data[]
	- server serializes the Segment class into a byte[] to be send over the network
	- server waits for client response with correct sequence numb
	- if the client returns the correct sequence number, it will send the next segment of data from file
	- if the client does not return the correct number or times out, it resends the same segment up to 3 times before sending the next segment
client
	- client reads in streamBuffer
	- client deserializes the buffer/byte[] as a Segment class
	- client then sends the seq# back the server confirming it got the correct segment
	- client waits to receive the next segment from server
	
this continues until the server cannot read anymore lines from the file or all segments have been transmitted
 				
*/
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.util.Random;

public class UDPServer {
	public static void main(String argv[]) throws Exception
    {		
		final int udp_port = 5897;
		final int tcp_port = 7934;
		final int udp_port2 = 8587;
		final int TIMEOUT = 50; 
		final int MAXTRIES = 3;
		 		 
		Segment s = new Segment();	// create an object for Segment so it can be filled with seqacknum and data	 
		Random rand = new Random();
        String clientSentence;      
    	int totalLineCount = 0;
    	String myLine;
    	int lineCount=0;
    	int windowSize =50; // CHANGE IT TO 100 TO DO TESTING CASES    
        int sendingLines=0;
        int ack;
        int duplicatedAck =0;
    	byte[] receivedACK = new byte[2048];
    	String filename = "alice.txt";
    	File myFile = new File(filename);
    	Scanner inputFile = new Scanner(myFile);// read the file line by line
    	   	
    	 ByteArrayOutputStream outputStream;
    	 ObjectOutputStream os;
    	 byte[] data;
    	 
    	//TCP:
        ServerSocket welcomeSocket = new ServerSocket(tcp_port); //create a welcoming socket for server at port 6789
        Socket connectionSocket = welcomeSocket.accept();
        //wait, in welcoming socket for contact by client
        
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        // create input stream, attached to socket    
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        // create output stream attached to socket
        
        
        //UDP:
        InetAddress IPAddress = InetAddress.getByName("localhost");
        DatagramSocket serverUDPSocket = new DatagramSocket();// UDP does't need resource host 
        
        
        	outToClient.writeBytes("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+"+ '\n');
        	outToClient.writeBytes("+-+-+-+-+-+-+ Multiple Channel Protocol +-+-+-+-+-+-+-+-+" + '\n');
        	outToClient.writeBytes("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+"+ '\n');
        	outToClient.writeBytes("Commands allowed by the server for this client:" + '\n');
        	outToClient.writeBytes(" query" + '\n');
        	outToClient.writeBytes(" download" + '\n');
        	// write out line to socket
        	
            clientSentence = inFromClient.readLine();// get the line from input stream
            System.out.println("FROM CLIENT:" + clientSentence);
    		

            if(clientSentence.contains("query"))
            {
        		outToClient.writeBytes("0" + '\n');// write out line to socket
            	System.out.println("TO CLIENT: 0");
            	while(inputFile.hasNext())
            	{
            		String line = inputFile.nextLine();
            		lineCount = line.length();
            		lineCount+=4;
            		totalLineCount += lineCount;
            		myLine = Integer.toString(totalLineCount);// convert int to string
            		outToClient.writeBytes(myLine+ '\n');// write out line to socket
            		System.out.println("TO CLIENT:" + totalLineCount);
            	}
        		outToClient.writeBytes("-1" + '\n');           	
            }   
            
            if(clientSentence.contains("download"))
            {   
           
            	while(inputFile.hasNext())
            	{
            		String line = inputFile.nextLine();
            		lineCount = line.length();
            		lineCount+=4;
            		totalLineCount += lineCount;
         		                                        
            		// this is UDP method :   
            		
            		s.seqacknum = totalLineCount;// byte data type 
            		s.data = line.getBytes();// translate string from file to byte 
            		
            		serverUDPSocket.setSoTimeout(rand.nextInt(49)+1);  // Maximum receive blocking time (milliseconds)
           		
            		if(sendingLines <windowSize)
            		{// limit the window to 50 or 100 . only send 50/100 lines a time and then wait for ACK 
            		
            			outputStream = new ByteArrayOutputStream();
            		    os = new ObjectOutputStream(outputStream);
            			os.writeObject(s);
            			data = outputStream.toByteArray();            			
                		DatagramPacket sendPacket = new DatagramPacket(data, data.length,IPAddress, udp_port);
                		serverUDPSocket.send(sendPacket);//send sequence number combined with data to client
                		//serverUDPSocket.setSoTimeout(TIMEOUT);  // Maximum receive blocking time (milliseconds)

                		sendingLines++;      		 	
            		}   
            		else
            		{
	            		DatagramPacket receiveAckPacket = new DatagramPacket(receivedACK, receivedACK.length);
	            		if(receivedACK.length >0)
	            		{
	            			//will resend the packet if it fails
	                        do 
	                        	{
	                            //get packet from client and parse
	                            serverUDPSocket.receive(receiveAckPacket);
	            		        String stack = new String(receiveAckPacket.getData());
	            		        ack = Integer.parseInt(stack);// convert string to integer
	
	                            //check if packet seq is the same as ack or if tries is > 3
	                            if(s.seqacknum == ack)// || duplicatedAck <= MAXTRIES)
	                            	{
	                               // duplicatedAck = 1;
	                                break;		
	                            	}
	                            else
	                            	{
	                                	duplicatedAck++; 
	                                	outputStream = new ByteArrayOutputStream();
	                                	os = new ObjectOutputStream(outputStream);
	                                	os.writeObject(s);
	                                	data = outputStream.toByteArray();            			
	                                	DatagramPacket sendPacket = new DatagramPacket(data, data.length,IPAddress, udp_port);
	                                	serverUDPSocket.send(sendPacket);
	                                
	                            	 }// end of else
	                        	} while (s.seqacknum != ack && duplicatedAck < MAXTRIES);
	            		 }//end of if
     			
	            	}//end of else
            		windowSize += sendingLines;// keep sending packet after checking packets inside window		                 	
            	}   // while close  
            	
            	
}// if close
            inputFile.close();
        	welcomeSocket.close();            	
        	serverUDPSocket.close();
}//void close
}// class close

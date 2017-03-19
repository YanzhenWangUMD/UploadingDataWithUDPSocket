
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

import java.io.*;
import java.net.*;
import java.util.Random;

public class UDPClient {
	 public static void main(String argv[]) throws Exception
	    {
		 
		 	final int udp_port = 5897;
		 	final int udp_port2 = 8587;// for retransmit purpose
		 	final int tcp_port = 7934;
	        String sentence;
	        String modifiedSentence = "0";
	        byte[] receiveData = new byte[2048];
	        int sendSeq;
	        Random rand = new Random();
	        String acknum;
	        int counter =1;// count the number of transmission that UDP pushed
	        	        
	        Segment recievedSeg = new Segment();//Create an object for class Segment
	        //TCP:
	        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); // create input stream
	        Socket clientSocket = new Socket("localhost", tcp_port);// create a clientSock connect to server with the port number 6789
	        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());//create output stream attached to socket
	        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        // create input stream attached to socket
	               
	        DatagramSocket clientUDPSocket = new DatagramSocket(udp_port);// Create UDP Socket
	        InetAddress IPAddress = InetAddress.getByName("localhost");
	        
	        for (int i = 0; i<6;i++)
	        	{
	        	modifiedSentence = inFromServer.readLine();// get input from server 
	        	System.out.println(modifiedSentence);// print out the input
          
	        	}
	        
	            sentence = inFromUser.readLine();
	            outToServer.writeBytes(sentence + '\n');// send line to server
	            
	           System.out.println("FROM SERVER: " + modifiedSentence);
	           System.out.println("1 0");
	           
	           DatagramPacket receivePacket;
	           ByteArrayInputStream in;
	           ObjectInputStream is;
	           int old_seq =0;
	           	           
	        while(!modifiedSentence.contains("-1"))
	        {	        	        	
	        	if(!modifiedSentence.contains("-1"))
		        {
	        		//UDP: 	        	
	        		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		        	// create receiving packet for byte coming in from server
		 	        clientUDPSocket.receive(receivePacket);// UDP 
		 	        //receiveData = receivePacket.getData();
		 	        
		 	        in = new ByteArrayInputStream(receiveData);// this statement is to serialize object as an byteArray
		 	        is = new ObjectInputStream(in);
		 	        
		 	        try{		 	    	 
		 	        	recievedSeg = (Segment)is.readObject();	// create an object and get byte data from stream	 	      
		 	           }catch(ClassNotFoundException e)
		 	        	{
		 	        	   e.printStackTrace();
		 	        	}
		 	        
		 	        sendSeq = recievedSeg.getseqacknum();
		 	        String outputData = new String (recievedSeg.getData());// convert from byte data to String
		 	        int temp = sendSeq;//create a temporary variable for printing purpose
		 	       if(rand.nextInt(9) == 0)// randomly created 10% of delay for the process
		 	       {  
		 	    	  sendSeq =-2;// created delay by purposely messing up the ACK number
			 	   }
		 	       		 	      
		 	       acknum = Integer.toString(sendSeq);// convert seq number to string		 	  
		 	       byte[] seqnum = acknum.getBytes();// convert string to byte
		 	        
		        	DatagramPacket sendseqPacket = new DatagramPacket(seqnum,seqnum.length,IPAddress, udp_port2);
		        	clientUDPSocket.send(sendseqPacket);//send sequence number to server
		 	       
		        	if(old_seq == sendSeq)
		        	{
		        		counter++;
		        	}
		        	else
		        	{
		        		System.out.println(counter +" " + temp+ "  " + outputData);
		        		counter=1;	// reset the counter
		        	}		 	       
		        	old_seq = sendSeq;	// reset the old_seq	        	
		 	      }
	        	
	        }
	        outToServer.flush();
	        clientUDPSocket.close();
	        clientSocket.close();  //close connection
	    }
}
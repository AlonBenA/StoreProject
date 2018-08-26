package com.amazonaws.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.Map.Entry;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.model.S3Object;

public class InitMain {

	public static void main(String[] args) {
		

		
		testDynamoDB();
		
		
	}
	
	
	 public static void testS3()
	 {
		 
	        AWSCredentials credentials = null;
			String region = "us-east-2";
	        String bucketName = "first-s3-bucket" + UUID.randomUUID();
	        String key = "VODKA";
	        
	        try {
	            credentials = new ProfileCredentialsProvider("default").getCredentials();
	        } catch (Exception e) {
	            throw new AmazonClientException(
	                    "Cannot load the credentials from the credential profiles file. " +
	                    "Please make sure that your credentials file is at the correct " +
	                    "location (C:\\Users\\alon\\.aws\\credentials), and is in valid format.",
	                    e);
	        }
	        
	        S3Handler s3Handler = new S3Handler(credentials, region, bucketName);
	        

	        try {
	        	
	        	
				s3Handler.putFile(new File("VODKA.jpg"), key);
				
				S3Object S3object = s3Handler.getItem(key);
				
				//displayTextInputStream(S3object.getObjectContent());
				
				
				s3Handler.DeleteObjectFromBucket(key);
				
				s3Handler.DeleteBucket();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        
	        
	 }
	
	
	 public static void testSQS()
	 {
			String region = "us-east-2";
			String queueName = new String("myQueue");
			ProfileCredentialsProvider credentialsProvider;
			
			// Read Credentials
			credentialsProvider = readCredentials();
			
	  
	        SQSHandler sqsHandler = new SQSHandler(credentialsProvider, region, queueName);

	        

	        sqsHandler.SendMessage("Alon 1");
	        sqsHandler.SendMessage("Alon 2");
	        sqsHandler.SendMessage("Alon 3");
	        
	        
	        Message message = sqsHandler.ReceiveMessages();
	        
	        displayMessage(message);
	        
	        
	        sqsHandler.DeleteMessage(message);
	        
	        
	        sqsHandler.DeleteQueue();
	 }
	 
	 
	    public static void testDynamoDB()
	    {
	        /* Read the name from command args */
	        String table_name = "HelloTable";
	        String region = new String("us-east-2");
	        ProfileCredentialsProvider credentialsProvider;
	        String ItemName = "VODKA";
		    int Amount;
	        
	   		
	   	 // Read Credentials
	   	 	credentialsProvider = readCredentials();
	   	 	
	   	 	DynamoDBHandler DDBH = new DynamoDBHandler(region, table_name, credentialsProvider);
	   	 	
	   	 	System.out.println("put 10 VODKA in table");
	   	 	DDBH.putItem(ItemName, 10);
	   	 	
	   	 	System.out.println("put 30 VODKA in table");
	   	 	DDBH.putItem(ItemName, 30);
	   	 	
	   	 	
	   	 	Amount = DDBH.retrieveItemAmount(ItemName);
	   	 	System.out.println("The Amount of " + ItemName + " is " + Amount);

	   	 	DDBH.deleteItem(ItemName);
	   	 	
	   	 	DDBH.deleteTable();
	   	 
	   	 
	    }
	    
	 
	
    public static ProfileCredentialsProvider readCredentials() {
    	
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\BorisM\\.aws\\credentials), and is in valid format.",
                    e);
        }
        return credentialsProvider;
    }
	
	
	
    private static File createSampleFile() throws IOException {
        File file = File.createTempFile("aws-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("01234567890112345678901234\n");
        writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
        writer.write("01234567890112345678901234\n");
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.close();

        return file;
    }

    /**
     * Displays the contents of the specified input stream as text.
     *
     * @param input
     *            The input stream to display as text.
     *
     * @throws IOException
     */
    private static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }
	

    public static void displayMessage(Message message) {

        System.out.println("Message");
        System.out.println("    MessageId:     " + message.getMessageId());
        System.out.println("    Body:          " + message.getBody());
        for (Entry<String, String> entry : message.getAttributes().entrySet()) {
            System.out.println("  Attribute");
            System.out.println("    Name:  " + entry.getKey());
            System.out.println("    Value: " + entry.getValue());
        }
    }
    
    

    
}

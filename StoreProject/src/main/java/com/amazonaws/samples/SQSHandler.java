package com.amazonaws.samples;


import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;



public class SQSHandler {
	
	private String region;
	private AmazonSQS sqs;
	private String queueName;
	private String myQueueUrl;
	
	
	public SQSHandler(ProfileCredentialsProvider credentialsProvider,String region,String queueName)
	{
		this.region = region;
		this.queueName = queueName;
		
		init(credentialsProvider);
		
	}
	
	
	
	private void init(ProfileCredentialsProvider credentialsProvider)
	{
		sqs = AmazonSQSClientBuilder.standard()
<<<<<<< HEAD
  //              .withCredentials(credentialsProvider)
=======
       //         .withCredentials(credentialsProvider)
>>>>>>> branch 'master' of https://github.com/AlonBenA/StoreProject.git
                .withRegion(region)
                .build();
		
		  System.out.println("Checks if the Queue " + queueName + " exists");
		  
		  if(!checksIfQueueExists())
		  {
			  System.out.println("The Queue " + queueName + " doesn't exists");
			  System.out.println("Creating Queue " + queueName + "\n");
			  createSQSQueue();			
		  }
		  else
		  {
			  System.out.println("The Queue " + queueName + " exists");
		  }		
	}
	
	
	private Boolean checksIfQueueExists()
	{
        try {
		GetQueueUrlResult outcome = sqs.getQueueUrl(queueName);
		myQueueUrl = outcome.getQueueUrl();
		return true;
		
        }catch (QueueDoesNotExistException ase) {
        	
            return false;	
        } 
        

	}
	
	
	private void createSQSQueue() {
		
        try {
		
	    System.out.println("Creating a new SQS queue called " + queueName + "\n");
	    
	    CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
	    CreateQueueResult createQueueResult = sqs.createQueue(createQueueRequest); 
	    myQueueUrl = createQueueResult.getQueueUrl();
	    
	    
	    System.out.println(queueName + " was Successfully created\n");
	    
        }catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
	
	//upload a new message to SQS
	public void SendMessage(String message)
	{
        
        try {

		 SendMessageRequest sendMsqRequest;
         sendMsqRequest = new SendMessageRequest(myQueueUrl, message);
         sqs.sendMessage(sendMsqRequest);
         System.out.println("The message was sent successfully");
         
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
         
	}
	
	//Receive a new message from SQS
	public Message ReceiveMessages()
	{
		ReceiveMessageResult result;
		List<Message> messages = null;
		
        try {

        	ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
        	result = sqs.receiveMessage(receiveMessageRequest);
        	messages = result.getMessages();
    		
        	for (Message message : messages)
        	{
        		return message;
        	}
        	
        	
        	
         
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        
		return null;
         
	}
	
	
	//Delete a message from SQS
	public void DeleteMessage(Message message)
	{		
        try {

            // Delete a message 
            System.out.println("Deleting a message.\n");
            String messageReceiptHandle = message.getReceiptHandle();
            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(myQueueUrl, messageReceiptHandle);
            sqs.deleteMessage(deleteMessageRequest); 
        
            System.out.println("The message was Successfully deleted\n");
         
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
       
         
	}
	
	//Delete a Queue from AWS
	public void DeleteQueue()
	{		
        try {

            // Delete a Queue 
            System.out.println("Deleting the queue\n");
            
            sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
        	
            System.out.println(queueName + " Successfully deleted");
         
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
       
         
	}
	

}

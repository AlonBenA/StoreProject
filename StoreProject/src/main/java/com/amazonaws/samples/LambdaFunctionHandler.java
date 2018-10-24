package com.amazonaws.samples;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class LambdaFunctionHandler implements RequestHandler<DynamodbEvent, Void> {
	static String queueOrderName = new String("queueOrder");
	private String region = "us-east-2";
	private String myQueueUrl= null;
	
	private AmazonSQS sqs = AmazonSQSClientBuilder.standard()
            .withRegion(region)
            .build();

    @Override
    public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {
    	String orderId=null;
    	String status=null;
		for (DynamodbStreamRecord record : dynamodbEvent.getRecords()) {

            if (record == null) {
                continue;
            }
            orderId=record.getDynamodb().getKeys().get("orderId").getS();

        	
        	status=record.getDynamodb().getNewImage().get("status").getS();
        	
            context.getLogger().log("status: " + status );
            context.getLogger().log("orderId: " + orderId );
            
            if("undone".equals(status)) {
            	sendMessage(orderId);
            	context.getLogger().log("myQueueUrl: " +myQueueUrl );
            	context.getLogger().log("queueOrderName: " +queueOrderName );
            	context.getLogger().log("Message done \n\n\n" );
            }
        }

        return null;
    }
    
    
    public void sendMessage(String message)
    {
    	if(checksIfQueueExists())
    	{
    		 SendMessageRequest sendMsqRequest;
             sendMsqRequest = new SendMessageRequest(myQueueUrl, message);
             sqs.sendMessage(sendMsqRequest);
    	}
    }
    
    
	private Boolean checksIfQueueExists()
	{
        try {
		GetQueueUrlResult outcome = sqs.getQueueUrl(queueOrderName);
		myQueueUrl = outcome.getQueueUrl();
		return true;
		
        }catch (QueueDoesNotExistException ase) {
        	
            return false;	
        } 
        

	}
    
    
}
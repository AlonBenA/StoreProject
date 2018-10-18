package com.amazonaws.samples;

import java.io.File;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3Handler {
	
	private AmazonS3 s3;
	private String region;
	private String bucketName;
	
	
	public S3Handler(AWSCredentials credentials,String region,String bucketName) {
		
		this.region = region;
		this.bucketName = bucketName;
		
		init(credentials);
		
	}
	
	
	private void init(AWSCredentials credentials)
	{
        try {
		
		
		  s3 = AmazonS3ClientBuilder.standard()
	//	            .withCredentials(new AWSStaticCredentialsProvider(credentials))
		            .withRegion(region)
		            .build();
		  
		  System.out.println("Checks if the bucket " + bucketName + " exists");
		  
		  if(!s3.doesBucketExistV2(bucketName))
		  {
			  System.out.println("The bucket " + bucketName + " doesn't exists");
			  System.out.println("Creating bucket " + bucketName + "\n");
			  s3.createBucket(bucketName);
		  }
		  else
		  {
			  System.out.println("The bucket " + bucketName + " exists");
		  }
		  
        }catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        
        
	}
	
	//upload a file to the S3
	public void putFile(File file,String key)
	{
		try {
		
		PutObjectRequest putobjectrequest = new PutObjectRequest(bucketName, key, file);
		s3.putObject(putobjectrequest);
		System.out.println("The object was upload");
		
		}catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
	}
	
	
	//get a file from s3
	public S3Object getItem(String key)
	{
		S3Object S3object = null;
		
		try {
		GetObjectRequest getobjectrequest = new GetObjectRequest(bucketName, key);
		 S3object = s3.getObject(getobjectrequest);
		 System.out.println("The object was successfully downloaded");
		
		}catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
		
		 return S3object;
		
	}
	
	//Delete a file from s3
	public void DeleteObjectFromBucket(String key)
	{
		try {
			
		s3.deleteObject(bucketName, key);
		
		System.out.println("The object was successfully deleted");
		
		}catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
	}
	
	//Delete a Bucket from s3
	public void DeleteBucket()
	{
		try {
			
		s3.deleteBucket(bucketName);
		System.out.println("The bucket "+ bucketName + " was successfully deleted");
		
		}catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
	}

}

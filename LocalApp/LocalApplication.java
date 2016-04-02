package LocalApp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.aspectj.weaver.patterns.PatternParser;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.SendMessageRequest;


public class LocalApplication {

    private static String tweetsFilePath;
    private static String tweetsHtmlPath;
    private static String inputFileName;
    
    private static UUID uuid;
    
    // Number of messages per worker.
    private static int n;
    private static boolean terminate = false;
    private static String managerInstanceId;
    private static AmazonEC2 ec2;
    private static AmazonS3 s3;
    private static String bucketName = "hayminirhodadi";
    private static AWSCredentials credentials;
    private static AmazonSQSClient sqs;
	private static String infoQueueURL;
	private static String mySummaryQueueURL;
	private static String jobsQueueURL;
	private static String s3Path = "https://%s.s3.amazonaws.com/%s";
	
	public static void main(String[] args){
		
		tweetsFilePath = "C:\\Users\\Haymi\\Documents\\BGU\\DSP\\tweetLinks.txt";
		//tweetsHtmlPath = args[1];
		uuid = UUID.randomUUID();
		try {
			initEC2();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		runManagerInstance();
		uploadToS3();
		sendMessageToSQS();
//		waitForResponse();
//		handleResponse();
		
		
		
	}
	
    private static void sendMessageToSQS() {
        sqs = new AmazonSQSClient(credentials);
        
        
        
        System.out.println("Creating new SQS queues.");

        Map<String,MessageAttributeValue> attributes = new HashMap<String,MessageAttributeValue>();

        attributes.put("BucketName", new MessageAttributeValue().withDataType("String").withStringValue(bucketName));
        attributes.put("InputFilename", new MessageAttributeValue().withDataType("String").withStringValue(inputFileName));
        attributes.put("UUID", new MessageAttributeValue().withDataType("String").withStringValue(String.valueOf(uuid)));
        attributes.put("WorkerPerMessage", new MessageAttributeValue().withDataType("Number").withStringValue(String.valueOf(n)));
        attributes.put("NumOfURLs", new MessageAttributeValue().withDataType("Number").withStringValue(String.valueOf(tweetsAmount())));

        //Creating the Manager-LocalApp Queue
        CreateQueueRequest createQueueRequest = new CreateQueueRequest().withQueueName("ClientsQueue");
        infoQueueURL = sqs.createQueue(createQueueRequest).getQueueUrl();

        System.out.println("ClientsQueue created ");

        //Creating the unique LocalApp Queue
        createQueueRequest = new CreateQueueRequest().withQueueName(uuid.toString());
        mySummaryQueueURL = sqs.createQueue(createQueueRequest).getQueueUrl();

        System.out.println("Personal localapp queue created.");

//        // Creating the Jobs queue
//        Map<String,String> attributes2 = new HashMap<String,String>();
//        attributes2.put("VisibilityTimeout", "160");
//        createQueueRequest = new CreateQueueRequest().withQueueName("JobsQueue").withAttributes(attributes2);
//        jobsQueueURL = sqs.createQueue(createQueueRequest).getQueueUrl();

        // Sending the message with the location of the input file to the manager
        try{
            sqs.sendMessage(new SendMessageRequest().withQueueUrl(infoQueueURL).withMessageBody("Please process these URLS").withMessageAttributes(attributes));
        }
        catch (QueueDoesNotExistException e){
            System.out.println("Manager is no longer running. Exiting...");
            System.exit(0);
        }


        System.out.println("SQS queues created.");

    }
	
	private static int tweetsAmount() {
		int result = 0;
	    try {
	    	BufferedReader br = new BufferedReader(new FileReader(tweetsFilePath));
			while ( br.readLine() != null)
		    	result++;
		    br.close();
		}
	    catch (Exception e){ 
			e.printStackTrace();
	    }
		return result;
	}

	private static void uploadToS3() {
		
		s3 = new AmazonS3Client(credentials);
        System.out.println("AmazonS3Client created.");
        if (!s3.doesBucketExist(bucketName))
            s3.createBucket(bucketName);
        System.out.println("Bucket exists.");
        File f;

        // Upload tweets file.
        f = new File(tweetsFilePath);
        inputFileName = uuid.toString() +"-"+ f.getName();
        PutObjectRequest por = new PutObjectRequest(bucketName, inputFileName, f);
        por.withCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(por);
        System.out.println(inputFileName + " uploaded.");

        // upload worker bootstrap to s3
        // f = new File("/Users/mtoledano/Dropbox/Courses_2016/Distributed Systems/Assignment1/worker_bootstrap.sh");

        //        por = new PutObjectRequest(bucketName, "worker_bootstrap.sh", f);
        //        por.withCannedAcl(CannedAccessControlList.PublicRead);
        //        s3.putObject(por);
        		
	}

	private static void initEC2() throws FileNotFoundException, IOException {
		credentials = new PropertiesCredentials(new FileInputStream("C:\\Users\\Haymi\\Documents\\BGU\\DSP\\rootkey.properties"));
        ec2 = new AmazonEC2Client(credentials);        
    }

	/*
	 * Makes sure the Manager is running, if not - run it.
	 */
	private static void runManagerInstance() {

        Instance managerInstance = getManagerInstance();
        if (managerInstance != null) { // Manager already exists.            
            managerInstanceId = managerInstance.getInstanceId();
            InstanceState managerState =  getInstanceState(managerInstance);
            if (managerState != InstanceState.Running) {
            	StartInstancesRequest startRequest = new StartInstancesRequest().withInstanceIds(managerInstanceId);
                ec2.startInstances(startRequest);
            }
        }
        else { // Manager doesn't exist.
            RunInstancesRequest runManagerRequest = new RunInstancesRequest("ami-b66ed3de", 1, 1);
            runManagerRequest.setInstanceType(InstanceType.T2Micro.toString());
            runManagerRequest.setUserData(fileToBase64String("C:\\Users\\Haymi\\Documents\\BGU\\DSP\\bootstart.sh"));
            runManagerRequest.setKeyName("nirhaymi");
            managerInstance = ec2.runInstances(runManagerRequest).getReservation().getInstances().get(0);
            managerInstanceId = managerInstance.getInstanceId();
            CreateTagsRequest createTagsRequest = new CreateTagsRequest()
            		.withResources(managerInstanceId)
                    .withTags(new Tag("Type", "Manager"));
            ec2.createTags(createTagsRequest);
        }
        waitForManagerInstanceState(InstanceState.Running, 5000);

        System.out.println("Manager is running.");
		
	}

	private static Instance getManagerInstance(){
		Filter tagFilter = new Filter("tag:Type", Arrays.asList("Manager"));
        Filter stateFilter = new Filter("instance-state-name", Arrays.asList("stopped", "pending", "running"));
        
        DescribeInstancesResult response = ec2.describeInstances((new DescribeInstancesRequest())
        		.withFilters(tagFilter, stateFilter));

        List<Reservation> managerInstances = response.getReservations();
        if(managerInstances.isEmpty())
        	return null;
        return managerInstances.get(0).getInstances().get(0);
	}
	
	private static void waitForManagerInstanceState(InstanceState state, int sleepTime) {
		System.out.println("waiting for instance and state");
		Instance manager;
		InstanceState curr;
		while(true){
			manager = getManagerInstance();
			if (manager != null) {
				curr = getInstanceState(manager);
				if (curr == state)
					break;
				System.out.println("curr state is " + curr + " sleeping...");
			} else {
				System.out.println("manager is null, sleeping...");
			}
			
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static String fileToBase64String(String path) {
		StringBuilder sb = new StringBuilder();
		String line = null;
	    try {
	    	BufferedReader br = new BufferedReader(new FileReader(path));
			
		    while ( (line= br.readLine()) != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		    }
		    br.close();
		}
	    catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	    return new String(Base64.encodeBase64(sb.toString().getBytes()));
	}

	private static InstanceState getInstanceState(Instance managerInstance) {
		Integer response = managerInstance.getState().getCode();
		Integer key = new Integer(managerInstance.getState().getCode().intValue() & 0xff);
		System.out.println("Response is " + response + " and key is " + key);
		System.out.println("Returning " + InstanceStatesDictionary.instanceStatesDictionary.get(key));
		return InstanceStatesDictionary.instanceStatesDictionary.get(key);
	}	
}

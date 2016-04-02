package Manager;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

public class Manager {
	
	private static AWSCredentials credentials;
	private static AmazonEC2 ec2;
	private static AmazonSQSClient sqs;
//	private static ExecutorService executor;
	private static Logger logger = Logger.getLogger("Manager Logger");
//	private static Vector<String> workerInstanceIds;
	private static ConcurrentHashMap<String, Integer> clientsUUIDToURLLeft;
	private static AtomicBoolean shouldTerminate;
//	private static String myInstanceID;
	private static String jobDoneAckQueue;
	private static String clientsQueueURL;
	private static String jobsQueueURL;
	private static int mapperNumOfThreads = 3;
	private static int reducerNumOfThreads = 3;

	public static void main(String[] args) {
		shouldTerminate = new AtomicBoolean(false);
		FileHandler fh;
        try {
            fh = new FileHandler("/tmp/managerLog.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info("===== Manager started =====");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            initEC2Client();
            initSQSQueues();
        } catch (IOException e) {
            logger.info("cannot find access key file!");
        }

        clientsUUIDToURLLeft = new ConcurrentHashMap<String, Integer>();
        
//        jobsQueueURL = sqs.getQueueUrl("JobsQueue").getQueueUrl();
//        informationQueue = sqs.getQueueUrl("InformationQueue").getQueueUrl();
//        jobDoneAckQueue = sqs.createQueue(new CreateQueueRequest("JobDoneAckQueue")).getQueueUrl();
        Runnable mapper = new Mapper(jobsQueueURL, clientsQueueURL, 
        		clientsUUIDToURLLeft, sqs, ec2, shouldTerminate, mapperNumOfThreads,
        		logger, credentials);
        Runnable reducer = new Reducer(jobDoneAckQueue, clientsUUIDToURLLeft,
        		sqs, shouldTerminate, reducerNumOfThreads, logger, credentials);
        
        mapper.run();
        reducer.run();
        
        while(!shouldTerminate.get()){
        	synchronized(shouldTerminate){
            	try {
					shouldTerminate.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
//        terminateServer();
    }
		
	private static void initEC2Client() throws IOException {
        logger.info("Manager :: Starting to init ec2 client ");
        credentials = new PropertiesCredentials(new FileInputStream("/tmp/rootkey.properties"));
        ec2 = new AmazonEC2Client(credentials);
        logger.info("Manager :: EC2 Client initialized.");
    }

    private static void initSQSQueues() {
        logger.info("Manager :: Starting to init queues");
        sqs = new AmazonSQSClient(credentials);
        clientsQueueURL = sqs.getQueueUrl("InformationQueue").getQueueUrl();
        jobsQueueURL = sqs.createQueue(new CreateQueueRequest("JobQueueQueue")).getQueueUrl();
        jobDoneAckQueue = sqs.createQueue(new CreateQueueRequest("JobDoneAckQueue")).getQueueUrl();
        logger.info("Manager :: Queues initialized");
    }
}


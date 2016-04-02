package Manager;

import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;

public class WorkerInstanceData {

	public static String ami = "ami-b66ed3de";
	public static String keyName = "nirhaymi";
	public static String instanceString = InstanceType.T2Micro.toString();
	
	/*
	 * returns the amount of new workers
	 */
	public static int getWorkers(int numOfWorkers, AmazonEC2 ec2){
		RunInstancesRequest workerRequestInstance = new RunInstancesRequest(ami, numOfWorkers, numOfWorkers);
		workerRequestInstance.setKeyName(keyName);
		workerRequestInstance.setInstanceType(instanceString);
		
		List<Instance> instances = ec2.runInstances(workerRequestInstance).getReservation().getInstances();
        for (int i=0; i < instances.size(); i++) {
    		String workerID = instances.get(i).getInstanceId();

            // Creating tags for workers instances
            CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(workerID)
                    .withTags(new Tag("Type", "Worker"), new Tag("ID", workerID));
            ec2.createTags(createTagsRequest);
        }
        return instances.size();
	}
}

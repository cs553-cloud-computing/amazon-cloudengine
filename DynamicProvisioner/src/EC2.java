import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.util.Base64;

public class EC2 {

	static AmazonEC2 ec2;

	public EC2(AWSCredentials credentials, Region region) {
		ec2 = new AmazonEC2Client(credentials);
		ec2.setRegion(region);
	}

	public List<String> launch(String workerAMI, String instanceType, int num,
			double price, List<String> securityGroups, String userData, String charset) throws UnsupportedEncodingException {
		RequestSpotInstancesRequest requestRequest = new RequestSpotInstancesRequest();
		requestRequest.setSpotPrice(Double.toString(price));
		requestRequest.setInstanceCount(Integer.valueOf(num));

		LaunchSpecification launchSpecification = new LaunchSpecification();
		launchSpecification.setImageId(workerAMI);
		launchSpecification.setInstanceType(instanceType);
		launchSpecification.setSecurityGroups(securityGroups);
		launchSpecification.setUserData(new String(Base64.encode(userData.getBytes(charset))));
		launchSpecification.setKeyName("cloudstack-key"); //for test
		
		requestRequest.setLaunchSpecification(launchSpecification);
		RequestSpotInstancesResult requestResult = ec2
				.requestSpotInstances(requestRequest);
		List<SpotInstanceRequest> requestResponses = requestResult
				.getSpotInstanceRequests();

		List<String> spotInstanceRequestIds = new ArrayList<String>();

		for (SpotInstanceRequest requestResponse : requestResponses) {
			System.out.println("Created Spot Request: "
					+ requestResponse.getSpotInstanceRequestId());
			spotInstanceRequestIds.add(requestResponse
					.getSpotInstanceRequestId());
		}
		return spotInstanceRequestIds;
	}

	public List<String> getActiveSpotInstanceId(
			List<String> spotInstanceRequestIds) {
		DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
		describeRequest.setSpotInstanceRequestIds(spotInstanceRequestIds);

		System.out
				.println("Checking to determine if Spot Bids have reached the active state...");

		List<String> instanceIds = new ArrayList<String>();

		try {
			DescribeSpotInstanceRequestsResult describeResult = ec2
					.describeSpotInstanceRequests(describeRequest);
			List<SpotInstanceRequest> describeResponses = describeResult
					.getSpotInstanceRequests();

			for (SpotInstanceRequest describeResponse : describeResponses) {
				System.out.println(" "
						+ describeResponse.getSpotInstanceRequestId()
						+ " is in the " + describeResponse.getState()
						+ " state.");

				if (describeResponse.getState().equals("open"))
					return null;

				if (describeResponse.getState().equals("active"))
					instanceIds.add(describeResponse.getInstanceId());
			}
		} catch (AmazonServiceException e) {
			System.out.println("Error when calling describeSpotInstances");
			System.out.println("Caught Exception: " + e.getMessage());
			System.out.println("Reponse Status Code: " + e.getStatusCode());
			System.out.println("Error Code: " + e.getErrorCode());
			System.out.println("Request ID: " + e.getRequestId());

			return null;
		}
		return instanceIds;
	}
	
	public boolean ifInstancePending(List<String> instanceIDs) {
		System.out.println("Checking to determine if a Instance is in Pending state...");
		DescribeInstancesRequest describeInstancesRequest=new DescribeInstancesRequest().withInstanceIds(instanceIDs);
		List<Reservation> reservations = ec2.describeInstances(describeInstancesRequest)
				.getReservations();
		for (Reservation reservation : reservations) {
			List<Instance> instances = reservation.getInstances();
			for (Instance instance : instances) {
				System.out.println(" "
						+ instance.getInstanceId()
						+ " is in the " + instance.getState().getName()
						+ " state.");
				if (instance.getState().getName().equals("pending"))
					return true;
			}
		}
		return false;
	}

	public void tagResources(List<String> resources, List<Tag> tags) {
		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.setResources(resources);
		createTagsRequest.setTags(tags);
		try {
			ec2.createTags(createTagsRequest);
		} catch (AmazonServiceException e) {
			System.out.println("Error terminating instances");
			System.out.println("Caught Exception: " + e.getMessage());
			System.out.println("Reponse Status Code: " + e.getStatusCode());
			System.out.println("Error Code: " + e.getErrorCode());
			System.out.println("Request ID: " + e.getRequestId());
		}
	}

	public void cancelRequest(List<String> spotInstanceRequestIds) {
		try {
			System.out.println("Cancelling requests.");
			CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(
					spotInstanceRequestIds);
			ec2.cancelSpotInstanceRequests(cancelRequest);
		} catch (AmazonServiceException e) {
			System.out.println("Error cancelling instances");
			System.out.println("Caught Exception: " + e.getMessage());
			System.out.println("Reponse Status Code: " + e.getStatusCode());
			System.out.println("Error Code: " + e.getErrorCode());
			System.out.println("Request ID: " + e.getRequestId());
		}
	}
	
	 public void terminateInstances (List<String> instanceIDs) {
	        try {
	            System.out.println("Terminate instances");
	            TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIDs);
	            ec2.terminateInstances(terminateRequest);
	        } catch (AmazonServiceException e) {
	            System.out.println("Error terminating instances");
	            System.out.println("Caught Exception: " + e.getMessage());
	            System.out.println("Reponse Status Code: " + e.getStatusCode());
	            System.out.println("Error Code: " + e.getErrorCode());
	            System.out.println("Request ID: " + e.getRequestId());
	        }
	    }

	public int getInstanceNumWithTag(Tag t) {
		assert (t != null);
		int num = 0;
		List<Reservation> reservations = ec2.describeInstances()
				.getReservations();
		for (Reservation reservation : reservations) {
			List<Instance> instances = reservation.getInstances();
			for (Instance instance : instances) {
				if (!instance.getState().getName().equals("pending")
						&& !instance.getState().getName().equals("running"))
					continue;
				List<Tag> tags = instance.getTags();
				for (Tag tag : tags) {
					if (tag.getKey().equals(t.getKey())
							&& tag.getValue().equals(t.getValue())) {
						num++;
						break;
					}
				}
			}
		}
		return num;
	}
}

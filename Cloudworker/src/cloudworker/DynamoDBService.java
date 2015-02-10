/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package cloudworker;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;

public class DynamoDBService {

	private static AmazonDynamoDBClient dynamoDB;
	private static String TABLE_NAME = "task-table";
	
	DynamoDBService(AWSCredentials credentials) throws Exception {
   
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        dynamoDB.setRegion(usEast1);
        
        createTable();
    }
	
	private static void createTable() throws Exception {
		try {
            // Create table if it does not exist yet
            if (Tables.doesTableExist(dynamoDB, TABLE_NAME)) {
                //System.out.println("Table " + TABLE_NAME + " is already ACTIVE");
            } else {
                // Create a table with a primary hash key named 'taskID', which holds a string
                CreateTableRequest createTableRequest = new CreateTableRequest()
                	.withTableName(TABLE_NAME)
                    .withKeySchema(new KeySchemaElement().withAttributeName("taskID").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("taskID").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
                    
                TableDescription tableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
                //System.out.println("Created Table: " + tableDescription);

                // Wait for it to become active
                //System.out.println("Waiting for " + TABLE_NAME + " to become ACTIVE...");
                Tables.waitForTableToBecomeActive(dynamoDB, TABLE_NAME);
            }

            // Describe our new table
//            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(TABLE_NAME);
//            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
//            System.out.println("Table Description: " + tableDescription);
		} catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

	}
	
	
	public static void addTask(String taskID, String task){

		HashMap<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("taskID", new AttributeValue().withS(taskID));
        item.put("Task", new AttributeValue(task));
        
        ExpectedAttributeValue notExpected = new ExpectedAttributeValue(false);
        Map<String, ExpectedAttributeValue> expected = new HashMap<String, ExpectedAttributeValue>();
        expected.put("taskID", notExpected);
        
        PutItemRequest putItemRequest = new PutItemRequest()
        	.withTableName(TABLE_NAME)
        	.withItem(item)
        	.withExpected(expected);  //put item only if no taskID exists!
        
        dynamoDB.putItem(putItemRequest);
		
	}
	
	/*public boolean getTask(String taskID){
		Map<String, AttributeValue> map=null;
		try{
			HashMap<String, AttributeValue> key = new HashMap<String, AttributeValue>();
			key.put("taskID", new AttributeValue().withS(taskID));
	
			GetItemRequest getItemRequest = new GetItemRequest()
				.withTableName(TABLE_NAME)
				.withKey(key);
			
			GetItemResult result = dynamoDB.getItem(getItemRequest);
			map = result.getItem();
			
		}catch(ResourceNotFoundException rnf){
			
			return false;
		}
		
		if(map==null){
			return false;
		}else{
			return true;
		}
		
	}*/
	
}

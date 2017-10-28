package services;

import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import models.CarAdvert;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ScanResult;


public class DynamoDBServiceTest {

    @Test
    public void test() {
        try {
            String tableName = "car-adverts";

            // Create a table with a primary hash key named 'name', which holds a string
            // TODO: check for pre-existing table instead of generic exception handling
            try {
                DynamoDBService.createTable(tableName, "name");
            }
            catch (Exception e) {
            }

            // Wait for it to become active
            DynamoDBService.waitForTableToBecomeAvailable(tableName);

            // Describe our new table
            System.out.println("Table Description: " + DynamoDBService.getTableDescription(tableName));

            // Add an item
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2015);
            cal.set(Calendar.MONTH, Calendar.NOVEMBER);
            cal.set(Calendar.DAY_OF_MONTH, 10);

            CarAdvert ca1 = new CarAdvert("BMW Series 3", CarAdvert.FuelType.DIESEL, 2000000, Boolean.FALSE, 100000, cal.getTime());
            Map<String, AttributeValue> item = ca1.toDynamoMap();
            DynamoDBService.putItem(tableName, item);

            // Add another item
            CarAdvert ca2 = new CarAdvert("Audi A6", CarAdvert.FuelType.GASOLINE, 3500000, Boolean.TRUE, 0, cal.getTime());
            item = ca2.toDynamoMap();
            DynamoDBService.putItem(tableName, item);

            // Scan for adverts with a price attribute greater than 2500000
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.GT.toString())
                    .withAttributeValueList(new AttributeValue().withN("2500000"));
            scanFilter.put("price", condition);

            ScanResult scanResult = DynamoDBService.scan(tableName, scanFilter);
            System.out.println("Result: " + scanResult);

            Assert.assertSame(1, scanResult.getCount());
            Map<String, AttributeValue> firstResult = scanResult.getItems().iterator().next();
            Assert.assertEquals(ca2.getTitle(), firstResult.get("title").getS());
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
            fail();
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            fail();
        }
    }

}
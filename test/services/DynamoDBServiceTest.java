package services;

import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import models.CarAdvert;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ScanResult;


public class DynamoDBServiceTest {
    public static final String TABLE_NAME = "car-adverts-api";
    public static Calendar cal;

    @BeforeClass
    public static void createTable() {
        // Create a table with a primary hash key named 'name', which holds a string
        DynamoDBService.deleteTable(TABLE_NAME);
        DynamoDBService.createTable(TABLE_NAME, "id");

        // Wait for it to become active
        DynamoDBService.waitForTableToBecomeAvailable(TABLE_NAME);

        // Describe our new table
        System.out.println("Table Description: " + DynamoDBService.getTableDescription(TABLE_NAME));
    }

    @BeforeClass
    public static void configureCalendar() {
        cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 10);
    }

    @Test
    public void putTest() {
        try {
            // Add an item
            CarAdvert ca1 = new CarAdvert("BMW Series 3", CarAdvert.FuelType.DIESEL, 2000000, Boolean.FALSE, 100000, cal.getTime());
            Map<String, AttributeValue> item = ca1.toDynamoMap();
            DynamoDBService.putItem(TABLE_NAME, item);

            // Add another item
            CarAdvert ca2 = new CarAdvert("Audi A6", CarAdvert.FuelType.GASOLINE, 3500000, Boolean.TRUE, 0, cal.getTime());
            item = ca2.toDynamoMap();
            DynamoDBService.putItem(TABLE_NAME, item);

            // Check if both items are present in the DB
            CarAdvert ca3 = CarAdvert.fromDynamoItem(DynamoDBService.getItem(TABLE_NAME, ca1.getId()));
            Assert.assertEquals(ca1.getTitle(), ca3.getTitle());

            CarAdvert ca4 = CarAdvert.fromDynamoItem(DynamoDBService.getItem(TABLE_NAME, ca2.getId()));
            Assert.assertEquals(ca2.getTitle(), ca4.getTitle());

        } catch (AmazonClientException ace) {
            fail();
        } catch (ParseException pse) {
            fail();
        }
    }

    @Test
    public void scanTest() {
        try {
            // Add an item
            CarAdvert ca1 = new CarAdvert("Renault Megane", CarAdvert.FuelType.DIESEL, 1000000, Boolean.FALSE, 100000, cal.getTime());
            Map<String, AttributeValue> item = ca1.toDynamoMap();
            DynamoDBService.putItem(TABLE_NAME, item);

            // Add another item
            CarAdvert ca2 = new CarAdvert("Volkswagen Passat", CarAdvert.FuelType.GASOLINE, 3220000, Boolean.TRUE, 0, cal.getTime());
            item = ca2.toDynamoMap();
            DynamoDBService.putItem(TABLE_NAME, item);

            // Scan for adverts with a price attribute greater than 2500000
            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.GT.toString())
                    .withAttributeValueList(new AttributeValue().withN("2500000"));
            scanFilter.put("price", condition);

            ScanResult scanResult = DynamoDBService.scan(TABLE_NAME, scanFilter);
            System.out.println("Result: " + scanResult);

            Assert.assertSame(1, scanResult.getCount());
            Map<String, AttributeValue> firstResult = scanResult.getItems().iterator().next();
            Assert.assertEquals("Volkswagen Passat", firstResult.get("title").getS());
        } catch (AmazonClientException ace) {
            fail();
        }
    }

    @Test
    public void updateTest() {
        try {
            // Add an item
            CarAdvert ca1 = new CarAdvert("Volvo X60", CarAdvert.FuelType.DIESEL, 5000000, Boolean.TRUE, 0, cal.getTime());
            Map<String, AttributeValue> item = ca1.toDynamoMap();
            DynamoDBService.putItem(TABLE_NAME, item);

            // update the item
            ca1.setPrice(6000000);
            ca1.setTitle("Volvo XC60");
            DynamoDBService.updateItem(TABLE_NAME, ca1.toDynamoMap());

            // get the item
            CarAdvert ca2 = CarAdvert.fromDynamoItem(DynamoDBService.getItem(TABLE_NAME, ca1.getId()));
            Assert.assertEquals(ca1.getTitle(), ca2.getTitle());
            Assert.assertEquals(ca1.getPrice(), ca2.getPrice());
        } catch (AmazonClientException ace) {
            fail();
        } catch (ParseException pse) {
            fail();
        }
    }

}
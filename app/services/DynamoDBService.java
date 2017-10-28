package services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.HashMap;
import java.util.Map;


public class DynamoDBService {

    private static AmazonDynamoDB client;
    private static DynamoDB dynamoDB;

    static {
        client = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000","us-west-2")).build();
        dynamoDB = new DynamoDB(client);
    }

    public static void createTable(String tableName, String primaryKeyAttributeName) {
        // Create a table with a primary hash key, which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                .withKeySchema(new KeySchemaElement().withAttributeName(primaryKeyAttributeName).withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName(primaryKeyAttributeName).withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
        TableDescription createdTableDescription = client.createTable(createTableRequest).getTableDescription();
        System.out.println("Created Table: " + createdTableDescription);
    }

    public static void waitForTableToBecomeAvailable(String tableName) {
        System.out.println("Waiting for " + tableName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10 * 60 * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {
                TableDescription tableDescription = getTableDescription(tableName);
                String tableStatus = tableDescription.getTableStatus();
                System.out.println("  - current state: " + tableStatus);
                if (tableStatus.equals(TableStatus.ACTIVE.toString())) return;
            } catch (AmazonServiceException ase) {
                if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false) throw ase;
            }
            try {
                Thread.sleep(1000 * 5);
            }
            catch (Exception e) {
            }
        }

        throw new RuntimeException("Table " + tableName + " never went active");
    }

    public static TableDescription getTableDescription(String tableName) {
        DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
        return client.describeTable(describeTableRequest).getTable();
    }

    public static void deleteTable(String tableName) {
        client.deleteTable(tableName);
    }

    /**
     * Create new item, i.e. a POST operation.
     *
     * @param tableName
     * @param item
     */
    public static PutItemResult putItem(String tableName, Map<String, AttributeValue> item) {
        PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
        PutItemResult putItemResult = client.putItem(putItemRequest);
        System.out.println("Result: " + putItemResult);
        return putItemResult;
    }

    public static UpdateItemResult updateItem(String tableName, Map<String, AttributeValue> item) {
        Table table = dynamoDB.getTable(tableName);

        // prepare attribute names
        Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
        for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
            String key = ":" + entry.getKey();
            AttributeValue value = entry.getValue();

            expressionAttributeValues.put(key, value.toString());
        }

        UpdateItemOutcome outcome = table.updateItem(
            "id",
            item.get("id"),
            "set title = :title set fuel = :fuel set price = :price set isNew = :isNew set mileage = :mileage set first_registration = :first_registration",
            null,
            expressionAttributeValues
        );

        return outcome.getUpdateItemResult();
    }

    public static Item getItem(String tableName, String id) {
        Table table = dynamoDB.getTable(tableName);
        return table.getItem("id", id);
    }

    public static ScanResult scan(String tableName, HashMap<String, Condition> scanFilter) {
        ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
        ScanResult scanResult = client.scan(scanRequest);
        System.out.println("Result: " + scanResult);
        return scanResult;
    }
}
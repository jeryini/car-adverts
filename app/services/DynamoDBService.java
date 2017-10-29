package services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.*;
import models.CarAdvert;

import java.util.*;


public class DynamoDBService {

    private static AmazonDynamoDB client;
    private static DynamoDBMapper mapper;

    static {
        // connect client to local endpoint
        client = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000","us-west-2")).build();

        // mapper for easier CRUD operations
        mapper = new DynamoDBMapper(client);
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
        try {
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
            return client.describeTable(describeTableRequest).getTable();
        } catch (ResourceNotFoundException rnfe) {
            return null;
        }
    }

    public static void deleteTable(String tableName) {
        client.deleteTable(tableName);
    }

    /**
     * If table already exists, then delete it. In either case create table in the end.
     *
     * @param tableName
     */
    public static void resetTable(String tableName) {
        try {
            TableDescription table = getTableDescription(tableName);

            if (table != null) {
                deleteTable(tableName);
            }

            createTable(tableName, "Id");
        } catch ( AmazonServiceException ase ) {
            if (!ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException"))
                throw ase;
        }
    }

    /**
     * Create new item or update existing one.
     *
     * @param item
     */
    public static void saveItem(CarAdvert item) {
        mapper.save(item);
    }

    public static CarAdvert getItem(String id) {
        return mapper.load(CarAdvert.class, id, DynamoDBMapperConfig.ConsistentReads.CONSISTENT.config());
    }

    public static void deleteItem(CarAdvert carAdvert) {
        mapper.delete(carAdvert);
    }

    public static List<CarAdvert> getAll(String sort) {
        PaginatedScanList<CarAdvert> adverts = mapper.scan(CarAdvert.class, new DynamoDBScanExpression());
        List<CarAdvert> advertsAll = new ArrayList<CarAdvert>(adverts);

        // for now do in memory sorting, though it would be wise to set up indexes to support sorting on the DB side
        Comparator cmp;
        switch (sort) {
            case "id":
                cmp = Comparator.comparing(CarAdvert::getId);
                break;
            case "title":
                cmp = Comparator.comparing(CarAdvert::getTitle);
                break;
            case "fuel":
                cmp = Comparator.comparing(CarAdvert::getFuel);
                break;
            case "price":
                cmp = Comparator.comparing(CarAdvert::getPrice);
                break;
            case "isNew":
                cmp = Comparator.comparing(CarAdvert::getIsNew);
                break;
            case "mileage":
                cmp = Comparator.comparing(CarAdvert::getMileage);
                break;
            case "first_registration":
                cmp = Comparator.comparing(CarAdvert::getFirst_registration);
                break;
            default:
                cmp = Comparator.comparing(CarAdvert::getId);
                break;
        }

        advertsAll.sort(cmp);
        return advertsAll;
    }

    public static ScanResult scan(String tableName, HashMap<String, Condition> scanFilter) {
        ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
        ScanResult scanResult = client.scan(scanRequest);
        System.out.println("Result: " + scanResult);
        return scanResult;
    }
}
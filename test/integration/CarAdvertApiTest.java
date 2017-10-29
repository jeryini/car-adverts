package integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import models.CarAdvert;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import services.DynamoDBService;

import java.io.IOException;
import java.util.Calendar;

import static junit.framework.TestCase.fail;

public class CarAdvertApiTest {
    public static final String TABLE_NAME = "AdvertsCatalog";
    private static final String BASE_URL = "http://localhost:9000";
    public static Calendar cal;

    @BeforeClass
    public static void resetTable() {
        // Create a table with a primary hash key named 'name', which holds a string
        DynamoDBService.resetTable(TABLE_NAME);

        // Wait for it to become active
        DynamoDBService.waitForTableToBecomeAvailable(TABLE_NAME);

        // Describe our new table
        System.out.println("Table Description: " + DynamoDBService.getTableDescription(TABLE_NAME));
    }

    @BeforeClass
    public static void configureCalendar() {
        cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2000);
        cal.set(Calendar.MONTH, Calendar.JUNE);
        cal.set(Calendar.DAY_OF_MONTH, 20);
    }

    @BeforeClass
    public static void setObjectMapper() {
        // Only one time
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test
    public void createTest() {
        try {
            CarAdvert ca1 = new CarAdvert("Alfa Romeo 155", CarAdvert.FuelType.GASOLINE, 500000, Boolean.FALSE, 150000, cal.getTime());
            HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:9000")
                .header("Content-Type", "application/json")
                .body(ca1)
                .asJson();
            Assert.assertEquals(201, jsonResponse.getStatus());
        } catch (UnirestException e) {
            fail();
        }
    }
}

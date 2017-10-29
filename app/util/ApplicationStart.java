package util;

import javax.inject.*;

import models.CarAdvert;
import play.inject.ApplicationLifecycle;
import play.Environment;
import services.DynamoDBService;

import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

// This creates an `ApplicationStart` object once at start-up.
@Singleton
public class ApplicationStart {

    public static final String TABLE_NAME = "AdvertsCatalog";

    // Inject the application's Environment upon start-up and register hook(s) for shut-down.
    @Inject
    public ApplicationStart(ApplicationLifecycle lifecycle, Environment environment) {

        // Shut-down hook
        lifecycle.addStopHook( () -> {
            return CompletableFuture.completedFuture(null);
        } );

        // Create a table with a primary hash key named 'name', which holds a string
        DynamoDBService.resetTable(TABLE_NAME);

        // Wait for it to become active
        DynamoDBService.waitForTableToBecomeAvailable(TABLE_NAME);

        // Add test data
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 10);
        
        // Add an item
        CarAdvert ca1 = new CarAdvert("BMW Series 3", CarAdvert.FuelType.DIESEL, 2000000, Boolean.FALSE, 100000, cal.getTime());
        DynamoDBService.saveItem(ca1);

        // Add another item
        CarAdvert ca2 = new CarAdvert("Audi A6", CarAdvert.FuelType.GASOLINE, 3500000, Boolean.TRUE, 0, cal.getTime());
        DynamoDBService.saveItem(ca2);
    }
}
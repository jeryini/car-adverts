package util;

import javax.inject.*;
import play.inject.ApplicationLifecycle;
import play.Environment;
import services.DynamoDBService;

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
    }
}
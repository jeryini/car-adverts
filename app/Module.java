import com.google.inject.AbstractModule;
import util.ApplicationStart;

// A Module is needed to register bindings
public class Module extends AbstractModule {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("application");

    @Override
    public void configure() {
        logger.info("Reseting DB");
        bind(ApplicationStart.class).asEagerSingleton();
    }
}
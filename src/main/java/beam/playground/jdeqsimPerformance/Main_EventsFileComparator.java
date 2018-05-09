package beam.playground.jdeqsimPerformance;

import org.apache.log4j.Logger;
import org.matsim.utils.eventsfilecomparison.EventsFileComparator;

public class Main_EventsFileComparator {
    private static final Logger log = Logger.getLogger(Main_EventsFileComparator.class);
    public static void main(String[] args) {
        if (args.length != 2) {
            log.error("Expected 2 events files as input arguments but found "+args.length);
            log.info("MainEventsFileComparator eventsFileAbsolutePath1 eventsFileAbsolutePath2");
        } else {
            String filePath   = args[0];
            String filePath1 = args[1];

            final EventsFileComparator.Result compare = EventsFileComparator.compare(filePath, filePath1);
            switch(compare){
                case DIFFERENT_NUMBER_OF_TIMESTEPS:
                    log.info("Different number of time steps");
                    break;
                case DIFFERENT_TIMESTEPS:
                    log.info("Different time steps");
                    break;
                case FILES_ARE_EQUAL:
                    log.info("Files are equal");
                    break;
                case MISSING_EVENT:
                    log.info("Missing Events");
                    break;
                case WRONG_EVENT_COUNT:
                    log.info("Wrong event count");
                    break;
            }
        }
    }

}

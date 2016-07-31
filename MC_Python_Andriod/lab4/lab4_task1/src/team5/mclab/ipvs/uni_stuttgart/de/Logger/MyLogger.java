package team5.mclab.ipvs.uni_stuttgart.de.Logger;

import java.util.logging.*;

/**
 * Created by fangjun on 11/06/16.
 */
public class MyLogger {
    static final String loggerName = "team5-lab4";
    static private Logger logger = null;

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(loggerName);

            logger.setUseParentHandlers(false);

            ConsoleHandler handler = new ConsoleHandler();
            //handler.setFormatter(new SimpleFormatter());
            handler.setFormatter(new SingleLineFormatter());
            logger.addHandler(handler);

            // logger.setLevel(Level.OFF);
            // logger.setLevel(Level.FINER);
            logger.setLevel(Level.INFO);
        }
        return logger;
    }
}

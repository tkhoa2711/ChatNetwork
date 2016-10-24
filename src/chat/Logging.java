package chat;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;

/**
 * Utility class for logging.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class Logging {

    private static final String LOG_FILE = "log";

    private static AsyncFileHandler ASYNC_FILE_HANDLER;
    private static Thread worker;

    private static final Formatter LOG_FORMATTER = new Formatter() {
        private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

        @Override
        public String format(LogRecord record) {
            return "[" + DATE_FORMAT.format(new Date(record.getMillis())) + "] "
                    + record.getSourceClassName() + "." + record.getSourceMethodName()
                    + " " + record.getLevel() + " - "
                    + formatMessage(record)
                    + "\n";
        }
    };

    static {
        try {
            ASYNC_FILE_HANDLER = new AsyncFileHandler(LOG_FILE);
            ASYNC_FILE_HANDLER.setFormatter(LOG_FORMATTER);
            worker = new Thread(ASYNC_FILE_HANDLER);
            worker.start();
        } catch (IOException e) {
            UserInterface.display("Unable to setup log file");
            ASYNC_FILE_HANDLER = null;
        }
    }

    /**
     * Setup the logger instance with pre-configured format.
     *
     * @param logger    the logger instance to setup
     * @return          the logger instance
     */
    public static Logger setup(Logger logger) {
        logger.setLevel(Level.ALL);         // we want every log messages
        logger.setUseParentHandlers(false); // don't use log handlers from parent classes

        // add a console handler if the program is not started using command line
        // we don't want to mess with the standard input/output
        // http://stackoverflow.com/questions/2870291/is-there-a-way-to-know-if-a-java-program-was-started-from-the-command-line-or-fr
        java.io.Console console = System.console();
        if (console == null) {
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(LOG_FORMATTER);
            logger.addHandler(consoleHandler);
        }

        if (ASYNC_FILE_HANDLER != null)
            logger.addHandler(ASYNC_FILE_HANDLER);
        return logger;
    }

    /**
     * Stop the logging worker thread.
     */
    public static void stop() {
        ASYNC_FILE_HANDLER.close();
        worker.interrupt();
    }
}

class AsyncFileHandler extends FileHandler implements Runnable {

    private final LinkedBlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>();

    private final ExecutorService executor = Executors.newScheduledThreadPool(10);

    /**
     * Create an asynchronous log-file handler.
     *
     * @param pattern       the log file name
     * @throws IOException  if there's error during file accessing
     */
    public AsyncFileHandler(String pattern) throws IOException {
        super(pattern);
    }

    @Override
    public void publish(LogRecord record) {
        int logLevel = getLevel().intValue();
        if (record.getLevel().intValue() < logLevel || logLevel == Level.OFF.intValue())
            return;

        // infer the caller to preserve information about source class and method name
        record.getSourceMethodName();
        record.getSourceClassName();

        executor.submit(() -> addToQueue(record));
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                super.publish(this.queue.take());
            }
        } catch (InterruptedException e) {
            close();
        }
    }

    @Override
    public synchronized void close() {
        Thread.currentThread().interrupt();
        shutdownQueue();
        executor.shutdownNow();
        super.close();
    }

    /**
     * Add a new log record to the queue.
     *
     * @param record    the log record
     */
    private void addToQueue(LogRecord record) {
        try {
            this.queue.put(record);
        } catch (InterruptedException expected) {
            // it's fine if the thread is interrupted
        }
    }

    /**
     * Shutdown the log queue by flushing all remaining items.
     */
    private void shutdownQueue() {
        LogRecord record;
        while ((record = this.queue.poll()) != null) {
            super.publish(record);
        }
    }
}

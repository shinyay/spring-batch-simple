package io.pivotal.shinyay.batch.configuration.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

public class CustomerSkipListener implements SkipListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onSkipInRead(Throwable t) {
        logger.info(">> Skipping");
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        logger.info(">> Skipping: " + item);
        logger.info(">> Failed processing: " + t);
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        logger.info(">> Skipping: " + item);
        logger.info(">> Failed writing: " + t);
    }
}

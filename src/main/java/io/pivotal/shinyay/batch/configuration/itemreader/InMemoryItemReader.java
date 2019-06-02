package io.pivotal.shinyay.batch.configuration.itemreader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;

public class InMemoryItemReader implements ItemReader<String> {

    private Iterator<String> data;

    public InMemoryItemReader(Iterator<String> data) {
        this.data = data;
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        if(data.hasNext()) {
            return data.next();
        } else {
            return null;
        }
    }
}

package io.pivotal.shinyay.batch.configuration.itemprocessor;

import org.springframework.batch.item.ItemProcessor;

public class SkipItemProcessor implements ItemProcessor<String, String> {
    private int procCount = 0;

    @Override
    public String process(String item) throws Exception {
        if(item.equals("42")) {
            throw new RuntimeException("Process Failed");
        } else {
            return String.valueOf(Integer.parseInt(item) * -1);
        }
    }
}
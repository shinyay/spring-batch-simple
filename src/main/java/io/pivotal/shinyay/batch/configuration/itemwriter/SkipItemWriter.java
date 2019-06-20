package io.pivotal.shinyay.batch.configuration.itemwriter;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class SkipItemWriter implements ItemWriter<String> {
    private int execCount =0;

    @Override
    public void write(List<? extends String> items) throws Exception {
        items.forEach(item -> {
            if(item.equals("-84")) {
                throw new RuntimeException("Write Failed: " + execCount);
            } else {
                System.out.println(item);
            }
        });
    }
}
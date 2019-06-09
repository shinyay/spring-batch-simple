package io.pivotal.shinyay.batch.configuration.itemreader;

import org.springframework.batch.item.*;

import java.util.List;

public class CustomItemReader implements ItemStreamReader<String > {

    private List<String> items;

    public CustomItemReader(List<String> items) {
        this.items = items;
    }

    private int curIndex = 0;
    private boolean restart = false;

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        System.out.println(">>> [READ]");
        String item = "";

        if(curIndex < items.size()) {
            item = items.get(curIndex);
            this.curIndex ++;
        }

        if(curIndex == 42 && !restart) {
            throw new RuntimeException("42: The Answer to the Ultimate Question of Life, the Universe, and Everything");
        }

        return item;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

        System.out.println(">>> [OPEN]");
        if(executionContext.containsKey("curIndex")) {
            curIndex = executionContext.getInt("curIndex");
            restart = true;
        } else {
            curIndex = 0;
            executionContext.put("curIndex", curIndex);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println(">>> [UPDATE]");
        executionContext.put("curIndex", curIndex);
    }

    @Override
    public void close() throws ItemStreamException {
        System.out.println(">>> [CLOSE]");
    }
}

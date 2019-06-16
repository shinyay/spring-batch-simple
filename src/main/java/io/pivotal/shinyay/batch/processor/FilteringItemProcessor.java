package io.pivotal.shinyay.batch.processor;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import org.springframework.batch.item.ItemProcessor;

public class FilteringItemProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer item) throws Exception {
        if (item.getId() % 2L != 0L) {
            return item;
        } else {
            return null;
        }
    }
}

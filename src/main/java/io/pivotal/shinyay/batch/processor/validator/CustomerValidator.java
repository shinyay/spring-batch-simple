package io.pivotal.shinyay.batch.processor.validator;

import io.pivotal.shinyay.batch.domain.customer.Customer;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

public class CustomerValidator implements Validator<Customer> {

    @Override
    public void validate(Customer value) throws ValidationException {
        if(value.getFirstName().startsWith("A")) {
            throw new ValidationException("First Name starts with A should be removed: " + value.toString());
        }
    }
}

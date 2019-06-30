package io.pivotal.shinyay.batch.controller;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class JobLaunchingController {

    private JobOperator jobOperator;

    public JobLaunchingController(JobOperator jobOperator) {
        this.jobOperator = jobOperator;
    }

    @PostMapping(name = "/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void launch(@RequestParam("name") String name) throws JobParametersInvalidException, JobInstanceAlreadyExistsException, NoSuchJobException {
        jobOperator.start("parameter-tasklet-job", "name=" + name);
    }

}

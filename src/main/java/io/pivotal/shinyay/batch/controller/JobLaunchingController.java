package io.pivotal.shinyay.batch.controller;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void stop(@PathVariable("id") long id) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
        jobOperator.stop(id);
    }
}

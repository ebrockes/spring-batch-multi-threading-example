package br.com.sample.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

public class JobCompletionNotificationListener extends JobExecutionListenerSupport {
		
	@Override
	public void beforeJob(JobExecution jobExecution){
		super.beforeJob(jobExecution);
	}
	
	@Override
	public void afterJob(JobExecution jobExecution){
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			System.out.println("Job Completed");
		}
	}

}

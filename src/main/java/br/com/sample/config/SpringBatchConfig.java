package br.com.sample.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import br.com.sample.batch.Attempt;
import br.com.sample.batch.AttemptProcessor;
import br.com.sample.batch.AttemptReader;
import br.com.sample.batch.AttemptWriter;
import br.com.sample.listener.ChunkExecutionListener;
import br.com.sample.listener.JobCompletionNotificationListener;
import br.com.sample.listener.StepExecutionNotificationListener;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

	public final static Logger logger = LoggerFactory.getLogger(SpringBatchConfig.class);

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Value("${chunk-size}")
	private int chunkSize;

	@Value("${max-threads}")
	private int maxThreads;

	@Bean
	public AttemptReader processAttemptReader() {
		return new AttemptReader();
	}

	@Bean
	public AttemptProcessor processAttemptProcessor() {
		return new AttemptProcessor();
	}

	@Bean
	public AttemptWriter processAttemptWriter() {
		return new AttemptWriter();
	}

	@Bean
	public JobCompletionNotificationListener jobExecutionListener() {
		return new JobCompletionNotificationListener();
	}
	
	@Bean
	public StepExecutionNotificationListener stepExecutionListener() {
		return new StepExecutionNotificationListener();
	}
	
	@Bean
	public ChunkExecutionListener chunkListener() {
		return new ChunkExecutionListener();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(maxThreads);
		return taskExecutor;
	}

	@Bean
	public Job processAttemptJob() {
		return jobBuilderFactory.get("process-attempt-job")
				.incrementer(new RunIdIncrementer())
				.listener(jobExecutionListener())
				.flow(step()).end().build();
	}

	@Bean
	public Step step() {
		return stepBuilderFactory.get("step").<Attempt, Attempt>chunk(chunkSize)
				.reader(processAttemptReader())
				.processor(processAttemptProcessor())
				.writer(processAttemptWriter())
				.taskExecutor(taskExecutor())
				.listener(stepExecutionListener())
				.listener(chunkListener())
				.throttleLimit(maxThreads).build();
	}

}

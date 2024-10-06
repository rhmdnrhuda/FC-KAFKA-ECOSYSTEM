package com.dummy.transaction;

import com.dummy.transaction.service.DummyTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DummyTransactionApplication {

	public static void main(String[] args) throws JsonProcessingException {
		ApplicationContext context = SpringApplication.run(DummyTransactionApplication.class, args);

		// Get the DummyTransactionService bean from the context
		DummyTransactionService dummyTransactionService = context.getBean(DummyTransactionService.class);

		// Call the generateAndPublishDummyTransaction method
		dummyTransactionService.generateAndPublishDummyTransaction();
	}

}

package com.kafkademo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafkademo.domain.Music;
import com.kafkademo.domain.MusicEvent;
import com.kafkademo.producer.MusicEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
class MusicEventProducerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void testNetworkFailure() throws JsonProcessingException {

	}

}


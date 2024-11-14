package com.chat

import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoDatabase
import org.bson.Document
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono;


@Testcontainers
@AutoConfigureWebTestClient(timeout = "3600000")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient

    @Container
    protected static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.12")
            .withReuse(true)

    protected static MongoDatabase database

    @DynamicPropertySource
    protected static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
    }

    @BeforeAll
    static void setup() {
        String mongoUri = mongoDBContainer.getReplicaSetUrl()
        def mongoClient = MongoClients.create(mongoUri)
        database = mongoClient.getDatabase("testdb")
        println("MongoDB контейнер запущен: " + mongoUri)
    }

    @BeforeEach
    void clearDatabase() {
        Flux.from(database.listCollectionNames())
                .flatMap { collectionName ->
                    Mono.from(database.getCollection(collectionName).deleteMany(new Document()))
                }
                .then()
                .block()
        println("All collections in 'testdb' have been cleared.")
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop()
    }
}
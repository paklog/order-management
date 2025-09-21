package com.paklog.ordermanagement.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.paklog.ordermanagement.domain.model.Address;
import com.paklog.ordermanagement.domain.model.FulfillmentOrder;
import com.paklog.ordermanagement.domain.model.OrderItem;
import com.paklog.ordermanagement.infrastructure.repository.mongodb.MongoFulfillmentOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final MongoFulfillmentOrderRepository fulfillmentOrderRepository;
    private final Faker faker = new Faker();

    public DatabaseSeeder(MongoFulfillmentOrderRepository fulfillmentOrderRepository) {
        this.fulfillmentOrderRepository = fulfillmentOrderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Running database seeder...");

        if (fulfillmentOrderRepository.count() == 0) {
            log.info("No orders found. Seeding database with 100 orders...");
            for (int i = 0; i < 100; i++) {
                fulfillmentOrderRepository.saveOrder(createFakeOrder());
            }
            log.info("Database seeding complete.");
        } else {
            log.info("Database already contains orders. Skipping seeding.");
        }
    }

    private FulfillmentOrder createFakeOrder() {
        List<OrderItem> items = new ArrayList<>();
        int itemCount = faker.number().numberBetween(1, 5);
        for (int i = 0; i < itemCount; i++) {
            items.add(new OrderItem(
                faker.commerce().productName(),
                "item-" + i,
                faker.number().numberBetween(1, 10),
                faker.lorem().sentence(),
                faker.lorem().sentence()
            ));
        }

        return new FulfillmentOrder(
            UUID.randomUUID(),
            "seller-order-" + faker.number().digits(10),
            "display-order-" + faker.number().digits(8),
            LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30)),
            faker.lorem().sentence(),
            "STANDARD",
            new Address(
                faker.name().fullName(),
                faker.address().streetAddress(),
                faker.address().secondaryAddress(),
                faker.address().city(),
                faker.address().stateAbbr(),
                faker.address().zipCode(),
                "US"
            ),
            items,
            UUID.randomUUID().toString()
        );
    }
}

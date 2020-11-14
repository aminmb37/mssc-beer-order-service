package com.amin.beer.order.service.services;

import com.amin.beer.order.service.domain.BeerOrder;
import com.amin.beer.order.service.domain.BeerOrderLine;
import com.amin.beer.order.service.domain.BeerOrderStatusEnum;
import com.amin.beer.order.service.domain.Customer;
import com.amin.beer.order.service.repositories.BeerOrderRepository;
import com.amin.beer.order.service.repositories.CustomerRepository;
import com.amin.beer.order.service.services.beer.BeerServiceImpl;
import com.amin.brewery.model.BeerDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(WireMockExtension.class)
public class BeerOrderManagerImplIT {
    private final UUID beerId = UUID.randomUUID();
    @Autowired
    private BeerOrderManager beerOrderManager;
    @Autowired
    private BeerOrderRepository beerOrderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WireMockServer wireMockServer;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder().customerName("Test Customer").build());
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12354").build();
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12354")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder();
        beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            Optional<BeerOrder> foundOrderOptional = beerOrderRepository.findById(beerOrder.getId());
            foundOrderOptional.ifPresent(foundOrder ->
                    assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus()));
        });

        await().untilAsserted(() -> {
            Optional<BeerOrder> foundOrderOptional = beerOrderRepository.findById(beerOrder.getId());
            foundOrderOptional.ifPresent(foundOrder -> foundOrder.getBeerOrderLines().forEach(beerOrderLine ->
                    assertEquals(beerOrderLine.getOrderQuantity(), beerOrderLine.getQuantityAllocated())));
        });

        Optional<BeerOrder> savedBeerOrderOptional = beerOrderRepository.findById(beerOrder.getId());
        savedBeerOrderOptional.ifPresent(savedBeerOrder -> {
            assertNotNull(savedBeerOrder);
            assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder.getOrderStatus());
            savedBeerOrder.getBeerOrderLines().forEach(beerOrderLine ->
                    assertEquals(beerOrderLine.getOrderQuantity(), beerOrderLine.getQuantityAllocated()));
        });
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12354").build();
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12354")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder();
        beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            Optional<BeerOrder> foundOrderOptional = beerOrderRepository.findById(beerOrder.getId());
            foundOrderOptional.ifPresent(foundOrder ->
                    assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus()));
        });

        beerOrderManager.beerOrderPickedUp(beerOrder.getId());

        await().untilAsserted(() -> {
            Optional<BeerOrder> foundOrderOptional = beerOrderRepository.findById(beerOrder.getId());
            foundOrderOptional.ifPresent(foundOrder ->
                    assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus()));
        });

        Optional<BeerOrder> pickedUpOrderOptional = beerOrderRepository.findById(beerOrder.getId());
        pickedUpOrderOptional.ifPresent(pickedUpOrder ->
                assertEquals(BeerOrderStatusEnum.PICKED_UP, pickedUpOrder.getOrderStatus()));
    }

    @Test
    void testFailedValidation() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12354").build();
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12354")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("fail-validation");
        beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            Optional<BeerOrder> foundOrderOptional = beerOrderRepository.findById(beerOrder.getId());
            foundOrderOptional.ifPresent(foundOrder ->
                    assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus()));
        });
    }

    @Test
    void testAllocationFailure() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12354").build();
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12354")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("fail-allocation");
        beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            Optional<BeerOrder> foundOrderOptional = beerOrderRepository.findById(beerOrder.getId());
            foundOrderOptional.ifPresent(foundOrder ->
                    assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus()));
        });
    }

    @Test
    void testPartialAllocation() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12354").build();
        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12354")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("partial-allocation");
        beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            Optional<BeerOrder> foundOrderOptional = beerOrderRepository.findById(beerOrder.getId());
            foundOrderOptional.ifPresent(foundOrder ->
                    assertEquals(BeerOrderStatusEnum.PENDING_INVENTORY, foundOrder.getOrderStatus()));
        });
    }

    private BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();
        Set<BeerOrderLine> beerOrderLines = new HashSet<>();
        beerOrderLines.add(BeerOrderLine.builder().beerId(beerId)
                .upc("12354").orderQuantity(1).beerOrder(beerOrder).build());
        beerOrder.setBeerOrderLines(beerOrderLines);
        return beerOrder;
    }

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }
}


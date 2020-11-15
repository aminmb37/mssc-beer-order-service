package com.amin.beer.order.service.services;

import com.amin.beer.order.service.domain.BeerOrder;
import com.amin.brewery.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {
    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void processValidationResult(UUID beerOrderId, boolean isValid);

    void beerOrderAllocationPassed(BeerOrderDto beerOrderDto);

    void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto);

    void beerOrderAllocationFailed(BeerOrderDto beerOrderDto);

    void beerOrderPickedUp(UUID beerOrderId);

    void cancelOrder(UUID beerOrderId);
}

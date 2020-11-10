package com.amin.beer.order.service.services;

import com.amin.beer.order.service.domain.BeerOrder;

public interface BeerOrderManager {
    BeerOrder newBeerOrder(BeerOrder beerOrder);
}

package com.amin.beer.order.service.services.beer;

import com.amin.brewery.model.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerService
{
    Optional<BeerDto> getBeerById(UUID beerId);

    Optional<BeerDto> getBeerByUpc(String beerUpc);
}

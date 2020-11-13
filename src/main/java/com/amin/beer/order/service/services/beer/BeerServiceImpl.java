package com.amin.beer.order.service.services.beer;

import com.amin.brewery.model.BeerDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@ConfigurationProperties(prefix = "amin.brewery", ignoreUnknownFields = false)
public class BeerServiceImpl implements BeerService {
    public static final String BEER_PATH_V1 = "/api/v1/beer/";
    public static final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/";

    private String beerServiceHost;
    private final RestTemplate restTemplate;

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID beerId) {
        return Optional.ofNullable(restTemplate.getForObject(
                beerServiceHost + BEER_PATH_V1 + beerId, BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String beerUpc) {
        return Optional.ofNullable(restTemplate.getForObject(
                beerServiceHost + BEER_UPC_PATH_V1 + beerUpc, BeerDto.class));
    }
}

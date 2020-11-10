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
    private final RestTemplate restTemplate;
    private String beerServiceHost;


    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder)
    {
        this.restTemplate = restTemplateBuilder.build();
    }


    public void setBeerServiceHost(String beerServiceHost)
    {
        this.beerServiceHost = beerServiceHost;
    }


    @Override
    public Optional<BeerDto> getBeerById(UUID beerId)
    {
        String beerPath = "/api/v1/beer/";
        return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + beerPath + beerId, BeerDto.class));
    }


    @Override
    public Optional<BeerDto> getBeerByUpc(String beerUpc)
    {
        String beerUpcPath = "/api/v1/beerUpc/";
        return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + beerUpcPath + beerUpc, BeerDto.class));
    }
}

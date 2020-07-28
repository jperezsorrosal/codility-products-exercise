package com.example.demo;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.empty;

public class SoldProductsAggregator {
    private final EURExchangeService exchangeService;

    SoldProductsAggregator(EURExchangeService EURExchangeService) {
        this.exchangeService = EURExchangeService;
    }

    SoldProductsAggregate aggregate(Stream<SoldProduct> soldProductStream) {

        // convert the products to simple products performing the currency exchange
        // then group by product name
        Map<String, List<SimpleSoldProduct>> byProductName = soldProductStream.map(p -> soldProductToSimple(p))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.groupingBy(SimpleSoldProduct::getName));

        System.out.println(byProductName.toString());

        List<SimpleSoldProduct> products = new ArrayList();
        BigDecimal total = BigDecimal.ZERO;

        for(Map.Entry<String, List<SimpleSoldProduct>> entry: byProductName.entrySet()) {

            // aggregate the total price per product
            BigDecimal totalSumPerProduct = entry.getValue().stream()
                    .map(p -> p.getPrice())
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);

            // add the product with the total prices aggregated to the list
            products.add(new SimpleSoldProduct(entry.getKey(), totalSumPerProduct));

            // add the total price for all the products
            total = total.add(totalSumPerProduct);
        }

        return new SoldProductsAggregate(products, total);

    }

    Optional<SimpleSoldProduct> soldProductToSimple(SoldProduct product) {

        try {
            BigDecimal euroPrice = exchangeService.rate(product.getCurrency()).multiply(product.getPrice());

            SimpleSoldProduct simple = new SimpleSoldProduct(product.getName(), euroPrice);

            return Optional.of(simple);

        } catch(UnknownCurrencyException e) {
            return Optional.empty();
        }

    }

    public static void main(String[] args) {
        EURExchangeService exchangeService = new EURExchangeService();
        SoldProductsAggregator aggregator = new SoldProductsAggregator(exchangeService);

        List<SoldProduct> products = new ArrayList<>();

        products.add(new SoldProduct("iPhonic", BigDecimal.TEN, "CHF"));
        products.add(new SoldProduct("iPhonic", BigDecimal.TEN, "EUR"));
        products.add(new SoldProduct("samsing", BigDecimal.ONE, "EUR"));
        products.add(new SoldProduct("samsing", BigDecimal.TEN, "EUR"));
        products.add(new SoldProduct("samsing", BigDecimal.TEN, "XXX")); // should be ignored


        SoldProductsAggregate aggregate = aggregator.aggregate(products.stream());

        System.out.println(aggregate.toString());

    }
}
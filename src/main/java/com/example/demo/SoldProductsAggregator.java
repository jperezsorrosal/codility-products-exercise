package com.example.demo;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SoldProductsAggregator {
    private final EURExchangeService exchangeService;

    SoldProductsAggregator(EURExchangeService EURExchangeService) {
        this.exchangeService = EURExchangeService;
    }

    SoldProductsAggregate aggregate(Stream<SoldProduct> soldProductStream) {

        // convert the products to simple products performing the currency exchange
        Map<String, List<SimpleSoldProduct>> byProductName = soldProductStream.map(p -> soldProductToSimple(p))
                .collect(Collectors.groupingBy(SimpleSoldProduct::getName));

        System.out.println(byProductName.toString());


        List<SimpleSoldProduct> products = new ArrayList();
        BigDecimal total = BigDecimal.ZERO;

        for(Map.Entry<String, List<SimpleSoldProduct>> entry: byProductName.entrySet()) {

            BigDecimal totalSumPerProduct = entry.getValue().stream().map(p -> p.getPrice())
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);

            products.add(new SimpleSoldProduct(entry.getKey(), totalSumPerProduct));
            total = total.add(totalSumPerProduct);
        }

        return new SoldProductsAggregate(products, total);

    }

    SimpleSoldProduct soldProductToSimple(SoldProduct product) {

        BigDecimal euroPrice = exchangeService.rate(product.getCurrency()).multiply(product.getPrice());

        System.out.println(euroPrice);
        SimpleSoldProduct simple = new SimpleSoldProduct(product.getName(), euroPrice);

        return simple;
    }

    public static void main(String[] args) {
        EURExchangeService exchangeService = new EURExchangeService();
        SoldProductsAggregator aggregator = new SoldProductsAggregator(exchangeService);

        List<SoldProduct> products = new ArrayList<>();

        products.add(new SoldProduct("iPhonic", BigDecimal.TEN, "CHF"));
        products.add(new SoldProduct("iPhonic", BigDecimal.TEN, "EUR"));
        products.add(new SoldProduct("samsing", BigDecimal.ONE, "EUR"));
        products.add(new SoldProduct("samsing", BigDecimal.TEN, "EUR"));


        SoldProductsAggregate aggregate = aggregator.aggregate(products.stream());

        System.out.println(aggregate.toString());

    }
}
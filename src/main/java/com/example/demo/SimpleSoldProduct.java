package com.example.demo;

import lombok.Value;

import java.math.BigDecimal;

@Value
class SimpleSoldProduct {
    String name;
    BigDecimal price;
}
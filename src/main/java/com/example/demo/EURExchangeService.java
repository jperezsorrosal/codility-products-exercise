package com.example.demo;

import java.math.BigDecimal;

public class EURExchangeService {

    public enum Rate {
        EUR(1.0),
        CHF(0.93),
        USD(1.09),
        GBP(0.84);

        private Double rateVal;

        Rate(Double rate) {
            this.rateVal = rate;
        }

        public Double getRateVal() {
            return rateVal;
        }

    }

    BigDecimal rate(String currency) throws UnknownCurrencyException {
        try {
            return BigDecimal.valueOf(Rate.valueOf(currency).getRateVal());
        } catch (IllegalArgumentException e) {
            throw new UnknownCurrencyException("Unknown Currency '" + currency + "'");
        }
    }
}

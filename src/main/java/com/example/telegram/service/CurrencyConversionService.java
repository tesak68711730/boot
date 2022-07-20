package com.example.telegram.service;

import com.example.telegram.enums.Currency;

public interface CurrencyConversionService {

    static CurrencyConversionService getInstance() {
        return new CurrencyConversionServiceImpl();
    }

    double getConversionRatio(Currency original, Currency target);
}

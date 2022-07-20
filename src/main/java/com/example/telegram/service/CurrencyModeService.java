package com.example.telegram.service;

import com.example.telegram.enums.Currency;

public interface CurrencyModeService {

    static CurrencyModeService getInstance() {
        return new CurrencyModeServiceImpl();
    }

    Currency getOriginalCurrency(long chatID);

    Currency getTargetCurrency(long chatID);

    void setOriginalCurrency(long chatID, Currency currency);

    void setTargetCurrency(long chatID, Currency currency);
}

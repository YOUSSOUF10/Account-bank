package com.bank.account.util;

import java.util.Currency;
import org.junit.Test;

import com.bank.account.util.CurrencyUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrencyUtilTest {

    @Test
    public void testConvertEURtoEUR() {
        // Given
        double amount = 1234567890;
        Currency currency = Currency.getInstance("EUR");

        // When
        double converted = CurrencyUtil.convertAmount(amount, currency, currency);

        // Then
        assertThat(converted).isEqualTo(amount);
    }

    @Test
    public void testConvertEURtoUSD() {
        // Given
        double amount = 1234567890;
        Currency eur = Currency.getInstance("EUR");
        Currency usd = Currency.getInstance("USD");

        // When
        double converted = CurrencyUtil.convertAmount(amount, eur, usd);

        // Then
        assertThat(converted).isGreaterThan(amount);
    }



}

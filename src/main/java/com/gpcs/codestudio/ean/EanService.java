package com.gpcs.codestudio.ean;

import org.springframework.stereotype.Service;

@Service
public class EanService {

    public EanValidationResult validateAndCompleteEan13(String base12) {
        if (base12 == null || !base12.matches("\\d{12}")) {
            return EanValidationResult.invalid("Vstup pre EAN-13 musí mať presne 12 číslic.");
        }

        int checkDigit = computeGs1CheckDigitLeftToRight(base12);
        String finalCode = base12 + checkDigit;
        String message = String.format(
                "Hodnota EAN-13 '%s' bola prijatá, kontrolná číslica '%d' bola automaticky doplnená. Výsledný kód je %s.",
                base12, checkDigit, finalCode
        );

        return EanValidationResult.valid(base12, finalCode, checkDigit, message);
    }

    public EanValidationResult validateAndCompleteEan8(String base7) {
        if (base7 == null || !base7.matches("\\d{7}")) {
            return EanValidationResult.invalid("Vstup pre EAN-8 musí mať presne 7 číslic.");
        }

        int checkDigit = computeEan8CheckDigit(base7);
        String finalCode = base7 + checkDigit;
        String message = String.format(
                "Hodnota EAN-8 '%s' bola prijatá, kontrolná číslica '%d' bola automaticky doplnená. Výsledný kód je %s.",
                base7, checkDigit, finalCode
        );

        return EanValidationResult.valid(base7, finalCode, checkDigit, message);
    }

    public EanValidationResult validateAndCompleteUpcA(String base11) {
        if (base11 == null || !base11.matches("\\d{11}")) {
            return EanValidationResult.invalid("Vstup pre UPC-A musí mať presne 11 číslic.");
        }

        int checkDigit = computeGs1CheckDigitLeftToRight(base11);
        String finalCode = base11 + checkDigit;
        String message = String.format(
                "Hodnota UPC-A '%s' bola prijatá, kontrolná číslica '%d' bola automaticky doplnená. Výsledný kód je %s.",
                base11, checkDigit, finalCode
        );

        return EanValidationResult.valid(base11, finalCode, checkDigit, message);
    }

    public EanValidationResult validateAndCompleteItf14(String base13) {
        if (base13 == null || !base13.matches("\\d{13}")) {
            return EanValidationResult.invalid("Vstup pre ITF-14 (GTIN-14 bez kontrolnej číslice) musí mať presne 13 číslic.");
        }

        int checkDigit = computeGs1CheckDigitLeftToRight(base13);
        String finalCode = base13 + checkDigit;
        String message = String.format(
                "Hodnota ITF-14 '%s' bola prijatá, kontrolná číslica '%d' bola automaticky doplnená. Výsledný kód je %s.",
                base13, checkDigit, finalCode
        );

        return EanValidationResult.valid(base13, finalCode, checkDigit, message);
    }

    // GS1 modulo-10 check digit pre EAN-13 / UPC-A / ITF-14 (váhy 1 a 3 zľava)
    private int computeGs1CheckDigitLeftToRight(String base) {
        int[] digits = base.chars().map(c -> c - '0').toArray();
        int sumOdd = 0;   // pozície 1,3,5,... (1-based)
        int sumEven = 0;  // pozície 2,4,6,... (1-based)
        for (int i = 0; i < digits.length; i++) {
            if ((i + 1) % 2 == 0) {
                sumEven += digits[i];
            } else {
                sumOdd += digits[i];
            }
        }
        int total = sumOdd + sumEven * 3;
        int mod = total % 10;
        return (10 - mod) % 10;
    }

    // Špeciálny vzorec pre EAN-8 podľa normy (7 vstupných číslic)
    private int computeEan8CheckDigit(String base7) {
        int[] d = base7.chars().map(c -> c - '0').toArray();
        // váhy zľava: 3,1,3,1,3,1,3
        int sum = 0;
        int[] weights = {3, 1, 3, 1, 3, 1, 3};
        for (int i = 0; i < d.length; i++) {
            sum += d[i] * weights[i];
        }
        int mod = sum % 10;
        return (10 - mod) % 10;
    }
}

package com.gpcs.codestudio.service;

public class EanService {

    public String doplnEan13(String zaklad) {
        if (zaklad == null || zaklad.isBlank()) {
            throw new IllegalArgumentException("Základná hodnota EAN-13 je povinná.");
        }
        if (!zaklad.matches("\\d+")) {
            throw new IllegalArgumentException("Základná hodnota EAN-13 môže obsahovať len číslice.");
        }
        if (zaklad.length() != 12) {
            throw new IllegalArgumentException("Základná hodnota EAN-13 musí mať presne 12 číslic.");
        }

        int suma = 0;
        for (int i = 0; i < 12; i++) {
            int cifra = zaklad.charAt(i) - '0';
            if ((i % 2) == 0) {
                suma += cifra;
            } else {
                suma += cifra * 3;
            }
        }

        int modulo = suma % 10;
        int kontrolnaCislica = modulo == 0 ? 0 : 10 - modulo;

        return zaklad + kontrolnaCislica;
    }
}


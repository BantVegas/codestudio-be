package com.gpcs.codestudio.gs1;

import com.gpcs.codestudio.ean.EanValidationResult;
import org.springframework.stereotype.Service;

/**
 * Základný GS1 service pre jednoduché overenie GS1 reťazcov s Application Identifiers (AI):
 * GS1-128, GS1 DataMatrix, GS1 DataBar.
 *
 * Cieľ: skontrolovať štruktúru (AI v zátvorkách) a niekoľko najčastejších AI
 * (01 – GTIN, 10 – Batch/Lot, 17 – Dátum minimálnej trvanlivosti).
 */
@Service
public class Gs1Service {

    public EanValidationResult validateGs1128(String data) {
        return validateGenericGs1(data, "GS1-128 reťazec");
    }

    public EanValidationResult validateGs1DataMatrix(String data) {
        return validateGenericGs1(data, "GS1 DataMatrix reťazec");
    }

    public EanValidationResult validateGs1DataBar(String data) {
        return validateGenericGs1(data, "GS1 DataBar reťazec");
    }

    private EanValidationResult validateGenericGs1(String data, String label) {
        if (data == null || data.isBlank()) {
            return EanValidationResult.invalid("Vstup pre " + label + " nesmie byť prázdny.");
        }

        String trimmed = data.trim();
        if (!trimmed.startsWith("(")) {
            return EanValidationResult.invalid(label + " musí začínať Application Identifier v zátvorkách, napr. (01)...");
        }

        int pos = 0;
        boolean hasAi01 = false;
        boolean anyAi = false;

        while (pos < trimmed.length()) {
            if (trimmed.charAt(pos) != '(') {
                return EanValidationResult.invalid("Neočakávaný formát pre " + label + ". Očakáva sa '('.");
            }
            int endAi = trimmed.indexOf(')', pos);
            if (endAi <= pos + 1) {
                return EanValidationResult.invalid("AI v " + label + " musí byť v tvare (NN) alebo (NNN).");
            }
            String ai = trimmed.substring(pos + 1, endAi);
            if (!ai.matches("\\d{2,4}")) {
                return EanValidationResult.invalid("Application Identifier musí obsahovať 2 až 4 číslice: '" + ai + "'.");
            }
            pos = endAi + 1;

            // Načítame hodnotu až po ďalšiu '('
            int nextAiStart = trimmed.indexOf('(', pos);
            String value = nextAiStart == -1 ? trimmed.substring(pos) : trimmed.substring(pos, nextAiStart);
            value = value.trim();
            if (value.isEmpty()) {
                return EanValidationResult.invalid("AI (" + ai + ") nemá žiadnu hodnotu.");
            }

            anyAi = true;

            switch (ai) {
                case "01" -> {
                    // GTIN-14: 14 číslic, posledná je GS1 kontrolná číslica
                    if (!value.matches("\\d{14}")) {
                        return EanValidationResult.invalid("AI (01) – GTIN musí mať presne 14 číslic.");
                    }
                    String base13 = value.substring(0, 13);
                    int expectedCd = computeGs1CheckDigitLeftToRight(base13);
                    int actualCd = value.charAt(13) - '0';
                    if (expectedCd != actualCd) {
                        return EanValidationResult.invalid(
                                "AI (01) – GTIN obsahuje nesprávnu kontrolnú číslicu. Očakávaná: " + expectedCd + ", nájdená: " + actualCd + ".");
                    }
                    hasAi01 = true;
                }
                case "10" -> {
                    // Batch/Lot – do 20 alfanumerických znakov podľa normy
                    if (value.length() > 20) {
                        return EanValidationResult.invalid("AI (10) – Batch/Lot môže mať maximálne 20 znakov.");
                    }
                }
                case "11" -> {
                    // Dátum výroby YYMMDD
                    if (!value.matches("\\d{6}")) {
                        return EanValidationResult.invalid("AI (11) – Dátum výroby musí byť v tvare YYMMDD.");
                    }
                }
                case "13" -> {
                    // Dátum balenia YYMMDD
                    if (!value.matches("\\d{6}")) {
                        return EanValidationResult.invalid("AI (13) – Dátum balenia musí byť v tvare YYMMDD.");
                    }
                }
                case "15" -> {
                    // Dátum minimálnej trvanlivosti YYMMDD
                    if (!value.matches("\\d{6}")) {
                        return EanValidationResult.invalid("AI (15) – Dátum minimálnej trvanlivosti musí byť v tvare YYMMDD.");
                    }
                }
                case "16" -> {
                    // Dátum posledného použitia YYMMDD
                    if (!value.matches("\\d{6}")) {
                        return EanValidationResult.invalid("AI (16) – Dátum posledného použitia musí byť v tvare YYMMDD.");
                    }
                }
                case "21" -> {
                    // Serial – do 20 znakov, ľubovoľný alfanumerický reťazec
                    if (value.length() > 20) {
                        return EanValidationResult.invalid("AI (21) – Sériové číslo môže mať maximálne 20 znakov.");
                    }
                }
                case "20" -> {
                    // Variant výrobku – do 2 znakov
                    if (value.length() > 2) {
                        return EanValidationResult.invalid("AI (20) – Variant výrobku môže mať maximálne 2 znaky.");
                    }
                }
                // 30x / 31x – množstvá / hmotnosti s 0–3 desatinnými miestami
                case "30", "31", "310", "311", "312", "313", "314", "315", "316" -> {
                    if (!value.matches("\\d+")) {
                        return EanValidationResult.invalid("AI (" + ai + ") – Hodnota musí obsahovať iba číslice.");
                    }
                    if (value.length() > 6) {
                        return EanValidationResult.invalid("AI (" + ai + ") – Hodnota môže mať maximálne 6 číslic pre množstvo / hmotnosť.");
                    }
                }
                case "37" -> {
                    // Count – do 8 číslic
                    if (!value.matches("\\d{1,8}")) {
                        return EanValidationResult.invalid("AI (37) – Počet kusov musí obsahovať 1 až 8 číslic.");
                    }
                }
                case "700", "701", "702", "703" -> {
                    // Traceability / reference čísla – do 20 znakov
                    if (value.length() > 20) {
                        return EanValidationResult.invalid("AI (" + ai + ") – Hodnota môže mať maximálne 20 znakov.");
                    }
                }
                default -> {
                    // Ostatné AI zatiaľ len akceptujeme bez detailnej validácie
                }
            }

            pos = nextAiStart == -1 ? trimmed.length() : nextAiStart;
        }

        if (!anyAi) {
            return EanValidationResult.invalid(label + " neobsahuje žiadny Application Identifier.");
        }

        StringBuilder msg = new StringBuilder();
        msg.append(label).append(" bol úspešne overený.");
        if (hasAi01) {
            msg.append(" Hlavný GTIN (AI 01) má platnú kontrolnú číslicu.");
        }

        // finalCode necháme ako pôvodný reťazec, keďže nič nedoplňame.
        // Kontrolná číslica v tomto kontexte nedáva zmysel, nastavíme ju na 0.
        return EanValidationResult.valid(trimmed, trimmed, 0, msg.toString());
    }

    // Rovnaký GS1 modulo-10 algoritmus ako pri EAN-13 / UPC-A / ITF-14
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
}

package com.gpcs.codestudio.gs1;

import com.gpcs.codestudio.ean.EanValidationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/gs1")
public class Gs1Controller {

    private final Gs1Service gs1Service;

    public Gs1Controller(Gs1Service gs1Service) {
        this.gs1Service = gs1Service;
    }

    /**
     * Univerzálny endpoint na validáciu GS1 reťazcov (GS1-128, GS1 DataMatrix, GS1 DataBar).
     *
     * Príklad requestu z FE:
     * POST /api/gs1/validate
     * { "codeType": "GS1128", "value": "(01)12345678901234(10)ABC123" }
     */
    @PostMapping("/validate")
    public ResponseEntity<EanValidationResult> validate(@RequestBody Gs1ValidationRequest request) {
        String type = request.getCodeType() != null
                ? request.getCodeType().trim().toUpperCase(Locale.ROOT)
                : "";
        String value = request.getValue();

        EanValidationResult result;

        switch (type) {
            case "GS1128":
            case "GS1-128":
            case "GS1_128":
                result = gs1Service.validateGs1128(value);
                break;

            case "GS1DM":
            case "GS1_DATAMATRIX":
            case "DATAMATRIX":
                result = gs1Service.validateGs1DataMatrix(value);
                break;

            case "GS1DATABAR":
                result = gs1Service.validateGs1DataBar(value);
                break;

            default:
                // fallback – zober to ako generický GS1 reťazec
                result = gs1Service.validateGs1128(value);
                break;
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Ak chceš mať špeciálne "linear" API, môžeš si nechať aj alias:
     */
    @PostMapping("/validate-linear")
    public ResponseEntity<EanValidationResult> validateLinear(@RequestBody Gs1ValidationRequest request) {
        // Sem môžeš neskôr dať špecifickejšie pravidlá pre linear,
        // zatiaľ len voláme hlavný validate().
        return validate(request);
    }
}

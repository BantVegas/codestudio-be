package com.gpcs.codestudio.api;

import com.gpcs.codestudio.ean.EanService;
import com.gpcs.codestudio.ean.EanValidationResult;
import com.gpcs.codestudio.gs1.Gs1Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiCodeController")
@RequestMapping("/api/code")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5176", "http://localhost:5180"})
public class CodeController {

    private final EanService eanService;
    private final Gs1Service gs1Service;

    public CodeController(EanService eanService, Gs1Service gs1Service) {
        this.eanService = eanService;
        this.gs1Service = gs1Service;
    }

    @PostMapping("/validate-and-complete")
    public ResponseEntity<EanValidationResult> validateAndComplete(@RequestBody EanValidateRequest request) {
        if (request == null || request.getType() == null || request.getValue() == null) {
            return ResponseEntity.badRequest().body(EanValidationResult.invalid("Request body, type and value are required."));
        }

        String type = request.getType().trim().toUpperCase();
        String value = request.getValue();

        EanValidationResult result;
        switch (type) {
            case "EAN13" -> result = eanService.validateAndCompleteEan13(value);
            case "EAN8" -> result = eanService.validateAndCompleteEan8(value);
            case "UPCA" -> result = eanService.validateAndCompleteUpcA(value);
            case "ITF14" -> result = eanService.validateAndCompleteItf14(value);
            case "GS1128", "GS1-128" -> result = gs1Service.validateGs1128(value);
            case "GS1DM", "GS1-DM" -> result = gs1Service.validateGs1DataMatrix(value);
            case "GS1DATABAR", "GS1-DATABAR" -> result = gs1Service.validateGs1DataBar(value);
            default -> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(EanValidationResult.invalid("Nepodporovaný typ kódu: " + request.getType()));
            }
        }

        if (!result.isValid()) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }
}
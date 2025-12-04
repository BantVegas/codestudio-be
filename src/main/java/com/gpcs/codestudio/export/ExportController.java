package com.gpcs.codestudio.export;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5176",
        "http://localhost:5180"
})
public class ExportController {

    private final InkscapeExportService inkscapeExportService;

    public ExportController(InkscapeExportService inkscapeExportService) {
        this.inkscapeExportService = inkscapeExportService;
    }

    @PostMapping(value = "/eps", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> exportEps(@RequestBody SvgExportRequest request) {
        if (request == null || request.getSvg() == null || request.getSvg().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        byte[] epsBytes = inkscapeExportService.convertSvgToEps(request.getSvg());

        String fileName = (request.getFileName() == null || request.getFileName().isBlank())
                ? "code.eps"
                : request.getFileName().endsWith(".eps")
                ? request.getFileName()
                : request.getFileName() + ".eps";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/postscript"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(fileName).build()
        );
        headers.setContentLength(epsBytes.length);

        return new ResponseEntity<>(epsBytes, headers, HttpStatus.OK);
    }
}

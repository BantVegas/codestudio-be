package com.gpcs.codestudio.export;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InkscapeExportService {

    private final String inkscapeCommand;

    public InkscapeExportService(
            @Value("${codestudio.inkscape.command:inkscape}") String inkscapeCommand
    ) {
        this.inkscapeCommand = inkscapeCommand;
    }

    /**
     * Skutočný vektorový EPS export pomocou Inkscape CLI.
     *
     * Počítam s moderným Inkscapom (1.x):
     *   inkscape input.svg --export-type=eps --export-filename=output.eps
     *
     * Ak máš starší Inkscape, bude treba upraviť parametre v ProcessBuilderi.
     */
    public byte[] convertSvgToEps(String svgContent) {
        if (svgContent == null || svgContent.isBlank()) {
            throw new IllegalArgumentException("SVG obsah nesmie byť prázdny.");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("codestudio-eps-" + UUID.randomUUID());
            Path svgFile = tempDir.resolve("input.svg");
            Path epsFile = tempDir.resolve("output.eps");

            Files.writeString(svgFile, svgContent, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder(
                    inkscapeCommand,
                    svgFile.toAbsolutePath().toString(),
                    "--export-type=eps",
                    "--export-filename=" + epsFile.toAbsolutePath()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("Inkscape skončil chybou (" + exitCode + "):\n" + output);
            }

            if (!Files.exists(epsFile)) {
                throw new IllegalStateException("Výstupný EPS súbor sa nenašiel: " + epsFile);
            }

            return Files.readAllBytes(epsFile);
        } catch (IOException e) {
            throw new RuntimeException("Chyba pri volaní Inkscape pre EPS export.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("EPS export bol prerušený.", e);
        } finally {
            // best-effort cleanup
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted((a, b) -> b.compareTo(a)) // najprv súbory, potom dir
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                }
                            });
                } catch (IOException ignored) {
                }
            }
        }
    }
}


package com.gpcs.codestudio.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobService {

    private final ObjectMapper objectMapper;
    private final Path storagePath;
    private final Map<String, JobRecord> jobs = new ConcurrentHashMap<>();

    public JobService(
            ObjectMapper objectMapper,
            @Value("${codestudio.jobs.storagePath:}") String storagePathProp
    ) {
        this.objectMapper = objectMapper;
        if (storagePathProp != null && !storagePathProp.isBlank()) {
            this.storagePath = Paths.get(storagePathProp);
        } else {
            this.storagePath = Paths.get(
                    System.getProperty("user.home"),
                    ".gpcs-codestudio-jobs.json"
            );
        }
        loadFromDisk();
    }

    private void loadFromDisk() {
        if (!Files.exists(storagePath)) {
            return;
        }
        try (InputStream in = Files.newInputStream(storagePath)) {
            List<JobRecord> list = objectMapper.readValue(
                    in, new TypeReference<List<JobRecord>>() {
                    }
            );
            jobs.clear();
            for (JobRecord rec : list) {
                if (rec.getId() != null) {
                    jobs.put(rec.getId(), rec);
                }
            }
        } catch (IOException e) {
            // nechceme zhodiť aplikáciu kvôli zlej histórii, len log
            e.printStackTrace();
        }
    }

    private synchronized void saveToDisk() {
        try {
            List<JobRecord> list = new ArrayList<>(jobs.values());
            // vytvor parent dir ak neexistuje
            if (storagePath.getParent() != null && !Files.exists(storagePath.getParent())) {
                Files.createDirectories(storagePath.getParent());
            }
            try (OutputStream out = Files.newOutputStream(storagePath)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<JobDto> listJobs() {
        List<JobRecord> list = new ArrayList<>(jobs.values());
        list.sort(Comparator.comparing(JobRecord::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
        List<JobDto> result = new ArrayList<>();
        for (JobRecord rec : list) {
            result.add(toDto(rec, false));
        }
        return result;
    }

    public Optional<JobDto> getJob(String id) {
        JobRecord rec = jobs.get(id);
        if (rec == null) {
            return Optional.empty();
        }
        return Optional.of(toDto(rec, true));
    }

    public JobDto saveJob(SaveJobRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Job name nesmie byť prázdny.");
        }

        String id = (request.getId() == null || request.getId().isBlank())
                ? UUID.randomUUID().toString()
                : request.getId();

        JobRecord rec = jobs.get(id);
        Instant now = Instant.now();

        if (rec == null) {
            rec = new JobRecord();
            rec.setId(id);
            rec.setCreatedAt(now);
        }

        rec.setName(request.getName());
        rec.setDescription(request.getDescription());
        rec.setCodeType(request.getCodeType());
        rec.setMainValue(request.getMainValue());
        rec.setUpdatedAt(now);

        JsonNode payloadNode = request.getPayload();
        if (payloadNode != null) {
            try {
                rec.setPayloadJson(objectMapper.writeValueAsString(payloadNode));
            } catch (IOException e) {
                throw new RuntimeException("Chyba pri serializácii payload JSON.", e);
            }
        }

        jobs.put(id, rec);
        saveToDisk();

        return toDto(rec, true);
    }

    public void deleteJob(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        jobs.remove(id);
        saveToDisk();
    }

    private JobDto toDto(JobRecord rec, boolean includePayload) {
        JobDto dto = new JobDto();
        dto.setId(rec.getId());
        dto.setName(rec.getName());
        dto.setDescription(rec.getDescription());
        dto.setCodeType(rec.getCodeType());
        dto.setMainValue(rec.getMainValue());
        dto.setCreatedAt(rec.getCreatedAt());
        dto.setUpdatedAt(rec.getUpdatedAt());

        if (includePayload && rec.getPayloadJson() != null) {
            try {
                dto.setPayload(objectMapper.readTree(rec.getPayloadJson()));
            } catch (IOException e) {
                // ak sa nedá parse-nuť, necháme payload null
                e.printStackTrace();
            }
        }

        return dto;
    }
}

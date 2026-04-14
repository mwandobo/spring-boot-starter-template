package com.bonnysimon.starter.features.reconciliation;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReconciliationService {

    public Map<String, Object> compareExcel(MultipartFile[] files) {

        if (files == null || files.length < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Two Excel files are required"
            );
        }

        List<FileDataset> datasets = Arrays.stream(files)
                .map(this::parseFile)
                .toList();

        FileDataset fileA = datasets.get(0);
        FileDataset fileB = datasets.get(1);

        Set<String> matches = fileA.values.stream()
                .filter(fileB.values::contains)
                .collect(Collectors.toSet());

        Map<String, List<String>> missing = new HashMap<>();

        missing.put(fileA.fileName,
                fileA.values.stream()
                        .filter(v -> !fileB.values.contains(v))
                        .toList());

        missing.put(fileB.fileName,
                fileB.values.stream()
                        .filter(v -> !fileA.values.contains(v))
                        .toList());

        return Map.of(
                "matches", matches,
                "missing", missing
        );
    }

    private FileDataset parseFile(MultipartFile file) {

        try (InputStream is = file.getInputStream()) {

            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            List<List<String>> rows = new ArrayList<>();

            // 👉 Read Excel like NestJS (array of arrays)
            for (Row row : sheet) {
                List<String> cols = new ArrayList<>();

                int maxCol = row.getLastCellNum();

                for (int i = 0; i < maxCol; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cols.add(normalize(cellValue(cell)));
                }

                rows.add(cols);
            }

            // 👉 Find header row
            List<String> header = rows.stream()
                    .filter(r -> r.contains("RRN") || r.contains("Reference Number"))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Header not found in " + file.getOriginalFilename()
                    ));

            int columnIndex = header.contains("RRN")
                    ? header.indexOf("RRN")
                    : header.indexOf("Reference Number");

            int start = rows.indexOf(header) + 1;

            // 👉 Extract + normalize + filter (CORE LOGIC)
            Set<String> values = rows.subList(start, rows.size())
                    .stream()
                    .map(r -> columnIndex < r.size() ? normalize(r.get(columnIndex)) : null)
                    .filter(this::isValidValue)
                    .collect(Collectors.toSet());

            return new FileDataset(file.getOriginalFilename(), values);

        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + file.getOriginalFilename(), e);
        }
    }

    // 🔹 Strict filter (this is the real fix)
    private boolean isValidValue(String val) {
        if (val == null) return false;

        val = val.trim();

        return !val.isEmpty()
                && !val.equalsIgnoreCase("null")
                && !val.equalsIgnoreCase("undefined")
                && !val.equalsIgnoreCase("posted")
                && val.matches("\\d+"); // only digits
    }

    private String normalize(Object val) {
        return val == null ? "" : val.toString().trim();
    }

    private Object cellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double n = cell.getNumericCellValue();
                yield (n == Math.floor(n))
                        ? String.valueOf((long) n)
                        : String.valueOf(n);
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            default -> null;
        };
    }

    private record FileDataset(String fileName, Set<String> values) {}
}
package com.aeropuertolosprimos.backend.util.export;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExportFileName {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ExportFileName() {
    }

    public static String withTimestamp(
            String base,
            String ext
    ) {

        String safeBase = base == null || base.isBlank()
                ? "export"
                : base.trim().replaceAll("[^A-Za-z0-9._-]+", "_");

        String safeExt = ext == null || ext.isBlank()
                ? "dat"
                : ext.trim().replace(".", "");

        return safeBase + "_" + LocalDateTime.now().format(TS) + "." + safeExt;
    }
}


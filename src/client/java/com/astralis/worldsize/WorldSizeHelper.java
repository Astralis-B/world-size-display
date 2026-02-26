package com.astralis.worldsize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class WorldSizeHelper {

    public static long getFolderSize(Path folder) {
        if (!Files.exists(folder)) return 0L;
        try (Stream<Path> stream = Files.walk(folder)) {
            return stream
                .filter(Files::isRegularFile)
                .mapToLong(p -> {
                    try { return Files.size(p); }
                    catch (IOException e) { return 0L; }
                })
                .sum();
        } catch (IOException e) {
            return 0L;
        }
    }

    public static String formatSize(long bytes) {
        if (bytes >= 1_073_741_824L)
            return String.format("%.2f GB", bytes / 1_073_741_824.0);
        if (bytes >= 1_048_576L)
            return String.format("%.1f MB", bytes / 1_048_576.0);
        return String.format("%.0f KB", bytes / 1_024.0);
    }
}

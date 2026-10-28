package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public final class ReportGenerator {
    private ReportGenerator() {
    }

    public static void generate(Path logsDirectory, Path reportFile) throws IOException {
        Files.createDirectories(logsDirectory);

        long totalDeposits = 0L;
        long totalWithdrawals = 0L;
        long totalFiles = 0L;
        long totalSize = 0L;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsDirectory)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    totalFiles++;
                    totalSize += Files.size(file);

                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    long lastModified = attrs.lastModifiedTime().toMillis();

                    try (BufferedReader reader = Files.newBufferedReader(file)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(",");
                            if (parts.length < 2) {
                                continue;
                            }

                            String type = parts[0].trim();
                            long amount = Long.parseLong(parts[1].trim());

                            if ("DEPOSIT".equalsIgnoreCase(type)) {
                                totalDeposits += amount;
                            } else if ("WITHDRAW".equalsIgnoreCase(type)) {
                                totalWithdrawals += amount;
                            }
                        }
                    }

                    System.out.println("Log file: " + file.getFileName() + " | size=" + Files.size(file)
                            + " bytes | lastModified=" + lastModified);
                }
            }
        }

        Files.createDirectories(reportFile.getParent());
        String summary = "MiniBank End-of-Day Summary\n"
                + "==========================\n"
                + "Files scanned: " + totalFiles + "\n"
                + "Total bytes: " + totalSize + "\n"
                + "Total deposits: " + totalDeposits + "\n"
                + "Total withdrawals: " + totalWithdrawals + "\n";

        try (BufferedWriter writer = Files.newBufferedWriter(reportFile)) {
            writer.write(summary);
        }
    }
}

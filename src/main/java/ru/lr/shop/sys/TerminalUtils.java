package ru.lr.shop.sys;

import java.io.IOException;
import java.nio.file.Path;

public class TerminalUtils {
    public static void executeDetach(String... commandParts) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commandParts);
        builder.directory(Path.of("").toAbsolutePath().toFile());
        var process = builder.start();
        process.getInputStream().transferTo(System.out);
    }
}

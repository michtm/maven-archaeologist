package fr.michtm.reprise.archaeologist.fixture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FixtureFiles {
    private FixtureFiles() {
    }

    public static Path file(Path root, String relative, String content) throws IOException {
        Path path = root.resolve(relative);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    public static String pom(String body) {
        return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">" +
                "<modelVersion>4.0.0</modelVersion>" + body + "</project>";
    }

    public static void basicProject(Path root) throws IOException {
        file(root, "pom.xml", pom("<groupId>demo</groupId><artifactId>sample</artifactId><version>1.0</version>"));
        file(root, "src/main/java/demo/App.java", "package demo; public class App { public static void main(String[] args) {} }");
        file(root, "src/test/java/demo/AppTest.java", "package demo; class AppTest {}");
    }
}

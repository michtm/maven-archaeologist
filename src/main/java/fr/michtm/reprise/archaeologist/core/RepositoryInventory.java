package fr.michtm.reprise.archaeologist.core;

import fr.michtm.reprise.archaeologist.model.FileKind;
import fr.michtm.reprise.archaeologist.model.Inventory;
import fr.michtm.reprise.archaeologist.model.RepositoryFile;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RepositoryInventory {
    public Inventory scan(Path root) throws IOException {
        Path start = Objects.requireNonNull(root).toAbsolutePath();

        if (!Files.isDirectory(start)) {
            throw new IOException(root + " doit être un répertoire existant");
        }

        List<RepositoryFile> files = new ArrayList<>();

        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @SuppressWarnings("NullableProblems")
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (".git".equals(dir.getFileName().toString())
                        || ".idea".equals(dir.getFileName().toString())
                        || "target".equals(dir.getFileName().toString())
                        || "build".equals(dir.getFileName().toString())
                ) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path relativePath = root.relativize(file);
                String path = toPortablePath(relativePath);
                FileKind kind = FileKind.OTHER;
                if (!attrs.isSymbolicLink()) {
                    if ("pom.xml".equals(relativePath.getFileName().toString())) {
                        kind = FileKind.POM;
                    } else if (path.matches("^src/main/java/.*\\.java")) {
                        kind = FileKind.JAVA_MAIN;
                    } else if (path.matches("^src/test/java/.*\\.java")) {
                        kind = FileKind.JAVA_TEST;
                    } else if (path.matches("^src/main/resources/.*")) {
                        kind = FileKind.RESOURCE;
                    }
                    files.add(new RepositoryFile(path, attrs.size(), kind));
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return new Inventory(files, null);
    }

    private static String toPortablePath(Path relativePath) {
        return StreamSupport.stream(relativePath.spliterator(), false)
                .map(Path::toString)
                .collect(Collectors.joining("/"));
    }
}

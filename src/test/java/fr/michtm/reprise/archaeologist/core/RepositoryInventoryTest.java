package fr.michtm.reprise.archaeologist.core;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import fr.michtm.reprise.archaeologist.fixture.FixtureFiles;
import fr.michtm.reprise.archaeologist.model.FileKind;
import fr.michtm.reprise.archaeologist.model.Inventory;
import fr.michtm.reprise.archaeologist.model.RepositoryFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryInventoryTest {
    @TempDir
    Path root;
    private final RepositoryInventory subject = new RepositoryInventory();

    @Test
    void shouldInventoryProjectFiles() throws Exception {
        FixtureFiles.basicProject(root);
        Inventory result = subject.scan(root);
        assertEquals(List.of("pom.xml", "src/main/java/demo/App.java", "src/test/java/demo/AppTest.java"),
                result.files().stream().map(RepositoryFile::path).toList());
    }

    @Test
    void shouldDistinguishFileCategories() throws Exception {
        FixtureFiles.basicProject(root);
        FixtureFiles.file(root, "src/main/resources/application.yml", "port: 1234");
        FixtureFiles.file(root, "notes/todo.txt", "hello");
        Inventory inv = subject.scan(root);
        assertEquals(FileKind.POM, kind(inv, "pom.xml"));
        assertEquals(FileKind.JAVA_MAIN, kind(inv, "src/main/java/demo/App.java"));
        assertEquals(FileKind.JAVA_TEST, kind(inv, "src/test/java/demo/AppTest.java"));
        assertEquals(FileKind.RESOURCE, kind(inv, "src/main/resources/application.yml"));
        assertEquals(FileKind.OTHER, kind(inv, "notes/todo.txt"));
    }

    @Test
    void shouldExcludeBuildAndToolDirectoriesAtAnyDepth() throws Exception {
        FixtureFiles.basicProject(root);
        for (String excluded : List.of("target/x.txt", "build/x.txt", ".git/HEAD", ".idea/x.xml",
                "module/target/x.class", "module/.git/config")) {
            FixtureFiles.file(root, excluded, "should not appear");
        }
        assertEquals(3, subject.scan(root).files().size());
    }

    @Test
    void shouldPreserveLegitimateHiddenDirectories() throws Exception {
        FixtureFiles.file(root, ".github/workflows/build.yml", "name: CI");
        FixtureFiles.file(root, ".mvn/wrapper/maven-wrapper.properties", "distributionUrl=x");
        assertEquals(2, subject.scan(root).files().size());
    }

    @Test
    void shouldMeasureSizesInBytesRatherThanCharacters() throws Exception {
        FixtureFiles.file(root, "épreuve.txt", "é");
        assertEquals(2, subject.scan(root).files().getFirst().bytes());
    }

    @Test
    void shouldUsePortableRelativePaths() throws Exception {
        FixtureFiles.file(root, "sous dossier/été.txt", "abc");
        assertEquals("sous dossier/été.txt", subject.scan(root).files().getFirst().path());
    }

    @Test
    void shouldIgnoreSymlinkToExternalFile() throws Exception {
        // Jimfs évite les privilèges spéciaux nécessaires aux liens symboliques NTFS.
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path repository = Files.createDirectory(fs.getPath("/repository"));
            Path external = FixtureFiles.file(fs.getPath("/outside"), "secret.txt", "secret");
            Path link = Files.createSymbolicLink(repository.resolve("secret.txt"), external);

            assertTrue(Files.isSymbolicLink(link), "La fixture doit contenir un vrai lien symbolique NIO");
            assertTrue(subject.scan(repository).files().isEmpty());
        }
    }

    @Test
    void shouldIgnoreSymlinkToExternalDirectory() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path repository = Files.createDirectory(fs.getPath("/repository"));
            Path external = fs.getPath("/outside");
            FixtureFiles.file(external, "hidden.txt", "secret");
            Path link = Files.createSymbolicLink(repository.resolve("linked"), external);

            assertTrue(Files.isSymbolicLink(link), "La fixture doit contenir un vrai lien symbolique NIO");
            assertTrue(subject.scan(repository).files().isEmpty());
        }
    }

    @Test
    void shouldRejectNonexistentPath() {
        assertThrows(IOException.class, () -> subject.scan(root.resolve("missing")));
    }

    @Test
    void shouldRejectFileAsRoot() throws Exception {
        Path file = FixtureFiles.file(root, "pom.xml", "x");
        assertThrows(IOException.class, () -> subject.scan(file));
    }

    @Test
    void shouldProduceRepeatableScansWithoutMutation() throws Exception {
        FixtureFiles.basicProject(root);
        Inventory a = subject.scan(root);
        Inventory b = subject.scan(root);
        assertEquals(a, b);
        assertTrue(Files.exists(root.resolve("pom.xml")));
    }

    private FileKind kind(Inventory inventory, String path) {
        return inventory.files().stream().filter(f -> f.path().equals(path)).findFirst().orElseThrow().kind();
    }
}

package fr.michtm.reprise.archaeologist.core;

import fr.michtm.reprise.archaeologist.error.InvalidPomException;
import fr.michtm.reprise.archaeologist.fixture.FixtureFiles;
import fr.michtm.reprise.archaeologist.model.Dependency;
import fr.michtm.reprise.archaeologist.model.PomModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PomReaderTest {

    @TempDir
    Path root;

    private final PomReader subject = new PomReader();

    private PomModel read(String xml) throws IOException, InvalidPomException {
        return subject.read(FixtureFiles.file(root, "pom.xml", xml));
    }

    private static String coordinates() {
        return """
                <groupId>g</groupId>
                <artifactId>a</artifactId>
                <version>1</version>
                """;
    }

    @Test
    void shouldReadCoordinatesAndDefaultValues() throws Exception {
        PomModel pom = read(FixtureFiles.pom(coordinates()));

        assertAll(
                () -> assertEquals("g", pom.groupId()),
                () -> assertEquals("a", pom.artifactId()),
                () -> assertEquals("1", pom.version()),
                () -> assertEquals("jar", pom.packaging()),
                () -> assertTrue(pom.modules().isEmpty()),
                () -> assertTrue(pom.dependencies().isEmpty()),
                () -> assertTrue(pom.managedDependencies().isEmpty()),
                () -> assertTrue(pom.profiles().isEmpty())
        );
    }

    @Test
    void shouldReadExplicitPackaging() throws Exception {
        PomModel pom = read(FixtureFiles.pom(
                coordinates() + "<packaging>pom</packaging>"
        ));

        assertEquals("pom", pom.packaging());
    }

    @Test
    void shouldAcceptPomWithoutNamespace() throws Exception {
        PomModel pom = read(
                "<project>" + coordinates() + "</project>"
        );

        assertEquals("a", pom.artifactId());
    }

    @Test
    void shouldInheritGroupIdAndVersionFromParent() throws Exception {
        PomModel pom = read(FixtureFiles.pom("""
                <parent>
                    <groupId>parent.g</groupId>
                    <artifactId>parent-a</artifactId>
                    <version>9</version>
                </parent>
                <artifactId>child</artifactId>
                """));

        assertAll(
                () -> assertEquals("parent.g", pom.groupId()),
                () -> assertEquals("child", pom.artifactId()),
                () -> assertEquals("9", pom.version())
        );
    }

    @Test
    void shouldPreferLocalCoordinatesOverParentCoordinates() throws Exception {
        PomModel pom = read(FixtureFiles.pom("""
                <parent>
                    <groupId>old</groupId>
                    <artifactId>parent</artifactId>
                    <version>1</version>
                </parent>
                <groupId>new</groupId>
                <artifactId>child</artifactId>
                <version>2</version>
                """));

        assertAll(
                () -> assertEquals("new", pom.groupId()),
                () -> assertEquals("2", pom.version())
        );
    }

    @Test
    void shouldPreservePropertyReferences() throws Exception {
        PomModel pom = read(FixtureFiles.pom("""
                <groupId>${project.group}</groupId>
                <artifactId>example</artifactId>
                <version>${revision}</version>
                <properties>
                    <project.group>com.example</project.group>
                    <revision>1.2.3</revision>
                </properties>
                """));

        assertAll(
                () -> assertEquals("${project.group}", pom.groupId()),
                () -> assertEquals("${revision}", pom.version())
        );
    }

    @Test
    void shouldPreserveModuleOrderAndTrimWhitespace() throws Exception {
        PomModel pom = read(FixtureFiles.pom("""
                <groupId>g</groupId>
                <artifactId>parent</artifactId>
                <version>1</version>
                <packaging>pom</packaging>
                <modules>
                    <module> alpha </module>
                    <module>beta/sub</module>
                    <module>gamma</module>
                </modules>
                """));

        assertEquals(List.of("alpha", "beta/sub", "gamma"), pom.modules());
    }

    @Test
    void shouldDistinguishProjectModulesFromProfileModules() throws Exception {
        PomModel pom = read(FixtureFiles.pom(
                coordinates() + """
                        <modules>
                            <module>principal</module>
                        </modules>
                        <profiles>
                            <profile>
                                <id>extra</id>
                                <modules>
                                    <module>optionnel</module>
                                </modules>
                            </profile>
                        </profiles>
                        """
        ));

        assertEquals(List.of("principal"), pom.modules());
        assertEquals(List.of("extra"), pom.profiles());
    }

    @Test
    void shouldReadDirectDependenciesAndTheirDefaults() throws Exception {
        PomModel pom = read(FixtureFiles.pom(
                coordinates() + """
                        <dependencies>
                            <dependency>
                                <groupId>org.demo</groupId>
                                <artifactId>lib</artifactId>
                                <version>3</version>
                            </dependency>
                        </dependencies>
                        """
        ));

        assertEquals(1, pom.dependencies().size());

        Dependency dep = pom.dependencies().getFirst();

        assertAll(
                () -> assertEquals("org.demo", dep.groupId()),
                () -> assertEquals("lib", dep.artifactId()),
                () -> assertEquals("3", dep.version()),
                () -> assertEquals("compile", dep.scope()),
                () -> assertEquals("jar", dep.type()),
                () -> assertEquals("false", dep.optional()),
                () -> assertEquals("", dep.classifier())
        );
    }

    @Test
    void shouldReadOptionalDependencyAttributes() throws Exception {
        PomModel pom = read(FixtureFiles.pom(
                coordinates() + """
                        <dependencies>
                            <dependency>
                                <groupId>g2</groupId>
                                <artifactId>x</artifactId>
                                <scope>runtime</scope>
                                <type>test-jar</type>
                                <optional>true</optional>
                                <classifier>tests</classifier>
                            </dependency>
                        </dependencies>
                        """
        ));

        Dependency dep = pom.dependencies().getFirst();

        assertAll(
                () -> assertEquals("", dep.version()),
                () -> assertEquals("runtime", dep.scope()),
                () -> assertEquals("test-jar", dep.type()),
                () -> assertEquals("true", dep.optional()),
                () -> assertEquals("tests", dep.classifier())
        );
    }

    @Test
    void shouldPreserveDependencyOrder() throws Exception {
        PomModel pom = read(FixtureFiles.pom(
                coordinates() + """
                        <dependencies>
                            <dependency>
                                <groupId>g</groupId>
                                <artifactId>z</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>g</groupId>
                                <artifactId>a</artifactId>
                            </dependency>
                        </dependencies>
                        """
        ));

        assertEquals(
                List.of("z", "a"),
                pom.dependencies().stream()
                        .map(Dependency::artifactId)
                        .toList()
        );
    }

    @Test
    void shouldDistinguishDependenciesFromDependencyManagement() throws Exception {
        PomModel pom = read(FixtureFiles.pom(
                coordinates() + """
                        <dependencyManagement>
                            <dependencies>
                                <dependency>
                                    <groupId>b</groupId>
                                    <artifactId>bom</artifactId>
                                    <version>1</version>
                                    <type>pom</type>
                                    <scope>import</scope>
                                </dependency>
                            </dependencies>
                        </dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>b</groupId>
                                <artifactId>lib</artifactId>
                            </dependency>
                        </dependencies>
                        """
        ));

        assertEquals(1, pom.dependencies().size());
        assertEquals(1, pom.managedDependencies().size());

        Dependency direct = pom.dependencies().getFirst();
        Dependency managed = pom.managedDependencies().getFirst();

        assertAll(
                () -> assertEquals("lib", direct.artifactId()),
                () -> assertEquals("", direct.version()),
                () -> assertEquals("bom", managed.artifactId()),
                () -> assertEquals("1", managed.version()),
                () -> assertEquals("pom", managed.type()),
                () -> assertEquals("import", managed.scope())
        );
    }

    @Test
    void shouldIgnoreProfileDependencies() throws Exception {
        PomModel pom = read(FixtureFiles.pom(
                coordinates() + """
                        <profiles>
                            <profile>
                                <id>test</id>
                                <dependencies>
                                    <dependency>
                                        <groupId>x</groupId>
                                        <artifactId>y</artifactId>
                                    </dependency>
                                </dependencies>
                            </profile>
                        </profiles>
                        """
        ));

        assertTrue(pom.dependencies().isEmpty());
        assertEquals(List.of("test"), pom.profiles());
    }

    @Test
    void shouldIgnorePluginDependencies() throws Exception {
        PomModel pom = read(FixtureFiles.pom(
                coordinates() + """
                        <build>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-compiler-plugin</artifactId>
                                    <dependencies>
                                        <dependency>
                                            <groupId>x</groupId>
                                            <artifactId>plugin-lib</artifactId>
                                        </dependency>
                                    </dependencies>
                                </plugin>
                            </plugins>
                        </build>
                        """
        ));

        assertTrue(pom.dependencies().isEmpty());
        assertTrue(pom.managedDependencies().isEmpty());
    }

    @Test
    void shouldDistinguishParentFromDependency() throws Exception {
        PomModel pom = read(FixtureFiles.pom("""
                <parent>
                    <groupId>pg</groupId>
                    <artifactId>pa</artifactId>
                    <version>pv</version>
                </parent>
                <artifactId>child</artifactId>
                """));

        assertTrue(pom.dependencies().isEmpty());
        assertEquals("child", pom.artifactId());
    }

    @Test
    void shouldRejectTruncatedXmlDocument() {
        assertThrows(InvalidPomException.class, () -> read("<project><artifactId>x"));
    }

    @Test
    void shouldRejectInvalidRootElement() {
        assertThrows(InvalidPomException.class, () -> read("<notProject/>"));
    }

    @Test
    void shouldRejectDoctypeWithoutExternalEntity() {
        String xml = """
                <!DOCTYPE project>
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>g</groupId>
                    <artifactId>a</artifactId>
                    <version>1</version>
                </project>
                """;

        assertThrows(InvalidPomException.class, () -> read(xml));
    }

    @Test
    void shouldRejectXxeAttempt() throws Exception {
        Path secret = FixtureFiles.file(root, "secret.txt", "private-data");

        String xml = """
                <!DOCTYPE project [
                    <!ENTITY xxe SYSTEM "%s">
                ]>
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>&xxe;</groupId>
                    <artifactId>a</artifactId>
                    <version>1</version>
                </project>
                """.formatted(secret.toUri());

        assertThrows(InvalidPomException.class, () -> read(xml));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<groupId>g</groupId><version>1</version>",
            "<artifactId>a</artifactId><version>1</version>",
            "<groupId>g</groupId><artifactId>a</artifactId>",
            "<groupId> </groupId><artifactId>a</artifactId><version>1</version>",
            "<groupId>g</groupId><artifactId> </artifactId><version>1</version>",
            "<groupId>g</groupId><artifactId>a</artifactId><version> </version>"
    })
    void shouldRejectMissingRequiredOrBlankCoordinates(String body) {
        String pom = FixtureFiles.pom(body);
        assertThrows(InvalidPomException.class, () -> read(pom));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<artifactId>lib</artifactId>",
            "<groupId>g</groupId>"
    })
    void shouldRejectDependencyWithMissingCoordinates(String dependency) {
        String xml = FixtureFiles.pom(
                coordinates()
                        + "<dependencies><dependency>"
                        + dependency
                        + "</dependency></dependencies>"
        );

        assertThrows(InvalidPomException.class, () -> read(xml));
    }

    @Test
    void shouldThrowIOExceptionForMissingFile() {
        Path missing = root.resolve("missing/pom.xml");
        assertThrows(IOException.class, () -> subject.read(missing));
    }
}

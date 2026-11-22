package fr.michtm.reprise.archaeologist.core;

import fr.michtm.reprise.archaeologist.error.InvalidPomException;
import fr.michtm.reprise.archaeologist.model.Dependency;
import fr.michtm.reprise.archaeologist.model.PomModel;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lit et analyse les fichiers POM Maven afin d'en extraire les métadonnées déclarées.
 *
 * <p>L'analyse repose sur le modèle fourni par Maven et ne réalise ni résolution des dépendances, ni interpolation des
 * propriétés, ni activation des profils, ni résolution des POM parents.
 *
 * <p>Les coordonnées {@code groupId} et {@code version} peuvent être héritées de la déclaration du parent.
 *
 * <p>Pour des raisons de sécurité, les déclarations DOCTYPE et les entités XML externes sont interdites.
 *
 * <p>L'analyse ne modifie pas le fichier et n'exécute aucun code appartenant au projet analysé.
 */
public final class PomReader {

    /**
     * Lit un POM Maven et construit sa représentation simplifiée.
     *
     * @param pom chemin du fichier POM à analyser
     * @return modèle contenant les métadonnées extraites du POM
     * @throws IOException si le fichier ne peut pas être lu
     * @throws InvalidPomException si le XML est invalide, interdit ou contient des coordonnées obligatoires absentes ou
     *         blanches
     */
    public PomModel read(Path pom) throws IOException, InvalidPomException {
        byte[] content = Files.readAllBytes(pom);

        validateXml(content);

        try {
            Model model = new MavenXpp3Reader().read(new ByteArrayInputStream(content), true);
            return toPomModel(model);
        } catch (XmlPullParserException e) {
            throw new InvalidPomException("Invalid Maven POM: " + pom, e);
        }
    }

    private static PomModel toPomModel(Model model) throws InvalidPomException {
        Parent parent = model.getParent();

        String groupId = model.getGroupId();
        String version = model.getVersion();

        if (parent != null) {
            requireCoordinate(parent.getGroupId(), "parent.groupId");
            requireCoordinate(parent.getArtifactId(), "parent.artifactId");
            requireCoordinate(parent.getVersion(), "parent.version");

            if (groupId == null) {
                groupId = parent.getGroupId();
            }

            if (version == null) {
                version = parent.getVersion();
            }
        }

        requireCoordinate(groupId, "project.groupId");
        requireCoordinate(model.getArtifactId(), "project.artifactId");
        requireCoordinate(version, "project.version");

        List<String> modules = model.getModules().stream()
                .map(String::strip)
                .toList();

        List<Dependency> dependencies = toDependencies(model.getDependencies());

        List<Dependency> managedDependencies =
                model.getDependencyManagement() == null
                        ? List.of()
                        : toDependencies(model.getDependencyManagement().getDependencies());

        List<String> profiles = model.getProfiles().stream()
                .map(Profile::getId)
                .toList();

        return new PomModel(
                groupId,
                model.getArtifactId(),
                version,
                model.getPackaging(),
                modules,
                dependencies,
                managedDependencies,
                profiles
        );
    }

    private static List<Dependency> toDependencies(List<org.apache.maven.model.Dependency> source)
            throws InvalidPomException {
        List<Dependency> result = new ArrayList<>(source.size());

        for (org.apache.maven.model.Dependency dependency : source) {
            result.add(toDependency(dependency));
        }

        return List.copyOf(result);
    }

    private static Dependency toDependency(org.apache.maven.model.Dependency dependency) throws InvalidPomException {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();

        requireCoordinate(groupId, "dependency.groupId");
        requireCoordinate(artifactId, "dependency.artifactId");

        return new Dependency(
                groupId,
                artifactId,
                defaultIfBlank(dependency.getVersion(), ""),
                defaultIfBlank(dependency.getScope(), "compile"),
                defaultIfBlank(dependency.getType(), "jar"),
                defaultIfBlank(dependency.getOptional(), "false"),
                defaultIfBlank(dependency.getClassifier(), "")
        );
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank()
                ? defaultValue
                : value;
    }

    private static void requireCoordinate(String value, String field) throws InvalidPomException {
        if (value == null || value.isBlank()) {
            throw new InvalidPomException("Missing or blank Maven coordinate: " + field);
        }
    }

    /**
     * Vérifie la validité syntaxique et la sécurité du document XML.
     *
     * <p>La validation interdit les déclarations DOCTYPE et les accès aux ressources XML externes.
     *
     * @param content contenu XML à vérifier
     * @throws IOException si une erreur de lecture survient pendant l'analyse XML
     * @throws InvalidPomException si le document est malformé ou contient une construction interdite
     */
    private static void validateXml(byte[] content) throws IOException, InvalidPomException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            SAXParser parser = factory.newSAXParser();

            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            parser.parse(new ByteArrayInputStream(content), new DefaultHandler());
        } catch (ParserConfigurationException | SAXException e) {
            throw new InvalidPomException("Invalid or forbidden POM XML content", e);
        }
    }
}

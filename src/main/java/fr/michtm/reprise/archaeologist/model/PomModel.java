package fr.michtm.reprise.archaeologist.model;

import java.util.List;

/**
 * Représente les métadonnées extraites d'un POM Maven.
 *
 * <p>Ce modèle décrit les déclarations du POM et non le modèle effectif calculé par Maven.
 *
 * <p>Les coordonnées peuvent utiliser les valeurs déclarées dans le parent, sans résolution de celui-ci. Les références
 * aux propriétés ne sont pas interpolées.
 *
 * @param groupId identifiant du groupe du projet
 * @param artifactId identifiant de l'artefact du projet
 * @param version version du projet
 * @param packaging type de packaging, {@code jar} par défaut
 * @param modules modules déclarés, dans leur ordre d'apparition
 * @param dependencies dépendances directes déclarées
 * @param managedDependencies dépendances déclarées dans {@code dependencyManagement}
 * @param profiles identifiants des profils déclarés, indépendamment de leur activation
 */
public record PomModel(String groupId, String artifactId, String version, String packaging,
                       List<String> modules, List<Dependency> dependencies,
                       List<Dependency> managedDependencies, List<String> profiles) {
}

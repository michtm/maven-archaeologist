package fr.michtm.reprise.archaeologist.model;

/**
 * Représente une dépendance déclarée dans un POM Maven.
 *
 * <p>Les valeurs sont extraites sans résolution des versions ni application du dependencyManagement.
 *
 * <p>Les champs facultatifs absents reçoivent les valeurs par défaut définies par le contrat d'analyse.
 *
 * @param groupId identifiant du groupe de la dépendance
 * @param artifactId identifiant de l'artefact de la dépendance
 * @param version version déclarée, ou chaîne vide si absente
 * @param scope portée de la dépendance, {@code compile} par défaut
 * @param type type de la dépendance, {@code jar} par défaut
 * @param optional caractère facultatif, représenté par une chaîne et valant {@code false} par défaut
 * @param classifier classificateur déclaré, ou chaîne vide si absent
 */
public record Dependency(String groupId, String artifactId, String version, String scope, String type, String optional,
                         String classifier) {
}
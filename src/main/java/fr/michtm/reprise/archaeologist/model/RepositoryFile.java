package fr.michtm.reprise.archaeologist.model;

/**
 * Représente un fichier découvert lors de l'inventaire.
 *
 * @param path chemin relatif à la racine, utilisant {@code /} comme séparateur
 * @param bytes taille du fichier en octets
 * @param kind catégorie du fichier
 */
public record RepositoryFile(String path, long bytes, FileKind kind) {
}

package fr.michtm.reprise.archaeologist.model;

import java.util.List;

/**
 * Résultat d'un inventaire de dépôt.
 *
 * @param files fichiers inventoriés, triés par chemin relatif
 * @param diagnostics anomalies non bloquantes détectées pendant l'inventaire
 */
public record Inventory(List<RepositoryFile> files, List<Diagnostic> diagnostics) {
}

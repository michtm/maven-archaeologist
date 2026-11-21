package fr.michtm.reprise.archaeologist.model;

/**
 * Décrit une anomalie détectée pendant une analyse.
 *
 * @param severity niveau de gravité
 * @param code identifiant de l'anomalie
 * @param location emplacement concerné
 * @param message description lisible de l'anomalie
 */
public record Diagnostic(Severity severity, String code, String location, String message) {
}

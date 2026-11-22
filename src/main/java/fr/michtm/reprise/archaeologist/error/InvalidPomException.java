package fr.michtm.reprise.archaeologist.error;

/**
 * Signale qu'un fichier POM ne respecte pas les exigences syntaxiques, structurelles ou de sécurité de l'analyseur.
 *
 * <p>Les erreurs d'accès au système de fichiers restent représentées par {@link java.io.IOException}.
 */
public class InvalidPomException extends RuntimeException {
    public InvalidPomException(String message) {
        super(message);
    }

    public InvalidPomException(String message, Throwable cause) {
        super(message, cause);
    }
}

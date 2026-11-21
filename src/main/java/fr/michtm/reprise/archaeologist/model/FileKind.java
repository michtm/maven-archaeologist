package fr.michtm.reprise.archaeologist.model;

/**
 * Catégories de fichiers reconnues par l'inventaire.
 *
 * <p>Un fichier {@code pom.xml} est classé {@link #POM}. Les sources Java sous {@code src/main/java} et
 * {@code src/test/java} sont respectivement classées {@link #JAVA_MAIN} et {@link #JAVA_TEST}. Les fichiers sous
 * {@code src/main/resources} sont classés {@link #RESOURCE}. Tout autre fichier est classé {@link #OTHER}.
 */
public enum FileKind {
    POM, JAVA_MAIN, JAVA_TEST, RESOURCE, OTHER
}

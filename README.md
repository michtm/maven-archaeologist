# maven-archaeologist

Outil de rétro-ingénierie destiné à faciliter la reprise de projets Maven sans documentation préalable.

## Objectif

Analyser un dépôt Maven inconnu et produire une documentation technique et fonctionnelle fondée sur des éléments vérifiables.

## Fonctionnalités

### Inventaire du dépôt

- Parcours récursif des fichiers avec `java.nio.file`.
- Exclusion des répertoires `.git`, `.idea`, `target` et `build`.
- Exclusion des liens symboliques.
- Classification des fichiers Maven, Java, ressources et autres.
- Normalisation portable des chemins relatifs.
- Calcul des tailles en octets.
- Résultats triés et déterministes.
- Analyse en lecture seule.

### Lecture des POM Maven

- Lecture des POM avec `maven-model:3.10.1`.
- Extraction des coordonnées et du packaging.
- Héritage déclaratif des coordonnées du parent.
- Extraction des modules et profils.
- Distinction entre `dependencies` et `dependencyManagement`.
- Conservation des valeurs et références de propriétés déclarées.
- Application des valeurs par défaut prévues par le contrat.
- Validation des coordonnées obligatoires.
- Rejet des XML malformés, DOCTYPE et entités externes.
- Gestion distincte des erreurs XML et des erreurs d'entrée/sortie.

## Architecture

```text
src/
├── main/java/fr/michtm/reprise/archaeologist
│   ├── core/       # Analyse et extraction
│   ├── model/      # Modèles de données
│   ├── error/      # Exceptions métier
└── test/java/fr/michtm/reprise/archaeologist
    ├── FixtureFiles.java
    └── **/*Test.java
```

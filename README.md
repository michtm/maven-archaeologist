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

## Architecture

```text
src/
├── main/java/fr/michtm/reprise/archaeologist
│   ├── core/       # Analyse du dépôt
│   ├── model/      # Modèles de données
└── test/java/fr/michtm/reprise/archaeologist
    ├── FixtureFiles.java
    └── **/*Test.java
```

package fr.michtm.reprise.archaeologist.model;

import java.util.List;

public record Inventory(List<RepositoryFile> files, List<Diagnostic> diagnostics) {
}

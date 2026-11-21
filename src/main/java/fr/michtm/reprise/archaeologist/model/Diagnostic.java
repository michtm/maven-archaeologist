package fr.michtm.reprise.archaeologist.model;

public record Diagnostic(Severity severity, String code, String location, String message) {
}

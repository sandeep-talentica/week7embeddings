package com.example.week7embeddings.model;

public record JobMatch(
        String jobId,
        String jobTitle,
        double similarityScore,
        String interpretation) {
}

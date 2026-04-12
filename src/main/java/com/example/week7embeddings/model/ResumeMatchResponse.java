package com.example.week7embeddings.model;

import java.util.List;

public record ResumeMatchResponse(
        String resume,
        JobMatch bestMatch,
        List<JobMatch> allMatches) {
}

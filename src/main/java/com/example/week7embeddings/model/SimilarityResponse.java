package com.example.week7embeddings.model;

import java.util.List;

public record SimilarityResponse(
        String text1,
        String text2,
        double cosineSimilarity,
        List<Float> embedding1,
        List<Float> embedding2,
        String interpretation) {
}

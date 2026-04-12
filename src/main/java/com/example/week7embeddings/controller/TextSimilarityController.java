package com.example.week7embeddings.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.week7embeddings.model.SimilarityRequest;
import com.example.week7embeddings.model.SimilarityResponse;

@RestController
@RequestMapping("/api/similarity")
public class TextSimilarityController {

    private final EmbeddingModel openAiEmbeddingModel;
    private final EmbeddingModel ollamaEmbeddingModel;

    public TextSimilarityController(
            @Qualifier("openAiEmbeddingModel") EmbeddingModel openAiEmbeddingModel,
            @Qualifier("ollamaEmbeddingModel") EmbeddingModel ollamaEmbeddingModel) {
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
    }

    @PostMapping("/compare/openai")
    public SimilarityResponse compareWithOpenAI(@RequestBody SimilarityRequest request) {
        return calculateSimilarity(request, openAiEmbeddingModel, "OpenAI (text-embedding-3-small)");
    }

    @PostMapping("/compare/ollama")
    public SimilarityResponse compareWithOllama(@RequestBody SimilarityRequest request) {
        return calculateSimilarity(request, ollamaEmbeddingModel, "Ollama (mxbai-embed-large)");
    }

    @PostMapping("/compare/both")
    public Map<String, SimilarityResponse> compareWithBoth(@RequestBody SimilarityRequest request) {
        SimilarityResponse openAiResult = calculateSimilarity(request, openAiEmbeddingModel, "OpenAI");
        SimilarityResponse ollamaResult = calculateSimilarity(request, ollamaEmbeddingModel, "Ollama");

        return Map.of(
                "openai", openAiResult,
                "ollama", ollamaResult);
    }

    private SimilarityResponse calculateSimilarity(
            SimilarityRequest request,
            EmbeddingModel embeddingModel,
            String modelName) {

        // Step 1: Generate embeddings for both texts
        EmbeddingResponse response1 = embeddingModel.embedForResponse(List.of(request.text1()));
        EmbeddingResponse response2 = embeddingModel.embedForResponse(List.of(request.text2()));

        // Step 2: Extract the float arrays (vectors)
        float[] embedding1 = response1.getResults().get(0).getOutput();
        float[] embedding2 = response2.getResults().get(0).getOutput();

        // Step 3: Calculate cosine similarity
        double similarity = cosineSimilarity(embedding1, embedding2);

        // Step 4: Interpret the similarity score
        String interpretation = interpretSimilarity(similarity) + " (using " + modelName + ")";

        // Step 5: Return the response
        return new SimilarityResponse(
                request.text1(),
                request.text2(),
                similarity,
                toList(embedding1),
                toList(embedding2),
                interpretation);
    }

    /**
     * Calculate cosine similarity between two vectors.
     * Formula: cos(θ) = (A · B) / (||A|| * ||B||)
     * Returns a value between -1 and 1, where:
     * - 1 means identical direction (very similar)
     * - 0 means orthogonal (unrelated)
     * - -1 means opposite direction (very dissimilar)
     */
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (normA * normB);
    }

    /**
     * Interpret the cosine similarity score for human understanding
     */
    private String interpretSimilarity(double similarity) {
        if (similarity >= 0.9) {
            return "Very High Similarity - Nearly identical meaning";
        } else if (similarity >= 0.7) {
            return "High Similarity - Closely related topics";
        } else if (similarity >= 0.5) {
            return "Moderate Similarity - Somewhat related";
        } else if (similarity >= 0.3) {
            return "Low Similarity - Loosely related";
        } else {
            return "Very Low Similarity - Unrelated topics";
        }
    }

    /**
     * Convert float array to List for JSON serialization
     */
    private List<Float> toList(float[] array) {
        Float[] boxed = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            boxed[i] = array[i];
        }
        return Arrays.asList(boxed);
    }

}

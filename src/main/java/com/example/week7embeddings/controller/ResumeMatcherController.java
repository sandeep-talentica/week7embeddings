package com.example.week7embeddings.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.week7embeddings.model.JobDescription;
import com.example.week7embeddings.model.JobMatch;
import com.example.week7embeddings.model.ResumeMatchRequest;
import com.example.week7embeddings.model.ResumeMatchResponse;

@RestController
@RequestMapping("/api/resume-matcher")
public class ResumeMatcherController {

    private final EmbeddingModel embeddingModel;
    private final List<JobDescription> jobDescriptions;

    public ResumeMatcherController(@Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        this.jobDescriptions = initializeJobDescriptions();
    }

    @GetMapping("/jobs")
    public List<JobDescription> getAvailableJobs() {
        return jobDescriptions;
    }

    @PostMapping("/match")
    public ResumeMatchResponse matchResume(@RequestBody ResumeMatchRequest request) {
        String resume = request.resume();

        // Generate embedding for the resume
        EmbeddingResponse resumeEmbedding = embeddingModel.embedForResponse(List.of(resume));
        float[] resumeVector = resumeEmbedding.getResults().get(0).getOutput();

        // Calculate similarity with each job description
        List<JobMatch> allMatches = new ArrayList<>();

        for (JobDescription job : jobDescriptions) {
            // Generate embedding for job description
            EmbeddingResponse jobEmbedding = embeddingModel.embedForResponse(List.of(job.description()));
            float[] jobVector = jobEmbedding.getResults().get(0).getOutput();

            // Calculate cosine similarity
            double similarity = cosineSimilarity(resumeVector, jobVector);

            // Create match result
            JobMatch match = new JobMatch(
                    job.jobId(),
                    job.title(),
                    similarity,
                    interpretSimilarity(similarity));

            allMatches.add(match);
        }

        // Sort by similarity score (highest first)
        allMatches.sort(Comparator.comparingDouble(JobMatch::similarityScore).reversed());

        // Best match is the first one after sorting
        JobMatch bestMatch = allMatches.get(0);

        return new ResumeMatchResponse(resume, bestMatch, allMatches);
    }

    private List<JobDescription> initializeJobDescriptions() {
        return List.of(
                new JobDescription(
                        "java-dev-001",
                        "Senior Java Developer",
                        "We are seeking an experienced Java Developer with strong expertise in Spring Boot, " +
                                "microservices architecture, and RESTful APIs. The ideal candidate should have 5+ years "
                                +
                                "of experience in enterprise Java development, knowledge of Docker and Kubernetes, " +
                                "and experience with relational databases like PostgreSQL or MySQL. Familiarity with " +
                                "CI/CD pipelines, Git, and Agile methodologies is required. Strong problem-solving " +
                                "skills and ability to work in a team environment are essential."),
                new JobDescription(
                        "python-ds-002",
                        "Python Data Scientist",
                        "Looking for a talented Data Scientist with strong Python programming skills. Must have " +
                                "experience with machine learning frameworks like TensorFlow, PyTorch, or scikit-learn. "
                                +
                                "Expertise in data analysis using pandas, NumPy, and data visualization with matplotlib "
                                +
                                "or seaborn. Knowledge of statistical modeling, A/B testing, and SQL is required. " +
                                "Experience with big data technologies like Spark is a plus. PhD or Master's degree in "
                                +
                                "Computer Science, Statistics, or related field preferred. Strong communication skills "
                                +
                                "to present findings to stakeholders."),
                new JobDescription(
                        "eng-mgr-003",
                        "Engineering Manager",
                        "We are hiring an Engineering Manager to lead a team of 8-10 software engineers. " +
                                "Responsibilities include team leadership, project planning, resource allocation, and "
                                +
                                "technical decision-making. Must have 7+ years of software development experience and "
                                +
                                "3+ years in a leadership role. Strong understanding of software development lifecycle, "
                                +
                                "Agile/Scrum methodologies, and modern development practices. Excellent communication, "
                                +
                                "mentoring, and conflict resolution skills required. Experience with performance reviews, "
                                +
                                "hiring, and building high-performing teams. Technical background in Java, Python, or "
                                +
                                "similar languages preferred."));
    }

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

    private String interpretSimilarity(double similarity) {
        if (similarity >= 0.8) {
            return "Excellent Match - Highly qualified";
        } else if (similarity >= 0.6) {
            return "Good Match - Strong candidate";
        } else if (similarity >= 0.4) {
            return "Moderate Match - Some relevant skills";
        } else if (similarity >= 0.2) {
            return "Weak Match - Limited alignment";
        } else {
            return "Poor Match - Not qualified";
        }
    }

}

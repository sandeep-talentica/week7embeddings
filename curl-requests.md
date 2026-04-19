# Curl Requests for Controller Endpoints

Base URL used in these examples:

```text
http://localhost:8080
```

> Notes:
> - Replace `localhost:8080` if your Spring Boot app runs on a different host or port.
> - The POST endpoints use `Content-Type: application/json`.
> - The embedding-based endpoints require the OpenAI and/or Ollama configuration to be available at runtime.

---

## 1) Get all available jobs

**Endpoint:** `GET /api/resume-matcher/jobs`

Returns the list of available job descriptions.

```bash
curl --location 'http://localhost:8080/api/resume-matcher/jobs'
```

---

## 2) Match a resume against the available jobs

**Endpoint:** `POST /api/resume-matcher/match`

Request body shape:
```json
{
  "resume": "string"
}
```

```bash
curl --location 'http://localhost:8080/api/resume-matcher/match' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "resume": "Experienced Java developer with Spring Boot, REST APIs, Docker, Kubernetes, and PostgreSQL. Built microservices and worked in Agile teams."
  }'
```

Expected response includes:
- the original `resume`
- `bestMatch` object
- `allMatches` array

---

## 3) Compare two texts using OpenAI embeddings

**Endpoint:** `POST /api/similarity/compare/openai`

Request body shape:
```json
{
  "text1": "string",
  "text2": "string"
}
```

```bash
curl --location 'http://localhost:8080/api/similarity/compare/openai' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "text1": "Spring Boot microservices development",
    "text2": "Building RESTful APIs with Java and Spring Boot"
  }'
```

Expected response includes:
- `cosineSimilarity`
- `embedding1`
- `embedding2`
- `interpretation`

---

## 4) Compare two texts using Ollama embeddings

**Endpoint:** `POST /api/similarity/compare/ollama`

Request body shape:
```json
{
  "text1": "string",
  "text2": "string"
}
```

```bash
curl --location 'http://localhost:8080/api/similarity/compare/ollama' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "text1": "Spring Boot microservices development",
    "text2": "Building RESTful APIs with Java and Spring Boot"
  }'
```

Expected response includes the same fields as the OpenAI similarity endpoint.

---

## 5) Compare two texts with both OpenAI and Ollama

**Endpoint:** `POST /api/similarity/compare/both`

Request body shape:
```json
{
  "text1": "string",
  "text2": "string"
}
```

```bash
curl --location 'http://localhost:8080/api/similarity/compare/both' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "text1": "Spring Boot microservices development",
    "text2": "Building RESTful APIs with Java and Spring Boot"
  }'
```

Expected response is a JSON object with two keys:
- `openai`
- `ollama`

Each key maps to a similarity response object.

---

## Quick copy list

If you just want the endpoints at a glance:

- `GET  /api/resume-matcher/jobs`
- `POST /api/resume-matcher/match`
- `POST /api/similarity/compare/openai`
- `POST /api/similarity/compare/ollama`
- `POST /api/similarity/compare/both`


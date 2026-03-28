# RAG Pipeline Implementation Guide

## Overview
Your Spring AI Code Review application now includes a **Retrieval-Augmented Generation (RAG)** pipeline that enhances code reviews by:
- Ingesting repository metadata, coding guidelines, and commit history
- Converting documents to embeddings using Ollama's `nomic-embed-text` model
- Storing embeddings in PostgreSQL using pgvector
- Retrieving relevant context for each code review
- Augmenting LLM prompts with context-aware suggestions

## Architecture

```
Document Upload
      ↓
Document Storage (PostgreSQL)
      ↓
Content Chunking & Embedding (Ollama)
      ↓
Vector Storage (pgvector in PostgreSQL)
      ↓
Vector Search
      ↓
Context-Augmented Review (LLM)
```

## Setup Requirements

### 1. PostgreSQL Configuration
Ensure pgvector extension is installed:

```sql
-- Run as PostgreSQL superuser
CREATE EXTENSION IF NOT EXISTS vector;

-- Verify installation
SELECT * FROM pg_extension WHERE extname = 'vector';
```

### 2. Required Ollama Models
Ensure these models are available in Ollama:

```bash
ollama pull nomic-embed-text  # For embeddings
ollama pull orca-mini         # For chat/review
```

### 3. Maven Dependencies Updated
The following were added to `pom.xml`:
- `spring-ai-pgvector-store` - Vector store integration
- `spring-ai-ollama` - Embedding client
- `pgvector` - JDBC support

## API Endpoints

### Upload Documents

**POST** `/documents/upload`

Upload a document for RAG context:

```bash
curl -X POST "http://localhost:8080/documents/upload" \
  -F "file=@coding-guidelines.md" \
  -F "type=GUIDELINE"
```

Document types:
- `CODE` - Source code patterns
- `GUIDELINE` - Coding standards and best practices
- `COMMIT_HISTORY` - Historical commit messages
- `DOCUMENTATION` - Project documentation

**Response:**
```json
{
  "id": 1,
  "name": "coding-guidelines.md",
  "type": "GUIDELINE",
  "indexed": true,
  "message": "Document uploaded and indexed successfully"
}
```

### Get All Documents

**GET** `/documents`

Retrieve list of all ingested documents:

```bash
curl http://localhost:8080/documents
```

### Get Document Details

**GET** `/documents/{id}`

Retrieve specific document:

```bash
curl http://localhost:8080/documents/1
```

### Index Pending Documents

**POST** `/documents/index-pending`

Manually trigger indexing of any unindexed documents:

```bash
curl -X POST http://localhost:8080/documents/index-pending
```

## Code Review with RAG Context

**POST** `/review`

The code review endpoint now automatically includes RAG context:

```bash
curl -X POST "http://localhost:8080/review" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "public class MyClass { }",
    "language": "java"
  }'
```

**What happens:**
1. Code is sent to vector store for similarity search
2. Top 5 most relevant documents are retrieved
3. Retrieved context is combined with the code
4. Augmented prompt is sent to Ollama
5. LLM generates review using organizational context

## Implementation Details

### Document Chunking Strategy
Documents are split into chunks to fit embedding context:
- Default chunk size: 1000 characters
- Chunks are separated by double newlines when possible
- Empty chunks are filtered out

### Vector Search
- Returns top 5 most similar documents (configurable)
- Uses cosine similarity for semantic matching
- Metadata includes document type, name, and ID for traceability

### Error Handling
- If vector search fails, reviews proceed without context
- Failed embeddings are logged but don't block uploads
- Automatic retries for transient failures

## Example Workflow

### Step 1: Upload Coding Guidelines
```bash
curl -X POST "http://localhost:8080/documents/upload" \
  -F "file=@guidelines.txt" \
  -F "type=GUIDELINE"
```

### Step 2: Upload Repository Code Patterns
```bash
curl -X POST "http://localhost:8080/documents/upload" \
  -F "file=@patterns.md" \
  -F "type=CODE"
```

### Step 3: Review Code with Context
```bash
curl -X POST "http://localhost:8080/review" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "// Your code here",
    "language": "java"
  }'
```

The review will now be based on:
- Your organizational coding guidelines
- Your established code patterns
- Your team's best practices

## Performance Considerations

- **Embedding Generation**: ~1-2 seconds per 1000 chars with Ollama
- **Vector Search**: <100ms for similarity search
- **Storage**: ~1KB per embedding vector in pgvector

## Troubleshooting

### pgvector Extension Not Found
```
ERROR: could not open extension control file
```
**Solution**: Install pgvector PostgreSQL extension in your database:
```bash
psql -U postgres -d ai_review -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### Model Not Found Error
Ensure Ollama models are available:
```bash
ollama list
ollama pull nomic-embed-text
ollama pull orca-mini
```

### Vector Store Connection Error
Check PostgreSQL connection in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/ai_review
    username: postgres
    password: your_password
```

## Next Steps

1. **Add More Document Types**: Implement custom chunking strategies
2. **Optimize Embeddings**: Use larger embedding models for better accuracy
3. **Advanced Filtering**: Filter retrieval by document type or date
4. **Hybrid Search**: Combine keyword search with semantic search
5. **Feedback Loop**: Log which documents helped each review for analytics

-- PostgreSQL setup script for RAG pipeline

-- Enable pgvector extension (run as superuser)
CREATE EXTENSION IF NOT EXISTS vector;

-- Verify pgvector is installed
SELECT * FROM pg_extension WHERE extname = 'vector';

-- The following tables will be created by Spring Boot's JPA automatically:
-- 1. documents - stores uploaded documents
-- 2. ai_document_store - created by PgVectorStore for embeddings

-- You can pre-seed some sample documents if needed:
-- INSERT INTO documents (name, content, type, uploaded_at, indexed) VALUES
-- ('Java Coding Guidelines', 'Follow proper naming conventions...', 'GUIDELINE', NOW(), false);

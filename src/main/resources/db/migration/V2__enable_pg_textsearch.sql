-- Migration script to enable pg_textsearch extension and create BM25 indexes
-- This enables BM25-ranked full-text search for courses and students

-- Enable the pg_textsearch extension
CREATE EXTENSION IF NOT EXISTS pg_textsearch;

-- Add generated columns for combined search fields
-- BM25 indexes can only be created on columns, not expressions

-- For courses: create a generated column that combines title and description
ALTER TABLE courses 
ADD COLUMN IF NOT EXISTS search_text TEXT 
GENERATED ALWAYS AS (title || ' ' || COALESCE(description, '')) STORED;

-- For students: create a generated column that combines first_name, last_name, and email
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS search_text TEXT 
GENERATED ALWAYS AS (first_name || ' ' || last_name || ' ' || email) STORED;

-- Create BM25 indexes on the generated columns
CREATE INDEX IF NOT EXISTS idx_courses_search_bm25 
ON courses 
USING bm25(search_text) 
WITH (text_config='english');

CREATE INDEX IF NOT EXISTS idx_students_search_bm25 
ON students 
USING bm25(search_text) 
WITH (text_config='english');

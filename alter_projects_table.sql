-- SQL script to alter projects table columns to support longer text
-- Run this script in your PostgreSQL database

ALTER TABLE projects 
    ALTER COLUMN abstract_text TYPE TEXT,
    ALTER COLUMN scope TYPE TEXT,
    ALTER COLUMN reference_text TYPE TEXT;


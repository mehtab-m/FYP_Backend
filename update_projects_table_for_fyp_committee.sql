-- SQL script to update projects table for FYP Committee functionality
-- Run this script in your PostgreSQL database

-- 1. Update status constraint to include 'accepted' status
-- First, drop the existing constraint (if it exists)
ALTER TABLE projects 
    DROP CONSTRAINT IF EXISTS projects_status_check;

-- Add new constraint with 'accepted' status
ALTER TABLE projects 
    ADD CONSTRAINT projects_status_check 
    CHECK (status IN ('pending', 'accepted', 'approved', 'rejected'));

-- 2. Add rejection_reason column to store rejection reasons
-- This is optional but recommended for better tracking
ALTER TABLE projects 
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

-- 3. Verify the changes
-- You can run these queries to verify:
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'projects' AND column_name = 'rejection_reason';
--
-- SELECT constraint_name, check_clause 
-- FROM information_schema.check_constraints 
-- WHERE constraint_name = 'projects_status_check';


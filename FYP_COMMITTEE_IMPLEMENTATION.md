# FYP Committee Backend Implementation

This document describes the backend implementation for the FYP Committee Dashboard functionality.

## Files Created/Modified

### New Files Created:
1. **`FYP_Backend/src/main/java/com/scd/fyp/service/FYPCommitteeService.java`**
   - Service class containing all business logic for FYP Committee operations
   - Methods: getAllProjectRegistrations(), getAvailableSupervisors(), assignSupervisor(), acceptProject(), rejectProject(), approveProject()

2. **`FYP_Backend/src/main/java/com/scd/fyp/controller/FYPController/FYPController.java`**
   - REST controller with all FYP Committee endpoints
   - Base path: `/api/admin`
   - All endpoints are CORS enabled

3. **`FYP_Backend/update_projects_table_for_fyp_committee.sql`**
   - SQL migration script to update database schema
   - Adds 'accepted' status to projects table constraint
   - Adds rejection_reason column

### Modified Files:
1. **`FYP_Backend/src/main/java/com/scd/fyp/model/Project.java`**
   - Added `rejectionReason` field to store rejection reasons
   - Updated status comment to include 'accepted'

## API Endpoints

All endpoints are under `/api/admin`:

### 1. GET `/api/admin/projects/registrations`
- Returns all project registrations with full details
- Includes: group members, supervisor preferences, assigned supervisor, rejection reason (if applicable)

### 2. GET `/api/admin/supervisors/available`
- Returns all available supervisors (users with SUPERVISOR role)

### 3. POST `/api/admin/projects/assign-supervisor`
- Assigns a supervisor to a project
- Request body: `{ "projectId": 1, "supervisorId": 10 }`
- Can be done before or after accepting the project

### 4. POST `/api/admin/projects/accept`
- Accepts a project registration
- Changes status from "pending" to "accepted"
- Request body: `{ "projectId": 1 }`

### 5. POST `/api/admin/projects/reject`
- Rejects a project registration
- Changes status from "pending" to "rejected"
- Stores rejection reason
- Request body: `{ "projectId": 1, "reason": "Rejection reason..." }`

### 6. POST `/api/admin/projects/approve`
- Approves a project (finalizes it)
- Changes status from "accepted" to "approved"
- Requires supervisor to be assigned
- Request body: `{ "projectId": 1 }`

## Database Changes Required

**IMPORTANT:** Run the SQL migration script before using the new functionality:

```sql
-- File: update_projects_table_for_fyp_committee.sql
```

This script will:
1. Update the status constraint to include 'accepted' status
2. Add `rejection_reason` column to projects table

## Status Flow

The project status follows this flow:

1. **pending** → (Accept) → **accepted** → (Assign Supervisor) → (Approve) → **approved**
2. **pending** → (Reject) → **rejected**

## Business Rules

1. **Accept Project:**
   - Project must be in "pending" status
   - Changes status to "accepted"

2. **Reject Project:**
   - Project must be in "pending" status
   - Rejection reason is required
   - Changes status to "rejected"
   - Stores rejection reason in database

3. **Assign Supervisor:**
   - Project can be in any status (pending, accepted, approved)
   - Supervisor must exist and have SUPERVISOR role
   - Creates or updates ProjectApproval record

4. **Approve Project:**
   - Project must be in "accepted" status
   - Supervisor must be assigned
   - Changes status to "approved"
   - Updates ProjectApproval record with committee info

## Response Format

All endpoints return JSON responses:

**Success Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error message here"
}
```

## Dependencies

The implementation uses existing repositories:
- `ProjectRepository`
- `ProjectSupervisorPreferenceRepository`
- `ProjectApprovalRepository`
- `GroupRepository`
- `GroupMemberRepository`
- `UserRepository`

## Testing

After running the database migration script, test the endpoints:

1. Get all project registrations
2. Get available supervisors
3. Accept a pending project
4. Assign a supervisor to an accepted project
5. Approve the project
6. Test rejection flow

## Notes

1. **Authentication/Authorization:** Currently, the endpoints don't have authentication checks. You should add authentication middleware to verify the user is an FYP Committee member.

2. **Committee ID:** The `approveProject` method accepts an optional `committeeId`. You can extract this from the authentication context or pass it in the request.

3. **Error Handling:** All methods include proper error handling and validation.

4. **Transaction Management:** All write operations use `@Transactional` to ensure data consistency.

5. **No Existing Functionality Disturbed:** The implementation is completely separate from existing ProjectService methods, so no existing functionality is affected.

## Next Steps

1. Run the SQL migration script
2. Add authentication/authorization checks
3. Add logging for audit trail
4. Consider adding notifications when projects are accepted/rejected/approved
5. Test all endpoints thoroughly


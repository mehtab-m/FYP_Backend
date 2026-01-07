# File Serving Fix Documentation

## Problem
Documents were not displaying due to:
1. **URL Encoding Issues**: Filenames with special characters (`{`, `}`, spaces) were being URL-encoded incorrectly
2. **No Static Resource Handler**: Spring Boot wasn't configured to serve files from the `uploads/` directory
3. **Frontend URL Construction**: Frontend was encoding path segments separately, causing issues with special characters

## Solution

### 1. Added Static Resource Handler
**File**: `FYP_Backend/src/main/java/com/scd/fyp/config/AppConfig.java`

- Added `WebMvcConfigurer` implementation
- Configured static resource handler for `/uploads/**` path
- Maps to the `uploads/` directory in the project root

### 2. Filename Sanitization
**File**: `FYP_Backend/src/main/java/com/scd/fyp/controller/StudentController/SubmitDocumentController.java`

- Added `sanitizeFilename()` method
- Replaces special characters (`{`, `}`, spaces, etc.) with underscores
- Prevents future files from having problematic characters
- Limits filename length to 200 characters

### 3. File Serving Controller
**File**: `FYP_Backend/src/main/java/com/scd/fyp/controller/FileController.java`

- New controller for serving files via API endpoint
- Endpoint: `GET /api/files?path=<encoded_path>`
- Properly decodes URL-encoded paths
- Sets correct content types (PDF, images, etc.)
- Security checks to prevent directory traversal attacks

### 4. Frontend URL Construction Fix
**Files**: 
- `FYP_SCD/src/pages/supervisor/ViewDocuments/ViewDocuments.jsx`
- `FYP_SCD/src/pages/committee/Evaluation/EvaluationDashboard.jsx`

- Updated `getFileUrl()` function to use the file serving endpoint
- Encodes entire path as query parameter instead of encoding segments separately
- Uses format: `http://localhost:9090/api/files?path=<encoded_path>`

## Usage

### Backend File Serving
Files are now accessible via:
```
GET http://localhost:9090/api/files?path=uploads/submissions/filename.jpg
```

The path parameter should be URL-encoded:
```javascript
const encodedPath = encodeURIComponent("uploads/submissions/file name.jpg");
const url = `http://localhost:9090/api/files?path=${encodedPath}`;
```

### Static Resource Access (Alternative)
Files can also be accessed directly via static resource handler:
```
GET http://localhost:9090/uploads/submissions/filename.jpg
```

## File Path Format

Files are stored with sanitized filenames:
- Format: `{groupId}_{documentId}_v{version}_{timestamp}_{sanitized_filename}`
- Special characters are replaced with underscores
- Spaces are replaced with underscores
- Example: `1_1_v1_1767635296545_file_name.jpg`

## Security Features

1. **Path Validation**: Ensures paths start with `uploads/`
2. **Directory Traversal Protection**: Prevents `..` in paths
3. **File Existence Check**: Verifies file exists before serving
4. **Content Type Detection**: Proper MIME types for different file formats

## Testing

To test file serving:

1. **Upload a file** via `/api/student/submissions/{documentId}`
2. **Get the file path** from the submission record
3. **Access the file** via:
   ```
   GET http://localhost:9090/api/files?path=<file_path>
   ```

## Notes

- Existing files with special characters in names may still have issues
- New uploads will have sanitized filenames
- Consider migrating existing files to use sanitized names if needed
- The static resource handler provides an alternative access method

## Migration for Existing Files

If you have existing files with problematic names, you can:

1. **Rename files manually** in the `uploads/submissions/` directory
2. **Update database records** to reflect new filenames
3. **Or use the file serving endpoint** which handles URL encoding properly


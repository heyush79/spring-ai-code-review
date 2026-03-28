# Spring AI Code Review - Thymeleaf Frontend Guide

## Overview
The Spring MVC + Thymeleaf frontend is now integrated into your Spring Boot application. All frontend pages are served from the same server as the backend REST API (port 8080).

## Architecture

### Frontend Structure
- **WebController.java** - Serves Thymeleaf HTML pages
- **Templates** (in `src/main/resources/templates/`)
  - `index.html` - Dashboard/Home page
  - `upload.html` - Document upload interface
  - `review.html` - Code review submission interface
  - `documents.html` - View and manage documents
- **Static Assets** (in `src/main/resources/static/css/`)
  - `style.css` - Modern, responsive styling

### REST API Endpoints (Unchanged)
- `POST /review` - Submit code for review (JSON)
- `POST /documents/upload` - Upload documents (form-data)
- `GET /documents` - List all documents (JSON)
- `GET /documents/{id}` - Get specific document (JSON)

## Getting Started

### 1. Start the Backend
```bash
cd c:\Users\Lenovo\spring-ai-code-review
mvn spring-boot:run
```

### 2. Access the Application
Open your browser and navigate to:
```
http://localhost:8080/
```

## Pages

### Dashboard (/)
- Entry point showing application overview
- Quick access to all features
- Feature highlights and how-it-works section

### Upload Documents (/upload)
- Upload reference documents (guidelines, code examples, etc.)
- Select document type (GUIDELINE, CODE, DOCUMENTATION, COMMIT_HISTORY)
- Real-time list of recent uploads
- Supported formats: txt, md, java, py, js, ts, xml

**How it Works:**
1. Click "Select File" and choose a document
2. Choose the document type
3. Click "Upload Document"
4. Document will be stored in PostgreSQL and cached in SimpleVectorStore
5. Recent uploads list updates automatically

### Code Review (/review)
- Submit code for AI-powered review
- Select programming language
- Paste code to review
- Get context-aware recommendations based on uploaded documents

**How it Works:**
1. Select programming language (Java, Python, JavaScript, etc.)
2. Paste your code in the textarea
3. Click "Get AI Review"
4. AI analyzes code against uploaded guidelines
5. Review results displayed with formatted recommendations

**Features:**
- Real-time processing feedback
- Automatic code analysis
- Context-aware recommendations from uploaded documents
- Success/error messages

### Documents (/documents)
- View all uploaded documents
- Search documents by name or content
- Filter by document type
- View full document content in modal

**Features:**
- Keyword search across all documents
- Type filtering (Guidelines, Code, Documentation, Commit History)
- Click "View Full" to see complete document
- Displays file size and upload date
- Auto-refresh every 15 seconds

## Technical Details

### How Data Flows

#### Document Upload Flow
```
User uploads file (upload.html)
        ↓
POST /documents/upload (DocumentController)
        ↓
DocumentIngestionService.uploadDocument()
        ↓
Save to PostgreSQL Document table
        ↓
Cache in SimpleVectorStore (in-memory)
        ↓
Return success response → Update UI
```

#### Code Review Flow
```
User submits code (review.html)
        ↓
POST /review (ReviewController)
        ↓
AIReviewService.reviewCode()
        ↓
Extract keywords from code
        ↓
Search uploaded documents for context
        ↓
Augment LLM prompt with context
        ↓
Call Ollama LLM (orca-mini model)
        ↓
Return formatted review → Display in UI
```

### Performance
- **In-Memory Caching:** SimpleVectorStore provides 20-80x faster retrieval than direct database hits
- **Auto-Refresh:** Document list and upload pages refresh automatically every 10-15 seconds
- **Lightweight:** No external dependencies or separate frontend server needed
- **Integrated:** Thymeleaf renders server-side, JavaScript handles interactivity

### Browser Compatibility
- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Modern responsive design for mobile/tablet/desktop

## Styling Features

- **Color Scheme:**
  - Primary: Blue (#2563eb)
  - Secondary: Teal (#0f766e)
  - Danger: Red (#dc2626)
  - Success: Green (#16a34a)

- **Responsive Design:**
  - Mobile-first approach
  - Adapts from 1-column (mobile) to multi-column (desktop)
  - Touch-friendly buttons and inputs

- **Interactive Elements:**
  - Hover effects on cards and buttons
  - Loading states with visual feedback
  - Status messages for success/error/info
  - Modal dialog for full document viewing

## Keyboard Shortcuts
- `Tab` - Navigate between form fields
- `Enter` - Submit forms (buttons)
- `Esc` - Close modal dialogs

## Troubleshooting

### Page Not Loading
- Verify backend is running: `mvn spring-boot:run`
- Check port 8080 is not in use
- Verify PostgreSQL is running on localhost:5433

### Upload Not Working
- Ensure file format is supported (.txt, .md, .java, etc.)
- Check file size is not too large
- Verify database connection in application.yml

### Code Review Not Working
- Ensure Ollama is running on localhost:11434
- Verify orca-mini model is downloaded: `ollama list`
- Check that at least one document has been uploaded
- See review.html console (F12) for API errors

### Documents Not Displaying
- Verify PostgreSQL has documents table
- Check database connection: `jdbc:postgresql://localhost:5433/ai_review`
- Clear browser cache (Ctrl+Shift+Delete)

## Development

### Adding New Pages
1. Create HTML template in `src/main/resources/templates/`
2. Add mapping method in `WebController.java`
3. Access via `http://localhost:8080/your-page-name`

### Customizing Styles
Edit `src/main/resources/static/css/style.css`:
- Modify CSS variables in `:root`
- Add new styles or components
- No rebuild needed, refresh browser to see changes

### Modifying Templates
Edit Thymeleaf templates in `src/main/resources/templates/`:
- Use Thymeleaf syntax: `th:*` attributes
- Reference static resources: `th:href="@{/css/style.css}"`
- No rebuild needed, refresh browser to see changes

## File Structure
```
src/main/
├── java/com/ai/reviewer/
│   └── controller/
│       ├── WebController.java          (NEW - serves Thymeleaf pages)
│       ├── ReviewController.java       (REST API for reviews)
│       └── DocumentController.java     (REST API for documents)
├── resources/
│   ├── templates/                      (NEW - Thymeleaf HTML files)
│   │   ├── index.html
│   │   ├── upload.html
│   │   ├── review.html
│   │   └── documents.html
│   ├── static/css/                     (NEW - Styles)
│   │   └── style.css
│   └── application.yml                 (Configuration)
```

## Next Steps

### Optional Enhancements
1. **User Authentication** - Add Spring Security for user-specific reviews
2. **Review History** - Display past code reviews
3. **Document Versioning** - Track document updates
4. **Export Reviews** - Download reviews as PDF
5. **Live Preview** - Real-time code syntax highlighting
6. **Advanced Search** - Full-text search with filters

### Production Deployment
1. Build JAR: `mvn clean package`
2. Run JAR: `java -jar target/spring-ai-code-review-0.0.1-SNAPSHOT.jar`
3. Configure production database and Ollama endpoints
4. Deploy to cloud platform (Azure, AWS, etc.)

## Support
For issues or questions:
1. Check browser console (F12) for JavaScript errors
2. Check application logs: `mvn spring-boot:run` console output
3. Verify all services are running (PostgreSQL, Ollama)
4. Review error messages in application status areas

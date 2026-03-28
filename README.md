I have used the orca-mini model of the Ollama AI API, since the default model was taking up more memory than my system's memory. Also I am 
using the Ollama API since it is free to use unlike Open AI API which is paid. 
In this project I have used Spring AI feature of spring boot to integrate the AI API with the code. The API reads the input from the client and gives the reponse on the 
piece of code provided with the language used. Unlike OpenAI API, the orca mini model is bit slow therfore it takes up some time to give back the response.
Currently I am only having the review feature but I will be adding RAG pipeline to this to get background knowledge as well.

<img width="1012" height="689" alt="image" src="https://github.com/user-attachments/assets/8ac6859a-70d0-4bb5-8083-56d317c41c5d" />

<img width="1055" height="607" alt="image" src="https://github.com/user-attachments/assets/5708f0d9-6180-4946-943d-96199aed3d9b" />

After latest changes of RAG pipeline:
<img width="998" height="919" alt="image" src="https://github.com/user-attachments/assets/2846e747-d188-4fc6-ba86-eebe4b3d9294" />
┌──────────────────────────────┐
│   Upload guidelines.md       │
│   "Use camelCase..."         │
│   "Never hardcode..."        │
└──────────────────┬───────────┘
                   │
        ┌──────────▼──────────┐
        │   DocumentController│
        │   /documents/upload │
        └──────────┬──────────┘
                   │
    ┌──────────────┴──────────────┐
    │                             │
    ▼                             ▼
┌─────────────────────┐   ┌──────────────────┐
│  PostgreSQL         │   │  SimpleVectorStore│
│  documents table    │   │  (In-Memory)      │
│                     │   │                  │
│ ID | Name | Content │   │ Chunks + Metadata│
│ 1  | ... | "Use..." │   │ "Use camelCase"  │
│    |     | "Never.."│   │ "Never hardcode" │
└─────────────────────┘   └──────────────────┘
   (PERSISTENT)          (SESSION-BASED)
   Survives restart       Lost on restart

<img width="1046" height="605" alt="image" src="https://github.com/user-attachments/assets/3296a928-6ea9-41fd-b546-64f2b691f45e" />

┌─────────────────────────────────────────────────────────────────┐
│                   SPRING AI CODE REVIEW - RAG FLOW              │
└─────────────────────────────────────────────────────────────────┘

                         PHASE 1: INGESTION
                         ═════════════════

Step 1: Upload Documents
┌──────────────────────────────────────────────┐
│ Developer uploads:                           │
│ • coding-guidelines.md                       │
│ • design-patterns.md                         │
│ • security-practices.md                      │
│ • architecture-decisions.md                  │
└──────────────────────────────────────────────┘
        ↓ (HTTP POST to /documents/upload)
   
Step 2: Store in Database
┌──────────────────────────────────────────────┐
│ PostgreSQL documents table:                  │
│ ┌─────┬──────────────────┬─────────────────┐ │
│ │ ID  │ Name             │ Content         │ │
│ ├─────┼──────────────────┼─────────────────┤ │
│ │ 1   │ guidelines.md    │ "Use camelCase" │ │
│ │ 2   │ patterns.md      │ "Singleton..." │ │
│ │ 3   │ security.md      │ "Never hard..." │ │
│ └─────┴──────────────────┴─────────────────┘ │
└──────────────────────────────────────────────┘
        ↓
   
Step 3: Make Searchable
┌──────────────────────────────────────────────┐
│ Application searches by keyword:             │
│ • Extract keywords: "java", "security"      │
│ • Query: SELECT * FROM documents WHERE      │
│          content LIKE '%keyword%'           │
└──────────────────────────────────────────────┘


                       PHASE 2: CODE REVIEW
                       ══════════════════

Step 4: Developer Requests Review
┌──────────────────────────────────────────────┐
│ POST /review                                 │
│ {                                            │
│   "code": "public class User { ... }",      │
│   "language": "java"                        │
│ }                                            │
└──────────────────────────────────────────────┘
        ↓

Step 5: Search for Context
┌──────────────────────────────────────────────┐
│ Extract keywords from code:                  │
│ ["class", "public", "User", "java"]  → Search │
│                                              │
│ Found Matches:                               │
│ 1. guidelines.md - "Use meaningful names"   │
│ 2. patterns.md - "Class naming convention"  │
│ 3. security.md - "Validate user input"      │
└──────────────────────────────────────────────┘
        ↓

Step 6: Augment Prompt with Context
┌──────────────────────────────────────────────┐
│ CREATE ENHANCED PROMPT:                      │
│                                              │
│ System: "You are an expert code reviewer"    │
│                                              │
│ Context from docs:                           │
│ • "Use camelCase for variables"             │
│ • "Implement Singleton pattern"             │
│ • "Validate all user inputs"                │
│ • "Never hardcode credentials"              │
│                                              │
│ Code:                                        │
│ public class User { ... }                   │
└──────────────────────────────────────────────┘
        ↓

Step 7: Call LLM (Ollama)
┌──────────────────────────────────────────────┐
│ Send augmented prompt to:                    │
│ Ollama orca-mini model                       │
│                                              │
│ Model receives:                              │
│ • Your org guidelines                        │
│ • Code to review                             │
│ • Context from uploaded docs                 │
└──────────────────────────────────────────────┘
        ↓

Step 8: Generate Context-Aware Review
┌──────────────────────────────────────────────┐
│ Response:                                    │
│                                              │
│ "Review of User class:                       │
│                                              │
│ Issues found per your guidelines:            │
│ 1. ⚠️ Variable naming violates camelCase     │
│ 2. ⚠️ Missing input validation (per your     │
│       security-practices.md)                 │
│ 3. ✓ Correctly uses Singleton pattern       │
│ 4. ⚠️ Hardcoded values (violates security)   │
│                                              │
│ Recommendations:                             │
│ • Follow pattern from design-patterns.md     │
│ • Apply validation from security-practices  │
│ "                                            │
└──────────────────────────────────────────────┘

# Java Coding Guidelines - ayush Corp

## Naming Conventions
- Variables: camelCase (myVariable)
- Classes: PascalCase (MyClass)
- Constants: UPPER_CASE (MAX_SIZE)

## Error Handling
- Always null-check parameters
- Catch specific exceptions
- Log with full stacktrace

## Security
- Never hardcode credentials
- Use environment variables
- Validate all inputs from users
- Use prepared statements

## Database Access
- Use Repository pattern
- Implement pooling
- Avoid N+1 queries

# Company Standard Patterns

## Singleton Pattern - Database Connection
```java
public class DatabaseConnection {
    private static DatabaseConnection instance;
    
    private DatabaseConnection() {}
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}

I have used the orca-mini model of the Ollama AI API, since the default model was taking up more memory than my system's memory. Also I am 
using the Ollama API since it is free to use unlike Open AI API which is paid. 
In this project I have used Spring AI feature of spring boot to integrate the AI API with the code. The API reads the input from the client and gives the reponse on the 
piece of code provided with the language used. Unlike OpenAI API, the orca mini model is bit slow therfore it takes up some time to give back the response.
Currently I am only having the review feature but I will be adding RAG pipeline to this to get background knowledge as well.

<img width="1012" height="689" alt="image" src="https://github.com/user-attachments/assets/8ac6859a-70d0-4bb5-8083-56d317c41c5d" />

<img width="1055" height="607" alt="image" src="https://github.com/user-attachments/assets/5708f0d9-6180-4946-943d-96199aed3d9b" />

After latest changes of RAG pipeline:
<img width="998" height="919" alt="image" src="https://github.com/user-attachments/assets/2846e747-d188-4fc6-ba86-eebe4b3d9294" />

<img width="731" height="703" alt="image" src="https://github.com/user-attachments/assets/222d5334-2e6c-442b-8cd5-eceed7265075" />


<img width="1046" height="605" alt="image" src="https://github.com/user-attachments/assets/3296a928-6ea9-41fd-b546-64f2b691f45e" />

Here’s your **complete single README.md content** (copy-paste directly into GitHub) 👇

---

````markdown
# 🧠 Spring AI Code Review System (RAG आधारित)

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Ollama](https://img.shields.io/badge/Ollama-LLM-black)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 🚀 Overview

This project is a **Spring AI-powered Code Review System** using **Retrieval-Augmented Generation (RAG)**.

Instead of relying only on an LLM, the system enhances code reviews by injecting **organization-specific knowledge**, such as:

- Coding Guidelines  
- Design Patterns  
- Security Practices  
- Architecture Decisions  

This results in **context-aware, personalized code reviews** aligned with your team's standards.

---

## 🏗️ Architecture

```mermaid
flowchart TD
    A[Upload Documents] --> B[Store in PostgreSQL]
    B --> C[Keyword Search / Retrieval]
    C --> D[Augment Prompt]
    D --> E[LLM (Ollama)]
    E --> F[Context-Aware Code Review]
````

---

## 📥 PHASE 1: INGESTION

### 📄 Step 1: Upload Documents

Developers upload internal knowledge documents:

* `coding-guidelines.md`
* `design-patterns.md`
* `security-practices.md`
* `architecture-decisions.md`

```http
POST /documents/upload
```

---

### 🗄️ Step 2: Store in Database

Documents are stored in PostgreSQL:

| ID | Name          | Content           |
| -- | ------------- | ----------------- |
| 1  | guidelines.md | Use camelCase     |
| 2  | patterns.md   | Singleton...      |
| 3  | security.md   | Never hardcode... |

---

### 🔍 Step 3: Make Searchable

The application performs keyword-based retrieval:

```sql
SELECT * 
FROM documents 
WHERE content LIKE '%keyword%';
```

---

## 🔎 PHASE 2: CODE REVIEW

### 📤 Step 4: Request Review

```http
POST /review
```

```json
{
  "code": "public class User { ... }",
  "language": "java"
}
```

---

### 🔎 Step 5: Context Retrieval

Keywords extracted from code:

```
["class", "public", "User", "java"]
```

Matched documents:

* guidelines.md → Use meaningful names
* patterns.md → Class naming convention
* security.md → Validate user input

---

### 🧩 Step 6: Prompt Augmentation

The system builds an enhanced prompt:

```
System: You are an expert code reviewer

Context:
• Use camelCase for variables
• Implement Singleton pattern
• Validate all user inputs
• Never hardcode credentials

Code:
public class User { ... }
```

---

### 🤖 Step 7: LLM Processing (Ollama)

* Model: `orca-mini`
* Input includes:

  * Retrieved context
  * Code snippet
  * Organization guidelines

---

### ✅ Step 8: Context-Aware Output

Example response:

```
Review of User class:

Issues found:

1. ⚠️ Variable naming violates camelCase
2. ⚠️ Missing input validation
3. ✓ Correct Singleton pattern usage
4. ⚠️ Hardcoded values detected

Recommendations:
• Follow design-patterns.md
• Apply security-practices.md
```

---

## ⚙️ Setup Instructions

### 1️⃣ Clone Repository

```bash
git clone https://github.com/your-username/spring-ai-code-review.git
cd spring-ai-code-review
```

---

### 2️⃣ Prerequisites

Make sure you have:

* Java 17+
* Maven or Gradle
* PostgreSQL installed
* Ollama installed

---

### 3️⃣ Setup PostgreSQL

Create database:

```sql
CREATE DATABASE ai_review;
```

Update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/ai_review
    username: postgres
    password: your_password

  jpa:
    hibernate:
      ddl-auto: update
```

---

### 4️⃣ Setup Ollama

Run the model:

```bash
ollama run orca-mini
```

---

### 5️⃣ Run Application

```bash
./mvnw spring-boot:run
```

---

## 🧪 API Endpoints

### 📄 Upload Documents

```http
POST /documents/upload
```

---

### 🔍 Review Code

```http
POST /review
```

---

## 💡 Future Improvements

* 🔥 Replace keyword search with **pgvector (semantic search)**
* 🔥 Add embeddings via OpenAI / Ollama
* 🔥 Build frontend dashboard
* 🔥 Multi-language code support

---

## 🧠 Tech Stack

* **Backend:** Spring Boot, Spring AI
* **Database:** PostgreSQL
* **LLM:** Ollama (`orca-mini`)
* **Future Scope:** pgvector (vector search)

---

## 👨‍💻 Author

**Ayush Bundela**
B.Tech, NIT Trichy

---

## ⭐ Contributing

Feel free to fork the repo, open issues, and submit pull requests!

---

```

---

If you want, next I can:
- 🔥 Add **pgvector + embeddings section (this will make your project stand out in placements)**  
- 🎯 Convert this into a **resume-ready project description (very high impact)**
```


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

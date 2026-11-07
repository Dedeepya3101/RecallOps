# RecallOps — AI-Powered Debugging Agent 🛠️

**Debug smarter. Remember past incidents. Resolve recurring errors faster.**

RecallOps is an AI-powered debugging assistant that helps developers investigate software errors by analyzing logs and available evidence, generating diagnostic hypotheses, and retrieving relevant lessons from previous incidents using **Hindsight persistent memory**.

Instead of treating every error as a completely new problem, RecallOps is designed to reuse previously confirmed debugging knowledge to help developers investigate recurring issues more effectively.

## 🚀 Key Features

- **AI-Powered Incident Analysis:** Analyze submitted errors and logs to generate possible root causes and recommended fixes.
- **Evidence-Based Diagnosis:** Present supporting evidence, diagnostic hypotheses, and missing information that could help identify the problem.
- **Persistent Memory with Hindsight:** Retrieve relevant information from previous incidents to provide additional debugging context.
- **Feedback-Driven Learning:** Retain debugging lessons only after the developer confirms that a proposed resolution was successful.
- **Incident History:** Review incident analysis results during the current backend session.
- **Secret Redaction:** Help protect sensitive information by redacting recognized secrets before processing.
- **Developer-Friendly Dashboard:** Use a React-based interface to submit incidents, inspect diagnoses, and provide feedback.

## 🎯 Problem Statement

Developers frequently encounter recurring errors involving application configuration, API failures, database connections, dependency issues, and unexpected application behavior.

Traditional debugging often requires developers to search logs, revisit previous fixes, and repeat investigations that have already been performed.

RecallOps aims to reduce this repeated effort by combining current incident evidence with relevant debugging knowledge retained from earlier incidents.

## 💡 Our Solution

RecallOps connects a web dashboard, a Spring Boot backend, an AI model, and Hindsight memory.

The application follows this workflow:

1. A developer submits an error description, logs, or other relevant evidence.
2. The backend processes the incident and redacts recognized secrets.
3. RecallOps attempts to retrieve relevant context from Hindsight.
4. The AI model analyzes the current evidence alongside any retrieved context.
5. The dashboard presents possible causes, supporting evidence, missing information, and suggested fixes.
6. The developer tests the suggested resolution and provides feedback.
7. When the developer confirms success, the application can retain the corresponding debugging lesson in Hindsight, provided the service is configured and the retention request succeeds.
8. In a later session, RecallOps can retrieve that lesson when investigating a similar incident.

**Important:** AI-generated diagnoses are suggestions, not guaranteed fixes. Developers should validate recommendations before applying them.

## 🏗️ Architecture

```text
Developer
    |
    v
React + Vite Dashboard
    |
    v
Spring Boot REST API
    |
    +----------------------+
    |                      |
    v                      v
Hindsight Memory       OpenAI API
    |                      |
    v                      v
Past incident context  AI diagnosis
    |                      |
    +-----------+----------+
                |
                v
       Diagnosis Response
                |
                v
       Developer Feedback
                |
                v
    Confirmed Resolution
                |
                v
       Hindsight Retention
```

Hindsight provides external memory for the application. It does not train or fine-tune the AI model. The model uses retrieved information as additional context when generating a diagnosis.

## 🧰 Technology Stack

| Component | Technology |
|---|---|
| Frontend | React |
| Frontend tooling | Vite |
| Backend | Java, Spring Boot |
| Backend build tool | Maven |
| AI diagnosis | OpenAI Responses API |
| Persistent AI memory | Hindsight API |
| API communication | HTTP / REST |
| Version control | Git and GitHub |

## 📁 Project Structure

The project contains a React frontend and a Spring Boot backend.

```text
RecallOps/
├── frontend/             # React + Vite application
├── backend/              # Spring Boot API
├── .env.example          # Environment variable template
├── .gitignore            # Files excluded from Git
└── README.md             # Project documentation
```

*Note: Adjust the directory names above if your actual repository uses a different structure.*

## ⚙️ Prerequisites

Install the following before running RecallOps:

- Git
- Node.js and npm
- A compatible Java Development Kit (JDK)
- Maven, or use the project's Maven Wrapper if provided
- An OpenAI API key
- A configured Hindsight Cloud account and API credentials

Use the Java version required by the backend's `pom.xml`.

## 🔧 Setup and Installation

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/RecallOps.git
cd RecallOps
```

Replace `YOUR_USERNAME` with the GitHub repository owner's username.

### 2. Configure environment variables

Review `.env.example` and the backend's configuration to identify the exact environment variables required.

Configure the credentials for:

- OpenAI API access and model selection
- Hindsight API access
- Hindsight memory bank identification
- Backend configuration, if required

Use the exact variable names expected by the application.

**Security:** Never commit real API keys, passwords, or private credentials to GitHub. Keep local secrets in ignored environment files or environment variables. Do not put backend API keys in React's public environment variables.

### 3. Start the backend

Open a terminal in the backend directory.

If the project includes a Maven Wrapper on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Otherwise, use Maven if it is installed:

```powershell
mvn spring-boot:run
```

Use the port configured in the Spring Boot application.

### 4. Start the frontend

Open another terminal in the frontend directory:

```bash
npm install
npm run dev
```

Open the local URL printed by Vite in the terminal.

*Check the actual project directories, configured ports, and README environment instructions before running these commands.*

## 🧪 Testing

The current development verification report records:

- Frontend production build: passed.
- Backend automated tests: 5 passed, 0 failures.

The reported tests and build verify only the checks that were executed. Live OpenAI and Hindsight requests have not yet been verified with configured credentials.

Before considering the application production-ready, test the complete workflow:

1. Submit a representative software error.
2. Verify that the backend returns an AI-generated diagnosis.
3. Check whether Hindsight retrieval succeeds and whether retrieved context is used.
4. Confirm that unsuccessful or unverified resolutions are not retained as successful lessons.
5. Mark a resolution successful and verify that the retention request succeeds.
6. Submit a similar error in a later session and verify that the earlier lesson is retrieved.
7. Test unavailable services, malformed requests, and secret redaction.

## 🔐 Security and Reliability

RecallOps is designed with several safety considerations:

- Recognized secrets should be redacted before data is sent to external services.
- API credentials should be stored outside the source code.
- Suggested fixes should be reviewed and tested by developers.
- Memory retention should depend on explicit successful-resolution feedback.
- Service failures should be communicated clearly rather than presented as successful operations.

Secret redaction cannot guarantee that every sensitive value will be detected. Review logs and submitted code before sharing them with external AI services.

## ⚠️ Current Limitations

- Live OpenAI and Hindsight integration requires configuration and end-to-end verification.
- Incident history is currently stored in backend memory and resets when the backend restarts.
- AI-generated root causes and suggested fixes may be incorrect or incomplete.
- Successful-feedback-only retention does not independently prove that a fix is correct; the developer's confirmation is currently the basis for retention.

## 🔮 Future Enhancements

- Persistent database storage for incident history.
- More comprehensive integration and end-to-end tests.
- Support for additional programming languages and frameworks.
- Improved incident similarity search and diagnosis evaluation.
- Repository integration for analyzing source code and stack traces.
- Optional developer-approved fix generation and application.
- Deployment with secure configuration and monitoring.

## 👥 Team

**Project:** RecallOps — AI-Powered Debugging Agent

**Team members:**
- [Team Member 1]
- [Team Member 2]
- [Team Member 3]
- [Team Member 4]

Update the team list with your actual members and GitHub or LinkedIn profiles.

## 📚 References

- [Hindsight Documentation](https://docs.hindsight.vectorize.io/)
- [Hindsight Recall API](https://docs.dev.hindsight.vectorize.io/api-reference/recall-memories/)
- [OpenAI Responses API](https://developers.openai.com/api/reference/resources/responses/methods/create)
- [React Documentation](https://react.dev/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/index.html)
- [Vite Documentation](https://vite.dev/)

## 📄 License

A license has not yet been selected. Add an appropriate open-source license if the project owners intend to permit public reuse and distribution.

---

**RecallOps — Turning previous debugging experience into reusable knowledge.**

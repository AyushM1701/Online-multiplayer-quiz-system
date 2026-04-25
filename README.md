# Multiplayer Quiz System

A real-time LAN-based multiplayer quiz game built in Java. Multiple players compete simultaneously over a shared WiFi network, answering timed questions fetched from a MySQL database.

---

## Features

- Lobby system that waits for all players before starting
- Synchronized gameplay — all players get the same questions at the same time
- 10 second countdown timer per question with auto-submit on timeout
- Shuffled answer options every round to prevent copying
- Live leaderboard showing top 5 scores after each round
- Scores saved permanently to a MySQL database
- Play Again button to restart without closing the app
- Clean light-themed Swing GUI

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (JDK 17+) |
| GUI | Java Swing |
| Networking | Java Sockets + Multithreading |
| Database | MySQL 8+ |
| DB Driver | MySQL Connector/J 9.6.0 |

---

## Project Structure

```
MultiplayerQuizSystem/
├── src/
│   ├── model/
│   │   ├── Question.java        # Question data model
│   │   └── Player.java          # Player data model
│   ├── server/
│   │   ├── QuizServer.java      # Entry point, lobby loop
│   │   ├── ClientHandler.java   # One thread per connected player
│   │   ├── QuizManager.java     # Loads questions, saves scores, leaderboard
│   │   └── DBConnection.java    # MySQL JDBC connection
│   └── client/
│       ├── LoginUI.java         # Login window (name + server address)
│       ├── QuizUI.java          # Main quiz window
│       ├── ResultUI.java        # Final score display
│       └── ClientConnection.java # Socket connection to server
├── database/
│   └── reset.sql                # Creates DB, tables and seed questions
├── lib/
│   └── mysql-connector-j-9.6.0.jar
├── launchers/
│   ├── server-manifest.txt
│   └── client-manifest.txt
├── build.bat                    # Compiles and packages fat JARs (Windows)
├── QuizServer.bat               # Starts the server
├── QuizClient.bat               # Starts the client
├── RunAll.bat                   # Builds + starts everything in one click
└── HOW_TO_PLAY.txt              # Setup and play guide for all users
```

---

## Prerequisites

- [Java JDK 17+](https://adoptium.net)
- [MySQL 8+](https://dev.mysql.com/downloads/installer)

Verify Java is installed:
```bash
java -version
javac -version
```

---

## Setup (First Time Only)

### 1. Clone the repository
```bash
git clone https://github.com/your-username/Online-multiplayer-quiz-system.git
cd Online-multiplayer-quiz-system
```

### 2. Set your MySQL password
Open `src/server/DBConnection.java` and update this line:
```java
if (p == null) p = "your_mysql_password_here";
```

### 3. Set up the database
Open Command Prompt (not PowerShell) and run:
```cmd
mysql -u root -p < database/reset.sql
```
This creates the `quiz_db` database, all tables, and loads 10 sample questions.

### 4. Build the JARs

**Windows:**
```cmd
build.bat
```

**Linux / Mac:**
```bash
chmod +x build.sh && ./build.sh
```

This produces two files: `QuizServer.jar` and `QuizClient.jar`

---

## Running the Game

### Option A — One click (Windows)
Double-click `RunAll.bat` — builds the project and opens both the server and client automatically.

### Option B — Manual

**Start the server:**
```cmd
StartServer.bat
```
or
```bash
java -jar QuizServer.jar
```

**Start the client:**
```cmd
StartClient.bat
```
or
```bash
java -jar QuizClient.jar
```

---

## Multiplayer over LAN

1. Find your IP address:
```cmd
ipconfig
```
Look for **IPv4 Address** — e.g. `192.168.1.5`

2. Host starts the server and connects using `localhost`

3. Other players connect using the host's IP address, port `5000`

4. Once all players have joined the lobby, a 3…2…1 countdown fires and the quiz begins simultaneously for everyone

> Both devices must be on the same WiFi network. On Windows, allow port 5000 through the firewall:
> ```cmd
> netsh advfirewall firewall add rule name="QuizServer" dir=in action=allow protocol=TCP localport=5000
> ```

---

## How It Works

```
Player 1 joins          Player 2 joins
      │                       │
      └──────────┬────────────┘
                 │
         Lobby countdown
         3 … 2 … 1 … GO
                 │
     Questions sent to all players
     simultaneously via sockets
                 │
      Each player answers independently
      (10 second timer per question)
                 │
      Scores saved to MySQL database
                 │
      Leaderboard broadcast to all
```

---

## Adding More Questions

Open MySQL and run:
```sql
USE quiz_db;

INSERT INTO questions (question, option1, option2, option3, option4, answer)
VALUES ('Your question here?', 'Option A', 'Option B', 'Option C', 'Option D', 0);
```
The `answer` column is **0-indexed** — `0` means option1 is correct, `1` means option2, and so on.

---

## Changing the Number of Players

Open `src/server/QuizServer.java` and change:
```java
public static int MIN_PLAYERS = 2;
```
Or pass it as an argument at runtime:
```bash
java -jar QuizServer.jar 3
```

## Division of Work

Ayush Mishra — Database & Data Layer
Files: reset.sql, DBConnection.java, QuizManager.java, Question.java, Player.java

Piyush Rawat— Server & Networking
Files: QuizServer.java, ClientHandler.java

vrishali sahay — Client & GUI
Files: ClientConnection.java, LoginUI.java, QuizUI.java, ResultUI.java

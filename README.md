# Cinephile – Movie Exploration, Quiz and Recommendation

This is an Android application that uses the TMDB (The Movie Database) API to allow users to search movies, manage watchlists, and take quizzes.

---

## Developer Workflow & Guidelines

### 1. Git Configuration (One-Time Setup)

To avoid line-ending issues between macOS and Windows, all developers **must** run this command once on their local machine:

```bash
git config --global core.autocrlf input
```

---

### 2. Android Resources (Drawables, Layouts, etc.)

**Naming Convention:**  
All resource files must use **snake_case**, all lowercase:

- `fragment_search.xml`
- `ic_movie_placeholder.xml`
- `activity_main.xml`

**Image Assets:**  
Use Android Studio **Resource Manager**:

`View → Tool Windows → Resource Manager`  
Then import drawables, icons, and images.

---

### 3. Commit Frequency & Commit Message Rules

Commit **small**, **logical**, and **atomic** units of work.

#### ✔ Good Commit Message Examples

```
feat: Add RecyclerView to SearchFragment layout
test: Add unit test for movie search success state
fix: Handle API error in MovieRepository correctly
docs: Update README with setup instructions
chore: Add MR template and project structure folders
```

---

## Day-1 Completion Checklist

Both developers **must** complete these steps to confirm the Day-1 environment setup is correct.

---

### ✔ 1. Sync With Main Branch

```bash
git checkout main
git pull origin main
```

Expected: No conflicts, branch updates successfully.

---

### ✔ 2. Run Unit Tests Locally

**Mac/Linux:**
```bash
./gradlew testDebugUnitTest
```

**Windows:**
```bat
gradlew.bat testDebugUnitTest
```

Expected Outcome:  
**BUILD SUCCESSFUL** with all unit tests passing.

---

### ✔ 3. Build the App Locally

**Mac/Linux:**
```bash
./gradlew assembleDebug
```

**Windows:**
```bat
gradlew.bat assembleDebug
```

Expected Outcome:  
**BUILD SUCCESSFUL** (no compile errors).

---

### ✔ 4. Run the App

1. Open the project in Android Studio.
2. Select a device/emulator.
3. Run the app.

Expected Outcome:  
The app launches successfully without crashes.

---

### End of Document

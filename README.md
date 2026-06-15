# VaultWise

VaultWise is a personal budgeting Android application that helps users track expenses, manage monthly spending goals, view spending analytics, and earn rewards for positive money habits. The app stores user data locally using Room Database and follows an MVVM structure.

## Final POE Required Features
## Github Link: https://github.com/MothokaST10439690/VaultWise
## OneDrive video link:

### 1. Expense Data Capture and Expense Views

Users can log expenses with the following details:

* Amount
* Description
* Date
* Start and end time
* Payment method: Cash or Digital/Card
* Category
* Custom category
* Optional receipt photo

Saved expenses can be viewed on the History screen. Users can also filter their expense history by date range.

### 2. Graph Showing Amount Spent Per Category Over a User-Selectable Period

The Analytics screen includes a Category Spending Graph.

Users can press Select Graph Period and choose a custom date range. The donut graph and category legend then update according to the selected period.

The graph section displays:

* Selected graph period
* Total amount spent during the selected period
* Minimum spending goal
* Maximum spending goal
* Status showing whether the user is below, within, or over the goal range

### 3. Visual Display of Monthly Goal Progress

The Analytics screen includes a Past Month Budget Progress card.

This card shows:

* Remaining budget
* Progress bar
* Amount spent during the past month
* Minimum and maximum goal range
* Status message showing whether the user is below the minimum goal, within the goal range, or over the maximum goal

Committed monthly bills are included in this monthly progress calculation.

### 4. Gamification: Rewards and Badges

The Rewards & Money Match screen includes badges that unlock based on real user behaviour in the app.

Available badges include:

* First Step: Log your first expense
* Consistency Star: Log at least one expense today
* 7-Day Logger: Log expenses on 7 different days
* Under Max Hero: Stay below the maximum monthly goal
* Budget Keeper: Stay between the minimum and maximum monthly goals
* Receipt Pro: Attach 5 receipt photos
* Category Explorer: Track expenses in 3 or more categories during the current month

The same screen also includes the Money Match mini-game, where users practise smart money habits in a simple interactive game.

## Own Features for Final POE

### Own Feature 1: Receipt Photo Capture

Users can take and attach a receipt photo when logging an expense. The receipt URI is saved with the expense record, making the expense entry more complete and easier to check later.

Where to test it:

1. Open Log Expense.
2. Tap Receipt Photo.
3. Take or select a photo.
4. Save the expense.
5. View the saved expense in History.

### Own Feature 2: Committed Monthly Bills

Users can add fixed monthly expenses such as rent, subscriptions, transport contracts, or other recurring bills. These committed expenses are included in the monthly budget progress calculation so the user gets a more realistic view of available money.

Where to test it:

1. Open Settings.
2. Add a bill name and amount under Committed Expenses.
3. Open Analytics.
4. Check that the bills total appears.
5. Check that the committed bills are included in the monthly budget progress calculation.

### Own Feature 3: Profile Photo

Users can add a profile picture to personalise their account.

Where to test it:

1. Open Settings.
2. Tap the profile image area.
3. Select or add a picture.
4. Check that the profile picture updates.

## Extra Features

* User registration and login
* Local session handling
* Profile photo selection
* Dark mode toggle
* English and Afrikaans language option
* Cash and digital spending totals
* Daily spending chart
* Category spending graph
* Date range filtering
* GitHub Actions workflow for tests and build
* Automated unit tests for budget rules, time validation, goal validation, and badge unlock logic

## Project Structure

```text
com.example.vaultwise/
├── data/
│   ├── dao/            # Room database queries
│   ├── entity/         # Room tables
│   └── repository/     # Data access layer
├── ui/
│   ├── auth/           # Login, sign-up, and budget setup screens
│   ├── fragments/      # Dashboard screens
│   ├── viewmodel/      # UI state and business logic
│   └── DashboardActivity.kt
├── util/               # Session, settings, category helpers, and budget rules
└── MainActivity.kt
```

## Tech Stack

* Kotlin
* Android XML layouts
* Material Components
* Room Database
* Coroutines and Flow
* MVVM architecture
* Gradle Kotlin DSL
* JUnit tests
* GitHub Actions

## Testing

The project includes automated unit tests for important app logic, including:

* Budget goal validation
* Time validation
* Goal status calculation
* Badge unlock logic

To run tests:

```bash
./gradlew test
```

On Windows:

```bash
gradlew.bat test
```

## Setup Instructions

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run the unit tests.
4. Build the project.
5. Install and run the app on a real Android phone for the final POE demonstration.

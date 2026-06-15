# VaultWise

VaultWise is a personal budgeting Android app for tracking expenses, managing monthly spending goals, viewing analytics, and earning rewards for healthy money habits. The app stores data locally with Room and follows an MVVM structure.

## Final POE Required Features

### 1. Expense Data Capture and Views

Users can log expenses with:

- Amount
- Description
- Date
- Start and end time
- Payment method: Cash or Digital/Card
- Category
- Custom category
- Optional receipt photo

Saved expenses can be viewed in the History screen and filtered by date range.

### 2. Graph Showing Amount Spent Per Category Over a User-Selectable Period

The Analytics screen includes a **Category Spending Graph**. The user can press **Select Graph Period** and choose a custom date range. The donut graph and category legend update based on the selected period.

The graph section also displays:

- Selected graph period
- Selected period spending total
- Minimum goal
- Maximum goal
- Status showing whether the selected period is below, within, or over the goal range

### 3. Visual Display of Monthly Goal Progress

The Analytics screen includes a **Past Month Budget Progress** card. It shows:

- Remaining budget
- Progress bar
- Amount spent in the past month
- Minimum and maximum goal range
- Status message for below minimum, within range, or over maximum

Committed monthly bills are included in this monthly progress calculation.

### 4. Gamification: Rewards and Badges

The Rewards & Money Match screen includes badges that unlock based on real app behaviour:

- **First Step**: Log your first expense
- **Consistency Star**: Log at least one expense today
- **7-Day Logger**: Log expenses on 7 different days
- **Under Max Hero**: Stay below the maximum monthly goal
- **Budget Keeper**: Stay between the minimum and maximum monthly goals
- **Receipt Pro**: Attach 5 receipt photos
- **Category Explorer**: Track expenses in 3 or more categories this month

The same screen also keeps the Money Match mini-game, where the user practises smart money habits.

## Own Features for Final POE

### Own Feature 1: Receipt Photo Capture

Users can take and attach a receipt photo when logging an expense. The receipt URI is saved with the expense record, making the expense entry more complete and useful for later checking.

Where to test it:

1. Open **Log Expense**.
2. Tap **Receipt Photo**.
3. Take a photo.
4. Save the expense.

### Own Feature 2: Committed Monthly Bills

Users can add fixed monthly expenses such as rent, subscriptions, transport contracts, or other bills. These committed expenses are included in the monthly budget progress calculation so the user sees a more realistic view of available money.

Where to test it:

1. Open **Settings**.
2. Add a bill name and amount under **Committed Expenses**.
3. Open **Analytics**.
4. Check that the bills total appears and is included in the monthly budget progress.


### Own Feature 3: user can add profile
User can simply add a **Profile picture**
Where to test it:

1.Open **Setting** 
2.Click Profile 
3.ADD a picture


## Extra Features

- User registration and login
- Local session handling
- Profile photo selection
- Dark mode toggle
- English/Afrikaans language option
- Cash and digital spending totals
- Daily spending chart
- GitHub Actions workflow for tests and build
- Automated unit tests for budget rules, time validation, goal validation, and badge unlock logic

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

- Kotlin
- Android XML layouts
- Material Components
- Room Database
- Coroutines and Flow
- MVVM architecture
- Gradle Kotlin DSL
- JUnit tests
- GitHub Actions

## Setup

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run unit tests with `./gradlew test`.
4. Run the app on a real Android phone for final POE demonstration.

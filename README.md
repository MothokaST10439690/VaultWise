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

## Suggested Demonstration Video Flow

1. Open the app on a real mobile phone.
2. Register or log in.
3. Set minimum and maximum monthly goals in Settings.
4. Add a committed monthly bill.
5. Log an expense with category, payment method, date, time, and receipt photo.
6. Open History and show the saved expense.
7. Open Analytics and show the past-month progress card.
8. Select a graph period and show the category graph updating.
9. Show the minimum and maximum goals displayed with the graph.
10. Open Rewards & Money Match and show earned/locked badges.
11. Play one round of Money Match.

## Notes

This project is designed for the final POE requirements. The strongest screens to demonstrate are Log Expense, Analytics, Settings, History, and Rewards & Money Match.

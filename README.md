## About Spendly Space

Spendly is a modern, collaborative personal finance application. It is designed to allow individuals, families, or groups of friends to track and manage their shared or individual financial tasks. The application is built using modern Android development practices, utilizing Jetpack Compose for a fully reactive, high-performance UI and Room Database for reliable offline local storage.
                                             
---

## Core Features and Operations

### 1. Collaborative Multi-Member Vaults
Spendly supports tracking financial accounts across multiple group members. Users can easily view the aggregate status of all vaults or filter down to a single member's dashboard.
* **Member Creation**: Add members with specific roles (Primary, Partner, Family, Friend) and customized visual colors.
* **Aggregated Balances**: The dashboard automatically calculates total wealth across all members or filters logs instantly based on selected member avatars.

### 2. Live Ledger & Real-Time Search
* **Income and Expense Tracking**: Log incoming and outgoing transactions. Expenses are recorded as negative values and incomes as positive values.
* **Description Search**: Filter the entire transaction ledger instantly via keyword matching on descriptions and categories.

### 3. Cosmic Exchange (Vault Transfers)
The app features a custom Exchange screen that facilitates transferring funds between different member accounts. 
* When a transfer is performed, the app automatically creates two offsetting transactions: a negative ledger entry for the sender and a corresponding positive entry for the receiver, ensuring the overall ledger remains balanced.

### 4. Financial Planning Tools
* **Active Category Budgets**: Define monthly budget limits for different categories (e.g., Food, Rent, Entertainment, Shopping). The app calculates real-time usage percentages relative to transactions in the active month.
* **Goals Vault**: Create long-term savings goals with defined targets. Users can make incremental deposits to vault goals and view progress bars.
* **Bill Reminders**: Create upcoming bill reminders with due dates. Track which bills have been settled, and simulate local push alert notifications to preview payment reminders.

---

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

*View the app in Android Studio: https://developer.android.com/studio*

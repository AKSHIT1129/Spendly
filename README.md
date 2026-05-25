<div align="center">
<img width="250" height="250" alt="Android Studio Logo" src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/android_studio.png" />
</div>

# Spendly Space 🚀

**Spendly** is a premium, modern Android application built using Jetpack Compose and Room Database. It helps households, friends, and individuals collaborate on expense tracking, category budgets, saving goals, and upcoming bill reminders.

---

## 🌟 Features & App Flow

### 1. Multi-Member Wallet Vaults
Track spending across multiple profiles. Filter the dashboard by a specific user (e.g. Partner, Friend, Family) or view the aggregated "All Vaults" balance.
* **Add Member**: Assign a custom name, role (Primary, Partner, Family, Friend), and signature color.
* **Interactive Filtering**: Tap a member avatar to instantaneously filter transactions.

### 2. Intelligent Ledger & Ledger Search
* **Income/Expense Log**: Record transactions with specific categories, dates, and descriptions.
* **Instant Filter & Search**: Search descriptions and categories in real-time.

### 3. Cosmic Exchange (Peer-to-Peer Transfers)
Need to transfer money between members? The **Exchange Screen** allows you to seamlessly transfer funds from one member's vault to another, creating balanced ledger items automatically.

### 4. Smart Financial Controls
* **Active Budgets**: Set category-specific monthly limits (e.g., Food, Rent, Entertainment) and track percentage usage.
* **Goals Vault**: Set target amounts for future projects, make incremental deposits, and visually track progress.
* **Upcoming Bills**: View bill dates, toggle payment status, and simulate push alert notifications.

---

## 🛠 How It Works (Code Architecture)

### Core Data Models
Spendly leverages **Room Database** for local persistence. Here is how the schema entities are defined:

* **Member**:
  ```kotlin
  data class Member(
      val id: Int = 0,
      val name: String,
      val colorHex: String,
      val role: String // e.g., "Primary", "Partner", "Family"
  )
  ```
* **Transaction**:
  ```kotlin
  data class Transaction(
      val id: Int = 0,
      val amount: Double, // Positive for Income, Negative for Expense
      val category: String, // e.g., "Food", "Rent", "Salary"
      val description: String,
      val date: Long,
      val memberId: Int,
      val isShared: Boolean = false
  )
  ```

---

## 🚀 Run and Deploy Your Android Studio App

This repository contains everything you need to run your app locally.

### Prerequisites
* [Android Studio](https://developer.android.com/studio)

### Installation & Run Steps
1. **Clone & Open**: Open Android Studio, select **Open**, and choose the project directory.
2. **Build Configuration**: Allow Android Studio to download dependencies and sync Gradle.
3. **Environment Setup**: Create a file named `.env` in the root directory and add your Gemini API Key:
   ```env
   GEMINI_API_KEY=your_api_key_here
   ```
   *(See `.env.example` for details)*
4. **Debug Signing**: Remove this line from the app's `build.gradle.kts` file:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```
5. **Launch**: Run the app on an Emulator or physical device using the play button in Android Studio.

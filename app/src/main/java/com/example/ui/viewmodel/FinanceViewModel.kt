package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Member
import com.example.data.model.Transaction
import com.example.data.model.Budget
import com.example.data.model.SavingGoal
import com.example.data.model.BillReminder
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    // Native reactive StateFlows from database
    val members: StateFlow<List<Member>>
    val transactions: StateFlow<List<Transaction>>
    val budgets: StateFlow<List<Budget>>
    val savingGoals: StateFlow<List<SavingGoal>>
    val billReminders: StateFlow<List<BillReminder>>

    // UI state states
    private val _selectedMemberId = MutableStateFlow<Int?>(null) // null means "All Members"
    val selectedMemberId = _selectedMemberId.asStateFlow()

    private val _notification = MutableStateFlow<String?>(null)
    val notification = _notification.asStateFlow()

    private val _notificationsList = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(message = "System initialized and ready for Akshit. Double-check your active budgets below.")
        )
    )
    val notificationsList = _notificationsList.asStateFlow()

    fun clearAllNotifications() {
        _notificationsList.value = emptyList()
    }

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Currency configuration flow
    private val _currency = MutableStateFlow("INR")
    val currency = _currency.asStateFlow()

    val currencySymbol = flowOf("₹").stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "₹"
    )

    val currencyRate = flowOf(1.0).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 1.0
    )

    fun setCurrency(newCurrency: String) {
        // Only Rupees supported
    }

    fun getCurrencySymbol(): String {
        return "₹"
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao())

        members = repository.allMembers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        budgets = repository.allBudgets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savingGoals = repository.allSavingGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        billReminders = repository.allBillReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default starter data if database is empty
        viewModelScope.launch {
            val currentMembers = repository.allMembers.first()
            if (currentMembers.isEmpty()) {
                seedDatabase()
            }
        }
    }

    private suspend fun seedDatabase() {
        // Create 3 initial members (including Primary)
        val m1 = Member(name = "You", colorHex = "#10B981", role = "Primary")
        val m2 = Member(name = "Sarah", colorHex = "#6366F1", role = "Partner")
        val m3 = Member(name = "Akshit", colorHex = "#EC4899", role = "Family")

        repository.insertMember(m1)
        repository.insertMember(m2)
        repository.insertMember(m3)

        // Add pre-configured category budgets
        repository.insertBudget(Budget(category = "Food", monthlyLimit = 15000.0, monthYear = "2026-05"))
        repository.insertBudget(Budget(category = "Rent", monthlyLimit = 40000.0, monthYear = "2026-05"))
        repository.insertBudget(Budget(category = "Shopping", monthlyLimit = 12000.0, monthYear = "2026-05"))
        repository.insertBudget(Budget(category = "Entertainment", monthlyLimit = 6000.0, monthYear = "2026-05"))
        repository.insertBudget(Budget(category = "Utilities", monthlyLimit = 8000.0, monthYear = "2026-05"))

        // Add Saving Goals
        repository.insertSavingGoal(SavingGoal(title = "Emergency Fund", targetAmount = 250000.0, currentAmount = 100000.0, targetDate = "Dec 2026"))
        repository.insertSavingGoal(SavingGoal(title = "Japan Summer Trip", targetAmount = 500000.0, currentAmount = 150000.0, targetDate = "Aug 2026"))

        // Add Bill Reminders
        val now = System.currentTimeMillis()
        repository.insertBillReminder(BillReminder(title = "Netflix Premium", amount = 649.00, dueDate = now + 2 * 24 * 60 * 60 * 1000, isPaid = false, category = "Entertainment"))
        repository.insertBillReminder(BillReminder(title = "Residential Rent", amount = 35000.00, dueDate = now + 5 * 24 * 60 * 60 * 1000, isPaid = false, category = "Rent"))
        repository.insertBillReminder(BillReminder(title = "Fiber Optic Internet", amount = 999.00, dueDate = now + 10 * 24 * 60 * 60 * 1000, isPaid = true, category = "Utilities"))

        // Add initial structured ledger transactions (seeding 1-based member IDs)
        // Note: First inserted members will get autogenerated IDs 1, 2, 3 in order
        repository.insertTransaction(Transaction(amount = 120000.0, category = "Salary", description = "Tech Corp Monthly Base", date = now - 5 * 24 * 60 * 60 * 1000, memberId = 1, isShared = false))
        repository.insertTransaction(Transaction(amount = -35000.0, category = "Rent", description = "Monthly Apart. Base", date = now - 4 * 24 * 60 * 60 * 1000, memberId = 1, isShared = true))
        repository.insertTransaction(Transaction(amount = -3500.50, category = "Food", description = "Whole Foods Organic Groceries", date = now - 3 * 24 * 60 * 60 * 1000, memberId = 2, isShared = true))
        repository.insertTransaction(Transaction(amount = -850.00, category = "Entertainment", description = "Cinema Standard Tickets", date = now - 2 * 24 * 60 * 60 * 1000, memberId = 3, isShared = true))
        repository.insertTransaction(Transaction(amount = -2500.00, category = "Shopping", description = "Winter Warm Jacket", date = now - 1 * 24 * 60 * 60 * 1000, memberId = 2, isShared = false))
        repository.insertTransaction(Transaction(amount = -1800.00, category = "Utilities", description = "Clean water & Power bill", date = now - 8 * 60 * 60 * 1000, memberId = 1, isShared = true))
        repository.insertTransaction(Transaction(amount = 15000.0, category = "Salary", description = "Mobile Consulting Freelance", date = now - 2 * 60 * 60 * 1000, memberId = 1, isShared = false))
    }

    // --- Member Interface ---
    fun selectMember(memberId: Int?) {
        _selectedMemberId.value = memberId
    }

    fun addMember(name: String, colorHex: String, role: String) {
        viewModelScope.launch {
            val count = members.value.size
            if (count >= 10) {
                showInAppNotification("⚠️ Profile Limit Reached: Safe max capacity is 10 shared accounts.")
                return@launch
            }
            repository.insertMember(Member(name = name, colorHex = colorHex, role = role))
            showInAppNotification("🎉 Sourced profile for '$name' as $role!")
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            if (member.role == "Primary") {
                showInAppNotification("👮 Cannot dismantle the primary administrative profile.")
                return@launch
            }
            repository.deleteMember(member)
            showInAppNotification("🗑️ Account and profile database entry for ${member.name} removed.")
        }
    }

    // --- Transaction Interface ---
    fun addTransaction(amount: Double, category: String, description: String, memberId: Int, isShared: Boolean) {
        viewModelScope.launch {
            val tx = Transaction(
                amount = amount,
                category = category,
                description = description,
                date = System.currentTimeMillis(),
                memberId = memberId,
                isShared = isShared
            )
            repository.insertTransaction(tx)

            // Trigger proactive notification checking if they crossed a budget
            checkBudgetsForCrossing(category, amount)

            showInAppNotification(
                if (amount > 0) "💰 Streamed revenue input: +${getCurrencySymbol()}${String.format("%.2f", amount)}"
                else "📉 Expense authorized: -${getCurrencySymbol()}${String.format("%.2f", -amount)}"
            )
        }
    }

    private fun checkBudgetsForCrossing(category: String, amount: Double) {
        if (amount >= 0) return
        val expenseVal = -amount

        viewModelScope.launch {
            // Find active budget limits for that category
            val budgetList = budgets.value
            val targetBudget = budgetList.find { it.category.equals(category, ignoreCase = true) } ?: return@launch

            // Compute current aggregate spent
            val allTx = transactions.value
            val currentSpent = allTx
                .filter { it.category.equals(category, ignoreCase = true) && it.amount < 0 }
                .sumOf { -it.amount }

            val projectedSpent = currentSpent + expenseVal
            if (projectedSpent > targetBudget.monthlyLimit) {
                showInAppNotification("⚠️ Budget Alert: Limit of ${getCurrencySymbol()}${String.format("%.2f", targetBudget.monthlyLimit)} exceeded for '$category' category!")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            showInAppNotification("🗑️ Entry deleted.")
        }
    }

    // --- Budget Interface ---
    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(Budget(category = category, monthlyLimit = limit, monthYear = "2026-05"))
            showInAppNotification("📊 Created monthly budget limit of ${getCurrencySymbol()}${limit} for $category.")
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            showInAppNotification("🗑️ Budget limit dismissed.")
        }
    }

    // --- Saving Goals Interface ---
    fun addSavingGoal(title: String, targetAmount: Double, currentAmount: Double, targetDate: String) {
        viewModelScope.launch {
            val goal = SavingGoal(title = title, targetAmount = targetAmount, currentAmount = currentAmount, targetDate = targetDate)
            repository.insertSavingGoal(goal)
            showInAppNotification("🎯 Launched saving goal: '$title' to secure ${getCurrencySymbol()}${targetAmount}!")
        }
    }

    fun updateSavingProgress(goal: SavingGoal, addAmount: Double) {
        viewModelScope.launch {
            val dbGoal = repository.allSavingGoals.first().find { it.id == goal.id } ?: goal
            val updatedAmount = (dbGoal.currentAmount + addAmount).coerceIn(0.0, dbGoal.targetAmount)
            val updatedGoal = dbGoal.copy(currentAmount = updatedAmount)
            repository.insertSavingGoal(updatedGoal)

            if (updatedAmount >= dbGoal.targetAmount) {
                showInAppNotification("🏆 Target Reached! Saved ${getCurrencySymbol()}${String.format("%.2f", dbGoal.targetAmount)} for '${dbGoal.title}'!")
            } else {
                showInAppNotification("💰 Seeded ${getCurrencySymbol()}${addAmount} to '${dbGoal.title}'. Saved: ${getCurrencySymbol()}${String.format("%.2f", updatedAmount)}/${getCurrencySymbol()}${String.format("%.2f", dbGoal.targetAmount)}")
            }
        }
    }

    fun deleteSavingGoal(goal: SavingGoal) {
        viewModelScope.launch {
            repository.deleteSavingGoal(goal)
            showInAppNotification("🗑️ Savings Goal deleted.")
        }
    }

    // --- Bill Reminders Interface ---
    fun addBillReminder(title: String, amount: Double, dueDate: Long, category: String) {
        viewModelScope.launch {
            val bill = BillReminder(title = title, amount = amount, dueDate = dueDate, isPaid = false, category = category)
            repository.insertBillReminder(bill)
            showInAppNotification("📅 Calendar bill scheduled: '$title' (${getCurrencySymbol()}${amount})")
        }
    }

    fun toggleBillPaid(bill: BillReminder) {
        viewModelScope.launch {
            val dbBill = repository.allBillReminders.first().find { it.id == bill.id } ?: bill
            val updated = dbBill.copy(isPaid = !dbBill.isPaid)
            repository.insertBillReminder(updated)
            showInAppNotification(if (updated.isPaid) "✅ Marked bill '${bill.title}' as paid!" else "📅 Bill marked as outstanding.")
        }
    }

    fun simulateBillNotification(bill: BillReminder) {
        showInAppNotification("🔔 REMINDER: '${bill.title}' bill of ${getCurrencySymbol()}${String.format("%.2f", bill.amount)} is due shortly!")
    }

    fun deleteBillReminder(bill: BillReminder) {
        viewModelScope.launch {
            repository.deleteBillReminder(bill)
            showInAppNotification("🗑️ Bill reminder deleted.")
        }
    }

    // --- Common Notification alert Banner logic ---
    fun showInAppNotification(message: String) {
        viewModelScope.launch {
            _notification.value = message
            val newItem = AppNotification(message = message)
            _notificationsList.value = listOf(newItem) + _notificationsList.value
        }
    }

    fun dismissNotification() {
        _notification.value = null
    }
}

package com.example.data.repository

import com.example.data.db.FinanceDao
import com.example.data.model.Member
import com.example.data.model.Transaction
import com.example.data.model.Budget
import com.example.data.model.SavingGoal
import com.example.data.model.BillReminder
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {

    val allMembers: Flow<List<Member>> = financeDao.getAllMembers()
    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = financeDao.getAllBudgets()
    val allSavingGoals: Flow<List<SavingGoal>> = financeDao.getAllSavingGoals()
    val allBillReminders: Flow<List<BillReminder>> = financeDao.getAllBillReminders()

    // --- Member Operations ---
    suspend fun insertMember(member: Member) {
        financeDao.insertMember(member)
    }

    suspend fun deleteMember(member: Member) {
        financeDao.deleteMember(member)
    }

    // --- Transaction Operations ---
    suspend fun insertTransaction(transaction: Transaction) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        financeDao.deleteTransactionById(id)
    }

    // --- Budget Operations ---
    suspend fun insertBudget(budget: Budget) {
        financeDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        financeDao.deleteBudget(budget)
    }

    // --- Saving Goal Operations ---
    suspend fun insertSavingGoal(goal: SavingGoal) {
        financeDao.insertSavingGoal(goal)
    }

    suspend fun deleteSavingGoal(goal: SavingGoal) {
        financeDao.deleteSavingGoal(goal)
    }

    // --- Bill Reminder Operations ---
    suspend fun insertBillReminder(bill: BillReminder) {
        financeDao.insertBillReminder(bill)
    }

    suspend fun deleteBillReminder(bill: BillReminder) {
        financeDao.deleteBillReminder(bill)
    }
}

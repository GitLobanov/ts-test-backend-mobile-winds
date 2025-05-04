package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(record: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            BudgetEntity.new {
                year = record.year
                month = record.month
                amount = record.amount
                type = record.type
                record.author?.let {
                    author = AuthorEntity.findById(it.id) ?: throw IllegalArgumentException("Author not found")
                }
            }.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .leftJoin(AuthorTable)
                .select {
                    BudgetTable.year eq param.year and
                            (param.authorName?.let {
                                AuthorTable.fullName.lowerCase() like "%${it.lowercase()}%"
                            } ?: Op.TRUE)
                }

            val total = query.count()
            val data = BudgetEntity.wrapRows(
                query
                    .orderBy(BudgetTable.month to SortOrder.ASC)
                    .orderBy(BudgetTable.amount to SortOrder.DESC)
                    .limit(param.limit, param.offset)
            ).map { it.toResponse() }

            val sumByType = BudgetTable
                .slice(BudgetTable.type, BudgetTable.amount.sum())
                .select { BudgetTable.year eq param.year }
                .groupBy(BudgetTable.type)
                .associate { it[BudgetTable.type].name to (it[BudgetTable.amount.sum()] ?: 0) }

            BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}
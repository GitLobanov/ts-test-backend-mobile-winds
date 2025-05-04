package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun createAuthor(request: AuthorRecord): AuthorResponse = withContext(Dispatchers.IO) {
        require(request.fullName.isNotBlank()) {
            "Full name must not be blank"
        }
        transaction {
            AuthorEntity.new {
                fullName = request.fullName
            }.let {
                AuthorResponse(
                    id = it.id.value,
                    fullName = it.fullName,
                    createdAt = it.createdAt.toString()
                )
            }
        }
    }
}

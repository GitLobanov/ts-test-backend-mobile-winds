package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.author() {
    route("/author") {
        post<Unit, AuthorResponse, AuthorRecord>(info("Создать автора")) { _, body ->
            respond(AuthorService.createAuthor(body))
        }
    }
}

data class AuthorRecord(
    val fullName: String
)

data class AuthorResponse(
    val id: Int,
    val fullName: String,
    val createdAt: String
)
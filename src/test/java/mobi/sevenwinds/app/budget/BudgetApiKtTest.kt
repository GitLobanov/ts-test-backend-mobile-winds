package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import io.restassured.http.ContentType
import mobi.sevenwinds.app.author.AuthorRecord
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.apache.http.HttpStatus
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
    }

    @Test
    fun testBudgetPagination() {
        addBudgetRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(5, response.total)
                Assert.assertEquals(3, response.items.size)
                Assert.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addBudgetRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход))

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assert.assertEquals(30, response.items[0].amount)
                Assert.assertEquals(5, response.items[1].amount)
                Assert.assertEquals(400, response.items[2].amount)
                Assert.assertEquals(100, response.items[3].amount)
                Assert.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testStatsAuthorOutput() {
        val savedAuthorA = addAuthorRecord(AuthorRecord("Леонтий Половинко"))
        val savedAuthorB = addAuthorRecord(AuthorRecord("Алексей Кузнецов"))
        val savedAuthorC = addAuthorRecord(AuthorRecord("Александр Макаров"))

        addBudgetRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход, savedAuthorB))
        addBudgetRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход, savedAuthorC))

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .then()
            .statusCode(200)
            .extract()
            .toResponse<BudgetYearStatsResponse>()
            .let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                response.items.forEach { record ->
                    Assert.assertNotNull("Запись должна иметь автора", record.author)
                    Assert.assertNotNull("Должна быть дата создания автора", record.author?.createdAt)
                }

                Assert.assertEquals(30, response.items[0].amount)
                Assert.assertEquals("Алексей Кузнецов", response.items[0].author?.fullName)

                Assert.assertEquals(5, response.items[1].amount)
                Assert.assertEquals("Леонтий Половинко", response.items[1].author?.fullName)

                Assert.assertEquals(400, response.items[2].amount)
                Assert.assertEquals("Александр Макаров", response.items[2].author?.fullName)

                Assert.assertEquals(100, response.items[3].amount)
                Assert.assertEquals("Леонтий Половинко", response.items[3].author?.fullName)

                Assert.assertEquals(50, response.items[4].amount)
                Assert.assertEquals("Леонтий Половинко", response.items[4].author?.fullName)
            }
    }

    @Test
    fun testStatsBudgetByFilterFIO() {
        val savedAuthorA = addAuthorRecord(AuthorRecord("Леонтий Половинко"))
        val savedAuthorB = addAuthorRecord(AuthorRecord("Алексей Кузнецов"))
        val savedAuthorC = addAuthorRecord(AuthorRecord("Александр Макаров"))

        addBudgetRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход, savedAuthorB))
        addBudgetRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход, savedAuthorC))

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0&authorName=Леонтий Половинко")
            .then()
            .statusCode(200)
            .extract()
            .toResponse<BudgetYearStatsResponse>()
            .let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(3, response.items.size)
                Assert.assertTrue(
                    response.items.all {
                        it.author?.fullName == "Леонтий Половинко"
                    }
                )
            }
    }

    @Test
    fun testStatsBudgetBySubstringAuthorNameWithoutRegister() {
        val savedAuthorA = addAuthorRecord(AuthorRecord("Леонтий Половинко"))
        val savedAuthorB = addAuthorRecord(AuthorRecord("Алексей Кузнецов"))
        val savedAuthorC = addAuthorRecord(AuthorRecord("Александр Макаров"))

        addBudgetRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход, savedAuthorA))
        addBudgetRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход, savedAuthorB))
        addBudgetRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход, savedAuthorC))

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0&authorName=алекс")
            .then()
            .statusCode(200)
            .extract()
            .toResponse<BudgetYearStatsResponse>()
            .let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(2, response.items.size)
                Assert.assertTrue(
                    response.items.all {
                        it.author?.fullName?.contains("Алекс", ignoreCase = true) == true
                    }
                )
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecord(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecord(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }

    private fun addBudgetRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecord>().let { response ->
                Assert.assertEquals(record, response)
            }
    }

    private fun addAuthorRecord(record: AuthorRecord): AuthorResponse {
        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(record)
            .post("/author")
            .then()
            .extract()
            .response()

        val authorResponse = response.`as`(AuthorResponse::class.java)

        Assert.assertEquals(record.fullName, authorResponse.fullName)
        Assert.assertNotNull(authorResponse.id)
        Assert.assertNotNull(authorResponse.createdAt)

        return authorResponse
    }
}
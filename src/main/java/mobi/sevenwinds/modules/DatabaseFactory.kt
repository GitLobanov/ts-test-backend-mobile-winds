package mobi.sevenwinds.modules

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    lateinit var appConfig: ApplicationConfig

    private val dbDriver: String by lazy { appConfig.property("db.jdbcDriver").getString() }
    private val dbUrl: String by lazy { appConfig.property("db.jdbcUrl").getString() }
    private val dbUser: String by lazy { appConfig.property("db.dbUser").getString() }
    private val dbPassword: String by lazy { appConfig.property("db.dbPassword").getString() }

    fun init(config: ApplicationConfig) {
        appConfig = config

        Database.connect(hikari())

        val flyway = Flyway.configure().dataSource(dbUrl, dbUser, dbPassword)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .outOfOrder(true)
            .load()

        try {
            val shouldClean = appConfig.propertyOrNull("flyway.clean")?.getString()?.toBoolean() ?: false

            if (shouldClean) {
                val cleanDisabled = Flyway.configure()
                    .dataSource(dbUrl, dbUser, dbPassword)
                    .load()
                    .configuration
                    .isCleanDisabled

                if (cleanDisabled) {
                    println("Warning: Database cleaning was requested but is globally disabled!")
                } else {
                    flyway.clean()
                    println("Database cleaned successfully")
                }
            }

            flyway.migrate()
            println("Database migrated successfully")

        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize database", e)
        }
    }

    fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = dbDriver
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password = dbPassword
        config.maximumPoolSize = appConfig.property("db.maxPoolSize").getString().toInt()
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }
}
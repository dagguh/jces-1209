import java.io.File
import java.net.URI
import java.util.Properties

class CohortProperties(
    val jira: URI,
    val userName: String,
    val userPassword: String,
    val cohort: String
) {
    companion object {
        fun load(secrets: File): CohortProperties {
            val properties = Properties()
            secrets.bufferedReader().use { properties.load(it) }
            return CohortProperties(
                jira = URI(properties.getProperty("jira.uri")!!),
                userName = properties.getProperty("user.name")!!,
                userPassword = properties.getProperty("user.password")!!,
                cohort = properties.getProperty("cohort")!!
            )
        }
    }
}

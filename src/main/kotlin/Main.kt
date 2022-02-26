import com.google.gson.Gson
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Date
import java.util.Locale

val gson = Gson()

fun main(args: Array<String>) {
    println(TalentCollector().getAllMembers())
}
import com.github.kittinunf.fuel.httpGet
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.ZonedDateTime

class ScheduleCollector {

    private val hololiveScheduleUrl = "https://schedule.hololive.tv/lives/hololive"

    fun getAllSchedule() {
        parse(search() ?: return)
    }

    private fun search(): String? {
        return try {
            val (_, response, result) = hololiveScheduleUrl.httpGet().responseString()
            if(response.statusCode == 200) result.get() else null
        } catch (e: Throwable) {
            null
        }
    }

    private fun parse(resultHtml: String) {
        val htmlDocument = Jsoup.parse(resultHtml)
        val scheduleRootElement = htmlDocument.getElementById("all") ?: return

        val scheduleDataList = mutableListOf<ScheduleData>()
        var currentDate = ""

        for(containerElement in scheduleRootElement.children()) {
            for (colElement in containerElement.getElementsByClass("row")[0].children()) {
                val dateElement = colElement.getElementsByClass("holodule navbar-text")
                if (dateElement.isNotEmpty()) {
                    currentDate = dateElement.text()
                    continue
                }

                val rowElement = colElement.getElementsByClass("row")
                if(rowElement.isNotEmpty()) {
                    for(scheduleElement in rowElement[0].children()) {
                        val linkElement = scheduleElement.child(0)
                        val nameElement = scheduleElement.getElementsByClass("name")[0]
                        val timeElement = scheduleElement.getElementsByClass("datetime")[0]

                        val link = linkElement.attr("href")
                        val name = nameElement.ownText()
                        val time = "$currentDate ${timeElement.ownText()}"


                    }
                }
            }
        }
    }

    data class ScheduleData(
        val member: TalentCollector.MemberData,
        val time: LocalDateTime,
        val url: String
    )
}

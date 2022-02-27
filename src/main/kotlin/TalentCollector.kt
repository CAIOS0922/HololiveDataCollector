import com.github.kittinunf.fuel.httpGet
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.jsoup.Jsoup

class TalentCollector {

    private val hololiveTalentUrl = "https://hololive.hololivepro.com/talents"

    fun getMembers(genArray: Array<GenerationName>): JsonObject {
        val jsonObject = JsonObject()

        for(gen in genArray) {
            jsonObject.add(gen.toString(), getGenMembers(gen) ?: continue)
        }

        return jsonObject
    }

    fun getAllMembers(): JsonObject {
        return getMembers(GenerationName.values())
    }

    private fun getGenMembers(gen: GenerationName): JsonArray? {
        return parse(search(gen) ?: return null, gen)
    }

    private fun search(gen: GenerationName): String? {
        return try {
            print("getting $gen member's html... ")

            val url = "$hololiveTalentUrl?gp=${gen.id}"
            val (_, response, result) = url.httpGet().responseString()
            if(response.statusCode == 200) {
                println("[OK]")
                result.get()
            } else {
                println("[FAILED]")
                println("CODE: ${response.statusCode}, MES: ${response.responseMessage}\n")
                null
            }
        } catch (e: Throwable) {
            null
        }
    }

    private fun parse(resultHtml: String, gen: GenerationName): JsonArray {
        val htmlDocument = Jsoup.parse(resultHtml)
        val talentRootElement = htmlDocument.getElementsByClass("talent_list clearfix")[0]
        val jsonArray = JsonArray()

        for (talentElement in talentRootElement.children()) {
            try {
                val link = talentElement.getElementsByTag("a")[0].attr("href")
                val icon = talentElement.getElementsByTag("img")[0].attr("src")
                val jaName = talentElement.getElementsByTag("h3")[0].ownText()
                val enName = talentElement.getElementsByTag("h3")[0].select("span")[0].text()

                println("getting the details of \"$jaName\"...")

                val personalCollector = PersonalCollector(jaName, enName, icon, link, gen)
                jsonArray.add(personalCollector.get() ?: continue)
            } catch (_: Throwable) {

            }
        }

        println("completed.\n")

        return jsonArray
    }

    enum class GenerationName(val id: String) {
        Gen0("gen-0"),
        Gen1("1stgen"),
        Gen2("gen-2"),
        Gamers("gamers"),
        Gen3("gen-3"),
        Gen4("gen-4"),
        Gen5("gen-5"),
        HoloX("holox"),
        INNK_MUSIC("innk-music"),
        INDONESIA("indonesia"),
        ENGLISH("english"),
        MYTH("myth"),
        HOPE("project-hope"),
        COUNCIL("council"),
        OG("og")
    }

    data class MemberData(
        val name: String,
        val enName: String,
        val subName: String,
        val catch: String,
        val summary: String,
        val iconLink: String,
        val detailLink: String,
        val generation: String,
        val figureList: List<String>,
        val snsList: List<SnsData>
    )

    data class SnsData(
        val name: String,
        val link: String
    )
}
import com.github.kittinunf.fuel.httpGet
import kotlinx.serialization.json.*
import org.jsoup.Jsoup

class TalentCollector {

    private val hololiveTalentUrl = "https://hololive.hololivepro.com/talents"

    fun getMembers(genArray: Array<Generation>): JsonObject {
        return buildJsonObject {
            for(gen in genArray) {
                this.put(gen.toString(), getGenMembers(gen) ?: continue)
            }
        }
    }

    fun getAllMembers(): JsonObject {
        return getMembers(Generation.values())
    }

    private fun getGenMembers(gen: Generation): JsonArray? {
        return parse(search(gen) ?: return null, gen)
    }

    private fun search(gen: Generation): String? {
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

    private fun parse(resultHtml: String, gen: Generation): JsonArray {
        val htmlDocument = Jsoup.parse(resultHtml)
        val talentRootElement = htmlDocument.getElementsByClass("talent_list clearfix")[0]
        val jsonArray = buildJsonArray {
            for (talentElement in talentRootElement.children()) {
                try {
                    val link = talentElement.getElementsByTag("a")[0].attr("href")
                    val icon = talentElement.getElementsByTag("img")[0].attr("src")
                    val jaName = talentElement.getElementsByTag("h3")[0].ownText()
                    val enName = talentElement.getElementsByTag("h3")[0].select("span")[0].text()

                    println("getting the details of \"$jaName\"...")

                    PersonalCollector(jaName, enName, icon, link, gen).get()?.let { this.add(it) }
                } catch (_: Throwable) {

                }
            }
        }

        println("completed.\n")

        return jsonArray
    }

    enum class Generation(val id: String) {
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

    @kotlinx.serialization.Serializable
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

    @kotlinx.serialization.Serializable
    data class SnsData(
        val name: String,
        val link: String
    )
}
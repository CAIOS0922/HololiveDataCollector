import com.github.kittinunf.fuel.httpGet
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.jsoup.Jsoup

class TalentCollector {

    private val hololiveTalentUrl = "https://hololive.hololivepro.com/talents"
    private val generationTags = listOf("gen-0", "1stgen", "gen-2", "gamers", "gen-3", "gen-4", "gen-5", "holox")

    fun getAllMembers(): JsonObject {
        val jsonObject = JsonObject()

        for(genTag in generationTags) {
            val gen = convertTagToName(genTag)
            val members = getGenMembers(gen) ?: continue

            jsonObject.add(gen.toString(), members)
        }

        return jsonObject
    }

    private fun getGenMembers(gen: GenerationName): JsonArray? {
        return parse(search(gen) ?: return null, gen)
    }

    private fun search(gen: GenerationName): String? {
        return try {
            print("getting $gen member's html... ")

            val url = "$hololiveTalentUrl?gp=${convertNameToTag(gen)}"
            val (_, response, result) = url.httpGet().responseString()
            if(response.statusCode == 200) {
                println("[OK]")
                result.get()
            } else {
                println("[FAILED]")
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

    private fun convertNameToTag(gen: GenerationName): String {
        return when (gen) {
            GenerationName.Gen0   -> generationTags[0]
            GenerationName.Gen1   -> generationTags[1]
            GenerationName.Gen2   -> generationTags[2]
            GenerationName.Gamers -> generationTags[3]
            GenerationName.Gen3   -> generationTags[4]
            GenerationName.Gen4   -> generationTags[5]
            GenerationName.Gen5   -> generationTags[6]
            GenerationName.HoloX  -> generationTags[7]
        }
    }

    private fun convertTagToName(tag: String): GenerationName {
        return when (tag) {
            generationTags[0] -> GenerationName.Gen0
            generationTags[1] -> GenerationName.Gen1
            generationTags[2] -> GenerationName.Gen2
            generationTags[3] -> GenerationName.Gamers
            generationTags[4] -> GenerationName.Gen3
            generationTags[5] -> GenerationName.Gen4
            generationTags[6] -> GenerationName.Gen5
            generationTags[7] -> GenerationName.HoloX
            else              -> throw IllegalStateException("Unknown Tag")
        }
    }

    enum class GenerationName {
        Gen0, Gen1, Gen2, Gamers, Gen3, Gen4, Gen5, HoloX
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
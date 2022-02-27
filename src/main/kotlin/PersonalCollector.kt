import com.github.kittinunf.fuel.httpGet
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jsoup.Jsoup

class PersonalCollector(
    private val jaName: String,
    private val enName: String,
    private val icon: String,
    private val link: String,
    private val gen: TalentCollector.Generation
) {
    fun get(): JsonElement? {
        return parse(search() ?: return null)
    }

    private fun search(): String? {
        return try {
            val (_, response, result) = link.httpGet().responseString()
            if(response.statusCode == 200) result.get() else null
        } catch (e: Throwable) {
            null
        }
    }

    private fun parse(resultHtml: String): JsonElement {
        val htmlDocument = Jsoup.parse(resultHtml)
        val talentRootElement = htmlDocument.getElementsByClass("bg_box")[0]
        val figureRootElement = htmlDocument.getElementById("talent_figure")!!

        val subName = getSubName(jaName)
        val catchText = talentRootElement.getElementsByClass("catch").text()
        val summaryText = talentRootElement.getElementsByClass("txt").text().replace("\n", "")
        val figureElement = figureRootElement.getElementsByTag("figure")

        val snsList = mutableListOf<TalentCollector.SnsData>()
        for(snsElement in talentRootElement.getElementsByClass("t_sns clearfix")[0].children()) {
            val aTag = snsElement.getElementsByTag("a")[0]
            val snsName = aTag.text()
            val link = cutoutParameter(aTag.attr("href"))

            snsList.add(TalentCollector.SnsData(snsName, link))
        }

        val figureLinkList = mutableListOf<String>()
        for (imgElement in figureElement[0].children()) {
            figureLinkList.add(imgElement.attr("src"))
        }

        return Json.encodeToJsonElement(TalentCollector.MemberData.serializer(), TalentCollector.MemberData(
            jaName, enName, subName, catchText, summaryText, icon, link, gen.toString(), figureLinkList, snsList
        ))
    }

    private fun getSubName(name: String) = when(name) {
        "アキ・ローゼンタール"  -> "アキロゼ"
        "ラプラス・ダークネス"  -> "ラプラス"
        "【卒業生】桐生ココ"   -> "桐生ココ"
        "ハコス・ベールズ"    -> "Baelz"
        "クレイジー・オリー"   -> "Ollie"
        "小鳥遊キアラ"      -> "Kiara"
        "アーニャ・メルフィッサ" -> "Anya"
        "がうる・ぐら"      -> "Gura"
        "アイリス"        -> "IRyS"
        "森カリオペ"       -> "Calli"
        "パヴォリア・レイネ"   -> "Reine"
        "ムーナ・ホシノヴァ"   -> "Moona"
        "ワトソン・アメリア"   -> "Amelia"
        else          -> ""
    }

    private fun cutoutParameter(link: String): String {
        val index = link.indexOf("?")
        return if(index != -1) link.substring(0, index) else link
    }
}
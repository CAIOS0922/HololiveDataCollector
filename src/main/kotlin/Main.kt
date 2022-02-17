import com.google.gson.Gson

val gson = Gson()

fun main(args: Array<String>) {
    val collector = TalentCollector()

    println(collector.getAllMembers().toString())

}
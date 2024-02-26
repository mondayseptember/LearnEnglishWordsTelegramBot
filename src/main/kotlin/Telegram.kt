import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val text = "Hello"

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
        val matchResult = updateIdRegex.find(updates)
        val groups = matchResult?.groups
        val updateIdString = groups?.get(1)?.value
        if (updateIdString != null) {
            updateId = updateIdString.toInt() + 1
        }

        val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
        val matchChatIdResult = chatIdRegex.find(updates)
        val chatId = matchChatIdResult?.groups?.get(1)?.value

        val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
        val matchTextResult = messageTextRegex.find(updates)
        val userMessage = matchTextResult?.groups?.get(1)?.value

        if (userMessage == text && text.length in 1..4096) {
            sendMessage(botToken, chatId, text)
        }
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun sendMessage(botToken: String, chatId: String?, text: String): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

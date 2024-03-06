import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val TELEGRAM_URL = "https://api.telegram.org/bot"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val MENU = "menu"

class TelegramBotService(
    private val botToken: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)

        val sendMessage = "$TELEGRAM_URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long): String {
        val sendMenu = "$TELEGRAM_URL$botToken/sendMessage"
        val sendMenuBody = """ 
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова", 
                                "callback_data": "$LEARN_WORDS_CLICKED" 
                            },
                            {
                                "text": "Статистика", 
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(chatId: Long, question: Questions): String {
        val sendQuestion = "$TELEGRAM_URL$botToken/sendMessage"

        val variantsString = question.variants
            .mapIndexed { index, word ->
                """
                    {
                        "text": "${word.translate}", 
                        "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${index}" 
                    }
                 """.trimIndent()
            }
            .joinToString(separator = ",")

        val sendQuestionBody = """ 
            {
                "chat_id": $chatId,
                "text": "${question.correctAnswer.original}",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            $variantsString,
                            {
                                "text": "Меню", 
                                "callback_data": "$MENU" 
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendQuestion))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}

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
    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: String, text: String): String {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        println(encoded)

        val sendMessage = "$TELEGRAM_URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: String): String {
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

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(chatId: String, question: Questions): String {
        val sendQuestion = "$TELEGRAM_URL$botToken/sendMessage"
        val variants = question.variants.mapIndexed { index, word: Word ->
            "${index + 1}"
        }

        val sendQuestionBody = """ 
            {
                "chat_id": $chatId,
                "text": "${question.correctAnswer.original}",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "${question.variants[0].translate}", 
                                "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${variants[0]}"
                            },
                            {
                                "text": "${question.variants[1].translate}", 
                                "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${variants[1]}" 
                            }, 
                            {
                                "text": "${question.variants[2].translate}", 
                                "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${variants[2]}" 
                            },
                            {
                                "text": "${question.variants[3].translate}", 
                                "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${variants[3]}" 
                            },
                            {
                                "text": "Меню", 
                                "callback_data": "$MENU" 
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendQuestion))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}

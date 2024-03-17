import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String? = null,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBot = TelegramBotService(botToken)
    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val response = telegramBot.getUpdates(lastUpdateId)
        println(response)

        if (response.result.isEmpty()) continue
        val sortedUpdated = response.result.sortedBy { it.updateId }
        sortedUpdated.forEach { handleUpdate(it, trainers, telegramBot) }
        lastUpdateId = sortedUpdated.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
    telegramBot: TelegramBotService
) {
    val userMessage = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    val statistics = trainer.getStatistics()

    if (userMessage == "/start") {
        telegramBot.sendMenu(chatId)
    }

    if (data == STATISTICS_CLICKED) {
        telegramBot.sendMessage(chatId, "Выучено ${statistics.learnedWords} из ${statistics.wordCount} слов | " +
                    "${statistics.percentageOfLearnedWords}%"
        )
    }

    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        telegramBot.sendMessage(chatId, "Прогресс сброшен")
    }

    if (data == MENU) {
        telegramBot.sendMenu(chatId)
    }

    if (data == LEARN_WORDS_CLICKED) {
        checkNextQuestionAndSend(trainer, telegramBot, chatId)
    }

    if (data != null) {
        if (data.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
            val index = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()

            if (trainer.checkAnswer(index)) {
                telegramBot.sendMessage(chatId, "Правильно!"
                )
            } else {
                telegramBot.sendMessage(chatId, "Неправильно: ${trainer.question?.correctAnswer?.original} - " +
                            "${trainer.question?.correctAnswer?.translate}"
                )
            }
            checkNextQuestionAndSend(trainer, telegramBot, chatId)
        }
    }
}

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, telegramBot: TelegramBotService, chatId: Long) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBot.sendMessage(
            chatId, "Вы выучили все слова в базе"
        )
    } else {
        telegramBot.sendQuestion(
            chatId, question
        )
    }
}

fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBot = TelegramBotService(botToken)
    var updateId = 0
    val trainer = LearnWordsTrainer()

    val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
    val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBot.getUpdates(updateId)
        println(updates)

        val matchResult = updateIdRegex.find(updates)
        val groups = matchResult?.groups
        val updateIdString = groups?.get(1)?.value
        if (updateIdString != null) {
            updateId = updateIdString.toInt() + 1
        }

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toLong() ?: continue
        val userMessage = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        val statistics = trainer.getStatistics()

        if (userMessage == "/start") {
            telegramBot.sendMenu(chatId)
        }

        if (data == STATISTICS_CLICKED) {
            telegramBot.sendMessage(
                chatId, "Выучено ${statistics.learnedWords} из ${statistics.wordCount} слов | " +
                        "${statistics.percentageOfLearnedWords}%"
            )
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
                    telegramBot.sendMessage(
                        chatId, "Правильно!"
                    )
                } else {
                    telegramBot.sendMessage(
                        chatId, "Неправильно: ${trainer.question?.correctAnswer?.original} - " +
                                "${trainer.question?.correctAnswer?.translate}"
                    )
                }
                checkNextQuestionAndSend(trainer, telegramBot, chatId)
            }
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

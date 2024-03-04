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

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value
        val userMessage = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        val statistics = trainer.getStatistics()

        if (userMessage == "/start" && chatId != null) {
            telegramBot.sendMenu(chatId)
        }

        if (data == STATISTICS_CLICKED && chatId != null) {
            telegramBot.sendMessage(
                chatId, "Выучено ${statistics.learnedWords} из ${statistics.wordCount} слов | " +
                        "${statistics.percentageOfLearnedWords}%"
            )
        }

        if (data == MENU && chatId != null) {
            telegramBot.sendMenu(chatId)
        }

        if (data == LEARN_WORDS_CLICKED && chatId != null) {
            checkNextQuestionAndSend(trainer, botToken, chatId)
        }

        if (data != null && chatId != null) {
            if (data.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
                val index = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()

                if (trainer.checkAnswer(index.minus(1))) {
                    telegramBot.sendMessage(
                        chatId, "Правильно!"
                    )
                } else {
                    telegramBot.sendMessage(
                        chatId, "Неправильно: ${trainer.question?.correctAnswer?.original} - " +
                                "${trainer.question?.correctAnswer?.translate}"
                    )
                }
                checkNextQuestionAndSend(trainer, botToken, chatId)
            }
        }
    }
}

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, botToken: String, chatId: String) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        TelegramBotService(botToken).sendMessage(
            chatId, "Вы выучили все слова в базе"
        )
    } else {
        TelegramBotService(botToken).sendQuestion(
            chatId, question
        )
    }
}

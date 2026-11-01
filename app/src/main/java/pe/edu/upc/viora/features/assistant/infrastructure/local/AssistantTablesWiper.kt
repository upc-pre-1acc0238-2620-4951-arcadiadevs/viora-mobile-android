package pe.edu.upc.viora.features.assistant.infrastructure.local

import pe.edu.upc.viora.core.database.AccountDataWiper
import javax.inject.Inject

/** The answers and the summary of the day of the assistant: emptied when the account leaves the phone. */
class AssistantTablesWiper @Inject constructor(
    private val answerDao: AssistantAnswerDao,
    private val summaryDao: DailySummaryDao,
) : AccountDataWiper {
    override suspend fun wipe() {
        answerDao.wipeAll()
        summaryDao.wipeAll()
    }
}

package ua.syt0r.kanji.core

import kotlinx.coroutines.coroutineScope
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.user_data.database.CachedUserDataState
import ua.syt0r.kanji.core.user_data.database.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData

data class ResolvedVocabCard(
    val dictionaryId: Long,
    val kanjiReading: String?,
    val kanaReading: String,
    val furigana: FuriganaString?,
    val meaning: String,
    val pos: List<String>
)

fun ResolvedVocabCard.toInfoScreenData() =
    InfoScreenData.Vocab(dictionaryId, kanjiReading, kanaReading)

class VocabCardResolver(
    private val vocabPracticeRepository: VocabPracticeRepository,
    private val appDataRepository: AppDataRepository
) {

    private val cardsCache = CachedUserDataState(
        resetFlow = vocabPracticeRepository.changesFlow,
        provider = { vocabPracticeRepository.getAllCards().associateBy { it.cardId } },
        debugTitle = "vocab_cards"
    )

    suspend fun resolveUserCard(cardId: Long): ResolvedVocabCard {
        val cards = cardsCache.await()
        val vocabCard = cards.getValue(cardId)

        return coroutineScope {

            val kanjiReading = vocabCard.data.kanjiReading
            val kanaReading = vocabCard.data.kanaReading

            val word = vocabCard.data.run {
                appDataRepository.getWord(dictionaryId, kanjiReading, kanaReading)
            }

            ResolvedVocabCard(
                dictionaryId = vocabCard.data.dictionaryId,
                kanjiReading = kanjiReading,
                kanaReading = kanaReading,
                furigana = word.reading.furigana,
                meaning = vocabCard.data.meaning ?: word.combinedGlossary(),
                pos = word.partOfSpeechList
            )
        }
    }

}
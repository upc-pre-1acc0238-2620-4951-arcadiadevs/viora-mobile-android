package pe.edu.upc.viora.features.assistant.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The assistant of viora-platform (`/api/v1/assistant`). Every route needs an active plan (403
 * otherwise); the texts come in the language of Accept-Language.
 */
interface AssistantService {

    /** 400 unknown screen; 403 plot of another producer or no plan; 404 unknown plot. */
    @GET("assistant/questions")
    suspend fun getQuestions(
        @Query("screen") screen: String,
        @Query("plotId") plotId: String,
    ): Response<QuestionCatalogDto>

    /** 409 the question has no data; 429 too many new answers, with Retry-After. */
    @POST("assistant/answers")
    suspend fun ask(@Body request: AskQuestionRequestDto): Response<AssistantAnswerDto>

    /** Answers of the last 10 days, newest first. */
    @GET("assistant/answers")
    suspend fun getAnswers(@Query("limit") limit: Int): Response<AnswerHistoryDto>

    /** 404 while no answer was given on that screen about the plot: the normal case, not a failure. */
    @GET("assistant/answers/latest")
    suspend fun getLatest(
        @Query("screen") screen: String,
        @Query("plotId") plotId: String,
    ): Response<AssistantAnswerDto>

    /** 404 when it expired or is not the producer's. */
    @GET("assistant/answers/{answerId}")
    suspend fun getAnswer(@Path("answerId") answerId: String): Response<AssistantAnswerDto>
}

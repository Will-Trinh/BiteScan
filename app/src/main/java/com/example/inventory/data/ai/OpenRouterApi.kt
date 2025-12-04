package com.example.inventory.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Body body: OrChatRequest
    ): OrChatResponse
}

@Serializable
data class OrMessage(
    val role: String,
    val content: String
)

@Serializable
data class OrResponseFormat(
    val type: String
)

@Serializable
data class OrChatRequest(
    val model: String,
    val messages: List<OrMessage>,
    @SerialName("response_format")
    val responseFormat: OrResponseFormat? = null,
    val temperature: Double = 0.7
)

@Serializable
data class OrChoiceMessage(
    val role: String,
    val content: String
)

@Serializable
data class OrChoice(
    val index: Int,
    val message: OrChoiceMessage
)

@Serializable
data class OrChatResponse(
    val choices: List<OrChoice>
)

@Serializable
data class AiRecipeList(
    val recipes: List<AiRecipe>
)

@Serializable
data class AiRecipe(
    val id: Long,
    val name: String,
    val description: String,
    val time_minutes: Int,
    val servings: Int,
    val calories: String? = null,
    val protein: String? = null,
    val carbs: String? = null,
    val fat: String? = null,
    val ingredients: List<String>,
    val instructions: List<String>?
)


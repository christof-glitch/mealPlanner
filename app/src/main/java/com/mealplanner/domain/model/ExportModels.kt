package com.mealplanner.domain.model

/** ENT-U1-012: U1 fallback boundary; extraction seam deferred to U3. */
data class SharePayload(
    val shoppingListId: ShoppingListId,
    val title: String,
    val bodyText: String,
    val includesCheckedItems: Boolean = true,
    val generatedFromEditedList: Boolean = true,
    val instructionsText: String? = null,
)

/** ENT-U1-013 */
data class ShareResult(
    val status: ShareStatus,
    val method: ShareMethod,
    val payload: SharePayload? = null,
    val fallbackReason: ShareBoundaryReason? = null,
    val userActionRequired: String? = null,
)

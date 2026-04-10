package com.exist.app.domain.model

data class DaySummary(
    val dayKey: String,
    val photos: List<MemoryPhoto>
) {
    val hasProof: Boolean get() = photos.any { it.isProofPhoto }
}

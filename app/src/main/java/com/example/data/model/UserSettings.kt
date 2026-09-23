package com.example.data.model

enum class SortOption(val displayName: String, val shortName: String, val description: String) {
    DATE_DESC(
        displayName = "Date (Newest First)",
        shortName = "Newest",
        description = "Recently modified or indexed files at top"
    ),
    DATE_ASC(
        displayName = "Date (Oldest First)",
        shortName = "Oldest",
        description = "Earliest files at top"
    ),
    NAME_ASC(
        displayName = "Name (A to Z)",
        shortName = "A → Z",
        description = "Alphabetical ascending order"
    ),
    NAME_DESC(
        displayName = "Name (Z to A)",
        shortName = "Z → A",
        description = "Alphabetical descending order"
    ),
    SIZE_DESC(
        displayName = "Size (Largest First)",
        shortName = "Largest",
        description = "Heaviest files first to reclaim storage"
    ),
    SIZE_ASC(
        displayName = "Size (Smallest First)",
        shortName = "Smallest",
        description = "Lightest files first"
    ),
    CATEGORY(
        displayName = "Category & Type",
        shortName = "Category",
        description = "Grouped by media category and file extension"
    ),
    DUPLICATES_FIRST(
        displayName = "Duplicates First",
        shortName = "Duplicates",
        description = "Redundant files and reclaimable space prioritized"
    ),
    PENDING_RENAME_FIRST(
        displayName = "To Rename First",
        shortName = "Renames",
        description = "Files with suggested intelligent renames prioritized"
    )
}

enum class AutoSortMode(val displayName: String, val description: String) {
    SMART_SUBTYPES(
        displayName = "Smart Subtypes & Hierarchy",
        description = "e.g. Images/Screenshots, Audio/Artist/Album, Docs/PDF"
    ),
    CATEGORY_ONLY(
        displayName = "Category Folders",
        description = "e.g. Images, Audio, Documents, Video, Archives"
    ),
    DATE_ARCHIVES(
        displayName = "Date Archives",
        description = "e.g. Archive/YYYY/MM/Category"
    )
}

data class UserSettings(
    val autoSortEnabled: Boolean = true,
    val defaultSortOption: SortOption = SortOption.DATE_DESC,
    val autoSortDestinationMode: AutoSortMode = AutoSortMode.SMART_SUBTYPES,
    val autoSortOnScan: Boolean = true,
    val autoSuggestRenamesOnScan: Boolean = true,
    val autoHashSha256OnScan: Boolean = true,
    val enableGeminiOnlineEnrichment: Boolean = true,
    val defaultCasing: String = "TITLE_CASE", // LOWERCASE, UPPERCASE, TITLE_CASE, SNAKE_CASE
    val defaultSpaceReplacement: String = "_", // "_", "-", " "
    val groupDuplicatesByHash: Boolean = true,
    val pinnedApps: List<String> = emptyList(),
    val setupCompleted: Boolean = false
)

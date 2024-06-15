package dev.yokai.core.migration.migrations

import dev.yokai.core.migration.Migration
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

val migrations: ImmutableList<Migration> = persistentListOf(
    SetupAppUpdateMigration(),
    SetupBackupCreateMigration(),
    SetupExtensionUpdateMigration(),
    SetupLibraryUpdateMigration(),

    // For archive purposes
    EvernoteJobUpgradeMigration(),
    InternalChapterCacheUpdateMigration(),
    CoverCacheMigration(),
    ChapterCacheMigration(),
    DownloadedChaptersMigration(),
    WorkManagerMigration(),
    CustomInfoMigration(),
    MyAnimeListMigration(),
    DoHMigration(),
    RotationTypeMigration(),
    ShortcutsMigration(),
    RotationTypeEnumMigration(),
    EnabledLanguageMigration(),
    UpdateIntervalMigration(),
    ReaderUpdateMigration(),
    PrefsMigration(),
    LibraryUpdateResetMigration(),
    TrackerPrivateSettingsMigration(),
    LibrarySortMigration(),

    // Yokai fork
    ThePurgeMigration(),
    ExtensionInstallerEnumMigration(),
    CutoutMigration(),
    RepoJsonMigration(),
)

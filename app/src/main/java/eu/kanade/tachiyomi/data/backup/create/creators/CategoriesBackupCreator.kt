package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.models.BackupCategory
import eu.kanade.tachiyomi.data.database.DatabaseHelper
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class CategoriesBackupCreator(
    private val db: DatabaseHelper = Injekt.get(),
) {
    /**
     * Backup the categories of library
     *
     * @return list of [BackupCategory] to be backed up
     */
    fun backupCategories(): List<BackupCategory> {
        return db.getCategories()
            .executeAsBlocking()
            .map { BackupCategory.copyFrom(it) }
    }
}

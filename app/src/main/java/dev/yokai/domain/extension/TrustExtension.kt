package dev.yokai.domain.extension

import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat
import dev.yokai.domain.source.SourcePreferences
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class TrustExtension(
    private val sourcePreferences: SourcePreferences = Injekt.get(),
) {
    fun isTrusted(pkgInfo: PackageInfo, signatureHash: String): Boolean {
        val key = "${pkgInfo.packageName}:${PackageInfoCompat.getLongVersionCode(pkgInfo)}:$signatureHash"
        return key in sourcePreferences.trustedExtensions().get()
    }

    fun trust(pkgName: String, versionCode: Long, signatureHash: String) {
       sourcePreferences.trustedExtensions().let { exts ->
           val removed = exts.get().filterNot { it.startsWith("$pkgName:") }.toMutableSet()

           removed += "$pkgName:$versionCode:$signatureHash"
           exts.set(removed)
       }
    }

    fun revokeAll() {
        sourcePreferences.trustedExtensions().delete()
    }
}

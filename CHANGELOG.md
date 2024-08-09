<!-- Formatting
## Additions  ?? New features

## Changes  ?? Behaviour changes

## Fixes  ?? Bugfixes

## Translation  ?? translation changes/updates

## Other  ?? Technical stuff, what happened behind the scene
-->
## Additions
- Add missing "Max automatic backups" option on experimental Data and Storage setting menu
- Add information on when was the last time backup automatically created to experimental Data and Storage setting menu
- Add monochrome icon

## Changes
- Add more info to WorkerInfo page
  - Added "next scheduled run"
  - Added attempt count
- `english` tag no longer cause reading mode to switch to LTR (mangkoran)
- `chinese` tag no longer cause reading mode to switch to LTR
- `manhua` tag no longer cause reading mode to switch to LTR
- Local source manga's cover now being invalidated on refresh
- It is now possible to create a backup without any entries using experimental Data and Storage setting menu
- Increased default maximum automatic backup files to 5
- It is now possible to edit a local source entry without adding it to library
- Long Strip and Continuous Vertical background color now respect user setting
- Display Color Profile setting no longer limited to Android 8 or newer
- Increased long strip cache size to 4 for Android 8 or newer
- Use Coil pipeline to handle HEIF images

## Fixes
- Fixed auto backup, auto extension update, and app update checker stop working
  if it crash/failed
- Fixed crashes when trying to reload extension repo due to connection issue
- Fixed tap controls not working properly after zoom
- Fixed (sorta, more like workaround) ANR issues when running background tasks, such as updating extensions

## Translation
- Update Japanese translation (akir45)
- Update Brazilian Portuguese translation (AshbornXS)
- Update Filipino translation (infyProductions)

## Other
- Some code refactors
  - Simplify some messy code
  - Rewrite version checker
  - Rewrite Migrator
  - Split the project into several modules
  - Migrated i18n to use Moko Resources
  - Removed unnecessary dependencies
- Update firebase bom to v33.1.0
- Replace com.google.android.gms:play-services-oss-licenses with com.mikepenz:aboutlibraries
- Update dependency com.google.gms:google-services to v4.4.2
- Add crashlytics integration for Kermit
- Replace ProgressBar with ProgressIndicator from Material3 to improve UI consistency
- More StorIO to SQLDelight migrations
  - Merge lastFetch and lastRead query into library_view VIEW
  - Migrated a few more chapter related queries
  - Migrated most of the manga related queries
- Bump dependency com.github.tachiyomiorg:unifile revision to a9de196cc7
- Update project to Kotlin 2.0 (v2.0.10)
- Update compose bom to v2024.08.00-alpha01
- Refactor archive support to use `libarchive`
- Use version catalog for gradle plugins
- Update dependency org.jsoup:jsoup to v1.7.1
- Bump dependency com.github.tachiyomiorg:image-decoder revision to 41c059e540
- Update dependency io.coil-kt.coil3 to v3.0.0-alpha10
- Update Android Gradle Plugin to v8.5.1
- Update gradle to v8.9
- Start using Voyager for navigation
- Update dependency androidx.work:work-runtime-ktx to v2.9.1
- Update dependency androidx.annotation:annotation to v1.8.2
- Replace dependency br.com.simplepass:loading-button-android with
  com.github.leandroBorgesFerreira:LoadingButtonAndroid
- Replace dependency com.github.florent37:viewtooltip with
  com.github.CarlosEsco:ViewTooltip

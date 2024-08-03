<!-- Formatting
## Additions  ?? New features

## Changes  ?? Behaviour changes

## Fixes  ?? Bugfixes

## Translation  ?? translation changes/updates

## Other  ?? Technical stuff, what happened behind the scene
-->
## Additions
- Add missing "Max automatic backups" option on experimental Data and Storage setting menu

## Changes
- Add more info to WorkerInfo page
  - Added "next scheduled run"
  - Added attempt count
- `english` tag no longer cause reading mode to switch to LTR (mangkoran)
- `chinese` tag no longer cause reading mode to switch to LTR
- `manhua` tag no longer cause reading mode to switch to LTR
- Local source manga's cover now being invalidated on refresh
- You can now create a backup without any entries using experimental Data and Storage setting menu

## Fixes
- Fixed auto backup, auto extension update, and app update checker stop working
  if it crash/failed
- Fixed crashes when trying to reload extension repo due to connection issue

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
- Update project to Kotlin 2.0
- Update compose bom to v2024.07.00-alpha02
- Refactor archive support to use `libarchive`
- Use version catalog for gradle plugins
- Update dependency org.jsoup:jsoup to v1.7.1
- Bump dependency com.github.tachiyomiorg:image-decoder revision to 41c059e540
- Update dependency io.coil-kt.coil3 to v3.0.0-alpha08
- Update Android Gradle Plugin to v8.5.1
- Update gradle to v8.9
- Start using Voyager for navigation

<!-- Formatting
## Additions  ?? New features

## Changes  ?? Behaviour changes

## Fixes  ?? Bugfixes

## Other  ?? Technical stuff, what happened behind the scene
-->
## Changes
- Add more info to WorkerInfo page
  - Added "next scheduled run"
  - Added attempt count

## Fixes
- Fixed auto backup, auto extension update, and app update checker stop working
  if it crash/failed

## Other
- Some code refactors
- Update firebase bom to v33.1.0
- Update dependency co.touchlab:kermit-crashlytics to v3.9.0
- Update dependency com.google.android.gms:play-services-oss-licenses to v17.1.0
- Update dependency com.google.gms:google-services to v4.4.2
- Add crashlytics integration for Kermit
- Replace ProgressBar with ProgressIndicator from Material3 to improve UI consistency
- Rewrite version checker
- Rewrite Migrator

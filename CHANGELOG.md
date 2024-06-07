<!-- Formatting
## Additions

## Changes

## Fixes

## Other
-->
## Changes
- Remove download location redirection from `Settings > Downloads`
- Moved cache related stuff from `Settings > Advanced` to `Settings > Data and storage`

## Fixes
- Fix potential crashes for some custom Android rom
- Allow MultipartBody.Builder for extensions
- Refresh extension repo now actually refresh extension(s) trust status
- Custom manga info now relink properly upon migration
- Fixed extension repo list did not update when a repo is added via deep link

## Other
- Migrate `RawQueries#librayQuery` to use SQLDelight (`GetLibraryManga`), should improve stability

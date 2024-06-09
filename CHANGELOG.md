<!-- Formatting
## Additions

## Changes

## Fixes

## Other
-->
## Additions
- Added option to change long tap browse and recents nav behaviour
  - Added browse long tap behaviour to open global search (AshbornXS)
  - Added recents long tap behaviour to open last read chapter (AshbornXS)

## Changes
- Remove download location redirection from `Settings > Downloads`
- Moved cache related stuff from `Settings > Advanced` to `Settings > Data and storage`
- Improve webview (AshbornXS)
  - Show url as subtitle
  - Add option to clear cookies
  - Allow zoom
- Handle urls on global search (AshbornXS)
- Improve download queue (AshbornXS)
  - Download badge now show download queue count
  - Add option to move series to bottom
- Only show "open repo url" button when repo url is not empty

## Fixes
- Fix potential crashes for some custom Android rom
- Allow MultipartBody.Builder for extensions
- Refresh extension repo now actually refresh extension(s) trust status
- Custom manga info now relink properly upon migration
- Fixed extension repo list did not update when a repo is added via deep link
- Fixed download unread trying to download filtered (by scanlator) chapters
- Fixed extensions not retaining their repo url

## Other
- Migrate `RawQueries#librayQuery` to use SQLDelight (`GetLibraryManga`), should improve stability

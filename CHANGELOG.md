<!-- Formatting
## Additions

## Changes

## Fixes

## Other
-->
Since there's a lot of new people coming over, I'll repeat my statement a few releases ago, if you think this is the "successor" of J2K, please consider migrating back to (or staying on) J2K as this fork will start **diverting back to how I imagine it**, you're free to use my fork but I'm just gonna clarify that this fork is **NOT** a successor of J2K

## Additions
- Ported Tachi's cutout option

## Changes
- Repositioned cutout options in settings
- Splash icon now uses coloured variant of the icon
- Removed deep link for sources, this should be handled by extensions
- Removed braces from nightly (and debug) app name

## Fixes
- Fixed splash icon hardcoded to white
- Fixed preference summary not updating after being changed once
- Fixed legacy appbar is visible on compose when being launched from deeplink
- Fixed some app icon not generated properly
- Fixed splash icon doesn't fit properly on Android 12+

## Other
- Migrate to using Android 12's SplashScreen API
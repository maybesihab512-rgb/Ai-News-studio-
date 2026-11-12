# AI News Studio (v0.1.0 foundation)

Native Android (Kotlin + Jetpack Compose). Open this folder in Android Studio (Koala or newer, JDK 17) and press Run.

- minSdk 26, targetSdk/compileSdk 34, AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06.00
- Only dependencies: core-ktx, activity-compose, Compose UI, Material3, navigation-compose
- AI photos: Gemini REST API (user supplies their own API key in Settings). History is in memory only.
- Without an AI photo, cards show abstract placeholder artwork drawn with Canvas.

Structure: data/ (templates, view model) · ui/components (NewsCard with 10 layouts, shared widgets) · ui/screens · ui/AppNav.kt
Note: gradle-wrapper.jar is not included; Android Studio will fetch Gradle 8.7 from gradle-wrapper.properties.

## APK without Android Studio
Push this folder to a GitHub repository. The Actions tab runs "Build APK" automatically; download the APK from the run's Artifacts.

## v0.3.0
- 20 templates (1-10 unchanged, 11-20 new broadcast/newspaper styles)
- 10 Bengali font styles from 9 open-source families (downloaded in CI from Google Fonts, SIL OFL)

## v0.4.0
- Branding tab: channel name, website and date (auto or custom) with on/off toggles; saved on the device
- Every card shows a broadcast-style footer bar (channel left, date and website right)

## v0.5.0
- Download (PNG, 1080x1350, saved to Pictures/AI News Studio) and share from the preview screen
- In-app editing: headline text, template, font, photo (AI / own gallery photo / remove), footer info

## v0.6.0
- AI photo provider choice in Settings: Pollinations (free, no key) or Gemini (paid plan needed for image models)
- Bengali captions are translated to an English photo description before image generation

## v0.7.0
- Two more free AI photo providers: Cloudflare Workers AI (FLUX.1 schnell, needs a free account + token) and AI Horde (anonymous, slow queue)

## v0.8.0
- Automatic mode (default): tries Cloudflare -> Gemini -> Pollinations -> AI Horde until one returns an image
- Optional credentials can be baked in at build time from GitHub Secrets: CF_ACCOUNT_ID, CF_API_TOKEN, GEMINI_API_KEY
  (anything in an APK can be extracted: do not share the APK publicly if secrets are set)

## v0.9.0
- Templates are blank: no preset category/headline text. Category, speaker ("statement by") and source are optional inputs, hidden when empty
- One-time creator profile (logo, channel, website, Facebook page + link, contact) saved on the device; editable from Settings / branding tab
- Stable debug signing key committed in app/debug.keystore so new builds install over the old one and keep saved data

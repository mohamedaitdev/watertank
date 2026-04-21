# WaterTank

A Jetpack Compose Android app for monitoring industrial water tanks — designed to feel like
a small interactive game on the Home screen, but practical enough for daily use by a plant
operator.

Built for a facility with ~10,000 m³ horizontal cylindrical tanks, pumps, and a chlorine
bottle rack (see the reference images the app's visuals are styled after).

---

## Features

- **Animated tank visual** on Home: horizontal cylinder with rising water + surface waves,
  tap to jump into the flow calculator.
- **Flow calculator**: enter two level readings + interval → get m³/h, L/s, time-to-full,
  and ETA.
- **Smart fill prediction**: the Home screen's "time to full" updates automatically from
  the two most recent saved readings.
- **Alarm**: schedule a one-shot notification that fires when the tank is projected to hit
  a configurable threshold (default 90%).
- **Chlorine bottle tracker**: grid of bottles, tap to cycle ✅ Working → 🟡 Standby → ⚪ Empty
  → ❌ Fault. Long-press to add notes or delete.
- **Facility schematic**: a small animated SVG-like Canvas showing chlorine inlet → tank →
  pumps → outlet, with dashed flow motion.
- **Settings**: tank capacity, max level, alarm threshold, alarm on/off, vibrate on/off,
  clear readings.
- **Offline-first**: everything lives in Room + DataStore, no network.

---

## Architecture

MVVM with a small manual DI container (`WaterTankApplication`). No Hilt so the project is
easy to open and run without extra setup.

```
app/src/main/java/com/watertank/app/
├── MainActivity.kt              # single-activity host, requests POST_NOTIFICATIONS
├── WaterTankApplication.kt      # lazy-initialised Room DB + repos + prefs + notif channels
├── data/
│   ├── local/                   # Room DB, DAOs, TypeConverters, seed data
│   ├── model/                   # Tank, TankReading, ChlorineBottle entities
│   ├── repository/              # TankRepository, ChlorineRepository
│   └── preferences/             # DataStore-backed PreferencesManager
├── domain/
│   └── FlowCalculator.kt        # pure math (Δvolume / Δtime, cylinder volume, duration fmt)
├── notification/
│   ├── NotificationHelper.kt    # channels + AlarmManager scheduling
│   └── TankAlarmReceiver.kt     # broadcast receiver that posts the notification
└── ui/
    ├── theme/                   # industrial dark + water-blue palette, typography, theme
    ├── components/              # AnimatedTank, FlowGauge, PipeNetwork, InlineKpi
    ├── navigation/              # AppNavigation (NavHost)
    └── screens/
        ├── home/                # HomeScreen + HomeViewModel
        ├── calculator/          # CalculatorScreen + CalculatorViewModel
        ├── chlorine/            # ChlorineScreen + ChlorineViewModel
        └── settings/            # SettingsScreen + SettingsViewModel
```

### Key math

Flow rate uses the linear-volume model (ideal for vertical tanks, acceptable for horizontal
cylinders at mid-fill). For an exact horizontal cylinder volume there is also
`FlowCalculator.horizontalCylinderVolume(h, r, L)` ready to wire in later:

```
V(h) = L · [ r² · arccos((r − h)/r) − (r − h)·√(2rh − h²) ]
```

---

## Setup & run

Requires **Android Studio Koala (2024.1.1) or newer**, JDK 17, and an emulator/device
running **Android 8.0+ (API 26)**.

1. Unzip the project.
2. Open Android Studio → *File → Open…* → pick the `WaterTankApp` folder.
3. Let Gradle sync (first sync downloads Compose BOM, Room/KSP, DataStore, Navigation,
   WorkManager — a few minutes on first run).
4. Pick an emulator or connect a device and press ▶️ Run.

On first launch the database is seeded with:
- one tank (`Main Reservoir`, 10,000 m³, 6 m max level),
- four chlorine bottles in assorted statuses.

### Permissions

Declared in the manifest and requested at runtime on Android 13+:
- `POST_NOTIFICATIONS` — for the fill alarm
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — for on-the-dot fill alerts
- `VIBRATE`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`

If `canScheduleExactAlarms()` returns false the app falls back to
`setAndAllowWhileIdle` automatically.

---

## Typical operator flow

1. Open the app → see current fill level as animated water in the tank.
2. Tap the tank → opens the Flow Calculator.
3. Enter current level in meters, level after 15 min, and the interval → **Calculate**.
4. **Save** stores both readings so Home picks up the ETA; **Alarm at 90%** schedules a
   notification for when the tank is projected to hit the threshold.
5. Swap to the Chlorine screen to update bottle statuses during shift handoff.

---

## Extending

- **Multiple tanks**: `TankDao.observeAll()` already returns a list. Add a tank picker at
  the top of Home and change `HomeViewModel` to accept a `tankId`.
- **Exact horizontal cylinder math**: swap `linearVolume(...)` for
  `horizontalCylinderVolume(h, r, L)` in `HomeViewModel`/`CalculatorViewModel` once you
  expose `radius` and `length` in `Settings`.
- **History chart**: `ReadingDao.observeRecent()` already exposes the last 50 readings;
  drop a Compose line chart into Home.
- **Reminders**: `NotificationHelper.scheduleAt(...)` is generic — hook it up to a small
  “Remind me to check in…” button.

---

## License

MIT — use, fork, and ship it.

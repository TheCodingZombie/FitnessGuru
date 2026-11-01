# Fitness Guru (Java / XML, Android SDK 36)
A simple app for tracking your progress and taking note of your goals.

## Build
1. Open this folder in Android Studio (recent version with Android Gradle Plugin 8.10+).
2. Install Android SDK Platform 36 and let Gradle sync.
3. Run on Android 16.0+.

Package: `com.undead.fitnessguru`.

## Feature Plans
- Start a workout, allowing you to add exercises.
  - Exercises have a name and a list of sets.
    - Sets each have a weight, a unit, and a rep count. (Maybe eventually add a timer)
    - You can remove a set from the bottom of the list or add a set to the bottom.
    - You can also add a copy of the bottom set to the bottom.
  - Workouts also have a total time (basically time elapsed since the start time)
- The following screens:
  - Workout history
  - Workout screen (allowing you to edit the current workout. Does not allow editing of previous workouts)
  - Exercise history (allows you to see the history of each named exercise with how much weight you can do for each)
  - Measurements (allows you to note your measurements and then see a history of each body part)
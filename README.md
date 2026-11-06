# Fitness Guru (Java / XML, Android SDK 36)
A simple app for tracking your progress and taking note of your goals.

## Build
1. Open this folder in Android Studio (recent version with Android Gradle Plugin 8.10+).
2. Install Android SDK Platform 36 and let Gradle sync.
3. Run on Android 16.0+.

Package: `com.undead.fitnessguru`.

## Current Features
- Start a workout, allowing you to add exercises.
  - Exercises have a name and a list of sets.
    - Sets each have a weight, a unit, and a rep count. (Maybe eventually add a timer)
    - You can remove a set from the bottom of the list or add a set to the bottom.
  - Workouts also have a total time (basically time elapsed since the start time)
    - This is displayed in the stopwatch at the top left.
- See the history of when you've done workouts, as well as what you did during that workout
  - This screen allows you to search your workouts and start a new one.
- See the history of individual exercises you've done and your trajectory through each one
  - This stores the PB of each individual workout and plots them on a graph so you can see how well you've done over time
- You can also add a copy of the bottom set to the bottom.
- Name workouts and exercises
- Measurements Screen (allows you to note your measurements and then see a history of each body part)
  - Measurements allow for a full body measurement (basically every possible body part)
  - Body weight units as well (restrict it to just if the user is adding a body weight measurement, since
    it wouldn't make much sense if you measured how much your bicep weighed)

## Feature Plans
- Goals
  - Perhaps the measurement screen could also allow you to set a goal for measurements, which is automatically pinned to the top.
  - Perhaps the exercise screen could have an additional section that lets you set a goal for individual exercises as well.
  - Perhaps there could be a popup when your set date for a goal has been reached and lets you know that you should take measurements.
- Create a preset workout, allowing you to have a bunch of exercises ready for you to fill out.
  - Also, a button in addition to the start workout button that  allows you to choose a preset workout.
  - Store preset workouts in the database (and then retrieve them all into a dropdown menu?)
- Preset Exercises as well?
  - Would probably just be a list of exercise names created by the programmer
  - Would make it much easier to find an exercise.
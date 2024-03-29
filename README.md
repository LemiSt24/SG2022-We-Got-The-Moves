# Serious Games - We got the Moves

## Description

In the following we present 'We got the Moves' as tracking App for sport movements and exercises
developed in cooperation with TU Darmstadt as part of
the [Serious Games](https://www.etit.tu-darmstadt.de/serious-games/willkommen_sg/index.en.jsp) (
Project-)Praktikum.

## Authors

- Alexander Hartmann
- Jan-Luca Barthel
- Lennard Michael Strohmeyer
- Simon Westermann

## Features

- Camera tracking of movements during a selected workout (
  via [Google's mediapipe](https://google.github.io/mediapipe/))
- Quality measurement for exercises during workout sessions
- Statistics for user's calories (daily & calendar) and exercises
- Profile settings
- Tutorial for the app (as manual)
- Tutorials for exercises
- Workout planner
- Training video replays
- Training video recording (Future work)

## Demo

You can download the apk for
Android [here](https://github.com/LemiSt24/SG2022-We-Got-The-Moves/releases/tag/v.2.0). This
app can run on phones with Android 9.0 or newer installed and is not running on emulators in Android
Studio due to problems with OpenGL.

## License

[MIT](https://choosealicense.com/licenses/mit/)

# For Developers

## Basics

#### Preferable Android IDE

- [Android Studio IDE](https://developer.android.com/studio/install)

#### Android Developer Guides

- [Basic Android Developer Guide](https://developer.android.com/guide)
- [Detailed Tutorial / Training](https://google-developer-training.github.io/android-developer-fundamentals-course-concepts-v2/unit-1-get-started/lesson-1-build-your-first-app/1-0-c-introduction-to-android/1-0-c-introduction-to-android.html)
- [Guide UI Layout Editor](https://developer.android.com/studio/write/layout-editor.html)

#### Code style / Code formatter

To ensure that Code is readable and is formatted uniformly, the
plugin [google-java-format](https://plugins.jetbrains.com/plugin/8527-google-java-format) can be
installed via Settings -> Plugins. Note that the plugin has to be enabled afterwards. Furthermore
it's advisable to enable 'Reformat Code', 'Organize imports', 'Rearrange Code' in Android Studio as
show [here](https://www.jetbrains.com/idea/guide/tutorials/reformatting-code/reformatting-before-commit/)

## UI libraries

### Basics

'We got the moves' offers the use of two UI binding libraries as **alternative** possibilities.
Please refer to the specific links below.

For most purposes, it's easier to use the **View Binding Library**. In case you want to bind
specific data objects to a view's components (e.g. list items bounded to certain objects), it can be
advantageous to use the **Data Binding Library** instead. Once any **example_layout.xml** file has
been created under **app/src/main/res/layout**, both libraries will generate the target layout
binding class, e.g. named as **ExampleLayoutBinding**
automatically.

- [View Binding Library](http://developer.android.com/topic/libraries/view-binding)
- [Data Binding Library](https://developer.android.com/topic/libraries/data-binding)

### Images

In order to render images of exercises in the Dashboard in fitting size,
the [Bumptech / Glide library](https://github.com/bumptech/glide) is used. It's recommend to place
input images in the drawable folder **res/images/drawable** in order to not mix them with **
icons.xml** files in the common **res/drawable** folder.

### Youtube Videos

Exercises are links with their corresponding Youtube Videos provided
using [PierfrancescoSoffritti / Android-youtube-player library](https://github.com/PierfrancescoSoffritti/android-youtube-player)
.

### Statistics

We use a well known [PhilJay / MPAndroidChart library](https://github.com/PhilJay/MPAndroidChart)
for building and displaying charts in the statistic view. This tool allows all kind of charts, like
bar, graph or pie charts.

## ML libraries

### Mediapipe

One of core features (pose and position tracking) provided by 'We go the Moves' App relies on
the [Google's Mediapipe library](https://google.github.io/mediapipe/).

## Database

#### Android Room DB library

'We go the Moves' uses [Android Room](https://developer.android.com/jetpack/androidx/releases/room)
as abstract Layer over Android SQLite. Local data is saved persistently on device.

#### General procedure:

- Create an **Entity Class** (or **Model Class**)
  and [annotate](https://tonyowen.medium.com/room-entity-annotations-379150e1ca82) it (table name,
  primary key, columns, etc.)
- Create an abstract **DAO Class (Data Object Class)** which maps queries (e.g. insert, update,
  delete, read) to their related methods
  using [annotations](https://developer.android.com/training/data-storage/room/accessing-data).
  For **observable** data
  holding, [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
  or [RxJava](https://medium.com/androiddevelopers/room-rxjava-acb0cd4f3757) 's datatype Publisher,
  Observable or Flowable as well as **non-automatic fetching** data holder classes like Single,
  Maybe or Completable might be useful and can be used interchangeable. An overview is shown here:

####

![here](https://miro.medium.com/max/720/0*jEnmX0FZOBDdJIHK)

####

- In the **AppDataBase Class**, add the **Entity (Table)** as well as the **DAO** below the **TODO**
  comments. **Hint:** It isn't always necessary to increase the migration version number when the DB
  has been changed if you wipe the DB from your device (e.g. by deleting the app or wipe the
  emulator). Be aware that all data will be lost.
- Optionally, you can create a **Repository Class** ( e.g. as
  a [Singleton](https://en.wikipedia.org/wiki/Singleton_pattern)) which bundles data from DB,
  external sources (such as remote DBs) or allow **asynchronous execution**. The Repository can be
  instanced in the **BasicApp Class** below the **TODO** comment.

#### References

- [SQL Tutorial](https://www.w3schools.com/sql/)
- [Save data in a local DB](https://developer.android.com/training/data-storage/room)
- [Room Tutorial / Training](https://guides.codepath.com/android/Room-Guide)
- [LiveData + ViewModel](https://google-developer-training.github.io/android-developer-fundamentals-course-concepts-v2/unit-4-saving-user-data/lesson-10-storing-data-with-room/10-1-c-room-livedata-viewmodel/10-1-c-room-livedata-viewmodel.html)
- [RxJava Datatypes](https://medium.com/androiddevelopers/room-rxjava-acb0cd4f3757)
- [Debug DB with Database Inspector](https://developer.android.com/studio/inspect/database?utm_source=android-studio)

# [AutoTag](https://github.com/whizvox/AutoTag)

This is a small Java application for those who wish to split up
[osu!](https://osu.ppy.sh/) map difficulties according to specific
criteria. This was initially created as a personal tool to split up a
TAG4 map's new combos, but I decided to release it to the public for
those looking to do the same.

## Features

You may split up a map according to 2 criteria:

1. **New Combos**: Every time a new combo starts.
Difficulty name results in `[Difficulty - PLAYER1]`
2. **Object Count**: Whenever a certain number of objects is passed.
Difficulty name results in `[Difficulty - COUNT1]`

The number of resulting difficulties can also be specified.

## Gradle

This software uses [Gradle](https://gradle.org/) as its build tool.
The following listed tasks must be run in conjunction with either the
Gradle executable itself `gradle` or the provided wrapper scripts
(`gradlew` or `gradlew.bat`). Example: `gradlew run`

* `run`: Runs the application
* `cleanAutoTag`: Removes any files created while running
* `clean`: Deletes the build directory and executes `cleanAutoTag`
* `jar`: Creates a ready-to-run Jar file in `./build/libs/`

## Contributing

Feel free to suggest new ideas or submit bug reports via the
[Issues](https://github.com/whizvox/AutoTag/issues) tab. That is the
*only* place where I will take suggestions or bug reports. Not all
requested features will be added nor will all reported bugs be fixed.
Users are also free to apply their own suggestions via pull requests.

## Copying

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <<https://www.gnu.org/licenses/>>.
# FuzeCreek

A grid-based rafting game with explosives, powered by the
[jMonkeyEngine (JME)][jme] game engine.

It contains 4 sub-projects:

 1. FC2D: a (retro) 2-D version of the game
 2. FC3D: the 3-D version of the game
 3. FCCommon: shared sourcecode which implements the mechanics of the game
 4. FCConsole: a (very retro) console-based version of the game,
    implemented using "ASCII graphics"

Assets and Java source code are provided under
[a FreeBSD license][license].


<a name="toc"></a>

## Contents of this document

 + [How to download and run a pre-built release of Fuze Creek](#prebuilt)
 + [How to build and run Fuze Creek from source](#build)
 + [Playing Fuze Creek](#play)
 + [Wish list](#wishlist)
 + [History](#history)
 + [Acknowledgments](#acks)


<a name="prebuilt"></a>

## How to download and run a pre-built release of Fuze Creek

(documentation not yet written)

[Jump to table of contents](#toc)


<a name="build"></a>

## How to build and run Fuze Creek from source

1. Install a [Java Development Kit (JDK)][adoptium],
   if you don't already have one.
2. Point the `JAVA_HOME` environment variable to your JDK installation:
   (The path might be something like "C:\Program Files\Java\jre1.8.0_301"
   or "/usr/lib/jvm/java-8-openjdk-amd64/" or
   "/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home" .)
  + using Bash or Zsh: `export JAVA_HOME="` *path to installation* `"`
  + using Windows Command Prompt: `set JAVA_HOME="` *path to installation* `"`
  + using PowerShell: `$env:JAVA_HOME = '` *path to installation* `'`
3. Download and extract the FuzeCreek source code from GitHub:
  + using [Git]:
    + `git clone https://github.com/stephengold/FuzeCreek.git`
    + `cd FuzeCreek`
  + using a web browser:
    + Browse to https://github.com/stephengold/FuzeCreek/archive/refs/heads/master.zip
    + save the ZIP file
    + extract the contents of the saved ZIP file
    + `cd` to the extracted directory/folder
4. Run the [Gradle] wrapper:
  + using Bash or PowerShell or Zsh: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

You can run the console-based version using the Gradle wrapper:
+ using Bash or PowerShell or Zsh: `./gradlew :FCConsole:run`
+ using Windows Command Prompt: `.\gradlew :FCConsole:run`

You can run the 2-D version using the Gradle wrapper:
+ using Bash or PowerShell or Zsh: `./gradlew :FC2D:run`
+ using Windows Command Prompt: `.\gradlew :FC2D:run`

You can run the 3-D version using the Gradle wrapper:
+ using Bash or PowerShell or Zsh: `./gradlew :FC3D:run`
+ using Windows Command Prompt: `.\gradlew :FC3D:run`

You can restore the project to a pristine state:
+ using Bash or PowerShell or Zsh: `./gradlew clean`
+ using Windows Command Prompt: `.\gradlew clean`

[Jump to table of contents](#toc)


<a name="play"></a>

## Playing Fuze Creek

(documentation not yet written)

[Jump to table of contents](#toc)


<a name="wishlist"></a>

## Wish list

(documentation not yet written)

[Jump to table of contents](#toc)


<a name="history"></a>

## History

(documentation not yet written)

[Jump to table of contents](#toc)


<a name="acks"></a>

## Acknowledgments

(documentation not yet written)

[Jump to table of contents](#toc)


[adoptium]: https://adoptium.net/releases.html "Adoptium Project"
[git]: https://git-scm.com "Git"
[gradle]: https://gradle.org "Gradle Project"
[jme]: https://jmonkeyengine.org "jMonkeyEngine Project"
[license]: https://github.com/stephengold/FuzeCreek/blob/master/LICENSE "FuzeCreek license"
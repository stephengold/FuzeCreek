# FuzeCreek

A grid-based rafting game with explosives, powered by
[the jMonkeyEngine (JME) game engine][jme].

It contains 4 sub-projects:

 1. FC2D: a (retro) 2-D version of the game
 2. FC3D: the 3-D version of the game
 3. FCCommon: shared sourcecode which implements the mechanics of the game
 4. FCConsole: a (very retro) console-based version of the game,
    implemented using "ASCII graphics"

Assets and complete source code (in [Java]) are provided under
[a 3-clause BSD license][license].


<a name="toc"></a>

## Contents of this document

+ [How to download and run a pre-built release of FuzeCreek](#prebuilt)
+ [How to build and run FuzeCreek from source](#build)
+ [Playing FuzeCreek](#play)
+ [Wish list](#wishlist)
+ [History](#history)
+ [Acknowledgments](#acks)


<a name="prebuilt"></a>

## How to download and run a pre-built release of FuzeCreek

(documentation not yet written)

[Jump to the table of contents](#toc)


<a name="build"></a>

## How to build and run FuzeCreek from source

1. Install a [Java Development Kit (JDK)][adoptium],
   if you don't already have one.
2. Point the "JAVA_HOME" environment variable to your JDK installation.
   (The path might be something like "C:\Program Files\Java\jre1.8.0_301"
   or "/usr/lib/jvm/java-8-openjdk-amd64" or
   "/Library/Java/JavaVirtualMachines/liberica-jdk-17-full.jdk/Contents/Home" .)
  + using Bash or Zsh: `export JAVA_HOME="` *path to installation* `"`
  + using [Fish]: `set -g JAVA_HOME "` *path to installation* `"`
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
  + using Bash or Fish or PowerShell or Zsh: `./gradlew build`
  + using Windows Command Prompt: `.\gradlew build`

You can run the console-based version:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew :FCConsole:run`
+ using Windows Command Prompt: `.\gradlew :FCConsole:run`

You can run the 2-D version:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew :FC2D:run`
+ using Windows Command Prompt: `.\gradlew :FC2D:run`

You can run the 3-D version:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew :FC3D:run`
+ using Windows Command Prompt: `.\gradlew :FC3D:run`

You can restore the project to a pristine state:
+ using Bash or Fish or PowerShell or Zsh: `./gradlew clean`
+ using Windows Command Prompt: `.\gradlew clean`

[Jump to the table of contents](#toc)


<a name="play"></a>

## Playing FuzeCreek

The game consists of steering a raft down a river
containing naval mines and exposed sharp rocks.
The raft automatically advances downstream.
It advances in discrete jumps, one row at a time.
The objective is to maximize points while avoiding mines and rocks.

### Scoring system

Points are awarded for:

1. making downstream progress (1 point per row) and
2. removing mines by navigating alongside them
   without making contact (20 points per mine)

If the raft crosses over a mine, the mine will detonate,
ending the game with a deduction of 100 points.

You begin the game with 20 patches.
Each time the raft crosses over a rock,
you automatically expend one patch to repair damage caused by the rock.
If no patches remain, the raft sinks, ending the game.

If the raft runs up against either bank (side) of the river, the game ends.

### Controls

All versions are controlled entirely using the keyboard;
no mouse or trackpad is required.

When run, the game opens a single window.
For the controls to work, this window must be selected to receive input.
Some window managers select windows
based on the position of the mouse pointer.
If none of the controls work, try selecting the game window
by clicking on it or moving the mouse pointer into it.

General controls found in all versions of the game:

+ Esc : end the game immediately
+ A or LeftArrow : steer left
+ D or RightArrow : steer right

Additional controls found in FC2D and FC3D:

+ H or F1 : toggle the help node in the upper-right corner of the window
+ F5 : toggle the render statistics in the lower-left corner of the window
+ Prt Scr : capture a screenshot to the working directory

Additional caveats:

+ Steering is effective only when the raft advances.
  To steer, you must press a key and hold it down.
+ On Linux systems, the Prt Scr key might be needed by the window manager,
  so the Scroll Lock key is used instead.
+ The key descriptions above assume a keyboard
  with the "United States QWERTY" layout.
  On some keyboards, the keys in the A, D, and H positions
  are labelled differently.

[Jump to the table of contents](#toc)


<a name="wishlist"></a>

## Wish list

Some ideas for future development:

+ Sound effects
+ Visualize exploding mines
+ Health bar to visualize the remaining patches
+ Shadows in FC3D

[Jump to the table of contents](#toc)


<a name="history"></a>

## History

(documentation not yet written)

[Jump to the table of contents](#toc)


<a name="acks"></a>

## Acknowledgments

Like most projects, FuzeCreek builds on the work of many who
have gone before.  I therefore acknowledge the following
artists and software developers:

+ the creators of (and contributors to) the following software:
    + the [Blender] 3-D animation suite
    + the [Checkstyle] tool
    + the [Firefox] web browser
    + the [Git] revision-control system and GitK commit viewer
    + the [GitKraken] client
    + the [Gradle] build tool
    + the [IntelliJ IDEA][idea] and [NetBeans] integrated development environments
    + the [Java] compiler, standard doclet, and runtime environment
    + [jMonkeyEngine][jme] and the jME3 Software Development Kit
    + the [Linux Mint][mint] operating system
    + LWJGL, the Lightweight Java Game Library
    + the [Markdown] document-conversion tool
    + the [Meld] visual merge tool
    + Microsoft Windows

I am grateful to [GitHub] for providing free hosting for this project
and many other open-source projects.

I'm also grateful to my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know, so I can
correct the situation: sgold@sonic.net

[Jump to the table of contents](#toc)


[adoptium]: https://adoptium.net/releases.html "Adoptium Project"
[blender]: https://docs.blender.org "Blender Project"
[checkstyle]: https://checkstyle.org "Checkstyle"
[firefox]: https://www.mozilla.org/en-US/firefox "Firefox"
[fish]: https://fishshell.com/ "Fish command-line shell"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gitkraken]: https://www.gitkraken.com "GitKraken client"
[gradle]: https://gradle.org "Gradle Project"
[idea]: https://www.jetbrains.com/idea/ "IntelliJ IDEA"
[java]: https://en.wikipedia.org/wiki/Java_(programming_language) "Java programming language"
[jme]: https://jmonkeyengine.org "jMonkeyEngine Project"
[license]: https://github.com/stephengold/FuzeCreek/blob/master/LICENSE "FuzeCreek license"
[markdown]: https://daringfireball.net/projects/markdown "Markdown Project"
[meld]: https://meldmerge.org "Meld merge tool"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
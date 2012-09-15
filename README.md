SSim Environment Demo
=====================

SSim-ED is part of a major rewrite of *SSim* - my little side project for quite some time - influenced by famous games like Silent Hunter III - that aims at creating a realtime "Submarine Simulator" featuring 3D graphics, physics, AI and more.

SSim-ED reimplements the 3D graphics part including dynamic day and night skies, moving clouds, precipitation, sun light and shadows, animated ocean, coarse terrain and a simple weather model (influencing them all) on the basis of [jMonkeyEngine 3](http://jmonkeyengine.org/) - a really great Java game engine!

## Preparations for compiling

Apache Ant and Ivy  are required for compiling.

Installing Ant heavily depends on your OS. Installing Ivy boils down to simply placing a file into Ant's plugin directory.

Download ivy-*version*.jar to your ANT_HOME/lib directory (on Unix it's commonly /usr/share/ant/lib or $HOME/.ant/lib for the current user):

    mkdir -p ~/.ant/lib; cd ~/.ant/lib; wget http://mycrobase.de/wtf/ivy-2.3.0-rc1.jar

## Compile

Change to this project's root directory and run

	ant

in order to produce the compiled Java classes in *bin* and a zip package containing ready-to-launch jar with all dependencies in *dist*.

## License

* Code: tbd
* Assets: tbd

test
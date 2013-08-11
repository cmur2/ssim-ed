SSim Environment Demo
=====================

[![Build Status](https://travis-ci.org/cmur2/ssim-ed.png)](https://travis-ci.org/cmur2/ssim-ed)

SSim-ED is part of a major rewrite of *SSim* - my little side project for quite some time - influenced by famous games like Silent Hunter III - that aims at creating a realtime "Submarine Simulator" featuring 3D graphics, physics, AI and more.

SSim-ED reimplements the 3D graphics part including dynamic day and night skies, moving clouds, precipitation, sun light and shadows, animated ocean, coarse terrain and a simple weather model (influencing them all) on the basis of [jMonkeyEngine 3](http://jmonkeyengine.org/) - a really great Java game engine!

## Preparations for compiling

Apache Ant and Apache Ivy are required for compiling.

Installing Ant heavily depends on your OS. Get a [binary download](https://ant.apache.org/bindownload.cgi) or install via your OSes package manager. Installing Ivy locally for this project can then be done via:

	ant install-ivy

## Compile

Change to this project's root directory and run

	ant

in order to produce the compiled Java classes in *bin/* and a ZIP package containing ready-to-launch *.jar* with all dependencies in *dist/*.

## License

* Code: [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) (see LICENSE)
* Assets: [Creative Commons BY-NC-SA](http://creativecommons.org/licenses/by-nc-sa/3.0/de/)

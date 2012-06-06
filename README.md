SSim Environment Demo
=====================

jMonkeyEngine
-------------

Retrieve compatible jME nightly build from
[http://jmonkeyengine.com/nightly/jME3_2011-11-19.zip](http://jmonkeyengine.com/nightly/jME3_2011-11-19.zip).

Create a user library in Eclipse named "jME3_2011-11-19" with an attached jar
pointing to *jMonkeyEngine3.jar* from the unzipped nightly archive.

To use the provided ant build.xml create a "build.properties" file in the project root folder
with similar contents:

    # This should be the path to jMonkeyEngine3.jar
    sed.jme3-jar.file = /path/to/jME3_2011-11-19/jMonkeyEngine3.jar
    sed.jme3-deps.dir = /path/to/jME3_2011-11-19/lib

That path above should be pointing to the previously downloaded *jMonkeyEngine3.jar*.
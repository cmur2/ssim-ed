SSim Environment Demo
=====================

Getting Ivy and dependencies
----------------------------

Retrieve *ivy-xyz.jar* to your ANT_HOME/lib directory (on Unix it's commonly
/usr/share/ant/lib):

    cd /usr/share/ant/lib; wget http://mycrobase.de/wtf/ivy-2.3.0-rc1.jar

Now run:

    ant resolve

in this projects root directory to fetch all dependencies not included in the
source code repository via Ivy.

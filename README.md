# Maven Incremental Module Builder

[![Build Status](https://cloud.drone.io/api/badges/khmarbaise/incremental-module-builder/status.svg)](https://cloud.drone.io/khmarbaise/incremental-module-builder)
[![Maven Central](https://img.shields.io/maven-central/v/com.soebes.maven.extensions/incremental-module-builder.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Ccom.soebes.maven.extensions)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/khmarbaise/incremental-module-builder.svg?label=License)](http://www.apache.org/licenses/)

If you are working with larger builds sometimes it is worth to think
to build only parts of the whole reactor. So it would be nice
to only build those modules which have been changed. This
can be controled by using the `--projects` option on command line.

The problem here is that you need to identify those modules which
have been changed manually which can become cumbersome.

This extension will handle this automatically.

Status
------

 * Currently in PoC state.
 * Code needs improvements.
 * Need to write more tests / integration tests.

Prerequisites
-------------

 * Maven 3.3.1+
 * Java 7 (as Maven 3.3.1+).

How to Configure?
-----------------


If you like to use this extension you have to
define the following in your `.mvn/extensions.xml` file:

``` xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
  <extension>
    <groupId>com.soebes.maven.extensions</groupId>
    <artifactId>incremental-module-builder</artifactId>
    <version>0.3.0</version>
  </extension>
</extensions>
```

How to use it?
--------------

The first step is to do a `mvn install` from the root of your whole reactor.

Afterwards you can simply call Maven via `mvn -b incremental package` which will
print out things like this:

The following will show how it looks like if you have not changed anything:

```
~/ws-git/supose (master)$ mvn -b incremental package
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]
[INFO] Subversion Repository Search Engine
[INFO] SupoSE :: Test
[INFO] SupoSE :: Filter
[INFO] SupoSE :: Filter : Filter Model
[INFO] SupoSE :: Filter : Filter
[INFO] SupoSE :: Core
[INFO] SupoSE :: CLI
[INFO] SupoSE :: WAR
[INFO] SupoSE :: Integration Test
[INFO] SupoSE :: Users Guide
[INFO] SupoSE :: Assembly
[INFO]  ------------------------------------
[INFO]  Maven Incremental Module Builder
[INFO]  ------------------------------------
[INFO] Executing: /bin/sh -c cd '/Users/kama/ws-git/supose' && 'git' 'rev-parse' '--show-toplevel'
[INFO] Working directory: /Users/kama/ws-git/supose
[INFO] Executing: /bin/sh -c cd '/Users/kama/ws-git/supose' && 'git' 'status' '--porcelain' '.'
[INFO] Working directory: /Users/kama/ws-git/supose
[INFO]  Nothing has been changed.
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] Subversion Repository Search Engine ................ SKIPPED
[INFO] SupoSE :: Test ..................................... SKIPPED
[INFO] SupoSE :: Filter ................................... SKIPPED
[INFO] SupoSE :: Filter : Filter Model .................... SKIPPED
[INFO] SupoSE :: Filter : Filter .......................... SKIPPED
[INFO] SupoSE :: Core ..................................... SKIPPED
[INFO] SupoSE :: CLI ...................................... SKIPPED
[INFO] SupoSE :: WAR ...................................... SKIPPED
[INFO] SupoSE :: Integration Test ......................... SKIPPED
[INFO] SupoSE :: Users Guide .............................. SKIPPED
[INFO] SupoSE :: Assembly ................................. SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 0.277 s
[INFO] Finished at: 2016-08-06T13:29:47+02:00
[INFO] Final Memory: 13M/245M
[INFO] ------------------------------------------------------------------------
```

Now you have changed some files in your modules so a `git status` 
(Using Subversion will work as well) will look like this:

```
~/ws-git/supose (master *)$ git st
On branch master
Your branch is ahead of 'origin/master' by 2 commits.
  (use "git push" to publish your local commits)
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   supose-assembly/pom.xml

no changes added to commit (use "git add" and/or "git commit -a")
```

Which means in consequence you only need to build the single
module `supose-assembly`. You can call Maven manually like this:

```
mvn -pl supose-assembly -amd package
```

Using this extension you don't need to think about which modules have been
changed and change every time you call Maven the command line.
This extension will automatically identify the changed modules and will run the build
accordingly. You can simply do it like this:

```
~/ws-git/supose (master *)$ mvn -b incremental package
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO]
[INFO] Subversion Repository Search Engine
[INFO] SupoSE :: Test
[INFO] SupoSE :: Filter
[INFO] SupoSE :: Filter : Filter Model
[INFO] SupoSE :: Filter : Filter
[INFO] SupoSE :: Core
[INFO] SupoSE :: CLI
[INFO] SupoSE :: WAR
[INFO] SupoSE :: Integration Test
[INFO] SupoSE :: Users Guide
[INFO] SupoSE :: Assembly
[INFO]  ------------------------------------
[INFO]  Maven Incremental Module Builder
[INFO]  ------------------------------------
[INFO] Executing: /bin/sh -c cd '/Users/kama/ws-git/supose' && 'git' 'rev-parse' '--show-toplevel'
[INFO] Working directory: /Users/kama/ws-git/supose
[INFO] Executing: /bin/sh -c cd '/Users/kama/ws-git/supose' && 'git' 'status' '--porcelain' '.'
[INFO] Working directory: /Users/kama/ws-git/supose
[INFO]  scmFile: supose-assembly/pom.xml modified
[INFO] Changed Project: com.soebes.supose:supose-assembly:pom:0.7.1-SNAPSHOT
[INFO] Calculated Reactor Order:
[INFO]  SupoSE :: Assembly
[INFO] Project: com.soebes.supose:supose-assembly:pom:0.7.1-SNAPSHOT
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building SupoSE :: Assembly 0.7.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-enforcer-plugin:1.4.1:enforce (enforce-maven) @ supose-assembly ---
[INFO]
[INFO] --- buildnumber-maven-plugin:1.4:create (default) @ supose-assembly ---
[INFO] Executing: /bin/sh -c cd '/Users/kama/ws-git/supose/supose-assembly' && 'git' 'rev-parse' '--verify' 'HEAD'
[INFO] Working directory: /Users/kama/ws-git/supose/supose-assembly
[INFO] Storing buildNumber: 2cf6fbf37dbd3a0922ebd607a5b0504e76a0fd76 at timestamp: 1470483260527
[INFO] Storing buildScmBranch: master
[INFO]
[INFO] --- maven-site-plugin:3.5.1:attach-descriptor (attach-descriptor) @ supose-assembly ---
[INFO]
[INFO] --- appassembler-maven-plugin:1.10:assemble (default) @ supose-assembly ---
[INFO]
[INFO] --- maven-assembly-plugin:2.6:single (default) @ supose-assembly ---
[INFO] Reading assembly descriptor: src/main/assembly/bin.xml
[INFO] Reading assembly descriptor: src/main/assembly/bin-unix.xml
[INFO] Reading assembly descriptor: src/main/assembly/src.xml
[INFO] Building zip: /Users/kama/ws-git/supose/supose-assembly/target/supose-assembly-0.7.1-SNAPSHOT-bin.zip
[INFO] Building tar: /Users/kama/ws-git/supose/supose-assembly/target/supose-assembly-0.7.1-SNAPSHOT-bin-unix.tar.gz
[INFO] Building tar: /Users/kama/ws-git/supose/supose-assembly/target/supose-assembly-0.7.1-SNAPSHOT-src.tar.gz
[INFO] Building zip: /Users/kama/ws-git/supose/supose-assembly/target/supose-assembly-0.7.1-SNAPSHOT-src.zip
[INFO] Starting building
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.159 s
[INFO] Finished at: 2016-08-06T13:34:23+02:00
[INFO] Final Memory: 29M/450M
[INFO] ------------------------------------------------------------------------
```

If you like to make your Maven call more convenient you can configure this into
the `.mvn/maven.config` like this ([see Release Notes][release-notes]):

```
-b incremental
```
So now you can call Maven simply via `mvn package`. If you want to run the usual build
simply this can be achieved by using the following command line:

```
mvn -b multithreaded package
```
The `multithreaded` defines the default builder. This means you have turned off
the incremental module builder.

ToDo
----

 * Better error handling
 * Better checks for prerequisites.
 * Checking if artifacts have been installed in local cache before?
 * Identify edge cases where it does not work?

 
[release-notes]: http://maven.apache.org/docs/3.3.1/release-notes.html

# Java based encrypting File Syncing Tool

[![Latest Release](https://img.shields.io/github/release/mgoellnitz/JFileSync3.svg)](https://github.com/mgoellnitz/JFileSync3/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/mgoellnitz/JFileSync3/gradle.yml)](https://github.com/mgoellnitz/JFileSync3/actions/workflows/gradle.yml)
[![Build Status](https://img.shields.io/gitlab/pipeline/mgoellnitz/JFileSync3.svg)](https://gitlab.com/mgoellnitz/JFileSync3/pipelines)
[![Download](https://img.shields.io/badge/Download-Snapshot-blue)](https://gitlab.com/mgoellnitz/JFileSync3/-/jobs/artifacts/master/download?job=JFileSync3)

File syncing with optional compression and encryption for local and WebDAV
folders. For local folders, encryption can optionally be accomplished with
EncFS and thus be compatible with encfs4win, EDS Lite, Encdroid and so on.

[Support this project](https://ko-fi.com/backendzeit):
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/L4L5T3APO)

JFileSync3 comes with a GUI, command line tooling, and stored profiles.

Find more on the scenarios where this tool might help you in the rather small
[project wiki](https://github.com/mgoellnitz/JFileSync3/wiki). We have a
little bit of discussion in the [issues area at GitHub](https://github.com/mgoellnitz/JFileSync3/issues),
so feel free to add your comments there.

This software was developed because I personally needed an easy to use syncing
tool to have everyday backups of highly confidential material - business and
private. - without any need of multiple versions in file history.

None of the services I came across could suit these needs.

The second reason why I find it helpful is, that I very much rely on online-syncing
tools for everyday backups and felt forced to use more than one of these
services.

Using too many of them slows down your local machine too much, so I identified
a set of backup areas which didn't need online syncing but just regular cloud
based backups. Some of the syncing services available provide WebDAV backends,
so I can decide to use a service online or manual (scheduled).

JFileSync3 is heavily based on the work of Jens Heidrich and his JFileSync2.2.
After some hacking around in his code, I felt that this one here is a fork,
ripping out his original remote backends and changing the purpose of the tool
quite a lot.

I had to realise, that Jens re-started his work on the original product, so that
this decision now would need some rethinking. Be aware that this code here is
based on the 2007 version JFileSync2.2 and not the more recent 2.3 and 2.4 work,
which heads in part in similar directions as my code (ripping out the server,
using VFS, presenting a new Windows and even Mac integration). I did a manual
back sync with 2.3a a while ago.

JFileSync is a SourceForge project and available via the following URL:

```
http://jfilesync.sourceforge.net/
```

Don't mix the derived work here and Jens' clean software on SourceForge. Only
bother him if it's related to his original version. Other complaints go here
:-) Feel free to add any issues to this project here.

Other contributions - though not directly committed by him - are from
https://github.com/srmo. Thanks so much for the fruitful discussions.


## Online Scenario

You replaced (or want to) your regular backups with online syncing tools and
don't have backup media anymore. This helps to do backups of your work in
remote situations where e.g. don't want to carry confidential backup media with
you.

Possible Solutions:

a) Trust the providers

Select any of them - I have some personal notes on them in the wiki.

b) Trust the providers encryption

Mega might have decent encryption keys, which you can extract locally. But you
cannot really see the encryption working. Other examples, I came across, are
similar.

c) Encrypt locally on every file access and thus only sync encrypted stuff
which you can still see.

EncFS e.g. comes into my mind. Disadvantage here is the de- and encryption on
nearly every access. Locally stored are in fact only the encrypted files.

d) If you trust your local system and need to store frequently accessed files,
you would like to encrypt only short before or on backing up/syncing files.

This is where this software fills the gap. It can sync directories, local or
WebDAV, and it can do this with either of these encrypted. Additionally, it can
sync with EncFS volumes stored in local folders. It is my common practice, that
the local encrypted folders are in turn synced with one of the online syncing
services.

If you use EncFS in this scenario, it is possible to access the encrypted and
synced files from mobile devices with FolderSync and Encroid or EDS. Also, this
EncFS solution is compatible with EncFS MP on MacOS and Windows, and Linux
based EncFS.


## Requirements

- Java 8 Runtime Environment, OpenJDK based JREs supported and recommended

- Only when using algorithms other than AES with such oldish JRE: JCE Extension


## Installation and Application Start

Unzip the distribution file to a directory of your choice.

You can start the application through

- a Windows launcher ('JFileSync3.exe')
- a batch file ('bin/JFileSync3.bat')
- a Unix shell script ('bin/JFileSync3')
- a XDG launcher script ('bin/JFileSync3.desktop')

ATTENTION: Because JFileSync3 is distributed as a ZIP archive, Unix users will
have to give executable rights manually before launching the application, e.g.:
'chmod a+rx bin/JFileSync3'.

Nearly all functions of JFileSync3 can be controlled via the Graphical User
Interface (GUI). However, JFileSync3 provides full access to all features
(apart from plug-ins) via the command line interface. Call `bin/JFileSync3 -help`
to get an overview of all possible command line options.


## Configuration

JFileSync3 works with profiles controlling which folders to use, which files to
include or omit, how to log into WebDAV servers, and which encryption cipher
and passphrase to use.

These profiles need to be set up and can be stored in XML files. The
distribution contains a set of profile file examples illustrating all the
intended scenarios.


## License and Usage Terms

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
St, Fifth Floor, Boston, MA, 02110-1301, USA

JFileSync3 uses libraries or parts of Open Source projects. Some of them had to
be included as source code for technical reasons in their distribution:

* EncFS Java: LGPL (Source)
* Sardine: Apache 2.0
* Apache http components: Apache 2.0
* Apache commons: Apache 2.0
* IO-Tools: MIT
* SLF4J: MIT
* Bouncy Castle providers: BC
* JCIFS: LGPL
* Eclipse icons: the Common Public License - v 1.0.
* SevenZip/LZMA: LZMA SDK is written and placed in the public domain by Igor Pavlov. (Source)

Some code in LZMA SDK is based on public domain code from other developers:
  1) PPMd var.H (2001): Dmitry Shkarin
  2) SHA-256: Wei Dai (Crypto++ library)

You can find a copy of all licenses of JFileSync3 and the used libraries in the
`legal/` directory of this distribution.


## Development Notes

Required packages for JFileSync3 development (not included in the distribution):

* Java 8 SDK >= 1.8.0 (see e.g. `https://projects.eclipse.org/projects/adoptium`)
* [Launch4j](https://sourceforge.net/projects/launch4j/) installation on the path

Used, and as a result recommended, development tools are:

* OpenJDK 11.0
* Netbeans 17

The following structure describes all directories and files included in the
JFileSync3 source repository:

- src: Java source code
- legal: The program licenses.
- profiles: Sample syncing profiles.
- xdg: FreeDesktop.org desktop integration files
- win: start scripts for parts of the system as tools - and a Windows icon file
- test: profiles and data for the encryption test suite

The encryption backend tries its very best, to avoid known plaintext attacks
with filenames like in directory names (`src/main/java`) and with the contents
of the files.

The contents are compressed before they are encrypted (in most cases -
depending on size and special file types) and the software tries three
algorithms to do that - LZMA, BZIP2, Deflate - concurrently.

The filenames are encoded, scrambled and then encrypted.

Metafiles to speed up access of the directory structures don't have constant
names, but calculated ones.

Passwords to derive keys from are not constant along the whole directory
structure.

For oldish Java 8 setups, you will need the unlimited encryption add-on by
Oracle for your JDK, if you want to use anything except the default cipher AES.

Feel free to issue bug reports and ideas here.


## Building and Packaging

Building requires a launch4j installation available from the path.

The software is built with the usual

```bash
./gradlew build
```

and can be started - e.g. for IDE integration - through

```bash
./gradlew run
```

Take the ZIP file from build/distributions.

A small test-suite can be found in profiles/test (which is not packaged in the
distribution zip) and can be called via

```bash
./gradlew encryptionTest
```

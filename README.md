# Java based encrypting File Syncing Tool

[![Latest Release](https://img.shields.io/github/release/mgoellnitz/JFileSync3.svg)](https://github.com/mgoellnitz/JFileSync3/releases/latest)
[![Dependency Status](https://www.versioneye.com/user/projects/54fff1994a1064db0e000071/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54fff1994a1064db0e000071)

[SNAPSHOT](https://gitlab.com/api/v3/projects/mgoellnitz%2FJFileSync3/builds/artifacts/master/download?job=JFileSync3)
builds are also available.

File syncing with optional compression and encryption for local and WebDAV 
folders. For local folders encryption can use EncFS and thus be compatible with 
encfs4win, BoxCryptor Classic, Encdroid and so on. 

JFileSync3 comes with a GUI, command line tooling, and stored profiles.

Find more on the scenarios where this tool might help you in in the growing
[project wiki](https://github.com/mgoellnitz/JFileSync3/wiki). Recently a 
little bit of discussion started in the [issues area at GitHub](https://github.com/mgoellnitz/JFileSync3/issues)
so feel free to add your comments there.

This software was developed because I personally needed an easy to use syncing 
tool to have everyday backups  - without thee need of multiple versions in file
history - of highly confidential material - business and private. 

None of the services I came accross could suite these needs.

The second reason why I find it helpful is, that I very much rely on online-syncing 
tools for everyday backups and felt forced to use more than one of these 
services. 

Using too many of them slows down your local machine too much, so I identified 
a set of backup areas which didn't need online syncing but just regular cloud 
based backup. Some of the syncing services available provide WebDAV backends, 
so I can decide to use a service online or manual (scheduled).

JFileSync3 is heavily based on the work of Jens Heidrich and his JFileSync2.2. 
After some hacking aroung in his code I felt that this one here is a fork, 
ripping out his original remote backends and changing the purpose of the tool 
quite a lot. 

I had to realise the Jens re-started his work on the original product, so that 
this decision now would need some rethinking. Be aware that this code here is 
based on the 2007 version JFileSync2.2 and not the more recent 2.3 and 2.4 work,
which heads in part in similar directions as my code (ripping out the server, 
using VFS, presenting a new Windows and even Mac integration).

JFileSync is a SourceForge project and available via the following URL:

```
http://jfilesync.sourceforge.net/
```

Don't mix the derived work here and Jens' clean software on sourceforge. Only 
bother him if it's related to his original version. Other complaints go here 
:-) Feel free to add any issues to this project here.

Other contributions - though not directly committed by him - are from 
https://github.com/srmo. Thanks so much for the fruitfull discussions.


## Online Scenario

You replaced (or want to) your regular backups with online syncing tools and 
don't have backup media anymore. This helps doing backups of your work in 
remote situations where e.g. don't want to carry confidential backup media with 
you.

Possible Solutions:

a) Trust the providers

Select any of them - I have some personal notes on the in the wiki.

b) Trust the providers encryption

Use Teamdrive, or iDrive Sync. With Teamdrive you can - in some cases - even 
see the encrypted files depending on the backend you use. With iDrive Sync you 
have to provide a custom password as a base for the generated keys (This rips 
out some functions like sharing which gives you a hint, that they really do 
what they are saying).

c) Encrypt locally on every file access and thus only sync encrypted stuff 
which you can still see.

Boxcryptor, EncFS come into my mind. Disadvantage here is the de- and 
encryption on nearly every access. Really locally stored are only the encrypted 
files.

d) If you trust your local system and need to store frequently accessed files, 
you would like to encrypt only short before or on backing up/syncing files.

This is where this software fills the gap. It can sync directories, local or 
WebDAV, and it can do this with either of these encrypted. Additionally it can 
sync with encfs volumes stored in local folders. It is my common practice, that
the local encrypted folders are in turn synced with one of the online syncing 
services.

If you use EncFS in this scenario, it is possible to access the encrypted and 
synced files from mobile devices with Boxcryptor Classic for iOS and Android 
and additionally EncFS tools for android. Also this EncFS solution is 
compatible with encfs4win and of course Linux based encfs.


## Requirements

- Java 8 Runtime Environment, OenJDK supported (see http://java.oracle.com)

- Launch4j installation on the path (see https://sourceforge.net/projects/launch4j/)

- Only when using other algorithms than AES: JCE extension  (see http://java.oracle.com)


## Installation and Application Start

Unzip the distribution file to a directory of your choice.

You can start the application through

- a Windows launcher ('JFileSync3.exe')
- a batch file ('bin/JFileSync3.bat')
- a Unix shell script ('bin/JFileSync3')
- a Unity launcher script

ATTENTION: Because JFileSync is distributed as a Zip archive, Unix users will 
have to give executable rights manually before launching the application, e.g.: 
'chmod a+rx JFileSync.sh'.

Nearly all functions of JFileSync3 can be controlled via the Graphical User 
Interface (GUI). However, JFileSync3 provides full access to all features 
(apart from plug-ins) via the command line interface. Call `bin/JFileSync -help`
to get an overview of all possible command line options.


## Configuration

JFileSync works with profiles controlling which folders to use, which files to 
include or ommit, how to log into WebDAV servers, and which encryption cipher 
and passphrase to use.

These profiles need to be set up and can be stored in XML files. The 
distribution contains a set of profile file examples illustrating all the 
intended scenarios.


## License and Usage Terms

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
St, Fifth Floor, Boston, MA, 02110-1301, USA

JFileSync3 uses libraries or parts of Open Source projects where additionally 
some of the are included as source code:

- EncFS Java: LGPL (Source)

- Sardine: Apache

- Apache http components: Apache

- Apache commons: Apache

- IO-Tools: new BSD

- Bouncy Castle providers: BC

- SevenZip/LZMA: LZMA SDK is written and placed in the public domain by Igor Pavlov. (Source)

Some code in LZMA SDK is based on public domain code from another developers:
  1) PPMd var.H (2001): Dmitry Shkarin
  2) SHA-256: Wei Dai (Crypto++ library)

- log4j: Apache

- JCIFS: LGPL

- SLF4J: MIT

- Eclipse icons: the Common Public License - v 1.0.

You can find a copy all licenses of JFileSync3 and the used libraries in the 
`legal/` directory of this distribution.


## Development Notes

Required packages for JFileSync3 development (not included in the distribution):

- Java 8 SDK >= 1.8.0 (see 'http://java.sun.com')
- Gradle >= 2.1.1 (see 'http://www.gradle.org')

Used and as a result recommended development tools:

- Java 8 SDK 1.8.0
- Netbeans 8.1

The following structure describes all directories and files included in the 
JFileSync3 source repository:

- src: Java source code
- legal: The program licenses.
- profiles: Sample syncing profiles.
- ubuntu: Ubuntu Unity desktop integration files
- win: start scripts for parts of the system as tools - and a Windows icon file
- test: profiles and data for the encryption test suite

The encryption backend tries its very best to avoid known plaintext attacks 
with filenames like in directory names (`src/main/java`) and with the contents 
of the files.

The contents are compressed before they are encrypted (in most cases - 
depending on size and special file types) and the software tries three 
algorithms to do that - LZMS, BZIP2, Deflate - in parallel.

The filenames are encoded, scrambled and then encrypted.

Metafiles to speed up access of the directory structures don't have constant 
names but calculated ones.

Passwords to derive keys from are not constant along the whole directory 
structure.

You will need the unlimited encryption add-on by Oracle for your JDK, if you 
want to use anything except the default cipher AES.

Feel free to issue bug reports and ideas here.


## Building and Packaging


Building requires a launch4j installation available from the path. This was 
necessary to use a pathed one on 64bit systems mixing the original launch4j with
the 64bit ld and windres from mingw-binutils.

The software is built with the usual

```bash
gradle build
```

and can be started - e.g. for IDE integration - through

```bash
gradle run
```

Take the ZIP file from build/distributions.

A small test-suite can be found in profiles/test (which is not packaged in the 
distribtions zip) and can be called via

```bash
gradle encryptionTest
```

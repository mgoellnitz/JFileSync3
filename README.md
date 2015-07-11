Java based File Syncing Tool
============================

[![Dependency Status](https://www.versioneye.com/user/projects/54fff1994a1064db0e000071/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54fff1994a1064db0e000071)

File Syncing with optional Compression and Encryption for Local and WebDAV Folders.

For local folders encryption can use EncFS and thus be compatible with encfs4win, BoxCryptor, Encdroid and so on.

(With GUI, command line, and stored profiles.)

This software was developed because I personally needed a easy to use syncing tool to have everyday backups of highly
confidential material - business and private. And none of the services I came accross could suite these needs.

It is heavily based on the work of Jens Heidrich and the JFileSync2.2

JFileSync is a SourceForge project and available via the following URL:
http://jfilesync.sourceforge.net/

Don't mix the derived work here and his clean software on sourceforge. Only bother him if it's related to his original
version. Other complaints go here :-)

Other contributions - though not directly committed by him - are from https://github.com/srmo

Binary Distribution:

https://bintray.com/artifact/download/mgoellnitz/generic/JFileSync3.zip

Scenario:

You replaced regular backups with online syncing tools like SugarSync, Syncplicity, Copy.com, Yandex Drive, Dropbox,
Wuala, Google Drive, Mega, and so on. After having synced your media files, downloaded software, publicly available
documents you come to the more confidential stuff...

Possible Solutions:

a) Trust the providers: Select any of them - I'll write a review for some of them once I find the time.

b) Trust the providers encryption: use Teamdrive, Wuala, or iDrive Sync. With Teamdrive you can - in some cases - even see the encrypted files depending on the backend you use. With Wuala you once again have to trust that it's really secure especially since they came up with the nice sharing solutions. With iDrive Sync you have to provide a custom password as a base for the generated keys (This rips out some functions like sharing which gives you a hint, that they really do what they are saying).

c) Encrypt locally on every file access and thus only sync encrypted stuff which you can still see. Boxcryptor, EncFS come into my mind. Disadvantage here is the de- and encryption on nearly every access. Really locally stored are only the encrypted files.

d) If you trust your local system and need to store frequently accessed files, you would like to encrypt only short before or on backing up/syncing files. This is where this software fills the gap. It can sync directories, local or webdav, and it can do this with either of these encrypted. Additionally it can sync with encfs volumes stored in local folders (which in turn can be synchronized and used in the net via encfs, encfs4win, BoxCryptor, and Encdroid)


Introduction
------------

JFileSync is used to synchronize directories of (usually) two different file systems.

What you have to do, to use JFileSync3 for that purpose is specifying an
appropriate configuration profile for JFileSync3.

Nearly all functions of JFileSync3 can be controlled via the Graphical User Interface
(GUI). However, JFileSync3 provides full access to all features (apart from
plug-ins) via the command line interface. Call 'bin/JFileSync -help' to
get an overview of all possible command line options.


Requirements
------------

- Java 6 Runtime Environment (see 'http://java.oracle.com')

- JCE extension when using other algorithms than AES (see 'http://java.oracle.com')

Installation and Application Start
----------------------------------

- Unzip the distribution file to a directory of your choice.
- For simplicity reasons a Windows launcher ('JFileSync3.exe'), a batch file ('bin/JFileSync3.bat') and a Unix
  shell script ('bin/JFileSync3') are available via the main distribution directory in order to start the application.
  ATTENTION: Because JFileSync is distributed as a Zip archive, Unix users
  will have to give executable rights manually before launching the
  application, e.g.: 'chmod a+rx JFileSync.sh'.

License and Usage Terms
-----------------------
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

JFileSync3 uses libraries or parts of Open Source projects where additionally some of the are included as source code:

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
'legal' sub directory of this distribution.


Development Notes
-----------------

Required packages for JFileSync3 development (not included in the distribution):
- Java 7 SDK >= 1.7.0 (see 'http://java.sun.com') (Java 8 should be working)
- Gradle >= 1.4 (see 'http://www.gradle.org' - Recommended are versions 2.2 and up)

Used and therewith recommended development tools:
- Java 7 SDK 1.7.0
- Netbeans 7.4

The encryption backend tries its very best to avoid known plaintext attacks with filenames like in directory names
("src/main/java") and with the contents of the files.

The contents are compressed before they are encrypted (in most cases - depending on size and special file types) and the
software tries three algorithms to do that: LZMS, BZIP2, Deflate.

The filenames are encoded, scrambled and then encrypted.

Metafiles to speed up acces of the directory structures don't have constant names but calculated ones.

Passwords to derive keys from are not constant along the whole directory structure.

So I now have a two-way synching from plain text directories to encrypted directories which I in turn sync with the internet
services. As you might see from some code snippets I am playing around or thinking about direct API usage for Dropbox, WebDAV and so on...

You will need the unlimited encryption add-on by Oracle for your JDK if you want to use anything except the default cipher
AES.

Feel free to issue bug reports and ideas here.

HOW-BUILD:

```bash
gradle distZip
```

Take the ZIP file from build/distributions

Optionally if launch4j is installed and available on your path you can build with

```bash
gradle launch4j distZip
```

so that the distribution will contain an additional JFileSync3.exe executable file.

a small test-suite can be found in profiles/test (which is not packaged in the distribtions zip) and can be called via

```bash
gradle encryptionTest
```

6) Included Directories and Files

The following structure describes all directories and files included in the
JFileSync3 end user releases containing only resources necessary to run the
application:
- src: Java source code
- legal: The program licenses.
- lib: Necessary libraries in order to run the system.
- profiles: Sample syncing profiles.
- ubuntu: Ubuntu Unity desktop integration files
- win: start scripts for parts of the system as tools - and a Windows icon file
- test: profiles and data for the encryption test suite
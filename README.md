Java based File Syncing Tool
==================

Syncing, Encryption, Local, and via WebDAV

(With GUI, command line, and stored profiles.)

This software was developed because I personally needed a easy to use syncing tool to have everyday backups of highly confidential material - business and private. And none of the services I came accross could suite these needs.

It is havily based on the work of Jens Heidrich and the JFileSystem 2.2

JFileSync is a SourceForge project and available via the following URL:
http://jfilesync.sourceforge.net/

Don't mix the derived work here and his clean software on sourceforge. Only bother him if it's related to his original version. Other complaints go here :-)

Scenario:

You replaced regular backups with online syncing tools like SugarSync, Syncplicity, MegaCloud, Wuala, Google Drive, Dropbox and so on. After having synced your media files, downloaded software, publicly available documents you come to the more confidential stuff...

Possible Solutions:

a) Trust the providers: Select any of them - I'll write a review for some of them once I find the time

b) Trust the providers encryption: use Teamdrive or Wuala. With Teamdrive you can - in some cases - even see the encrypted files depending on the backend you use. With Wuala you once again have to trust that it's really secure especially since they came up with the nice sharing solutions.

c) Encrypt locally on every file access and thus only sync encrypted stuff which you can still see. Boxcryptor, EncFS come into my mind. Disadvantage here is the de- and encryption on nearly every access. Really locally stored are only the encrypted files.

d) If you trust your local system and need to store frequently accessed files, you would like to encrypt only short before or on backing up/syncing files. This is where this software fills the gap. It can sync directories, local or webdav, and it can do this with either of these encrypted.


1) Introduction

JFileSync is used to synchronize directories of (usually) two different file
systems. 

What you have to do, to use JFileSync3 for that purpose is specifying an
appropriate configuration profile for JFileSync3.

Nearly all functions of JFileSync3 can be controlled via the Graphical User Interface
(GUI). However, JFileSync3 provides full access to all features (apart from
plug-ins) via the command line interface. Call 'java -jar jfs.jar -help' to
get an overview of all possible command line options.


2) Requirements

- Java 6 Runtime Environment (see 'http://java.oracle.com')

- JCE extension when using other algorithms than AES (see 'http://java.oracle.com')

3) Installation and Application Start

- Unzip the distribution file to a directory of your choice.
- For simplicity reasons a Windows batch file ('JFileSync.bat') and a Unix
  shell script ('JFileSync.sh') are available via the main distribution
  directory in order to start the application directly.
  ATTENTION: Because JFileSync is distributed as a Zip archive, Unix users
  will have to give executable rights manually before launching the
  application, e.g.: 'chmod a+rx JFileSync.sh'.
  If you have problems with the file encoding or line delimiters (when using
  an older version of the shell script) you will have to convert the script
  from DOS format to Unix format, e.g.: 'dos2unix JFileSync.sh'.
- If you do not want to use the start scripts you may also do the following:
  Enter the 'lib' sub directory of the distribution and call
  'java -jar jfs.jar' in order to start the Graphical User Interface or
  call 'java -jar jfs.jar -help' in order to get information about command
  line options. Depending in your Operating System a double click on
  'jfs.jar' will also launch the application.


2) License and Usage Terms

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

JFileSync3 uses libraries or parts of Open Source projects which are partly included as source code:

- EncFS Java: LGPL

- Sardine: Apache

- Apache http components: Apache

- Apache commons: Apache

- Bouncy Castle providers: BC

- SevenZip/LZMA: LZMA SDK is written and placed in the public domain by Igor Pavlov.

Some code in LZMA SDK is based on public domain code from another developers:
  1) PPMd var.H (2001): Dmitry Shkarin
  2) SHA-256: Wei Dai (Crypto++ library)

- log4j: Apache

- JCIFS: LGPL

- SLF4J: MIT

- Eclipse icons: the Common Public License - v 1.0.

You can find a copy all licenses of JFileSync3 and the used libraries in the
'legal' sub directory of this distribution.


5) Development Notes

Required packages for JFileSync3 development (not included in the distribution):
- Java 6 SDK >= 1.6.0 (see 'http://java.sun.com')
- Apache Ant >= 1.6.2 (see 'http://ant.apache.org')

Used and therewith recommended development tools:
- Java 7 SDK 1.7.0
- Eclipse 4.2.0

The encryption backend tries its very best to avoid known plaintext attacks with filenames like in directory names ("src/main/java") and with the contents of the files.

The contents are compressed before they are encrypted (in most cases - depending on size and special file types) and the software tries three algorithms to do that: LZMS, BZIP2, Deflate.

The filenames are encoded, scrambled and then encrypted.

Metafiles to speed up acces of the directory structures don't have constant names but calculated ones.

Passwords to derive keys from are not constant along the whole directory structure.

So I now have a two-way synching from plain text directories to encrypted directories which I in turn sync with the internet services. As you might see from some code snippets I am playing around or thinking about direct API usage for SugarSync, Dropbox, MegaCloud, and so on..

You will need the unlimited encryption add on by oracle for your JDK if you want to use anything except the default cipher AES.

Feel free to issue bug reports and ideas here. 

HOW-BUILD:

Create the jar from IDE. :-) 

(Rip out the org.bouncycastle part if you put all dependencies in a jar - it needs to be in a separate jar file)

(Rip out META-INF/maven to save some bytes)


6) Included Directories and Files

The following structure describes all directories and files included in the
JFileSync3 end user releases containing only resources necessary to run the
application:
- src: Java source code
- legal: The program licenses.
- lib: Necessary libraries in order to run the system.
- profiles: Sample syncing profiles.

- EncFS.bat: Batch file to access encfs directory structures.
- Extract.bat: Batch file extract files from encrypted directory structures.
- JFileSync.bat: Batch file to launch JFileSync from Windows.
- JFileSync.sh: Shell script to launch JFileSync from Unix/Linux.
- JFileSync.ico: Icon resource file containing the JFileSync logo.

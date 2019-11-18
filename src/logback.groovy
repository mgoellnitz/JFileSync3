/*
 * Copyright (C) 2016 Martin Goellnitz
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA, 02110-1301, USA
 */

// Log configuration
scan '3600 seconds' // Virtually don't rescan for this application

String filePattern = '%-9date{HH:mm:ss} %-5level %logger{35}.%msg%n'
String consolePattern = '%-5level %logger{35}.%msg%n'

def console = []
appender('CONSOLE', ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = consolePattern
  }
}
console.add('CONSOLE')

def appenders = []
appender('FILE', RollingFileAppender) {
  file = "jfs3.log"
  append = true
  rollingPolicy(TimeBasedRollingPolicy) {
    fileNamePattern = "jfs3-%d{yyyy-MM-dd_HH}.log"
    maxHistory = 4
  }
  encoder(PatternLayoutEncoder) {
    pattern = filePattern
  }
}
appenders.add('FILE')

root WARN, appenders
logger "org.apache", OFF, console, false
logger "org.webaccess", OFF, console, false

// logger "jfs", INFO, console, false
// logger "jfs.conf", DEBUG, console, false
// logger "jfs.sync.base", DEBUG, console, false
// logger "jfs.sync", DEBUG, console, false
// logger "jfs.sync.base", INFO, appenders, false
// logger "jfs.sync.local" ,OFF, console, false
// logger "jfs.sync.webdav", DEBUG, console, false
// logger "jfs.sync.cifs", DEBUG, console, false
// logger "jfs.sync.encfs", DEBUG, console, false
// logger "jfs.sync.encryption", DEBUG, console, false
// logger "jfs.sync.fileencrypted", DEBUG, console, false
// logger "jfs.sync.encrypted", DEBUG, console, false
// logger "jfs.sync.meta", DEBUG, console, false
// logger "jfs.sync.encdav", INFO, console, false
// logger "jfs.sync.dav", DEBUG, console, false
// logger "jfs.sync.util", INFO, console, false
// logger "org.apache.http.wire", DEBUG, console, false
// logger "com.github.sardine", DEBUG, console, false

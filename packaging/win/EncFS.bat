@echo off

if "%1" == "" (
  echo "Usage: EncFS <password> <basedir> <originalfilename>
) else (
  java -ms256m -mx512m -Xms256m -Xmx512m -classpath lib\slf4j-api-1.7.32.jar;lib\logback-core-1.2.6.jar;lib\logback-classic-1.2.6.jar;lib\bcprov-jdk15on-1.69jar;lib\JFileSync3.jar org.mrpdaemon.sec.encfs.EncFSShell %*
)

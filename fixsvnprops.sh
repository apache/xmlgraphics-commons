#!/bin/bash


for i in $(find . -name \*.java)
do
svn pg svn:keywords $i | grep Id > /dev/null || svn ps svn:keywords "Id" $i
done

find . -name \*.java -exec svn ps svn:eol-style native '{}' \;
find . -name \*.xsl  -exec svn ps svn:eol-style native '{}' \;
find . -name \*.xml  -exec svn ps svn:eol-style native '{}' \;

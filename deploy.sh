#!/bin/bash
svn up && ant clean war && cp svnwiki.war /scratch/mth/tomcat/webapps/

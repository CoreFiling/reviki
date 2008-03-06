#!/bin/bash
svn up && ant clean war && cp reviki.war /scratch/mth/tomcat/webapps/

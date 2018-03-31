# ncict2odm

## Introduction
This repository contains the NCICT2ODM java source code for converting the NCI EVS curated
CIDSC Controlled Terminology tab delimited text files into ODM-based CT-XML files. It
implements version 1.1.1 of the CT-XML extension to ODM v1.3.2. NCICT2ODM is a 
java command-line application.

## Getting Started
The NCI EVS provides the CDISC Controlled Terminology (CT) text files on their [CDISC CT
FTP site.](https://evs.nci.nih.gov/ftp1/CDISC/)

Usage for NCICT2ODM: `java -jar ncict2odm.jar txt=<text CT file> odm=<ODM CT file> std=<standard> date=<package date> ver=<schema version>`

The command-line arguments include:
* txt (required): the NCI EVS delimited text input CT path and file name for a specific standard and quarter
* odm (required): the CT-XML output path and file name generated based on the input file
* std (required): the standard being processed (e.g. SDTM) used in generating certain descriptive text strings in the output XML
* date (required): the date of the quarterly text-based load file in YYYY-MM-DD format (e.g. 2015-12-18)
* ver (optional): the version of CT-XML to generate - defaults to 1.1.1

 Example:
`java -jar ncict2odm.jar odm=c:/temp/sdtm-odm-2015-12-18.xml txt=c:/temp/sdtm-2015-12-18.txt date=2015-12-18 std=SDTM ver=1.1.1`

Note: this application should convert any of the tab delimited text CT files, but a few of the older ones have may have some
invalid binary characters that will need to be scrubbed prior to running the conversion. For example, the 2013-12-20 SDTM text file contains several
extra line-feed characters that break the row mid-line. In this case ncict2odm will display an error and cease processing the conversion.

## Binaries
The ncict2odm application was built using java 8 and uses the JDOM2 XML library.

Use the [JDOM2 downloads](http://www.jdom.org/downloads/) to retrieve the jar files. 

JDOM2 Lib files inlcuded:
* jdom-2.0.6.jar
* jdom-2.0.6-contrib.jar
* jdom-2.0.6-javadoc.jar

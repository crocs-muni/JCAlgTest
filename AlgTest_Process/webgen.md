# Installation
## 1. Get your AlgTestJClient.jar file 
Either compile it from source (see below), or [download it](https://github.com/crocs-muni/JCAlgTest/releases)

## 2. Compile your AlgTestProcess.jar file
Change your current working directory to `AlgTest_Process`, run:
```bash
ant compile
ant jar
```
Now the wanted `*.jar` file should be in the `AlgTest_Process/dist/` directory. AlgTest_JClient 
jar file is created analogically.

# Usage
## Support table page
```
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles HTML
```
This overwrites `../Profiles/AlgTest_html_table.html` to latest version. You need to add additional stuff 
manually eg. header,... TODO

## Algorithm execution time pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles/performance/fixed JCINFO
```
This creates a folder named `run_time` in the `../Profiles/performance/fixed` directory.

## Comparative table pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles/performance/fixed SORTABLE
```
This creates a file named `comparative-table.html` in the `../Profiles/performance/fixed` directory.

## Scalability pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles/performance/variable SCALABILITY
```
This creates a folder named `scalability` in the `../Profiles/performance/variable` directory.

## Radar graphs pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles/performance/fixed RADAR
```
This creates a folder named `radar_graphs` in the `../Profiles/performance/variable` directory.

## Performance similarity pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles/performance/fixed SIMILARITY
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles/performance/fixed COMPAREGRAPH
```
This creates html file and folder named `similarity-table.html` and `compare` respectively in the `../Profiles/performance/fixed` directory

# Notes
- Generated files almost always require `*.js` and `*.css` files to be present in the same folder

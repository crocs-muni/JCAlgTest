# Installation
## Compile your AlgTestProcess.jar file
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
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar *Profiles* HTML
```
This overwrites `Profiles/AlgTest_html_table.html` to latest version. You need to add additional stuff 
manually eg. header,... TODO

## Algorithm execution time pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar *Profiles/performance/fixed* JCINFO OUTPUT_BASE_PATH
```
This creates a folder named `run_time` in the output directory.

## Comparative table pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar *Profiles/performance/fixed path* SORTABLE OUTPUT_BASE_PATH
```
This creates a file named `comparative-table.html` in the output directory.

## Scalability pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar *Profiles/performance/variable* SCALABILITY OUTPUT_BASE_PATH
```
This creates a folder named `scalability` in the output directory.

## Radar graphs pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar *Profiles/performance/fixed* RADAR OUTPUT_BASE_PATH
```
This creates a folder named `radar_graphs` in the output directory.

## Performance similarity pages
```bash
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles/performance/fixed SIMILARITY OUTPUT_BASE_PATH
java -jar ../AlgTest_Process/dist/AlgTestProcess.jar ../Profiles/performance/fixed COMPAREGRAPH OUTPUT_BASE_PATH
```
This creates html file and folder named `similarity-table.html` and `compare` respectively in the output directory

# Notes
- Generated files almost always require `*.js` and `*.css` files to be present in the same output folder

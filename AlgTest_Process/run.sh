#!/bin/sh

set -x

# Clone the results repo 
if [ ! -d "./jcalgtest_results" ]; then 
    git clone https://github.com/crocs-muni/jcalgtest_results.git
fi

ALGTESTPROCESS_JAR="../AlgTest_Process/dist/AlgTestProcess.jar"
PROFILES="./jcalgtest_results/javacard/Profiles"
WEB="./jcalgtest_results/javacard/web/"

ant compile && ant jar && (
# Support table page
java -jar $ALGTESTPROCESS_JAR $PROFILES HTML
# Algorithm execution pages
java -jar $ALGTESTPROCESS_JAR ${PROFILES}/performance/fixed JCINFO ${WEB}
# Comparative table pages
java -jar $ALGTESTPROCESS_JAR ${PROFILES}/performance/fixed SORTABLE ${WEB}
# Scalability pages
java -jar $ALGTESTPROCESS_JAR ${PROFILES}/performance/variable SCALABILITY ${WEB}
# Radar graphs pages
java -jar $ALGTESTPROCESS_JAR ${PROFILES}/performance/fixed RADAR ${WEB}
# Performance similarity pages
java -jar $ALGTESTPROCESS_JAR ${PROFILES}/performance/fixed SIMILARITY ${WEB}
java -jar $ALGTESTPROCESS_JAR ${PROFILES}/performance/fixed COMPAREGRAPH ${WEB}
)





mvn versions:set -DnewVersion=$1
mvn clean install -Dgwt.compiler.skip=true
for proj in errai-demos/errai-* errai-bom errai-version-master; do (cd $proj; mvn versions:set -DnewVersion=$1); done

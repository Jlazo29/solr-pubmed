# Author: Jorge Lazo
# Purpose: Script to index downloaded files using SolrUtils java main class. 

#ARGUMENTS -- MAKE SURE TO PASS THEM CORRECTLY!
if [ $# -eq 0 ]; then
	echo "	ERROR: No Arguments supplied to script!";
	echo  "	USAGE: 
			arg[0]: Absolute path to folder containing XML files
			arg[1]: Optional argument, if 'del' is specified, all the indexed files will be cleared.

		Make absolutely sure that there are no other file types or compressed folders inside the XML directory,
		Or SolrUtils will complain. Depending on the file sizes, it may take up to 8 hours to fully index 
		a whole subset (eg: all of PMC open access, 1 million files, ~40GB). Double amount of space for
		indexing is required, so a total of 100GB of space is recommended for full indexing.";
	exit 1;
else
	echo "Compiling SolrUtils..";
fi

# 1: Move the schema desgins to the main folder, this is a temporal fix as JAXB tries to read the dtd file 
# from the main solr-pubmed folder, If you can figure out how to tell it to look elsewhere, this step and the last one can be skipped...
cd ../entrez-parsing/src/main/resources/schema;
mv JATS-archivearticle1.dtd ../../../../;
mv JATS-archivearticle.dtd ../../../../;
mv archivearticle.dtd ../../../../;
mv archivearticle3.dtd ../../../../;

# 2: Now to compile and run the main Java sources
cd ../../../../../entrez-parsing/;
mvn compile;

echo "Running SolrUtils..";
mvn  -e exec:java -Dexec.mainClass="ingestion.SolrUtils" -Dexec.args="$1 $2" -Dexec.MAVEN_OPTS=-Xmx1G;
echo "Done!"

# 3: Return files to schema folder
 
#mv JATS-archivearticle1.dtd src/main/resources/schema;
#mv JATS-archivearticle.dtd src/main/resources/schema;
#mv archivearticle.dtd src/main/resources/schema;
#mv archivearticle3.dtd src/main/resources/schema;

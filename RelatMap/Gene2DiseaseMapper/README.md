Project: Gene2DiseaseMapper

Introduction: The project is meant to map gene and disease at PubMed abstract level. We use gene2pubmed resource from NCBI for mapping genes and MeSH index for mapping diseases.


Procedure: 

*****************************************************************************

Step 1: Citation retrieval using E-Utilities. An example E-Utilities query is given below: 
http://www.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmax=100000&retmode=xml&mindate=2001/01/01&maxdate=2015/12/31&term=(Alzheimer's disease[MeSH] OR Alzheimer disease[MeSH] OR Alzheimers disease[MeSH]) AND hasAbstract[Text]

*****************************************************************************

Step 2: Processing of gene2pubmed file from NCBI. We filtered the citations related to human. We further restricted to the citations with abstract and annotated with up to 5 genes only. Please refer to 
Documentation/Processing_of_gene2pubmed.docx for details.

*****************************************************************************

Step 3: Retrieval of citations with gene and disease (based on MeSH). 

R script:
### Reading from file
> gene2pubmed <- as.matrix(read.table(FILE_A,colClasses="character"))
> eUtilsPMID <- as.matrix(read.table(FILE_B,colClasses="character"))

### Retrieving PMIDs with gene and disease annotation
> length(intersect(gene2pubmed[,1], eUtilsPMID[,1]))

### write the output to a file
> write.table(t(t(intersect(gene2pubmed[,1], eUtilsPMID[,1]))), file=OUTPUT_FILE, row.names=F, col.names=F, quote=F)

*****************************************************************************

Step 4: Mapping

Disease mapping:
To assign disease names for all PMIDs: 

							javac PubMedDiseaseMapper_43diseases.java
							java PubMedDiseaseMapper_43diseases

To filter PubMed to Disease mapping for the list of PMIDs with up to 5 genes
							
							javac PubMedDiseaseMapper1.java
							java PubMedDiseaseMapper1

Gene mapping:

							javac PubMedGeneMapper.java
							java PubMedGeneMapper

Disease-Gene mapping:

							javac GeneDiseaseMapper.java
							java GeneDiseaseMapper

*****************************************************************************

Step 5: Post-processing

Gene ID to Gene symbol mapping:

							javac GeneIDtoSymbolMapping.java
							java GeneIDtoSymbolMapping

*****************************************************************************


Java version used for development: JavaSE-1.8
Authors: Kalpana Raja PhD
Affiliation: Department of Dermatology, University of Michigan, Ann arbor 48019, MI, USA

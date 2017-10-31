Project Title: DiseaseDiseaseMapper


Introduction: DiseaseDiseaseMapper project is meant to map disease mentions in a sentence from PubMed abstract. This is developed to study the co-occurrence of diseases at sentence level. 

Prerequisites: 
** Disease lexicon and chemical/drug lexicon are required to run the project. Disease lexicon can be generated from UMLS Metathesaurus by using semantic types related to diseases (refer ConceptMap:UMLSMetathesaurusCompiler for related Java codes). Please refer
https://www.nlm.nih.gov/research/umls/knowledge_sources/metathesaurus/ for license requirement, 
download and installation of UMLS Metathesaurus.
** The project also requires a local version of PubMed database (refer ConceptMap:LocalPubmedDatabaseCompiler and ConceptMap:PubMedXMLParser for related Java codes). 
** MedTagger is required for executing the code and it is merged with our Java code. MedTagger is meant for indexing biomedical concepts (e.g. disease, chemical, drug) based on a lexicon and is distributed as an Open health Natural Language Processing project (OHNLP). 
** The project requires a range of existing Java libraries: apache-logging-log4j.jar, commons-lang-2.3.jar, commons-logging-1.1.1.jar, org.apache.commons.io.jar, Stanford lexical parser and Apache Lucene (version 5.1.0 or above). You can download them from the respective contributor's URL and add to the project's build path.  
  

---- RUN IN AN IDE ----
The entire project should be pulled into Java IDE, such as eclipse. You can compile the file Disease2DiseaseMapper/edu/uom/med/diseaseDiseaseMapper/DiseaseDiseaseMapper.java. 


---- COMPILE AND RUN ON THE COMMAND LINE ----
Mapping of diseases can be achieved with the following commands: 
						
						javac DiseaseDiseaseMapper.java
						java DiseaseDiseaseMapper INPUT_FILE OUTPUT_FILE  

Alternatively, you can generate a jar file of the entire project (DiseaseDiseaseMapper.jar, say for example) and execute the jar file from the command line using the standard command. 

						java -jar DiseaseDiseaseMapper.jar INPUT_FILE OUTPUT_FILE


Input: The input is one or more sentences from PubMed abstracts assigned with PMID (see data/sample_input). Please refer to ConceptMap:LocalPubmedDatabaseCompiler project for creating a local version of PubMed database and ConceptMap:PubMedXMLParser project for splitting an abstract into individual sentences.

Output: The output displays the mapped disease, chemical/drug, disease CUI (from UMLS Metathesaurus) and chemical/drug ID (from the lexicon). Please see data/sample_output.


Java version used for development: JavaSE-1.8
Author: Kalpana Raja PhD
Affiliation: Department of Dermatology, University of Michigan, Ann arbor 48019, MI, USA


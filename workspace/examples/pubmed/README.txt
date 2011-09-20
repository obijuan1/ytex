= UIMA Collection Processing Engine Example =
This example demonstrates annotating documents with the Collection Processing Engine (CPE).
We annotate the contents of the `YTEX_HOME\examples\pubmed\abstracts` directory that contains a 
handful of pubmed abstracts.

To run this example, do the following:
* open a command prompt / shell
* change to the `YTEX_HOME\examples\pubmed` directory
* start the UIMA CPE configurator.  Windows:
{{{
..\..\ytexCPE.cmd
}}}
Unix:
{{{
. ${HOME}/ytex.profile
${YTEX_HOME}/ytexTools.sh CPE
}}}
* click the 'Play' button

YTEX will annotate the documents and store them in the database.  
To take a quick look at the concept annotations, run the following query:
{{{
select * from v_document_cui_sent where analysis_batch = 'pubmed'
}}}

== CPE Configuration Details ==
The UIMA CPE Configurator (yes it is ugly) allows you to override parameters defined 
in the UIMA Pipeline.  Some of these properties are:

* Patterns
Regex used by the paragraph annotator to split paragraphs.

* analysis_batch
The `analysis_batch` is a way to identify document annotation runs or groups of documents.

* Store CAS
Should the UIMA XML representation of document annotations be stored in the database?
 
* Store Doc Text
Should the document text be stored in the database?

* XMI Output Directory
Directory where UIMA XML representations of document annotations should be stored; 
if empty they will not be stored in the file system.

* Types to Ignore
UIMA annotations that should not be stored in the database. 
Take a look at the `ref_uima_type` table for a list of types stored in the database.
The class name should give you an idea of what each annotation represents.


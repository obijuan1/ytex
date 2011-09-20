= DBCollectionReader Example =
This example demonstrates loading and annotating documents from a database using YTEX.
Notes used in this example are found in the `fracture_demo` table, and are taken from the Maveric ARC
distribution.

This example demonstrates how to use custom document keys.
The table `fracture_demo` contains sample documents that have a key that consists of 2 fields: 
note_id (integer) and site_id (character).

To run this example, do the following:
* (optional) modify the ytex document table and add a site_id column 
* run the collection processing engine and map the fracture_demo.note_id to the document.uid column, 
and the fracture_demo.site_id to the document.site_id column.

== Modify ytex.document ==
e.g. for MS SQL Server:
{{{
alter table document add site_id varchar(20)
}}} 

== Run the Collection Processing Engine ==

* Start the collection processing engine *
Windows: Run YTEX_HOME/ytexCPE.cmd
Unix: from a shell run the following commands
{{{
. ${HOME}/ytex.profile
cd ${YTEX_HOME}
./ytexTools.sh CPE 
}}}

* Open the CPE Descriptor *
Go to File->Open, and select YTEX_HOME/examples/cpe-fracture/fracture-demo.cpe.xml

The query to get the keys looks like this (note how note_id was renamed to uid):
{{{
select note_id uid, site_id from fracture_demo
}}}

The query to get the document looks like this:
{{{
select note_text from fracture_demo where note_id = :uid and site_id = :site_id
}}}

The CPE config "Store Doc Text" checkbox is unchecked: we will note store the document
text in the document.doc_text column, because it is already in the database - we
can join the document and fracture_demo tables on the uid/site_id columns 
to get the corresponding text.

* Run the CPE *
Press the play button

* Verify that the uid and site_id fields have been set *
{{{
select * from document where analysis_batch = 'cpe-fracture'
}}} 
 
== CPE Configuration Details ==
* Query Document Keys
This query retrieves the document keys - the way to uniquely identify each document.

* Query Get Document
This query retrieves the document using a key.  
Key parameters are specified using `:`.
The column names returned from the document key query must match the
parameter names exactly (case sensitive).  
Oracle users: oracle will return all column names as uppercase, 
unless you rename them using a quoted string (as in the uid column in the example above). 

* Key to document column mapping
We match key values to columns in the `document` table by column name (case-insensitive)
and data type.  We have tested this with non-decimal numeric types (short, int, bigint ...) 
and character types (char, varchar, ...).  Problems may occur with illegal column names
that require escaping.

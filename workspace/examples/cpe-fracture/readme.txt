= DBCollectionReader Example =
This example demonstrates how to use custom document keys (applicable to ytex v0.4 and above).
The table `fracture_demo` contains sample documents that have a key that consists of 2 fields: 
note_id (integer) and site_id (character).

To run this example, do the following:
* modify the ytex document table and add a site_id column
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
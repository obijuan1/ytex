#! /bin/sh
# create snomed-ct concept graph
export UKB_HOME=${HOME}/bin/ukb-0.1.6

mysql --user=ytex --password=ytex --database=ytex --skip-column-names < sct.sql > sct.rel.txt

${UKB_HOME}/bin/compile_kb -o sct.bin sct.rel.txt

#! /bin/sh
# create concept graph
# param ${1}: concept graph name
# expect ${1}.sql
# generate ${1}.rel.txt relations input for compile_kb
# and ${1}.bin concept graph
export UKB_HOME=${HOME}/bin/ukb-0.1.6

mysql --user=ytex --password=ytex --database=umls2011ab --skip-column-names < ${1}.sql > ${1}.rel.txt

${UKB_HOME}/bin/compile_kb -o ${1}.bin ${1}.rel.txt

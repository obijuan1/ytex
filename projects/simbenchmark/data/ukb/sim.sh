#! /bin/sh
# run ppr on 
export DATA_HOME=${HOME}/ytex/projects/simbenchmark/data
export UKB_HOME=${HOME}/bin/ukb-0.1.6

cd $DATA_HOME/ukb
PREFIX=$1
KB=$2

cat ../${PREFIX}_id.txt | ${UKB_HOME}/similarity.pl --sim dot --ukbargs "--concept_graph --only_synsets --nopos --dict_file ${PREFIX}_dict.txt --kb_binfile ./${kb}.bin" > ${PREFIX}_ppr.txt

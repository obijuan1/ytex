#! /bin/sh
# run ppr on 
export DATA_HOME=${HOME}/ytex/projects/simbenchmark/data
export UKB_HOME=${HOME}/bin/ukb-0.1.6

cd ${UKB_HOME}/UKBsim
PREFIX=$1
KB=$2
mkdir ${DATA_HOME}/ukb/${KB}

cat ${DATA_HOME}/${PREFIX}_id.txt | ./similarity.pl --sim dot --ukbargs "--concept_graph --only_synsets --nopos --dict_file ${DATA_HOME}/ukb/${PREFIX}_dict.txt --kb_binfile ${DATA_HOME}/ukb/${KB}.bin" > ${DATA_HOME}/ukb/${KB}/${PREFIX}_ppr.txt

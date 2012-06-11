#! /bin/sh
mkdir ${1}
nohup ./sim.sh MiniMayoSRS ${1} > ${1}/MayoSRS.out 2>&1 &
nohup ./sim.sh MayoSRS ${1} > ${1}/MayoSRS.out 2>&1 &
nohup ./sim.sh UMNSRS_relatedness ${1} > ${1}/UMNSRS_relatedness.out 2>&1 &
nohup ./sim.sh UMNSRS_similarity ${1} > ${1}/UMNSRS_similarity.out 2>&1 &

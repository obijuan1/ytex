= change variables in scripts =
* setupcg.sh
modify UKB_HOME to match your environment
modify the mysql user, password to match your environment

* setupcg_sct.sh
modify UKB_HOME to match your environment
modify the mysql database to match your environment

* sim.sh
modify DATA_HOME and UKB_HOME to match your environment

= run =
* make scripts executable:
chmod u+x *.sh

* generate the concept graphs:
nohup ./setupcg.sh sct ytex > sct.out 2>&1 & 
nohup ./setupcg.sh msh ytex > msh.out 2>&1 &
nohup ./setupcg.sh sct-umls umls2011ab > sct-umls.out 2>&1 &
nohup ./setupcg.sh msh-umls umls2011ab > msh-umls.out 2>&1 &
nohup ./setupcg.sh sct-msh-csp-aod umls2011ab > sct-msh-csp-aod.out 2>&1 &
nohup ./setupcg.sh umls umls2011ab > sct-msh-csp-aod.out 2>&1 &

* compute similarities
# compute similarities for sct concept graph
nohup ./sim.sh MiniMayoSRS_snomed sct >> sct.out 2>&1 &
nohup ./sim.sh MiniMayoSRS_mesh_umls msh-umls >> msh-umls.out 2>&1 &
nohup ./sim.sh MiniMayoSRS_mesh msh >> msh.out 2>&1 &

# compute similarities for sct-umls.  This will launch 3 processes so need to have enough cpu
./sim_all.sh sct-umls >> sct-umls.out 2>&1 &

# compute similarities for sct-msh-csp-aod.  This will launch 3 processes so need to have enough cpu
./sim_all.sh sct-msh-csp-aod >> sct-msh-csp-aod.out 2>&1 &

# compute similarities for umls.  This will launch 4 processes so need to have enough cpu
./sim_all.sh umls >> umls.out 2>&1 &


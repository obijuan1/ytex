select cast(concat('${HOME}/i2b2ant.sh -Dkernel.evalTest=yes -Dkernel.mod=2 -Dkernel.slice=1 -Dkernel.experiment=superlincutoff -Dexport.tree.outdir=./tree/tree-cuicutoff/', 
    label, '/', param1, 
    ' -Dkernel.xml=superlincutoff_', param2, '.xml  kernel.eval.slice -l ./tree/tree-cuicutoff/',
    label, '/', param1, '/superlincutoff_', param2, '.out') as char(300))
from cv_best_svm where experiment = 'superlincutoff'
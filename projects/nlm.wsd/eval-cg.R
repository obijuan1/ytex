# z-test between graphs
umls = read.csv("umls-10/wsd-results.csv")
sct = read.csv("sct-msh-csp-aod-10/wsd-results.csv")
umls = umls[umls$X != "n",]
sct = sct[sct$X != "n",]
umls = umls[order(umls$X),]
sct = sct[order(sct$X),]
x = data.frame(umls$X, umls = umls$all, sct = sct$all)
x = cbind(x, z=apply(x[,c("umls","sct")], 1, function(x) {z.test(x[1], x[2], 3983)}))
x = x[order(x$umls, decreasing=T),]
write.csv(x, file="nlm.wsd-cg.csv")

umls = read.csv("msh/umls-10/msh-wsd-results.csv")
sct = read.csv("msh/sct-msh-csp-aod-10/msh-wsd-results.csv")
umls = umls[umls$X != "n",]
sct = sct[sct$X != "n",]
umls = umls[order(umls$X),]
sct = sct[order(sct$X),]
x = data.frame(umls$X, umls = umls$all, sct = sct$all)
x = cbind(x, z=apply(x[,c("umls","sct")], 1, function(x) {z.test(x[1], x[2], 37888)}))
x = x[order(x$umls, decreasing=T),]
write.csv(x, file="msh.wsd-cg.csv")

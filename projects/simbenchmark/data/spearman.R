library(plyr)
# results
eval.con = function(cg, con) {
	gold = read.delim(paste(con, ".csv", sep=""), header=T, stringsAsFactors=F, sep=",")
	sim = read.delim(paste(cg, "/", con, "_id_sim.txt", sep=""), sep="\t", header=T, stringsAsFactors=F)
	simCol = ncol(sim)
	# skip the 1st 2 (concept ids) and last 3 columns (lcs info)
	sim = sim[,-c(1,2,(simCol-3):simCol)]
	res.m = data.frame(spearman=apply(sim, 2, function(x) { cor.test(gold$Mean, x, method="spearman")$estimate } ), p.value=apply(sim, 2, function(x) { cor.test(gold$Mean, x, method="spearman")$p.value } ))
	res.m = cbind(cg = rep(cg, nrow(res.m)), metric=rownames(res.m), con = rep(con, nrow(res.m)), res.m)
	return(res.m)
}
 
eval.mini = function(cg, prefix="MiniMayoSRS") {
	gold = read.delim("MiniMayoSRS.csv", header=T, stringsAsFactors=F, sep=",")
	sim = read.delim(paste(cg, "/", prefix, "_id_sim.txt", sep=""), sep="\t", header=T, stringsAsFactors=F)
	simCol = ncol(sim)
	# skip the 1st 2 (concept ids) and last 3 columns (lcs info)
	sim = sim[,-c(1,2,(simCol-3):simCol)]
	res.m1 = data.frame(
		con=rep("MiniMayoSRS_Physicians", ncol(sim)),
		spearman=apply(sim, 2, function(x) { cor.test(gold$Physicians, x, method="spearman")$estimate } ), 
		p.value=apply(sim, 2, function(x) { cor.test(gold$Physicians, x, method="spearman")$p.value } ))
	res.m1 = cbind(metric=rownames(res.m1), res.m1)
	res.m2 = data.frame(
		con=rep("MiniMayoSRS_Coders", ncol(sim)),
		spearman=apply(sim, 2, function(x) { cor.test(gold$Coders, x, method="spearman")$estimate } ), 
		p.value=apply(sim, 2, function(x) { cor.test(gold$Coders, x, method="spearman")$p.value } ))
	res.m2 = cbind(metric=rownames(res.m2), res.m2)
	comb = apply(gold[,c("Coders","Physicians"), ], 1, mean)
	res.m3 = data.frame(
		con=rep("MiniMayoSRS_Combined", ncol(sim)),
		spearman=apply(sim, 2, function(x) { cor.test(comb, x, method="spearman")$estimate } ), 
		p.value=apply(sim, 2, function(x) { cor.test(comb, x, method="spearman")$p.value } ))
	res.m3 = cbind(metric=rownames(res.m3), res.m3)
	res.m = rbind(res.m1, res.m2, res.m3)
	res.m = cbind(cg = rep(cg, nrow(res.m)), res.m)
	return(res.m)
}

# main
res = data.frame(cg=c(), file=c(), spearman=c(),p.value=c())

cgs = c("sct-umls", "sct-msh", "sct-msh-csp-aod", "sct-msh-mth-csp-aod") 
cons = c("UMNSRS_similarity", "UMNSRS_relatedness", "MayoSRS")

for(cg in cgs) {
	for(con in cons) {
		res.con = eval.con(cg, con)
		res = rbind(res, res.con)
	}
	res = rbind(res, eval.mini(cg))
}

res = rbind(res, eval.mini("sct", prefix="MiniMayoSRS_snomed"))
res = rbind(res, eval.mini("msh", prefix="MiniMayoSRS_mesh"))
res = rbind(res, eval.mini("msh-umls", prefix="MiniMayoSRS_mesh_umls"))

write.csv(res, file="spearman.csv", row.names=F)

# consolidate results
res.sum = ddply(res, .(cg, con), function(x) {
	data.frame(
		WUPALMER=x[x$metric=="WUPALMER","spearman"], 
		PATH=x[x$metric=="PATH","spearman"], 
		INTRINSIC_LIN=x[x$metric=="INTRINSIC_LIN","spearman"], 
		INTRINSIC_PATH=x[x$metric=="INTRINSIC_PATH","spearman"], 
		INTRINSIC_LCH=x[x$metric=="INTRINSIC_LCH","spearman"])
	})
write.csv(res.sum, file="spearman-summary.csv", row.names=F)

	
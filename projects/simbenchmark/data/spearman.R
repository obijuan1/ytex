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

cgs = c("sct-umls", "sct-msh", "sct-msh-csp-aod", "umls") 
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

write.csv(res, file="simbenchmark-spearman.csv", row.names=F)

# consolidate results
res.sum = ddply(res, .(con, cg), function(x) {
	data.frame(
		WUPALMER=x[x$metric=="WUPALMER","spearman"], 
		PATH=x[x$metric=="PATH","spearman"], 
		INTRINSIC_LIN=x[x$metric=="INTRINSIC_LIN","spearman"], 
		INTRINSIC_PATH=x[x$metric=="INTRINSIC_PATH","spearman"], 
		INTRINSIC_LCH=x[x$metric=="INTRINSIC_LCH","spearman"])
	})

# compare correlation between path and intrinsic lch

# fisher's r to z transformation
rtoz =  function(r1) { 0.5* log((1+r1)/(1-r1)) }

sigr = function(r1, r2, n) {
	z1 = rtoz(r1)
	z2 = rtoz(r2)
	z = (z1-z2)/sqrt(2/(n-3))
	return(min(pnorm(z), 1-pnorm(z)))
}

nbenchmark = function(benchmark) {
	con = benchmark
	if("MiniMayoSRS" == substring(con, 1, 11)) {
		con = "MiniMayoSRS"
	}
	gold = read.delim(paste(con, ".csv", sep=""), header=T, stringsAsFactors=F, sep=",")
	return(nrow(gold))	
}

names.n = unique(res.sum$con)
con.n = sapply(names.n, nbenchmark)
names(con.n) = names.n

pvals.res = function(x) {
	sim.metrics = names(x)
	sim.metrics = sim.metrics[-c(1,2)]
	n = as.numeric(con.n[x$con])
	pvals = array(0, choose(length(sim.metrics),2))
	k = 1
	for(i in 1:(length(sim.metrics)-1)) {
		for(j in (i+1):length(sim.metrics)) {
			metric1 = sim.metrics[i]
			metric2 = sim.metrics[j]
			pvals[k] =  sigr(as.numeric(x[metric1]), as.numeric(x[metric2]), n)
			names(pvals)[k] = paste(metric1, metric2, sep=".")
			k = k+1
		}
	}
	return(pvals)
}
res.sum.p = adply(res.sum, 1, pvals.res)
write.csv(res.sum.p, file="simbenchmark-summary.csv", row.names=F)

# concept graph p-value comparison
sim.cg = unique(res.sum$cg)
sim.cg = sim.cg[order(as.character(sim.cg))]
pvals.cg = function(x) {
	con = unique(x$con)
	n = as.numeric(con.n[con])
	#pvals = array(NA, choose(length(sim.cg),2))
	#k = 1
	pvals=data.frame()
	for(i in 1:(length(sim.cg)-1)) {
		for(j in (i+1):(length(sim.cg))) {
			cg1 = sim.cg[i]
			cg2 = sim.cg[j]
			if(sum(x$cg == cg1) > 0 && sum(x$cg == cg2) > 0) {
				r1 = as.numeric(x[x$cg == cg1,"INTRINSIC_LCH"])
				r2 = as.numeric(x[x$cg == cg2,"INTRINSIC_LCH"])
				p = sigr(r1, r2, n)
				pvals = rbind(pvals, data.frame(cg1=cg1, cg2 = cg2, p = p, intrinsic_lch1=r1, intrinsic_lch2=r2)) 
			}
			#names(pvals)[k] = paste(cg1, cg2, sep=".")
			#k = k+1
		}
	}
	return(pvals)
}

res.cg = ddply(res.sum[c("con","cg","INTRINSIC_LCH")], .(con), pvals.cg)
write.csv(res.cg, file="simbenchmark-cg-significance.csv", row.names=F)

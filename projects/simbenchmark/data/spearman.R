library(plyr)
# results

# compare correlation between path and intrinsic lch

# fisher's r to z transformation
rtoz =  function(r1) { 0.5* log((1+r1)/(1-r1)) }

sigr = function(r1, r2, n) {
  z1 = rtoz(r1)
  z2 = rtoz(r2)
  z = (z1-z2)/sqrt(2/(n-3))
  return(min(pnorm(z), 1-pnorm(z))*2)
}

nbenchmark = function(benchmark) {
  con = benchmark
  if("MiniMayoSRS" == substring(con, 1, 11)) {
    con = "MiniMayoSRS"
  }
  gold = read.delim(paste(con, ".csv", sep=""), header=T, stringsAsFactors=F, sep=",")
  return(nrow(gold))	
}

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

# cg concept graph
# con concept benchmark
eval.con = function(cg, con) {
	gold = read.delim(paste(con, ".csv", sep=""), header=T, stringsAsFactors=F, sep=",")
	sim = read.delim(paste(cg, "/", con, "_id_sim.txt", sep=""), sep="\t", header=T, stringsAsFactors=F)
	simCol = ncol(sim)
	# skip the 1st 2 (concept ids) and last 3 columns (lcs info)
	sim = sim[,-c(1,2,(simCol-3):simCol)]
	res.m = data.frame(spearman=apply(sim, 2, function(x) { cor.test(gold$Mean, x, method="spearman")$estimate } ), p.value=apply(sim, 2, function(x) { cor.test(gold$Mean, x, method="spearman")$p.value } ))
	res.m = cbind(
    cg = rep(cg, nrow(res.m)), 
    metric=rownames(res.m), 
    con = rep(con, nrow(res.m)), 
    res.m)
	return(res.m)
}

eval.reduced = function(cg) {
  gold = read.delim("UMNSRS_relatedness.csv", header=T, stringsAsFactors=F, sep=",")
  sim = read.delim(paste(cg, "/", "UMNSRS_relatedness_id_sim.txt", sep=""), sep="\t", header=T, stringsAsFactors=F)
  all = cbind(gold, sim)
  reduced = read.csv("UMNSRS_reduced_rel.csv", header=F)
  colnames(reduced) = c("Mean", "CUI1", "CUI2")
  reduced = unique(reduced[,c("CUI1", "CUI2")])
  all.reduced = merge(reduced, all)
  res.m = data.frame(
    spearman=
      apply(all.reduced[,11:16], 2, 
            function(x) { cor.test(all.reduced$Mean, x, method="spearman")$estimate } ), 
    p.value=
      apply(all.reduced[,11:16], 2, 
            function(x) { cor.test(all.reduced$Mean, x, method="spearman")$p.value } ))
  res.m = cbind(
    cg = rep(cg, nrow(res.m)), 
    metric=rownames(res.m), 
    con = rep("UMNSRS_reduced_rel", nrow(res.m)), 
    res.m)
  return(res.m)
}

eval.reduced.ppr = function(cg, cgSuffix="-ppr") {
  gold = read.delim("UMNSRS_relatedness.csv", header=T, stringsAsFactors=F, sep=",")
  sim = read.delim(paste(cg, cgSuffix, "/", "UMNSRS_relatedness_id_sim.txt", sep=""), sep="\t", header=T, stringsAsFactors=F)
  all = cbind(gold, sim)
  reduced = read.csv("UMNSRS_reduced_rel.csv", header=F)
  colnames(reduced) = c("Mean", "CUI1", "CUI2")
  reduced = unique(reduced[,c("CUI1", "CUI2")])
  all.reduced = merge(reduced, all)
  res.m = data.frame(
    cg = cg,
    metric = paste("PAGERANK", cgSuffix, sep=""),
    con = "UMNSRS_reduced_rel",
    spearman=cor.test(all.reduced$Mean, all.reduced$PAGERANK, method="spearman")$estimate, 
    p.value=cor.test(all.reduced$Mean, all.reduced$PAGERANK, method="spearman")$p.value)
  return(res.m)
}
# personalized pagerank evaluation
eval.con.ppr = function(cg, con, cgSuffix="-ppr") {
  ppr = paste(cg, cgSuffix, "/", con, "_id_sim.txt", sep="")
  if(!file.exists(ppr))
    return(data.frame(cg = cg, metric="PAGERANK", con = con, spearman=0, p.value=0))
  gold = read.delim(paste(con, ".csv", sep=""), header=T, stringsAsFactors=F, sep=",")
  sim = read.delim(ppr, sep="\t", header=T, stringsAsFactors=F)
  cor = cor.test(gold$Mean, sim[,"PAGERANK"], method="spearman")
  spearman = cor$estimate
  p.value = cor$p.value
  res.m = data.frame(cg = cg, metric=paste("PAGERANK", cgSuffix, sep=""), con = con, spearman=spearman, p.value=p.value)
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

eval.mini.ppr = function(cg, prefix="MiniMayoSRS", cgSuffix="-ppr") {
  ppr = paste(cg, cgSuffix, "/", prefix, "_id_sim.txt", sep="")
  if(!file.exists(ppr))
    return(data.frame(cg = rep(cg, 3),
                      metric = rep(paste("PAGERANK", cgSuffix, sep=""), 3),
                      con = c("MiniMayoSRS_Physicians", "MiniMayoSRS_Coders", "MiniMayoSRS_Combined"),
                      spearman = rep(0, 3),
                      p.value = rep(0, 3)))
  gold = read.delim("MiniMayoSRS.csv", header=T, stringsAsFactors=F, sep=",")
  sim = read.delim(ppr, sep="\t", header=T, stringsAsFactors=F)
  res = data.frame(cg = cg, 
                   metric=paste("PAGERANK", cgSuffix, sep=""), 
                   con = "MiniMayoSRS_Physicians",
                   spearman=cor.test(gold$Physicians, sim[,"PAGERANK"], method="spearman")$estimate, 
                   p.value=cor.test(gold$Physicians, sim[,"PAGERANK"], method="spearman")$p.value)
  res = rbind(res, 
              data.frame(cg = cg, 
                         metric=paste("PAGERANK", cgSuffix, sep=""), 
                         con = "MiniMayoSRS_Coders", 
                         spearman=cor.test(gold$Coders, sim[,"PAGERANK"], method="spearman")$estimate, 
                         p.value=cor.test(gold$Coders, sim[,"PAGERANK"], method="spearman")$p.value))
  comb = apply(gold[,c("Coders","Physicians"), ], 1, mean)
  res = rbind(res, 
              data.frame(cg = cg, 
                         metric=paste("PAGERANK", cgSuffix, sep=""), 
                         con = "MiniMayoSRS_Combined", 
                         spearman=cor.test(comb, sim[,"PAGERANK"], method="spearman")$estimate, 
                         p.value=cor.test(comb, sim[,3], method="spearman")$p.value))
  return(res)
}

# concatenate all similarity results
concat.results = function(cgs, cons) {
  sim = data.frame()
  for(cg in cgs) {
    for(con in cons) {
      f = paste(cg, "/", con, "_id_sim.txt", sep="")
      if(file.exists(f)) {
        sim1 = read.delim(f, sep="\t", header=T, stringsAsFactors=F)
        sim1 = cbind(
          cg = rep(cg, nrow(sim1)),
          con = rep(con, nrow(sim1)),
          sim1,
          PAGERANK_ppr = rep(NA, nrow(sim1)),
          PAGERANK_ppr_hier = rep(NA, nrow(sim1)))
      }
      f = paste(cg, "-ppr/", con, "_id_sim.txt", sep="")
      if(file.exists(f)) {
        ppr = read.delim(f, sep="\t", header=T, stringsAsFactors=F)
        sim1$PAGERANK_ppr = ppr[,3]
      }
      f = paste(cg, "-ppr-hier/", con, "_id_sim.txt", sep="")
      if(file.exists(f)) {
        ppr = read.delim(f, sep="\t", header=T, stringsAsFactors=F)
        sim1$PAGERANK_ppr_hier = ppr[,3]
      }
      sim = rbind(sim, sim1)
    }
  }
  return(sim)
}


# main
res = data.frame()

cgs = c("sct-umls", "sct-msh", "umls") 
cons = c("UMNSRS_similarity", "UMNSRS_relatedness", "MayoSRS")

# concatenate and save results
sim.sum = concat.results(cgs =cgs, cons=c(cons, "MiniMayoSRS"))
sim.sum = rbind(sim.sum, concat.results(cgs = c("sct"), cons=c("MiniMayoSRS_snomed")))
write.csv(sim.sum, file="sim.csv", row.names=F,na="")

res = data.frame()
for(cg in cgs) {
	for(con in cons) {
		res = rbind(res, eval.con(cg, con))
		res = rbind(res, eval.con.ppr(cg, con))
		res = rbind(res, eval.con.ppr(cg, con, cgSuffix="-ppr-hier"))
	}
	res = rbind(res, eval.mini(cg))
	res = rbind(res, eval.mini.ppr(cg))
	res = rbind(res, eval.mini.ppr(cg, cgSuffix="-ppr-hier"))
	res = rbind(res, eval.reduced(cg))
	res = rbind(res, eval.reduced.ppr(cg))
	res = rbind(res, eval.reduced.ppr(cg, cgSuffix="-ppr-hier"))
}

res = rbind(res, eval.mini("sct", prefix="MiniMayoSRS_snomed"))
res = rbind(res, eval.mini.ppr("sct", prefix="MiniMayoSRS_snomed"))
res = rbind(res, eval.mini.ppr("sct", prefix="MiniMayoSRS_snomed", "-ppr-hier"))

write.csv(res, file="simbenchmark-spearman.csv", row.names=F)


# consolidate results
res.sum = ddply(res, .(con, cg), function(x) {
	data.frame(
		WUPALMER=x[x$metric=="WUPALMER","spearman"], 
		PATH=x[x$metric=="PATH","spearman"], 
		INTRINSIC_LIN=x[x$metric=="INTRINSIC_LIN","spearman"], 
		INTRINSIC_PATH=x[x$metric=="INTRINSIC_PATH","spearman"], 
		INTRINSIC_LCH=x[x$metric=="INTRINSIC_LCH","spearman"], 
		PAGERANK_ppr=x[x$metric=="PAGERANK-ppr","spearman"],
		PAGERANK_ppr_hier=x[x$metric=="PAGERANK-ppr-hier","spearman"]
		)
	})
names.n = unique(res.sum$con)
con.n = sapply(names.n, nbenchmark)
names(con.n) = names.n

res.sum.p = cbind(res.sum, adply(res.sum, 1, pvals.res))
write.csv(res.sum.p, file="simbenchmark-summary.csv", row.names=F)


res.sct = res[res$cg == "sct", ]
res.sct = rbind(res.sct, res[res$cg == "sct-umls" & res$con == "MiniMayoSRS_Combined",])
res.sct = rbind(res.sct, res[res$cg == "sct-umls" & res$con == "MiniMayoSRS_Coders",])
res.sct = rbind(res.sct, res[res$cg == "sct-umls" & res$con == "MiniMayoSRS_Physicians",])

res.sct.p = ddply(
  res.sct, 
  .(con, metric), 
  function(x) {sigr(x[1,"spearman"], x[2,"spearman"], 29)})
colnames(res.sct.p)[3] = "p.value"

res.sct.p.sum = ddply(res.sct.p, .(con), function(x) { 
  r = x$p.value
  names(r) = x$metric
  return(r)
  }
)

write.csv(res.sct.p.sum, "sct-vs-umls.csv", row.names=F)




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


# mesh subset evaluation
eval.con.msh.umls = function(cg, con, ppr=F, cgSuffix="-ppr") {
  gold = read.delim(paste(con, ".csv", sep=""), header=T, stringsAsFactors=F, sep=",")
  dir = cg
  if(ppr) {
    dir = paste(dir, cgSuffix, sep="")
  }
  file = paste(dir, "/", con, "_id_sim.txt", sep="")
  if(!file.exists(file))
    return(data.frame())
  sim = read.delim(file, sep="\t", header=T, stringsAsFactors=F)
  all = cbind(sim, gold)
  # limit to concept pairs in mesh - these will have a defined lcs
  all = all[all$MESH1 != "", ]
  all = all[all$MESH2 != "", ]
  res.m = data.frame()
  if(ppr) {
    res.m = data.frame(
      cg = cg,
      metric = paste("PAGERANK", cgSuffix, sep=""),
      con = con,
      spearman=cor.test(all$Mean, all$PAGERANK, method="spearman")$estimate,
      p.value=cor.test(all$Mean, all$PAGERANK, method="spearman")$p.value)  
  } else {
    res.m = data.frame(
      spearman=apply(all[,3:9], 2, 
                     function(x) { 
                       cor.test(all$Mean, x, method="spearman")$estimate } ), 
      p.value=apply(all[,3:9], 2, 
                    function(x) { 
                      cor.test(all$Mean, x, method="spearman")$p.value } ))
      res.m = cbind(
        cg = rep(cg, nrow(res.m)), 
        metric=rownames(res.m), 
        con = rep(con, nrow(res.m)), 
        res.m)
  }
  res.m = cbind(res.m, N=rep(nrow(all), nrow(res.m)))
  return(res.m)
}


eval.con.msh = function(cg, con, ppr=F) {
  gold = read.delim(paste(con, ".csv", sep=""), header=T, stringsAsFactors=F, sep=",")
  dir = cg
  if(ppr) {
    dir = paste(dir, "-ppr", sep="")
  }
  file = paste(dir, "/", con, "_mesh_id_sim.txt", sep="")
  if(!file.exists(file))
    return(data.frame())
  sim = read.delim(file, sep="\t", header=T, stringsAsFactors=F)
  # limit to concept pairs in mesh
  gold = gold[gold$MESH1 != "", ]
  gold = gold[gold$MESH2 != "", ]
  all = merge(sim, gold, by.x=c("Concept.1", "Concept.2"), by.y=c("MESH1", "MESH2"), all = F)
  res.m = data.frame()
  if(ppr) {
    res.m = data.frame(
      cg = cg,
      metric = "PAGERANK-ppr",
      con = con,
      spearman=cor.test(all$Mean, all$PAGERANK, method="spearman")$estimate,
      p.value=cor.test(all$Mean, all$PAGERANK, method="spearman")$p.value)  
  } else {
    res.m = data.frame(
      spearman=apply(all[,3:9], 2, 
                     function(x) { 
                       cor.test(all$Mean, x, method="spearman")$estimate } ), 
      p.value=apply(all[,3:9], 2, 
                    function(x) { 
                      cor.test(all$Mean, x, method="spearman")$p.value } ))
    res.m = cbind(
      cg = rep(cg, nrow(res.m)), 
      metric=rownames(res.m), 
      con = rep(con, nrow(res.m)), 
      res.m)
  }
  res.m = cbind(res.m, N=rep(nrow(all), nrow(res.m)))
  return(res.m)
}

# concatenate and save results
sim.sum = concat.results(cgs = "msh", cons=c("MiniMayoSRS_mesh", "MayoSRS_mesh", "UMNSRS_similarity_mesh", "UMNSRS_relatedness_mesh"))
sim.sum = rbind(sim.sum, concat.results("msh-umls", cons=c("MiniMayoSRS_mesh_umls", "MayoSRS", "UMNSRS_similarity", "UMNSRS_relatedness")))
write.csv(sim.sum, file="sim-mesh.csv", row.names=F, na="")

# mesh
# get minimayo results
res.msh = eval.mini("msh", prefix="MiniMayoSRS_mesh")
res.msh = rbind(res.msh, eval.mini.ppr("msh", prefix="MiniMayoSRS_mesh"))
ptemp = eval.mini.ppr("msh", prefix="MiniMayoSRS_mesh")
ptemp$metric = rep("PAGERANK-ppr-hier", nrow(ptemp))
res.msh = rbind(res.msh, ptemp)
res.msh = rbind(res.msh, eval.mini("msh-umls", prefix="MiniMayoSRS_mesh_umls"))
res.msh = rbind(res.msh, eval.mini.ppr("msh-umls", prefix="MiniMayoSRS_mesh_umls"))
res.msh = rbind(res.msh, eval.mini.ppr("msh-umls", prefix="MiniMayoSRS_mesh_umls", cgSuffix="-ppr-hier"))
res.msh = cbind(res.msh, N=rep(29, nrow(res.msh)))

# get results for other benchmarks
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="MayoSRS"))
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="UMNSRS_relatedness"))
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="UMNSRS_similarity"))
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="MayoSRS", ppr=T))
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="UMNSRS_relatedness", ppr=T))
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="UMNSRS_similarity", ppr=T))
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="MayoSRS", ppr=T, cgSuffix="-ppr-hier"))
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="UMNSRS_relatedness", ppr=T, cgSuffix="-ppr-hier"))
res.msh = rbind(res.msh, eval.con.msh.umls("msh-umls", con="UMNSRS_similarity", ppr=T, cgSuffix="-ppr-hier"))

res.msh = rbind(res.msh, eval.con.msh("msh", con="MayoSRS"))
res.msh = rbind(res.msh, eval.con.msh("msh", con="UMNSRS_relatedness"))
res.msh = rbind(res.msh, eval.con.msh("msh", con="UMNSRS_similarity"))
res.msh = rbind(res.msh, eval.con.msh("msh", con="MayoSRS", ppr=T))
res.msh = rbind(res.msh, eval.con.msh("msh", con="UMNSRS_relatedness", ppr=T))
res.msh = rbind(res.msh, eval.con.msh("msh", con="UMNSRS_similarity", ppr=T))
# duplicate PAGERANK-ppr to PAGERANK-ppr-hier
ptemp = rbind(eval.con.msh("msh", con="MayoSRS", ppr=T))
ptemp = rbind(ptemp, eval.con.msh("msh", con="UMNSRS_relatedness", ppr=T))
ptemp = rbind(ptemp, eval.con.msh("msh", con="UMNSRS_similarity", ppr=T))
ptemp$metric = rep("PAGERANK-ppr-hier", nrow(ptemp))
res.msh = rbind(res.msh, ptemp)

res.msh.sum = ddply(res.msh, .(con, cg), function(x) {
  data.frame(
    WUPALMER=x[x$metric=="WUPALMER","spearman"], 
    PATH=x[x$metric=="PATH","spearman"], 
    INTRINSIC_LIN=x[x$metric=="INTRINSIC_LIN","spearman"], 
    INTRINSIC_PATH=x[x$metric=="INTRINSIC_PATH","spearman"], 
    INTRINSIC_LCH=x[x$metric=="INTRINSIC_LCH","spearman"], 
    LIN=x[x$metric=="LIN","spearman"],
    PAGERANK_ppr=x[x$metric=="PAGERANK-ppr","spearman"],
    PAGERANK_ppr_hier=x[x$metric=="PAGERANK-ppr-hier","spearman"],
    N=x[1, "N"])
})

res.msh.sum = cbind(res.msh.sum, 
                    SIG=adply(res.msh.sum, 1, 
                              function(x) { 
                                data.frame(SIG=sigr(x$LIN, x$INTRINSIC_LIN, x$N))
                                }
                              )$SIG
                    )

write.csv(res.msh.sum, "simbenchmark-msh-summary.csv", row.names=F)

res.msh.p = ddply(
  res.msh, 
  .(con, metric), 
  function(x) {sigr(x[1,"spearman"], x[2,"spearman"], x[1,"N"])})
colnames(res.msh.p)[3] = "p.value"
res.msh.p.sum = ddply(res.msh.p, .(con), function(x) { 
  r = x$p.value
  names(r) = x$metric
  return(r)
})

write.csv(res.msh.p.sum, "msh-vs-umls.csv", row.names=F)

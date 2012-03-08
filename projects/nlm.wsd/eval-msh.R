library(plyr)
wsd = read.table("msh-wsd.txt", 
	col.names = c("instanceId", "metric", "word", "pmid", "target", "pred", "scores"), sep="\t", 
	stringsAsFactors=F,
	header = FALSE)

jim = read.csv("../../../data/jimeno.csv", header = TRUE, stringsAsFactors=F)
words.A = jim$Term[jim$Type == "A "]
words.AT = jim$Term[jim$Type == "AT "]
words.T = jim$Term[jim$Type == "T "]

evalAcc = function(wsd) {
	return(sum(apply(wsd, 1, function(x) { x["target"] == x["pred"] }))/nrow(wsd))
}

z.test = function(p1, p2, n) {
	p = (p1 * n + p2 * n) / (2*n)
	SE = sqrt(p * ( 1 - p ) * (2/n))
	z = (p1 - p2) / SE
	return(min(pnorm(z), pnorm(-z))*2)
}

acc = daply(wsd, .(metric), evalAcc)
acc = cbind(all=acc, abbreviation=daply(merge(wsd, data.frame(word=words.A)), .(metric), evalAcc))
acc = cbind(acc, term=daply(merge(wsd, data.frame(word=words.T)), .(metric), evalAcc))
acc = cbind(acc, abbreviation_term=daply(merge(wsd, data.frame(word=words.AT)), .(metric), evalAcc))
acc = acc[order(acc[,"all"], decreasing=T),]
write.csv(acc, file="msh-wsd-results.csv")

sim.n = length(unique(wsd$instanceId))
sim.metrics = rownames(acc)
sim.p =acc[,"all"]

pvals = matrix(nrow=(length(sim.metrics)-1), ncol=(length(sim.metrics)-1), data=NA)
colnames(pvals) = sim.metrics[-1]
rownames(pvals) = sim.metrics[-length(sim.metrics)]

for(i in 1:(length(sim.metrics)-1)) {
	for(j in (i+1):length(sim.metrics)) {
		metric1 = sim.metrics[i]
		metric2 = sim.metrics[j]
		pvals[metric1, metric2] = z.test(as.numeric(sim.p[metric1]), as.numeric(sim.p[metric2]), as.numeric(sim.n))
	}
}
write.csv(pvals, file="msh-wsd-pvals.csv", na="")


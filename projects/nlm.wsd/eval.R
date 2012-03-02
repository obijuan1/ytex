library(plyr)
metrics = c("LCH", "INTRINSIC_LCH", "INTRINSIC_LIN", "PATH", "INTRINSIC_PATH", "JACCARD", "SOKAL", "WUPALMER", "RADA")
words.notsim =  c("extraction", "failure", "radiation", "transport")
words.unsup = c("adjustment", "blood_pressure", "degree", "evaluation", "growth", "immunosuppression", "mosaic", "nutrition", "radiation", "repair", "scale", "sensitivity", "white")

loadMetric = function(metric) {
	lch = read.table(paste(metric, ".txt", sep=""), 
		col.names = c("instanceId", "word", "target", "pred", "scores"), 
		sep="\t", 
		stringsAsFactors=F)
	lch = lch[lch$target != "None",]
	return(lch)
}

evalMetricWord = function(metric) {
	lch = loadMetric(metric)
	return(daply(lch, .(word), function(x) {sum(x$target==x$pred)/nrow(x)}))
}

evalMetric = function(metric) {
	lch = loadMetric(metric)
	lch.common = merge(data.frame(word=words.common), lch)
	lch.unsup  = merge(data.frame(word=words.unsup), lch)
	words.sim = unique(lch$word)
	for(word in words.notsim) {
		words.sim = words.sim[words.sim != word]
	}
	lch.sim = merge(data.frame(word=words.sim), lch)hist
	res = c(sum(lch$target==lch$pred)/nrow(lch)
		, sum(lch.unsup$target==lch.unsup$pred)/nrow(lch.unsup)
		, sum(lch.sim$target==lch.sim$pred)/nrow(lch.sim))
	names(res) = c("all", "mcinnes", "sim")
	return(res)
}
countMetric = function(metric) {
	lch = loadMetric(metric)
	lch.common = merge(data.frame(word=words.common), lch)
	lch.unsup  = merge(data.frame(word=words.unsup), lch)
	words.sim = unique(lch$word)
	for(word in words.notsim) {
		words.sim = words.sim[words.sim != word]
	}
	lch.sim = merge(data.frame(word=words.sim), lch)
	res = c(nrow(lch), nrow(lch.unsup), nrow(lch.sim))
	names(res) = c("all", "mcinnes", "sim")
	return(res)
}

z.test = function(p1, p2, n) {
	p = (p1 * n + p2 * n) / (2*n)
	SE = sqrt(p * ( 1 - p ) * (2/n))
	z = (p1 - p2) / SE
	return(dnorm(z))
}

res.word = aaply(metrics, 1, evalMetricWord)
res.word = t(res.word)
colnames(res.word) = metrics
lch = loadMetric("LCH")
n = daply(lch, .(word), nrow)
res.word = cbind(n=n, res.word)
write.csv(res.word, file="wsd-results-word.csv")

res = aaply(metrics, 1, evalMetric)
res = res[order(res[,"all"], decreasing=T),]
rownames(res) = metrics
res = rbind(res, n=countMetric("LCH"))
write.csv(res, file="wsd-results.csv")


sim = res[,"sim"]
sim.p = sim[-length(sim)]
sim.n = sim["n"]

sim.metrics = names(sim.p)

pvals = matrix(nrow=length(sim.metrics), ncol=(length(sim.metrics)-1), data=0)
colnames(pvals) = sim.metrics[-1]
rownames(pvals) = sim.metrics

for(i in 1:length(sim.metrics)) {
	if(i<length(sim.metrics)) {
		for(j in (i+1):length(sim.metrics)) {
			print(j)
			metric1 = sim.metrics[i]
			metric2 = sim.metrics[j]
			print(metric1)
			print(metric2)
			pvals[metric1, metric2] = z.test(sim.p[metric1], sim.p[metric2], n)
		}
	}
}
write.csv(pvals, file="wsd-pvals.csv")


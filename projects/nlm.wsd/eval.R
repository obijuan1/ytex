metrics = c("LCH", "INTRINSIC_LCH", "INTRINSIC_LIN", "PATH", "INTRINSIC_PATH", "JACCARD", "SOKAL")
words.common =  c("degree", "growth", "man", "mosaic", "nutrition", "repair", "scale", "weight", "white")
words.unsup = c("adjustment", "blood_pressure", "degree", "evaluation", "growth", "immunosuppression", "mosaic", "nutrition", "radiation", "repair", "scale", "sensitivity", "white")

evalMetric = function(metric) {
	lch = read.table(paste(metric, ".txt", sep=""), 
		col.names = c("instanceId", "word", "target", "pred", "scores"), 
		sep="\t", 
		stringsAsFactors=F)
	
	lch = lch[lch$target != "None",]
	lch.common = merge(data.frame(word=words.common), lch)
	lch.unsup  = merge(data.frame(word=words.unsup), lch)
	return(c(sum(lch$target==lch$pred)/nrow(lch), sum(lch.common$target==lch.common$pred)/nrow(lch.common), sum(lch.unsup$target==lch.unsup$pred)/nrow(lch.unsup)))
} 

res = adply(metrics, 1, evalMetric)
res = res[,-1]
colnames(res) = c("all", "common", "unsup")
rownames(res) = metrics
res = res[order(res$all, decreasing=T),]
write.csv(res, file="wsd-results.txt", sep="\t")

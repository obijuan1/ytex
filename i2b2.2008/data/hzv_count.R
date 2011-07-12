# plot cutoff vs. number of zero vectors
hzv = read.delim("hzv_count.txt", header=T)

labels = unique(hzv$label)

for(i in 1:length(labels)) {
	label = labels[i]
	hzv.amb = hzv[hzv$name == "i2b2.2008-cui",]
	hzv.amb = hzv.amb[hzv.amb$label == label,]
	if(i == 1) {
		plot(hzv.amb$cutoff, hzv.amb$hzvc, type="l", xlim=c(0.05, 0.08), main="Threshold vs Zero Vectors", xlab="threshold", ylab="Zero Vectors", col=i, lty=i)
	} else {
		lines(hzv.amb$cutoff, hzv.amb$hzvc, lty=i, col=i)
	}
}

legend("topleft", legend = labels, col=1:length(labels), lty=1:length(labels), cex=0.8)

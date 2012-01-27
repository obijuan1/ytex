bc = read.table("benchmark.csv", sep=",", header=T)
cors = data.frame(measure=c("LCH", "PATH", "LCH", "PATH"),stat=c("pearson", "spearman","pearson", "spearman"), Physicians=vector(mode="numeric", length=4), Coders=vector(mode="numeric", length=4), stringsAsFactors=F)

cors[1,"Physicians"] = cor.test(bc$LCH, bc$Physicians)$estimate
cors[1,"Coders"] = cor.test(bc$LCH, bc$Coders)$estimate
cors[2,"Physicians"] = cor.test(bc$PATH, bc$Physicians)$estimate
cors[2,"Coders"] = cor.test(bc$PATH, bc$Coders)$estimate

cors[3,"Physicians"] = cor.test(bc$LCH, bc$Physicians, method="spearman")$estimate
cors[3,"Coders"] = cor.test(bc$LCH, bc$Coders, method="spearman")$estimate
cors[4,"Physicians"] = cor.test(bc$PATH, bc$Physicians, method="spearman")$estimate
cors[4,"Coders"] = cor.test(bc$PATH, bc$Coders, method="spearman")$estimate

cors

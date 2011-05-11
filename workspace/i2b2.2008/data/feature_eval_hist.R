feval = read.delim("feature_eval_hist.txt", header=T)
feval.oa = feval[feval$label == "OA",]

# obesity plot
feval.obes = feval[feval$label == "Obesity",]
plot(density(feval.obes[feval.obes$name == 'i2b2.2008-cui',"evaluation"], from=0.01, to=0.1), log="x", col="red", main="Obesity", xlab="infogain", ylab="features", lty="solid")
lines(density(feval.obes[feval.obes$name == 'i2b2.2008-ncuiword',"evaluation"], from=0.01, to=0.1), col="black", lty="dashed")
lines(density(feval.obes[feval.obes$name == 'i2b2.2008-train',"evaluation"], from=0.01, to=0.1), col="blue", lty="dotted")
legend("topright", legend = c("Concept", "Word not in Concept", "Word"), col=c("red","black","blue"), lty=c("solid","dashed","dotted"))

# hypertension plot
feval.hypt = feval[feval$label == "Hypertension",]
plot(density(feval.hypt[feval.hypt$name == 'i2b2.2008-cui',"evaluation"], from=0.01, to=0.1), log="x", col="red", main="Hypertension", xlab="infogain", ylab="features", lty="solid")
lines(density(feval.hypt[feval.hypt$name == 'i2b2.2008-ncuiword',"evaluation"], from=0.01, to=0.1), col="black", lty="dashed")
lines(density(feval.hypt[feval.hypt$name == 'i2b2.2008-train',"evaluation"], from=0.01, to=0.1), col="blue", lty="dotted")
legend("topright", legend = c("Concept", "Word not in Concept", "Word"), col=c("red","black","blue"), lty=c("solid","dashed","dotted"))

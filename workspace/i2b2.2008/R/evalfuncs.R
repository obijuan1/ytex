library(kernlab)
library(caret)
library(foreach)


loadFolds = function() {
	return(read.table("instance.txt", header=T, colClasses=c("factor", "integer", "integer", "integer", "integer", "factor")))
}

loadGram = function(label) {
	return(read.table(paste("label", label, "_data.txt", sep=""), header=F))
}

loadAndNormGram = function(label) {
	gram = loadGram(label)
	gramN = matrix(nrow=nrow(gram), ncol=ncol(gram))
	for(i in 1:nrow(gram)) {
		for(j in i:nrow(gram)) {
			gramN[i,j] = gram[i,j] / sqrt(gram[i,i] * gram[j,j])
			gramN[j,i] = gramN[i,j]
		}
	}
	return(gramN)
}

instanceIdToIndex = function(instanceIDs, instance_id) {
	instanceIDs[instanceIDs[,1]==instance_id, 2]
}

evalAll = function(loadFoldsFn = loadFolds, loadGramFn = loadGram, costs = 10^(-3:3)) {
	results = c()
	#for(label in unique(folds$label)) {
	#	results = rbind(evalLabel(folds, label))
	#}
	results = foreach(label=unique(folds$label),.combine=rbind) %dopar% rbind(evalLabel(folds, label, costs))
	return(results)
}

evalLabel = function(folds, label, costs = 1, loadGramFn = loadGram) {
	gram = loadGramFn(label)
	instanceIDs = read.table(paste("label", label, "_instance_id.txt", sep=""))
	instanceIDs = cbind(instanceIDs, 1:nrow(instanceIDs))
	foldLabel = folds[folds$label == label, ]
	results = c()
	for(run in unique(folds$run)) {
		for(fold in unique(folds$fold)) {
			results = rbind(results, evalFold(folds, label, run, fold, gram, instanceIDs, costs=costs))
		}
	}
	return(results)
}

evalFold = function(folds, label, run, fold, gram, instanceIDs, costs=1) {
	foldtmp = folds[folds$label == label,]
	foldtmp = folds[foldtmp$run == run,]
	foldtmp = foldtmp[foldtmp$fold == fold,]
	train = foldtmp[foldtmp$train == 1,]
	test = foldtmp[foldtmp$train == 0,]
	results = c()
	for(cost in costs) {
		rtmp = evalFoldCost(train, test, gram, instanceIDs, cost)
		rtmp = cbind(label=rep(label, nrow(rtmp)), run=rep(run, nrow(rtmp)), fold=rep(fold, nrow(rtmp)), rtmp)
		results = rbind(results, rtmp)
	}
	return(results)
}

evalFoldCost = function(train, test, gram, instanceIDs, cost=1) {
	trainIndices = sapply(train$instance_id, function(instance_id) {instanceIdToIndex(instanceIDs, instance_id)})
	testIndices = sapply(test$instance_id, function(instance_id) {instanceIdToIndex(instanceIDs, instance_id)})
	trainK <- as.kernelMatrix(as.matrix(gram[-testIndices,-testIndices]))
	m <- ksvm(trainK, train$class, kernel='matrix')
	testK <- as.kernelMatrix(as.matrix(gram[testIndices,-testIndices][,SVindex(m), drop=F]))
	preds <- predict(m, testK)
	return(evalToResults(cost, m, test, preds))
}

evalToResults = function(cost, m, test, preds) {
	cm = confusionMatrix(preds, test$class)
	cm.tt = as.matrix(cm)
	classCount = nrow(cm.tt)
	results = data.frame(cost=rep(cost, classCount), sv=rep(nSV(m), classCount), class=rownames(cm.tt),  tp=rep(0, classCount), fp=rep(0, classCount), tn=rep(0, classCount), fn=rep(0, classCount), sens=rep(0,classCount), spec=rep(0,classCount), ppv=rep(0,classCount), npv=rep(0,classCount), f1=rep(0,classCount))
	for(cls in 1:nrow(cm.tt)) {
		results[cls, "tp"] = cm.tt[cls, cls]
		results[cls, "tn"] = sum(cm.tt[-cls, -cls])
		results[cls, "fp"] = sum(cm.tt[cls, -cls])
		results[cls, "fn"] = sum(cm.tt[-cls,cls])
		results[cls, c("sens", "spec", "ppv", "npv")] = sapply(cm$byClass[cls, c("Sensitivity","Specificity","Pos Pred Value", "Neg Pred Value")], function(x) {if(is.nan(x)) { return(0) } else {return (x)}})
		ppv = results[cls,"ppv"]
		sens = results[cls,"sens"]
		if((ppv+sens)>0) {
			results[cls, "f1"] = 2*ppv*sens/(ppv+sens)
		}
	}
	return(results)
}



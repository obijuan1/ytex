library(kernlab)
library(plyr)

# kpca the gram matrix, export for libsvm
# gramFile - path to gram file
# instanceFile - path to instance data
# idFile - path to instance ids corresponding to rows/cols of gram matrix
# outputDir - where to write data, must end with /
# features - number dimensions for kpca, default 0 - all
# inst.colnames - column names for instance, must include "instance_id" and "class", also "train", "label", "run", "fold"
export = function(gramFile = "./data.txt", instanceFile = "./instance.txt", idFile = "./instance_id.txt", outputDir= "./", features=0, inst.colnames=NULL) {
	# read the data
	data = as.matrix(read.delim("data.txt", header=FALSE, sep=" "))
	header = inst.colnames == NULL
	instance = read.delim("instance.txt", header=header)
	if(inst.colnames != NULL)
		colnames(instance) = inst.colnames
	# read instance ids corresponding to data
	iid = read.delim("instance_id.txt", header=FALSE)
	iid = cbind(iid, 1:nrow(iid))
	colnames(iid) = c("instance_id", "index")
	# export it
	exportKpcaSvmLight(instance, iid, data, features=features, prefix=outputDir)
} 

# convert a row of data into the libsvm format - [column index]:[value] ...
# 1:.09090 2:09090 ...
rowToLibsvm = function(r) {
	inst.row = rbind(1:length(r), r)
	inst.row = inst.row[, r != 0]
	line = paste(aaply(inst.row, 2, function(x) {sprintf("%d:%f", x[1], x[2])} ), collapse=" ")
	return(line)
}

# convert entire data matrix into array of strings in libsvm format 
matrixToLibsvm = function(x) {
	return(aaply(mtx.kpca, 1, rowToLibsvm))
}

# kpca the matrix
# for each label/fold/run export the svmlight data
exportKpcaSvmLight = function(instance, iid, data, features = 0, prefix= "./") {
	# convert to kernelmatrix
	data.km = as.kernelMatrix(data)
	# do the kernel pca
	data.kpca = kpca(data.km, features=features)
	# extract the primal representation
	mtx.kpca = rotated(data.kpca)
	data.libsvm = matrixToLibsvm(mtx.kpca)
	# export to libsvm
	label_ids = c(0)
	folds = c(0)
	runs = c(0)
	if(sum(colnames(instance) == "label_id") > 1)
		label_ids = unique(instance$label_id)
	if(sum(colnames(instance) == "fold") > 1)
		folds = unique(instance$fold)
	if(sum(colnames(instance) == "run") > 1)
		runs = unique(instance$run)
	# for testing
	# label_ids = c(1)
	# folds = c(1)
	# runs = c(1)
	for(label in label_ids) {
		for(fold in folds) {
			for(run in runs) {
				if(label > 0)
					prefix = paste(prefix, "label", label, "_", sep="")
				if(fold > 0)
					prefix = paste(prefix, "fold", fold, "_", sep="")
				if(run > 0)
					prefix = paste(prefix, "run", run, "_", sep="")
				inst.fold = instance[instance$label_id == label_id & instance$fold == fold & instance$run == run,]
				exportSvmlight(inst.fold, prefix, iid, data.libsvm)
			}
		}
	}
}

exportSvmlight = function(inst.fold, prefix, iid, data.libsvm) {
	print(paste("-> exportSvmlight(", prefix, ")"))
	inst.test = inst.fold[inst.fold$train == 0,]
	if(nrow(inst.test) > 0) {
		exportSvmlightFold(inst.test, paste(prefix, "test", sep=""), data.libsvm, iid)
	}
	inst.train = inst.test
	inst.train$class = rep(0, nrow(inst.test))
	inst.train = rbind(inst.train, inst.fold[inst.fold$train == 1,])
	inst.train = inst.train[order(inst.train$instance_id),]
	inst.test = inst.test[order(inst.test$instance_id),]
	exportSvmlightFold(inst.train, paste(prefix, "train", sep=""), data.libsvm, iid)
	print(paste("<- exportSvmlight()"))
}

instanceIdToIndex = function(iid, instance_id) {
	iid[iid[,1]==instance_id, 2]
}

instanceIdsToIndex = function(iid, instance_ids) {
	aaply(instance_ids, 1, function(instance_id) { instanceIdToIndex(iid, instance_id) }) 
}

exportSvmlightFold = function(inst, filePrefix, data.libsvm, iid) {
	print(paste("-> exportSvmlightFold(", filePrefix, ")"))
	idx = instanceIdsToIndex(iid, inst$instance_id) 
	write.table(cbind(inst$class, data.libsvm[idx]), file=paste(filePrefix, "_data.txt", sep=""), col.names=F, row.names=F, sep=" ", quote=F)
	write.table(inst$instance_id, file=paste(filePrefix, "_id.txt", sep=""), col.names=F, row.names=F, sep=" ", quote=F)
	print(paste("<- exportSvmlightFold()"))
}


exportKpcaTest(instance, data.libsvm, iid, outputDir) {
	instance = read.table("../kern-ctakes-ident-test/instance.txt")
	colnames(instance) = c("instance_id", "class", "train", "label")
	for(label in unique(instance$label)) {
		prefix = paste("../kern-ctakes-ident-test/label", label, "_", sep="")
		exportSvmlight(instance[instance$label == label,], prefix, iid, data.libsvm)
	}
}


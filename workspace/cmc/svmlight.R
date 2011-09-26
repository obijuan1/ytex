library(kernlab)
library(plyr)


# kpca the gram matrix, export for libsvm
# gramFile - path to gram file
# instanceFile - path to instance data
# idFile - path to instance ids corresponding to rows/cols of gram matrix
# outputDir - where to write data, must end with /
# features - number dimensions for kpca, default 0 - all
# inst.colnames - column names for instance, must include "instance_id" and "class", also "train", "label", "run", "fold"
export = function(gramFile = "./data.txt", 
	instanceFile = "./instance.txt", 
	idFile = "./instance_id.txt", 
	outputDir= "./", 
	features=0, 
	inst.colnames=NULL,
	exportFn = exportLibsvm,
	exportDataFn = NULL) 
{
	# read the data
	data = as.matrix(read.delim(gramFile, header=FALSE, sep=" "))
	instance = read.delim(instanceFile, header=is.null(inst.colnames))
	if(!is.null(inst.colnames))
		colnames(instance) = inst.colnames
	# read instance ids corresponding to data
	iid = read.delim(idFile, header=FALSE)
	iid = cbind(iid, 1:nrow(iid))
	colnames(iid) = c("instance_id", "index")
	# export it
	exportKpcaSvmLight(instance, iid, data, features=features, prefix=outputDir, exportFn = exportFn)
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
exportKpcaSvmLight = function(instance, iid, data, features = 0, 
	prefix= "./", 
	exportFn=exportLibsvm,
	exportDataFn = NULL) {
	# convert to kernelmatrix
	data.km = as.kernelMatrix(data)
	# do the kernel pca
	data.kpca = kpca(data.km, features=features)
	# extract the primal representation
	mtx.kpca = rotated(data.kpca)
	data.libsvm = matrixToLibsvm(mtx.kpca)
	if(!is.null(exportDataFn)) {
		exportDataFn(data.libsvm, prefix)
	}
	# export to libsvm
	labels = c(0)
	folds = c(0)
	runs = c(0)
	if(sum(colnames(instance) == "label") > 0)
		labels = unique(instance$label)
	if(sum(colnames(instance) == "fold") > 0)
		folds = unique(instance$fold)
	if(sum(colnames(instance) == "run") > 0)
		runs = unique(instance$run)
	# for testing
	# label_ids = c(1)
	# folds = c(1)
	# runs = c(1)
	for(label in labels) {
		for(run in runs) {
			for(fold in folds) {
				foldPrefix = prefix
				filter = rep(TRUE, nrow(instance))
				if(label > 0) {
					foldPrefix = paste(foldPrefix, "label", label, "_", sep="")
					filter = filter & instance$label == label
				}
				if(run > 0) {
					foldPrefix = paste(foldPrefix, "run", run, "_", sep="")
					filter = filter & instance$run == run
				}
				if(fold > 0) {
					foldPrefix = paste(foldPrefix, "fold", fold, "_", sep="")
					filter = filter & instance$fold == fold
				}
				#print(foldPrefix)
				inst.fold = instance[filter,]
				exportFn(inst.fold, foldPrefix, iid, data.libsvm)
			}
		}
	}
}

exportLibsvm = function(inst.fold, prefix, iid, data.libsvm) {
	print(paste("-> exportLibsvm(", prefix, ")"))
	inst.test = inst.fold[inst.fold$train == 0,]
	inst.test = inst.test[order(inst.test$instance_id),]
	exportSvmlightFold(inst.test, paste(prefix, "test", sep=""), data.libsvm, iid)
	inst.train = inst.fold[inst.fold$train == 1,]
	inst.train = inst.train[order(inst.train$instance_id),]
	exportSvmlightFold(inst.train, paste(prefix, "train", sep=""), data.libsvm, iid)
	print(paste("<- exportLibsvm()"))
}

exportSvmlight = function(inst.fold, prefix, iid, data.libsvm) {
	print(paste("-> exportSvmlight(", prefix, ")"))
	inst.test = inst.fold[inst.fold$train == 0,]
	exportSvmlightFold(inst.test, paste(prefix, "test", sep=""), data.libsvm, iid)
	inst.train = inst.test
	inst.train$class = rep(0, nrow(inst.test))
	inst.train = rbind(inst.train, inst.fold[inst.fold$train == 1,])
	inst.train = inst.train[order(inst.train$instance_id),]
	inst.test = inst.test[order(inst.test$instance_id),]
	exportSvmlightFold(inst.train, paste(prefix, "train", sep=""), data.libsvm, iid)
	print(paste("<- exportSvmlight()"))
}

instanceIdsToIndex = function(iid, instance_ids) {
	merge(iid, data.frame("instance_id" = inst$instance_id), by = "instance_id")[,"index"]
}

exportSvmlightFold = function(inst, filePrefix, data.libsvm, iid) {
	print(paste("-> exportSvmlightFold(", filePrefix, ")"))
	idx = instanceIdsToIndex(iid, inst$instance_id) 
	write.table(cbind(inst$class, data.libsvm[idx]), file=paste(filePrefix, "_data.txt", sep=""), col.names=F, row.names=F, sep=" ", quote=F)
	write.table(inst$instance_id, file=paste(filePrefix, "_id.txt", sep=""), col.names=F, row.names=F, sep=" ", quote=F)
	print(paste("<- exportSvmlightFold()"))
}

# export _label and _class file for svmlin
# with svmlin, we only have a training set - the test examples are unlabeled
exportSvmlin = function(inst.fold, filePrefix, iid, data.libsvm) {
	inst.fold = inst.fold[order(inst.fold$instance_id),]
	write.table(inst.fold[,c("instance_id", "train", "class")], file=paste(filePrefix, "class.txt", sep=""),
		col.names=F, row.names=F, sep=" ", quote=F)
	clazz = inst.fold$class
	# unlabel the test classes
	clazz[inst.fold$train == 0] = 0
	write.table(clazz, file=paste(filePrefix, "label.txt", sep=""),
		col.names=F, row.names=F, sep=" ", quote=F)
}

# export data file
# we don't comingle class labels and data, 
# so we only need to do this once per scope
exportDataSvmlin = function(data.libsvm, prefix) {
	write.table(data.libsvm, file=paste(prefix, "data.txt", sep=""),
		col.names=F, row.names=F, sep=" ", quote=F)
}

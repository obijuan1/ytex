
# get a 10 x n matrix of instance ids where n = total size * proportion
subsetInstId = function(label, instance, proportion) {
	# get training instance ids
	inst.train = instance[instance$label == label & instance$train == 1, "instance_id"]
	samp.size = round(length(inst.train) *  proportion / 100)
	aaply(1:10, 1, function(x) { sample(inst.train, samp.size) })
}

# convert a row of the instance index + gram matrix into a row for libsvm output
# for gram matrices, the instance index starts with 0: 
gramRowToLibsvm = function(r) {
	# first element in gram matrix is the instance index
	line = sprintf("%d:%d", 0, r[1])
	# the rest of the elements are the kernel evaluations
	inst.row = rbind(1:(length(r)-1), r[-1])
	line = paste(line, paste(aaply(inst.row, 2, function(x) {sprintf("%d:%f", x[1], x[2])} ), collapse=" "), sep=" ")
	return(line)
}

exportSubsets = function(instance, data.libsvm, iid, labels.subset, exportFn, exportDir) {
	# create directory for output data
	dir.create(exportDir, showWarnings = TRUE, recursive = TRUE)
	# iterate through proportions
	for(proportion in proportions) {
		subsetIid = read.table(file=paste("subset/subset", proportion, ".txt", sep = ""))
		# iterate through subsets for the given proportion
		for(i in 1:nrow(subsetIid)) {
			subset.ids = subsetIid[i,]
			# iterate through labels
			for(label in labels.subset) {
				# construct file name
				# run corresponds to proportion of labeled data used
				# fold is 1-10, or however many subsets we're using
				prefix = paste(exportDir, "/label", label, "_run", proportion, "_fold", i, "_", sep="")
				# construct inst.fold
				inst.fold = instance[instance$label == label,]
				# make everything a test instance
				inst.fold$train = 0
				# make only the instances in the subset training instances
				for(id in subset.ids) {
					inst.fold[inst.fold$instance_id == id, "train"] = 1
				}
				# export the data
				exportFn(inst.fold, prefix, iid, data.libsvm)
			}
		}
	}
}

exportSvmlin = function(instance, data, iid, labels.subset) { 
	# convert to kernelmatrix
	data.km = as.kernelMatrix(data)
	# do the kernel pca
	data.kpca = kpca(data.km, features=200)
	# extract the primal representation
	mtx.kpca = rotated(data.kpca)
	kpca.libsvm = matrixToLibsvm(mtx.kpca)
	exportDir = "./subset/svmlin"
	dir.create(exportDir, showWarnings = TRUE, recursive = TRUE)
	# export data matrix
	exportDataSvmlin(kpca.libsvm, paste(exportDir, "/", sep=""))
	# export label files
	exportSubsets(instance, kpca.libsvm, iid, labels.subset, exportSvmlin, exportDir)
}

main = function() {
	instanceFile = "data/instance-test.txt"
	gramFile = "data/data.txt"
	idFile = "data/instance_id.txt"
	# read label data
	instance = read.delim(instanceFile, header=is.null(inst.colnames))
	colnames(instance) = c("instance_id", "class", "train", "label")
	# read gram matrix
	data = as.matrix(read.delim(gramFile, header=FALSE, sep=" "))
	# read instance ids corresponding to rows in gram matrix
	iid = read.delim(idFile, header=FALSE)
	iid = cbind(iid, 1:nrow(iid))
	colnames(iid) = c("instance_id", "index")
	
	# percent of labeled training data to use
	proportions = c(10, 25, 50, 75)
	# labels for which subsets will be exported
	labels.subset = c(20,9)
	dir.create("./subset", showWarnings = TRUE, recursive = TRUE)
	# write a file with the instance ids to use in each sample
	for(proportion in proportions) {
		subsetIid = subsetInstId(20, instance, proportion)
		write.table(subsetIid, file=paste("./subset/subset", proportion, ".txt", sep=""))
	}
	# convert gram matrix to libsvm format
	data.libsvm = aaply(cbind(1:nrow(data), data), 1, gramRowToLibsvm)
	exportSubsets(instance, data.libsvm, iid, labels.subset, exportLibsvm, "./subset/libsvm")
	exportSvmlin(instance, data, iid, labels.subset)
}

set.seed(1)
source("./svmlight.R")
main()


# YTEX R classification example
# to run this example, do the following:
# 1) export the data
# 2) start R, change to the directory that contains the exported data
# 3) run the following: `source("../classify.R")`
# this will 
# - load the data, 
# - train a decision tree on the training set, 
# - run the decision tree on the test set,
# - and print the results
library(Matrix)
library(caret)
library(rpart)
# load the data matrix
# 1st column: row index, 2nd column: column index, 3rd column: column value
data.raw = read.delim("data.txt", sep="\t", header=FALSE)
# We convert this into a sparse matrix, and then into a dense matrix, and then into a data frame.
# This defeats the purpose of using a sparse matrix, but for this example the 
# dense matrix easily fits into memory.  We also want to use a decision tree (rpart), so we
# need a data frame.  Typically, you would train a classifier that supports
# sparse matrices (e.g. svm from e1071), or perform feature selection to get a 
# smaller matrix.
data = data.frame(as.matrix(sparseMatrix(i=data.raw[,1], j=data.raw[,2], x=data.raw[,3])))
if(length(grep("cui", getwd())) > 0) {
	# for cuis, load column names from attributes.txt
	colnames(data) = read.delim("attributes.txt", header=F, stringsAsFactors=F)[,1]
} else {
	# words are not legal R column names which breaks rpart
	colnames(data)[1] = "instance_id"
}
# set row names = instance_id
rownames(data) = data[, "instance_id"]
# load the class labels
instance = read.table("instance.txt", header=FALSE, sep="\t", col.names=c("instance_id", "class", "train"))
# get the training instance ids
train.iid = instance[instance$train == 1, "instance_id"]
# get the training dataset, drop the instance_id column
data.train = cbind(frac_class=instance$class[instance$train ==1], data[as.character(train.iid),-1])
rownames(data.train) = rownames(data[as.character(train.iid),])
# get the test instance ids and dataset
test.iid = instance[instance$train == 0, "instance_id"]
data.test = cbind(frac_class=instance$class[instance$train == 0], data[as.character(test.iid),-1])
rownames(data.test) = rownames(data[as.character(test.iid),])
# train a decision tree on the training data
# you would probably want to do some cross validation
frac.rpart = rpart(frac_class~., data.train)
print(frac.rpart)
# see how we did
print(confusionMatrix(predict(frac.rpart, data.test[,-1], type="class"), data.test$frac_class)) 

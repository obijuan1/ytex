# this R script creates a distance matrix for use with semil
# it takes as input the following parameters:
# - a sparse matrix (inputfile): see Matrix::sparseMatrix
# - a distance metrix (method): this parameter is passed to amap::Dist
# - an output file (outputfile): where the distance matrix will be written to
# - a degree parameter (degree): for k-nearest neighbor matrix
#
# we do the following
# - generate the distance matrix
# - generate the k-nearest neighbor matrix for the given degree
# - output the upper triangle of the distance matrix for the k-nearest neighbors of each instance

# get params from command line, following the --args option
# R started like this: "R --slave --args inputfile method outputfile degree < semil_dist.R
# or like this: R --slave --file=semil_dist.R --args label1_data.txt pearson label1_pearson.txt 10

inputfile=commandArgs(TRUE)[1]
method=commandArgs(TRUE)[2]
outputfileName = commandArgs(TRUE)[3]
degree = as.numeric(commandArgs(TRUE)[4])

print(inputfile)
print(method)
print(outputfileName)
print(degree)

#inputfile = 'label1_data.txt'
#method = 'pearson'
#outputfileName = paste('label1_data.txt', '.', method, sep='')
#degree = 10

library(Matrix)
library(amap)

# load data
x.raw = read.delim(inputfile, header=FALSE)
# put in sparse matrix
# compute distance matrix
x = sparseMatrix(i=x.raw[,1], j=x.raw[,2], x=x.raw[,3])
threshold = 0
x.dist = as.matrix(Dist(x, method))
# create k-nearest neighbors graph 
# select the top n neighbors for each vertex
x.conn = matrix(0,nrow=nrow(x.dist), ncol=ncol(x.dist))
# indices used to figure out connectivity
idxes = 1:nrow(x.dist)
for(i in 1:nrow(x.dist)) {
	#testing
	#i = 1
	# sort the vertices by distance
	topN = order(x.dist[i,])
	# exclude the vertex itself
	topN = topN[topN != i]
	if(length(topN) > 0) {
		# select the appropriate neighbors
		topN = topN[1:min(degree, length(topN))]
		for(j in topN) {
			x.conn[i,j] = 1
			x.conn[j,i] = 1
		}
	}
}

# write distance matrix
outfile = file(outputfileName, "w")
for(i in 1:(nrow(x.dist)-1)) {
	# select the neighbors from the connectivity matrix
	neigbors.idx = idxes[x.conn[i,]==1]
	# sort them
	neigbors.idx = neigbors.idx[order(x.dist[i,x.conn[i,]==1])]
	# only do the upper half of the distance matrix
	neigbors.idx = neigbors.idx[neigbors.idx > i]
	# limit to the neighbors > i
	if(length(neigbors.idx) > 0) {
		# there are some neighbors we haven't already added to the distance file
		for(j in neigbors.idx) {
			cat(i,j, sprintf('%f', x.dist[i,j]), file=outfile, sep="    ")
			cat("\n", file=outfile)
		}
	}
}
close(outfile)

experiment = "trvkern0.07"

if(size(commandArgs()) >= 5) {
	experiment = commandArgs()[5]
}


print(experiment)
source("evalfuncs.R")

# cd to directory
setwd(paste("data/",experiment,sep=""))
results = evalAll()

# save results
write.table(results, file = "results.txt", row.names = FALSE, sep="\t")

library(RJDBC)
drv <- JDBC("com.mysql.jdbc.Driver", "../../../libs.system/mysql-connector-java-5.1.9/mysql-connector-java-5.1.9-bin.jar")
conn <- dbConnect(drv, "jdbc:mysql://localhost/ytex", "ytex", "ytex")
dbWriteTable(conn, paste(gsub("\\.", "_", experiment), "_kernlab", sep=""), results, overwrite=F, append=T)
dbDisconnect(conn)
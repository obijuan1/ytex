ners = c("ctakes", "metamap-default")
windows = c(10,30,50,70)
cgs = c("msh-umls", "sct-msh-csp-aod", "umls")
metrics = c("LCH", "INTRINSIC_LCH", "INTRINSIC_LIN", "PATH", "INTRINSIC_PATH", "JACCARD", "SOKAL", "WUPALMER")

dat = data.frame()
dat.window = c()
y = c()
for(wsd in wsds) {
  for(ner in ners) {
    for(cg in cgs) {
      for(window in windows) {
        fname = paste("eval/", wsd, "-", ner, "/", cg, "/", window, "/", sep = "")
        colClasses = NA
        if(wsd == "nlm") {
          fname = paste(fname, "wsd-results.csv", sep = "")
          colClasses = c("character", "numeric", "numeric", "numeric")
        } else {
          fname = paste(fname, "msh-wsd-results.csv", sep = "")
          colClasses = c("character", "numeric", "numeric", "numeric", "numeric")
        }
        if(file.exists(fname)) {
          d = read.csv(fname, colClasses = colClasses)
          dat = rbind(dat, 
                  cbind(wsd = rep(wsd, nrow(d))
                  , ner = rep(ner, nrow(d))
                  , cg = rep(cg, nrow(d))
                  , metric = d[,1]))
          print(window)
          dat.window = c(dat.window, rep(window, nrow(d)))
          y=c(y, d[,2])
        }
      }
    }
  }
}
dat = cbind(dat, window = dat.window, y=y)
dat = dat[dat$metric != "n",]

nlm.dat = dat[dat$wsd == "nlm", -1]
nlm.lm = lm(y~., nlm.dat)
summary(nlm.lm)

msh.dat = dat[dat$wsd == "msh", -1]
msh.lm = lm(y~., msh.dat)
summary(msh.lm)

# dat.lm = lm(y~., dat)
# summary(dat.lm)

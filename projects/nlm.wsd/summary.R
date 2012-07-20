ners = c("ctakes", "metamap-default")
windows = c(10,30,50,70)
cgs = c("msh-umls", "sct-msh-csp-aod", "umls")
metrics = c("LCH", "INTRINSIC_LCH", "INTRINSIC_LIN", "PATH", "INTRINSIC_PATH", "JACCARD", "SOKAL", "WUPALMER")
wsds = c("nlm", "msh")

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
          dat.window = c(dat.window, rep(window, nrow(d)))
          y=c(y, d[,2])
        }
      }
    }
  }
}
dat = cbind(dat, window = dat.window, y=y)
dat = dat[dat$metric != "n",]
write.csv(dat, file="all-wsd-results.csv", row.names=F)

# fit models and write out coefficients
nlm.dat = dat[dat$wsd == "nlm", -1]
nlm.lm = lm(y~., nlm.dat)
summary(nlm.lm)
nlm.sum = summary(nlm.lm)
write.csv(rbind(nlm.sum$coefficients, "R_2" = c(nlm.sum$r.squared, NA, NA, NA)), file="nlm-model.csv", na="")

msh.dat = dat[dat$wsd == "msh", -1]
msh.lm = lm(y~., msh.dat)
summary(msh.lm)
msh.sum = summary(msh.lm)
write.csv(rbind(msh.sum$coefficients, "R_2" = c(msh.sum$r.squared, NA, NA, NA)), file="msh-model.csv", na="")

# dat.lm = lm(y~., dat)
# summary(dat.lm)

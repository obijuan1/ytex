drop table if exists i2b2_best_libsvm;
create table i2b2_best_libsvm (
  experiment varchar(50),
  label int,
  cutoff double,
  kernel int,
  cost double,
  gamma double,
  weight varchar(50),
  best_f1 double,
  best_f1_stdev double,
  primary key (experiment, label)
);

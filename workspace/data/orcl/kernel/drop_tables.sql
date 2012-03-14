drop index nk_feature_eval;
drop index ix_feature_eval;
drop trigger trg_feature_eval;

drop index nk_feature_name;
drop index ix_feature_rank;
drop index ix_feature_evaluation;
drop index trg_feature_rank;
drop trigger trg_feature_rank;

drop index NK_feature_parent;
drop trigger trg_feature_parchd;

drop sequence feature_eval_id_sequence;
drop sequence feature_rank_id_sequence;
drop sequence feature_parchd_id_sequence;

drop table feature_parchd;
drop table feature_rank;
drop table feature_eval;

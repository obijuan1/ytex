drop table hotspot_zv_default;
create table hotspot_zv_default (
  hotspot_zv_default_id int auto_increment primary key,
  name varchar(50) not null,
  label varchar(50) not null,
  cv_fold_id int not null default '0',
  class_name varchar(50) not null,
  cutoff double null,
  unique index NK_hotspot_zv_default(name, label, cv_fold_id, class_name, cutoff)
);

insert into hotspot_zv_default (name, label, class_name)
select 'i2b2.2008', disease, judgement
from
(
	select ja.disease, ja.judgement
	from
	(
		select disease, max(jc) jc
		from
		(
			select a.disease, a.judgement, count(*) jc
			from i2b2_2008_anno a
			inner join i2b2_2008_doc d on d.docId = a.docId and d.documentSet = 'train'
			where a.source = 'intuitive'
			group by a.disease, a.judgement
		) j
		group by disease
	) j
	inner join
	(
		select a.disease, a.judgement, count(*) jc
		from i2b2_2008_anno a
		inner join i2b2_2008_doc d on d.docId = a.docId and d.documentSet = 'train'
		where a.source = 'intuitive'
		group by a.disease, a.judgement
	) ja on j.disease = ja.disease and j.jc = ja.jc
)
;

insert into hotspot_zv_default (name, label, class_name, cv_fold_id, cutoff)
select 'i2b2.2008', disease, judgement, cv_fold_id, cutoff
from
(
	select ja.disease, ja.cv_fold_id, ja.cutoff, ja.judgement
	from
	(
		select disease, cv_fold_id, cutoff, max(jc) jc
		from
		(
			select f.cv_fold_id, a.disease, a.judgement, hzv.cutoff, count(*) jc
			from i2b2_2008_anno a
			inner join i2b2_2008_doc d on d.docId = a.docId and d.documentSet = 'train'
			inner join cv_fold_instance cvi on d.docId = cvi.instance_id and cvi.train = 1
			inner join cv_fold f on f.cv_fold_id = cvi.cv_fold_id and f.label = a.disease
			inner join hotspot_zero_vector hzv on hzv.instance_id = d.docId and hzv.label = f.label
			where a.source = 'intuitive'
			group by f.cv_fold_id, a.disease, a.judgement, hzv.cutoff
		) j
		group by disease, cv_fold_id, cutoff
	) j
	inner join
	(
		select f.cv_fold_id, a.disease, a.judgement, hzv.cutoff, count(*) jc
		from i2b2_2008_anno a
		inner join i2b2_2008_doc d on d.docId = a.docId and d.documentSet = 'train'
		inner join cv_fold_instance cvi on d.docId = cvi.instance_id and cvi.train = 1
		inner join cv_fold f on f.cv_fold_id = cvi.cv_fold_id and f.label = a.disease
		inner join hotspot_zero_vector hzv on hzv.instance_id = d.docId and hzv.label = f.label
		where a.source = 'intuitive'
		group by f.cv_fold_id, a.disease, a.judgement, hzv.cutoff
	) ja on j.cv_fold_id = ja.cv_fold_id and j.disease = ja.disease and j.jc = ja.jc and j.cutoff = ja.cutoff
) s
;

-- very few folds where it pays to look at the zero-vector class distribution vs. total class distribution
select label, cutoff, diffs, total
from
(
select hzdf.label, hzdf.cutoff, sum(case when hzdf.class_name <> hzd.class_name then 1 else 0 end) diffs, count(*) total
from hotspot_zv_default hzdf
inner join hotspot_zv_default hzd
  on hzd.label = hzdf.label and hzdf.name = hzd.name and hzdf.cv_fold_id is not null and hzdf.cutoff is not null
group by hzdf.label, hzdf.cutoff
) s where diffs > 0
;
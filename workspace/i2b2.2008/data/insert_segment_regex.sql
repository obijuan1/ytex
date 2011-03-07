insert into ref_segment_regex (regex, segment_id)
values ('(?m)^DISPOSITION:', 'DISPOSITION')
;

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^CODE STATUS:', 'CODE STATUS')
;

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^DISCHARGE MEDICATIONS:', 'MEDS');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^MEDICATIONS:', 'MEDS');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^Meds on admission:', 'MEDS');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^CURRENT MEDICATIONS:', 'MEDS');

insert into ref_segment_regex (regex, segment_id)
values ('\bDISCHARGE MEDICATIONS:', 'MEDS');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^MEDICATIONS AT HOME', 'MEDS');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^ADMIT DIAGNOSIS:', 'DIAGNOSIS ADMIT');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^ADMITTING DIAGNOSIS:', 'DIAGNOSIS ADMIT');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^PRINCIPAL DISCHARGE DIAGNOSIS:', 'DIAGNOSIS PRINCIPAL');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^PRINCIPAL DIAGNOSIS:', 'DIAGNOSIS PRINCIPAL');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^OTHER DIAGNOSIS:', 'DIAGNOSIS OTHER');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^FINAL DIAGNOSIS:', 'DIAGNOSIS OTHER');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^DISCHARGE DIAGNOSIS:', 'DIAGNOSIS OTHER');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^OPERATIONS AND PROCEDURES:', 'PROCEDURES');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^OTHER TREATMENTS/PROCEDURES ( NOT IN O.R. ):', 'PROCEDURES OTHER');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^BRIEF RESUME OF HOSPITAL COURSE:', 'HOSPITAL COURSE');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^HOSPITAL COURSE:', 'HOSPITAL COURSE');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^HOSPITAL COURSE AND PROCEDURES:', 'HOSPITAL COURSE');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^POSTOPERATIVE COURSE:', 'HOSPITAL COURSE');

insert into ref_segment_regex (regex, segment_id)
values ('\wCC:', 'CHIEF COMPLAINT');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^ID/CC:', 'CHIEF COMPLAINT');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^*CC:', 'CHIEF COMPLAINT');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^*HPI:', 'HISTORY ILLNESS');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^HISTORY OF PRESENT ILLNESS:', 'HISTORY ILLNESS');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^HPI:', 'HISTORY ILLNESS');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^PAST SURGICAL HISTORY:', 'HISTORY MEDICAL');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^PAST MEDICAL HISTORY:', 'HISTORY MEDICAL');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^PMH:', 'HISTORY MEDICAL');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^SOCIAL HISTORY:', 'HISTORY SOCIAL');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^FAMILY HISTORY:', 'HISTORY FAMILY');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^Exam on admission:', 'EXAM');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^ADMISSION PHYSICAL EXAMINATION:', 'EXAM');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^PHYSICAL EXAMINATION:', 'EXAM');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^PHYSICAL EXAMINATION ON ADMISSION:', 'EXAM');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^REVIEW OF SYSTEMS:', 'EXAM');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^Problem List:', 'PROBLEM LIST');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^LIST OF PROBLEMS AND DIAGNOSES:', 'PROBLEM LIST');



insert into ref_segment_regex (regex, segment_id)
values ('(?m)^DISCHARGE CONDITION:', 'DISCHARGE CONDITION');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^TO DO/PLAN:', 'PLAN');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^CONTINUED CARE PLAN:', 'PLAN');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^ALLERGY:', 'ALLERGY');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^ALLERGIES:', 'ALLERGY');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^ACTIVITY:', 'ACTIVITY');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^FOLLOW UP APPOINTMENT( S ):', 'FOLLOWUP');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^FOLLOWUP PLANS:', 'FOLLOWUP');


insert into ref_segment_regex (regex, segment_id)
values ('(?m)^FOLLOWUP:', 'FOLLOWUP');

insert into ref_segment_regex (regex, segment_id)
values ('(?m)^PHYSICIAN FOLLOWUP:', 'FOLLOWUP');

insert into ref_segment_regex (regex, segment_id)
values ('\bFOLLOWUP:', 'FOLLOWUP');

select * from ref_segment_regex;
delete from ref_segment_regex where segment_regex_id = 4;
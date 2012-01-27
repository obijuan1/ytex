http://wsd.nlm.nih.gov/Collaborations/NLM-WSD.target_word.choices_v0.3.tar.gz
http://wsd.nlm.nih.gov/Restricted/downloads/basic_reviewed_results.tar.gz

get words where the corresponding cuis are in the umls 
for some words the cuis are in the umls, but the words are not

select s.word
from
(
select c.word, count(distinct c.choice_code) wc
from nlm_wsd_cui c
inner join
    (
    select distinct word, choice_code
    from nlm_wsd
    ) n on c.word = n.word and c.choice_code = n.choice_code
inner join umls.MRCONSO mc on mc.cui = c.cui and mc.str = c.word
group by c.word
) s
inner join
(
select c.word, count(distinct c.choice_code) wc
from nlm_wsd_cui c
inner join
    (
    select distinct word, choice_code
    from nlm_wsd
    ) n on c.word = n.word and c.choice_code = n.choice_code
group by c.word
) c on s.word = c.word and s.wc = c.wc
order by c.word
;


words:
adjustment
blood pressure
cold
discharge
fit
glucose
immunosuppression
japanese
mole
pressure
reduction
repair
scale
strains
ultrasound

to get words that are not in the umls:
select c.*
from nlm_wsd_cui c
inner join
    (
    select distinct word, choice_code
    from nlm_wsd
    ) n on c.word = n.word and c.choice_code = n.choice_code
left join umls.MRCONSO mc on mc.cui = c.cui and mc.sab = 'snomedct' and mc.str = c.word
where mc.aui is null
;

adjustment, M3, adjustment <5> (Psychological adjustment), menp, Mental Process, C0683269
association, M2, association <2> (Relationship by association), socb, Social Behavior, C0699792
condition, M2, Conditioning (Conditioning (Psychology)), menp, Mental Process, C0009647
culture, M1, Culture <1> (Anthropological Culture), idcn, Idea or Concept, C0010453
culture, M2, Culture <3> (Laboratory culture), lbpr, Laboratory Procedure, C0430400
degree, M2, degree <2>, inpr, Intellectual Product, C0542560
depression, M1, Depression <1> (Mental Depression), mobd, Mental or Behavioral Dysfunction, C0011570
determination, M1, determination <1> (adjudication), gora, Governmental or Regulatory Activity, C0680730
determination, M2, determination <2>, lbpr, Laboratory Procedure, C0002778
discharge, M2, Discharge <1> (Patient Discharge), hlca, Health Care Activity, C0030685
energy, M2, energy <3> (Energy (physics)), npop, Natural Phenomenon or Process, C0542479
evaluation, M1, Evaluation, inpr, Intellectual Product; resa, Research Activity, C0220825
evaluation, M2, evaluation <2> (Health evaluation), hlca, Health Care Activity, C0175637
extraction, M1, extraction <1>, lbpr, Laboratory Procedure, C0684295
failure, M1, failure<1>, socb, Social Behavior, C0680095
fat, M1, FAT <1> (Obese build), orga, Organism Attribute, C0424612
fluid, M1, fluid<1> (Liquid substance, NOS), sbst, Substance, C0302908
frequency, M1, Frequency <2> (Frequencies), tmco, Temporal Concept, C0439603
frequency, M2, Frequency <3> (Increased frequency of micturition), fndg, Finding;sosy, Sign or Symptom, C0042023
ganglion, M1, Ganglion, NOS <1> (Benign cystic mucinous tumour), acab, Acquired Abnormality, C0085648
ganglion, M2, ganglion <2> (Ganglia), bpoc, Body Part, Organ, or Organ Component, C0017067
growth, M1, Growth <1>, orgf, Organism Function, C0018270
growth, M2, growth <2>, ftcn, Functional Concept, C0220844
implantation, M1, Implantation <1> (Blastocyst Implantation, natural), orgf, Organism Function, C0029976
inhibition, M1, Inhibition <1> (Psychological inhibition), menp, Mental Process, C0021467
lead, M2, Lead <2> (Lead measurement, quantitative), lbpr, Laboratory Procedure, C0524167
man, M1, MAN <1> (Male), orga, Organism Attribute, C0024554
mosaic, M2, Mosaic <2> (Mosaicism <1>), orga, Organism Attribute, C0026578
mosaic, M3, Mosaic <4>, inpr, Intellectual Product, C0700058
nutrition, M1, Nutrition, orga, Organism Attribute, C0392209
nutrition, M2, Nutrition <1> (Science of nutrition), bmod, Biomedical Occupation or Discipline; orgf, Organism Function, C0028707
nutrition, M3, Nutrition <4> (Feeding and dietary regimes), topp, Therapeutic or Preventive Procedure, C0600072
pathology, M2, pathology <3>, patf, Pathologic Function, C0677042
radiation, M1, Radiation <1> (Electromagnetic Energy), npop, Natural Phenomenon or Process, C0034519
radiation, M2, Radiation <2> (Radiation therapy), topp, Therapeutic or Preventive Procedure, C0034618
resistance, M1, resistance <1>, socb, Social Behavior, C0683598
resistance, M2, Resistance <2>, menp, Mental Process, C0237834
secretion, M1, Secretion <2> (Bodily secretions), bdsu, Body Substance, C0036537
secretion, M2, secretion <3>, biof, Biologic Function, C0036536
sensitivity, M1, Sensitivity <1> (Statistical sensitivity), qnco, Quantitative Concept, C0036667
sex, M1, Sex <1> (Coitus), inbe, Individual Behavior;orgf, Organism Function, C0009253
sex, M2, Sex <2>, orga, Organism Attribute, C0079399
sex, M3, Sex, NOS (Gender), orga, Organism Attribute, C0079399
single, M1, single <1> (Unmarried <2>), popg, Population Group, C0087136
surgery, M1, Surgery <1> (Surgery specialty), bmod, Biomedical Occupation or Discipline, C0038894
surgery, M2, Surgery <3>, topp, Therapeutic or Preventive Procedure, C0038895
transient, M2, Transient <2> (Transient Population Group), popg, Population Group, C0040704
transport, M2, Transport <2> (Patient Transport), hlca, Health Care Activity, C0150390
ultrasound, M2, Ultrasound <3> (Ultrasonic Shockwave), npop, Natural Phenomenon or Process, C0041621
variation, M1, Variation<1> (Variation (Genetics)), npop, Natural Phenomenon or Process, C0042333
weight, M1, Weight, qnco, Quantitative Concept, C0043100
weight, M2, Weight <2> (Body Weight), orga, Organism Attribute;qnco, Quantitative Concept, C0005910
white, M2, White <3> (Caucasoid Race), popg, Population Group, C0007457

terms removed since wsd study?
only 2 cuis mapped to adjustment
select distinct cui from umls.MRCONSO where str = 'adjustment';
select distinct cui from umls.mrxns_eng where nstr = 'adjustment';

need following vocabs:
('SNOMEDCT', 'MSH', 'MEDCIN', 'NCI', 'LNC', 'MTH')

* too many relations - building concept graph very slow upwards of 1 mil relationships
--  615828 distinct snomedct 
-- 1015422 distinct snomedct + msh 
-- 1372307 distinct snomedct + msh + medcin
-- 1449100 distinct snomedct + msh + medcin + nci
-- 1578228 distinct snomedct + msh + medcin + nci + lnc
-- 1692994 distinct snomedct + msh + medcin + nci + lnc + mth
select count(distinct cui1, cui2)
from umls.MRREL 
where sab in ('SNOMEDCT', 'MSH' , 'MEDCIN', 'NCI', 'LNC', 'MTH')
and rel in ('PAR', 'RB')


* which vocabs actually have relations for the concepts
-- 97 distinct cuis
select count(distinct cui) 
from nlm_wsd_cui wc 
inner join nlm_wsd ws 
    on ws.choice_code = wc.choice_code 
    and ws.word = wc.word;


-- missing 4 of the 97 from umls hierarchical relationships
select *
from
(
    select distinct wc.word, wc.choice_code, cui
    from nlm_wsd_cui wc inner join nlm_wsd ws on ws.choice_code = wc.choice_code and ws.word = wc.word
) wc
left join
(
    select distinct wc.cui
    from nlm_wsd_cui wc 
    inner join umls.MRREL mr 
        on (wc.cui = mr.cui1 or wc.cui = mr.cui2) 
        and mr.REL in ('PAR', 'CHD', 'RB', 'RN') 
) uc on wc.cui = uc.cui
where uc.cui is null
;

missing following concepts in any relationship:
extraction	M1	C0684295	
failure	M1	C0680095	
radiation	M2	C0034618	
transport	M2	C0150390	


select SAB, count(distinct wc.cui) sc
from tmp_cui wc 
inner join umls.MRREL mr on wc.cui = mr.cui1 or wc.cui = mr.cui2 and mr.REL in ('PAR', 'CHD', 'RB', 'RN')
group by SAB
order by count(distinct wc.cui) desc
;
top vocabs:
MTH	89
SNOMEDCT	73
NCI	52
MSH	52
MEDCIN	19
LNC	19

* use following SABS: 'SNOMEDCT', 'MSH', 'MTH', 'NCI'
-- 93 ALL SABS
-- 73 SNOMEDCT
-- 88 SNOMEDCT + MSH
-- 92 SNOMEDCT + MSH + MTH
-- 92 SNOMEDCT + MSH + MTH + LNC
-- 92 SNOMEDCT + MSH + MTH + MEDCIN
-- 93 SNOMEDCT + MSH + MTH + NCI
select count(distinct wc.cui)
from tmp_cui wc 
inner join umls.MRREL mr 
    on (wc.cui = mr.cui1 or wc.cui = mr.cui2) 
    and mr.REL in ('PAR', 'CHD', 'RB', 'RN') 
    and mr.sab in ('SNOMEDCT', 'MSH', 'MTH', 'NCI')

-- 1210154 relationships 'SNOMEDCT', 'MSH', 'MTH', 'NCI'
-- 1135734 relationships 'SNOMEDCT', 'MSH', 'MTH'
-- 1015422 relationships 'SNOMEDCT', 'MSH'
--  615828 relationships SNOMEDCT
select count(distinct cui1, cui2)
from umls.MRREL 
where sab in ('SNOMEDCT', 'MSH' , 'NCI', 'MTH')
and rel in ('PAR', 'RB')

---------
umls 2011ab
---------
/*
rel = all, sab = all: missing radiation	M2 C0034618

rel = hierarchical, sab = all: missing radiation	M2 C0034618

rel = hierarchical, sab = AOD, MSH, CSP, SNOMEDCT: missing 
radiation	M2	C0034618	
transport	M2	C0150390	
ultrasound	M2	C0041621	

rel = all, sab = AOD, MSH, CSP, SNOMEDCT: missing 
radiation	M2	C0034618	
transport	M2	C0150390	
*/
select *
from
(
    select distinct wc.word, wc.choice_code, cui
    from nlm_wsd_cui wc inner join nlm_wsd ws on ws.choice_code = wc.choice_code and ws.word = wc.word
) wc
left join
(
    select distinct wc.cui
    from nlm_wsd_cui wc 
    inner join umls2011ab.MRREL mr 
        on (wc.cui = mr.cui1 or wc.cui = mr.cui2) 
--        and mr.REL in ('PAR', 'CHD', 'RB', 'RN')
        and mr.SAB in ('AOD', 'MSH', 'CSP', 'SNOMEDCT')
) uc on wc.cui = uc.cui
where uc.cui is null
;

* relationship types

RB
PAR
RN
CHD
SY - synonymous
AQ - Allowed qualifier
QB - qualified by
SIB - sibling
RO - other
RQ - related
RL - similar

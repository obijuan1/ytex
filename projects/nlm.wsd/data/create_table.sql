delete from ref_segment_regex where segment_id in ('nlm.wsd.UI', 'nlm.wsd.TI', 'nlm.wsd.AB');
insert into ref_segment_regex (regex, segment_id) values ('UI\\s+-\\s', 'nlm.wsd.UI');
insert into ref_segment_regex (regex, segment_id) values ('TI\\s+-\\s', 'nlm.wsd.TI');
insert into ref_segment_regex (regex, segment_id) values ('AB\\s+-\\s', 'nlm.wsd.AB');


drop table if exists nlm_wsd;
create table nlm_wsd (
    instance_id int auto_increment not null primary key,
    word varchar(20) not null,
    choice_id int not null,
    sentence_reference varchar(20) not null,
    choice_code varchar(4) not null,
    sentence text not null,
    sent_ambiguity varchar(200) not null,
    sent_ambiguity_alias varchar(200) not null,
    sent_context_start int not null,
    sent_context_end int not null,
    sent_ambiguity_start int not null,
    sent_ambiguity_end int not null,
    sent_immediate_context varchar(200) not null,
    abstract text,
    abs_ambiguity varchar(200) not null,
    abs_ambiguity_alias varchar(200) not null,
    abs_context_start int not null,
    abs_context_end int not null,
    abs_ambiguity_start int not null,
    abs_ambiguity_end int not null,
    abs_immediate_context varchar(200) not null,
    unique key NK_nlm_wsd (word, choice_id)
) engine=myisam;


drop table if exists nlm_wsd_cui;
create table nlm_wsd_cui (
    word varchar(20) not null,
    choice_code varchar(4) not null,
	term varchar(100) not null,
	term_type varchar(100) not null,
	cui char(8) not null,
	primary key (word, choice_code)
) engine=myisam;

drop table if exists nlm_wsd_word;
create table nlm_wsd_word (
    word varchar(20) not null primary key
) engine=myisam;  


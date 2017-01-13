-- database fixes following an Updatedb run
update tracking_record_authors set authors_ORDER=0;
update tracking_record_authors_AUD set authors_ORDER=0;
update tracking_record_reviewers set reviewers_ORDER=0;
update tracking_record_reviewers_AUD set reviewers_ORDER=0;

alter table refsets drop workflowPath;
alter table refsets_AUD drop workflowPath;
alter table translations drop workflowPath;
alter table translations_AUD drop workflowPath;
        
update description_types set name = 'PT' where name = 'PN';
update description_types set name = 'PN' where name = 'PN';

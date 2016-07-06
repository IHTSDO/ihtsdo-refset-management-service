-- NOTE: these queries have to be on single lines to function properly.

-- Load initial admin user
INSERT INTO users (id, applicationRole, email, name, userName) values (1, 'ADMIN', 'test@example.com', 'Admin User', 'admin');
-- Load initial guest user
INSERT INTO users (id, applicationRole, email, name, userName) values (2, 'VIEWER', 'test@example.com', 'Guest User', 'guest');


-- Indexes on AUD tables
create index x_translations_aud on translations_AUD (id);
create index x_refsets_aud on refsets_AUD (id);
create index x_concept_refset_members_aud on concept_refset_members_AUD(refset_id);
create index x_concepts_aud on concepts_AUD (translation_id);
create index x_descriptions_aud on descriptions_AUD (concept_id);
create index x_desc_lang_aud on description_language_refset_members_AUD (description_id);
create index x_lang_aud on language_refset_members_AUD (id);

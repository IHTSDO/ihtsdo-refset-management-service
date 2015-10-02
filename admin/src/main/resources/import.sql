-- NOTE: these queries have to be on single lines to function properly.

-- Load initial admin user
INSERT INTO users (id, applicationRole, email, name, userName) values (1, 'ADMIN', 'test@example.com', 'Admin User', 'admin');
-- Load initial guest user
INSERT INTO users (id, applicationRole, email, name, userName) values (2, 'VIEWER', 'test@example.com', 'Guest User', 'guest');

-- Create ancestorPath index for tree positions (not needed bcause of lucene)
--create index x_ctr_ancestor_path on concept_tree_positions (ancestorPath(255));
--create index x_dtr_ancestor_path on descriptor_tree_positions (ancestorPath(255));
--create index x_cdtr_ancestor_path on code_tree_positions (ancestorPath(255));

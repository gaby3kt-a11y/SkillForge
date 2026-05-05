-- Insert roles
INSERT INTO roles (id, name, description) VALUES
                                              ('1', 'ROLE_STUDENT', 'Can browse and enroll in courses'),
                                              ('2', 'ROLE_INSTRUCTOR', 'Can create and manage courses'),
                                              ('3', 'ROLE_ADMIN', 'Full system access');

-- Insert permissions
INSERT INTO permissions (id, name, description) VALUES
                                                    ('1', 'COURSE_CREATE', 'Can create new courses'),
                                                    ('2', 'COURSE_EDIT', 'Can edit any course'),
                                                    ('3', 'COURSE_DELETE', 'Can delete courses'),
                                                    ('4', 'USER_VIEW', 'Can view user details'),
                                                    ('5', 'USER_MANAGE', 'Can manage users'),
                                                    ('6', 'REPORT_VIEW', 'Can view reports');

-- Assign permissions to roles
INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('1', '4'),  -- Student can view users (public profiles)
                                                          ('2', '1'), ('2', '4'),  -- Instructor can create courses and view users
                                                          ('3', '1'), ('3', '2'), ('3', '3'), ('3', '4'), ('3', '5'), ('3', '6');  -- Admin gets all
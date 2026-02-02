-- Sample data for Smart Attendance System

-- Insert sample classrooms only if table is empty
INSERT INTO classrooms (id, room_number, course_name, course_code, start_time, end_time) 
SELECT * FROM (VALUES
(1, 'A101', 'Computer Science Fundamentals', 'CS101', '09:00:00', '10:30:00'),
(2, 'B205', 'Data Structures and Algorithms', 'CS201', '11:00:00', '12:30:00'),
(3, 'C310', 'Database Management Systems', 'CS301', '14:00:00', '15:30:00')
) AS v(id, room_number, course_name, course_code, start_time, end_time)
WHERE NOT EXISTS (SELECT 1 FROM classrooms);

-- Insert sample students only if table is empty
INSERT INTO students (id, student_id, first_name, last_name, email, department, created_at) 
SELECT * FROM (VALUES
(1, 'STU001', 'John', 'Doe', 'john.doe@university.edu', 'Computer Science', CURRENT_TIMESTAMP),
(2, 'STU002', 'Jane', 'Smith', 'jane.smith@university.edu', 'Computer Science', CURRENT_TIMESTAMP),
(3, 'STU003', 'Michael', 'Johnson', 'michael.j@university.edu', 'Information Technology', CURRENT_TIMESTAMP),
(4, 'STU004', 'Emily', 'Davis', 'emily.davis@university.edu', 'Computer Science', CURRENT_TIMESTAMP),
(5, 'STU005', 'Robert', 'Brown', 'robert.b@university.edu', 'Software Engineering', CURRENT_TIMESTAMP)
) AS v(id, student_id, first_name, last_name, email, department, created_at)
WHERE NOT EXISTS (SELECT 1 FROM students);

-- Reset keys to prevent collisions with auto-generated IDs
-- Reset keys to prevent collisions (Dynamic Reset)
ALTER TABLE classrooms ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM classrooms);
ALTER TABLE students ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM students);

-- Insert default users only if table is empty (prevents restoring deleted users)
INSERT INTO users (username, password, role) 
SELECT * FROM (VALUES
    ('admin', '$2a$10$zCbLmRKFF4EQlwvQgi/iceecs3d9QsFQdHmm1rXo8/hKjV6SBtIabe', 'ADMIN')
) AS v(username, password, role)
WHERE NOT EXISTS (SELECT 1 FROM users);

-- Sample data for Smart Attendance System

-- Insert sample classrooms
INSERT INTO classrooms (id, room_number, course_name, course_code, start_time, end_time) VALUES
(1, 'A101', 'Computer Science Fundamentals', 'CS101', '09:00:00', '10:30:00'),
(2, 'B205', 'Data Structures and Algorithms', 'CS201', '11:00:00', '12:30:00'),
(3, 'C310', 'Database Management Systems', 'CS301', '14:00:00', '15:30:00');

-- Insert sample students (without face encodings for initial testing)
INSERT INTO students (id, student_id, first_name, last_name, email, department, created_at) VALUES
(1, 'STU001', 'John', 'Doe', 'john.doe@university.edu', 'Computer Science', CURRENT_TIMESTAMP),
(2, 'STU002', 'Jane', 'Smith', 'jane.smith@university.edu', 'Computer Science', CURRENT_TIMESTAMP),
(3, 'STU003', 'Michael', 'Johnson', 'michael.j@university.edu', 'Information Technology', CURRENT_TIMESTAMP),
(4, 'STU004', 'Emily', 'Davis', 'emily.davis@university.edu', 'Computer Science', CURRENT_TIMESTAMP),
(5, 'STU005', 'Robert', 'Brown', 'robert.b@university.edu', 'Software Engineering', CURRENT_TIMESTAMP);

-- Reset keys to prevent collisions with auto-generated IDs
-- Reset keys to prevent collisions (Dynamic Reset)
ALTER TABLE classrooms ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM classrooms);
ALTER TABLE students ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM students);

-- Insert default admin user
-- Username: admin, Password: password
INSERT INTO users (username, password, role) VALUES ('admin', '$2a$10$zCbLmRKFF4EQlwvQgi/iceecs3d9QsFQdHmm1rXo8/hKjV6SBtIabe', 'ADMIN');

-- Helper user as requested (id/password)
INSERT INTO users (username, password, role) VALUES ('id', '$2a$10$zCbLmRKFF4EQlwvQgi/iceecs3d9QsFQdHmm1rXo8/hKjV6SBtIabe', 'ADMIN');

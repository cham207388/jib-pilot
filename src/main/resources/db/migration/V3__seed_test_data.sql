-- Seed useful development/test data for local API exploration.
-- All seeded users use the password: Password123!

WITH seeded_courses(code, title, description) AS (
    VALUES
        ('CS101', 'Introduction to Computer Science', 'Foundations of programming, algorithms, data structures, and computational thinking.'),
        ('CS205', 'Data Structures and Algorithms', 'Lists, trees, graphs, hashing, sorting, searching, and algorithmic analysis.'),
        ('CS310', 'Database Systems', 'Relational modeling, SQL, indexing, transactions, query planning, and database application design.'),
        ('CS330', 'Web Application Development', 'Server-side APIs, frontend integration, HTTP, security basics, and deployment workflows.'),
        ('CS415', 'Cloud Native Engineering', 'Containers, orchestration, observability, CI/CD, and resilient service design.'),
        ('MATH201', 'Discrete Mathematics', 'Logic, proof techniques, sets, relations, combinatorics, and graph theory for computing.'),
        ('MATH240', 'Applied Statistics', 'Probability, statistical inference, regression, experiments, and data interpretation.'),
        ('ENG150', 'Technical Writing', 'Clear technical communication, documentation, API references, reports, and review practices.'),
        ('BUS210', 'Product Management Fundamentals', 'Customer discovery, prioritization, roadmap planning, metrics, and delivery tradeoffs.'),
        ('DS220', 'Data Analytics with SQL', 'Analytical SQL, joins, aggregations, window functions, dashboards, and business reporting.'),
        ('SEC260', 'Application Security', 'Authentication, authorization, secure coding, threat modeling, and vulnerability mitigation.'),
        ('AI340', 'Machine Learning Foundations', 'Supervised learning, model evaluation, feature engineering, and responsible ML practices.')
),
upserted_courses AS (
    INSERT INTO courses (code, title, description)
    SELECT code, title, description
    FROM seeded_courses
    ON CONFLICT (code) DO UPDATE
        SET title = EXCLUDED.title,
            description = EXCLUDED.description
    RETURNING id, code
),
seeded_students(first_name, last_name, email) AS (
    VALUES
        ('Avery', 'Johnson', 'avery.johnson@example.test'),
        ('Mia', 'Patel', 'mia.patel@example.test'),
        ('Noah', 'Kim', 'noah.kim@example.test'),
        ('Sophia', 'Garcia', 'sophia.garcia@example.test'),
        ('Liam', 'Nguyen', 'liam.nguyen@example.test'),
        ('Emma', 'Brown', 'emma.brown@example.test'),
        ('Ethan', 'Davis', 'ethan.davis@example.test'),
        ('Olivia', 'Martinez', 'olivia.martinez@example.test'),
        ('Lucas', 'Wilson', 'lucas.wilson@example.test'),
        ('Isabella', 'Anderson', 'isabella.anderson@example.test'),
        ('Mason', 'Thomas', 'mason.thomas@example.test'),
        ('Charlotte', 'Taylor', 'charlotte.taylor@example.test'),
        ('Logan', 'Moore', 'logan.moore@example.test'),
        ('Amelia', 'Jackson', 'amelia.jackson@example.test'),
        ('James', 'White', 'james.white@example.test'),
        ('Harper', 'Harris', 'harper.harris@example.test'),
        ('Benjamin', 'Clark', 'benjamin.clark@example.test'),
        ('Evelyn', 'Lewis', 'evelyn.lewis@example.test'),
        ('Elijah', 'Walker', 'elijah.walker@example.test'),
        ('Abigail', 'Hall', 'abigail.hall@example.test')
),
upserted_students AS (
    INSERT INTO students (first_name, last_name, email)
    SELECT first_name, last_name, email
    FROM seeded_students
    ON CONFLICT (email) DO UPDATE
        SET first_name = EXCLUDED.first_name,
            last_name = EXCLUDED.last_name
    RETURNING id, email
),
student_users(email, student_email) AS (
    VALUES
        ('avery.johnson@example.test', 'avery.johnson@example.test'),
        ('mia.patel@example.test', 'mia.patel@example.test'),
        ('noah.kim@example.test', 'noah.kim@example.test'),
        ('sophia.garcia@example.test', 'sophia.garcia@example.test'),
        ('liam.nguyen@example.test', 'liam.nguyen@example.test'),
        ('emma.brown@example.test', 'emma.brown@example.test'),
        ('ethan.davis@example.test', 'ethan.davis@example.test'),
        ('olivia.martinez@example.test', 'olivia.martinez@example.test'),
        ('lucas.wilson@example.test', 'lucas.wilson@example.test'),
        ('isabella.anderson@example.test', 'isabella.anderson@example.test'),
        ('mason.thomas@example.test', 'mason.thomas@example.test'),
        ('charlotte.taylor@example.test', 'charlotte.taylor@example.test'),
        ('logan.moore@example.test', 'logan.moore@example.test'),
        ('amelia.jackson@example.test', 'amelia.jackson@example.test'),
        ('james.white@example.test', 'james.white@example.test'),
        ('harper.harris@example.test', 'harper.harris@example.test'),
        ('benjamin.clark@example.test', 'benjamin.clark@example.test'),
        ('evelyn.lewis@example.test', 'evelyn.lewis@example.test'),
        ('elijah.walker@example.test', 'elijah.walker@example.test'),
        ('abigail.hall@example.test', 'abigail.hall@example.test')
),
upserted_student_users AS (
    INSERT INTO users (email, password, role, student_id)
    SELECT
        student_users.email,
        '$2a$10$d5DjmqP77RrJXikPe1iJb..vatf/ArKM8YngDO9I4JPpso5qa16HC',
        'STUDENT',
        upserted_students.id
    FROM student_users
    JOIN upserted_students ON upserted_students.email = student_users.student_email
    ON CONFLICT (email) DO UPDATE
        SET password = EXCLUDED.password,
            role = EXCLUDED.role,
            student_id = EXCLUDED.student_id
    RETURNING id
)
INSERT INTO users (email, password, role)
VALUES ('test-admin@example.test', '$2a$10$d5DjmqP77RrJXikPe1iJb..vatf/ArKM8YngDO9I4JPpso5qa16HC', 'ADMIN')
ON CONFLICT (email) DO UPDATE
    SET password = EXCLUDED.password,
        role = EXCLUDED.role;

WITH enrollments(student_email, course_code) AS (
    VALUES
        ('avery.johnson@example.test', 'CS101'),
        ('avery.johnson@example.test', 'MATH201'),
        ('avery.johnson@example.test', 'ENG150'),
        ('mia.patel@example.test', 'CS101'),
        ('mia.patel@example.test', 'DS220'),
        ('mia.patel@example.test', 'MATH240'),
        ('noah.kim@example.test', 'CS205'),
        ('noah.kim@example.test', 'CS310'),
        ('noah.kim@example.test', 'SEC260'),
        ('sophia.garcia@example.test', 'CS330'),
        ('sophia.garcia@example.test', 'BUS210'),
        ('sophia.garcia@example.test', 'ENG150'),
        ('liam.nguyen@example.test', 'CS205'),
        ('liam.nguyen@example.test', 'CS415'),
        ('liam.nguyen@example.test', 'SEC260'),
        ('emma.brown@example.test', 'CS101'),
        ('emma.brown@example.test', 'AI340'),
        ('emma.brown@example.test', 'MATH240'),
        ('ethan.davis@example.test', 'CS310'),
        ('ethan.davis@example.test', 'DS220'),
        ('ethan.davis@example.test', 'BUS210'),
        ('olivia.martinez@example.test', 'CS330'),
        ('olivia.martinez@example.test', 'AI340'),
        ('olivia.martinez@example.test', 'ENG150'),
        ('lucas.wilson@example.test', 'CS415'),
        ('lucas.wilson@example.test', 'SEC260'),
        ('lucas.wilson@example.test', 'MATH201'),
        ('isabella.anderson@example.test', 'CS101'),
        ('isabella.anderson@example.test', 'CS310'),
        ('isabella.anderson@example.test', 'DS220'),
        ('mason.thomas@example.test', 'CS205'),
        ('mason.thomas@example.test', 'CS330'),
        ('mason.thomas@example.test', 'MATH201'),
        ('charlotte.taylor@example.test', 'AI340'),
        ('charlotte.taylor@example.test', 'MATH240'),
        ('charlotte.taylor@example.test', 'BUS210'),
        ('logan.moore@example.test', 'CS415'),
        ('logan.moore@example.test', 'CS310'),
        ('logan.moore@example.test', 'ENG150'),
        ('amelia.jackson@example.test', 'CS101'),
        ('amelia.jackson@example.test', 'SEC260'),
        ('amelia.jackson@example.test', 'BUS210'),
        ('james.white@example.test', 'CS205'),
        ('james.white@example.test', 'DS220'),
        ('james.white@example.test', 'AI340'),
        ('harper.harris@example.test', 'CS330'),
        ('harper.harris@example.test', 'CS415'),
        ('harper.harris@example.test', 'MATH240'),
        ('benjamin.clark@example.test', 'CS310'),
        ('benjamin.clark@example.test', 'SEC260'),
        ('benjamin.clark@example.test', 'MATH201'),
        ('evelyn.lewis@example.test', 'CS101'),
        ('evelyn.lewis@example.test', 'CS330'),
        ('evelyn.lewis@example.test', 'ENG150'),
        ('elijah.walker@example.test', 'CS205'),
        ('elijah.walker@example.test', 'CS415'),
        ('elijah.walker@example.test', 'BUS210'),
        ('abigail.hall@example.test', 'AI340'),
        ('abigail.hall@example.test', 'DS220'),
        ('abigail.hall@example.test', 'MATH240')
)
INSERT INTO student_courses (student_id, course_id)
SELECT students.id, courses.id
FROM enrollments
JOIN students ON students.email = enrollments.student_email
JOIN courses ON courses.code = enrollments.course_code
ON CONFLICT (student_id, course_id) DO NOTHING;

INSERT INTO system_role (name) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_DEV');

INSERT INTO reviewer_role (name) VALUES
    ('ADVISOR'),
    ('JURY');

INSERT INTO reviewer (email, first_name, last_name) VALUES
    ('john.doe@usj.edu.lb', 'John', 'Doe'),
    ('jane.smith@usj.edu.lb', 'Jane', 'Smith'),
    ('michael.johnson@usj.edu.lb', 'Michael', 'Johnson'),
    ('emily.clark@usj.edu.lb', 'Emily', 'Clark'),
    -- For testing
    ('backend_service@service.com','Backend','Service');

INSERT INTO user_system_role (reviewer_id, system_role_id) VALUES
    ((SELECT id FROM reviewer WHERE email = 'backend_service@service.com'), (SELECT id FROM system_role WHERE name = 'ROLE_ADMIN')),
    ((SELECT id FROM reviewer WHERE email = 'backend_service@service.com'), (SELECT id FROM system_role WHERE name = 'ROLE_DEV'));

INSERT INTO team (name) VALUES
    ('Team Alpha'),
    ('Team Beta'),
    ('Team Gamma'),
    ('Team Delta');

INSERT INTO team_reviewer (team_id, reviewer_id) VALUES
    -- Team Alpha, John Doe
    ((SELECT id FROM team WHERE name = 'Team Alpha'), (SELECT id FROM reviewer WHERE email = 'john.doe@usj.edu.lb')),
    -- Team Alpha, Jane Smith
    ((SELECT id FROM team WHERE name = 'Team Alpha'), (SELECT id FROM reviewer WHERE email = 'jane.smith@usj.edu.lb')),
    -- Team Alpha, Michael Johnson
    ((SELECT id FROM team WHERE name = 'Team Alpha'), (SELECT id FROM reviewer WHERE email = 'michael.johnson@usj.edu.lb')),
    -- Team Beta, John Doe
    ((SELECT id FROM team WHERE name = 'Team Beta'), (SELECT id FROM reviewer WHERE email = 'john.doe@usj.edu.lb')),
    -- Team Beta, Michael Johnson
    ((SELECT id FROM team WHERE name = 'Team Beta'), (SELECT id FROM reviewer WHERE email = 'michael.johnson@usj.edu.lb')),
    -- Team Gamma, Emily Clark
    ((SELECT id FROM team WHERE name = 'Team Gamma'), (SELECT id FROM reviewer WHERE email = 'emily.clark@usj.edu.lb')),
    -- Team Gamma, Jane Smith
    ((SELECT id FROM team WHERE name = 'Team Gamma'), (SELECT id FROM reviewer WHERE email = 'jane.smith@usj.edu.lb')),
    -- Team Gamma, Michael Johnson
    ((SELECT id FROM team WHERE name = 'Team Gamma'), (SELECT id FROM reviewer WHERE email = 'michael.johnson@usj.edu.lb')),
    -- Team Delta, Emily Clark
    ((SELECT id FROM team WHERE name = 'Team Delta'), (SELECT id FROM reviewer WHERE email = 'emily.clark@usj.edu.lb')),
    -- Team Delta, Michael Johnson
    ((SELECT id FROM team WHERE name = 'Team Delta'), (SELECT id FROM reviewer WHERE email = 'michael.johnson@usj.edu.lb'));

INSERT INTO team_reviewer_role (team_reviewer_id, reviewer_role_id) VALUES
   -- Team Alpha, John Doe, Advisor
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Alpha') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'john.doe@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'ADVISOR')),
   -- Team Alpha, Jane Smith, Jury
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Alpha') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'jane.smith@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'JURY')),
   -- Team Alpha, Michael Johnson, Jury
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Alpha') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'michael.johnson@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'JURY')),
   -- Team Beta, John Doe, Advisor
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Beta') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'john.doe@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'ADVISOR')),
   -- Team Beta, Michael Johnson, Jury
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Beta') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'michael.johnson@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'JURY')),
   -- Team Gamma, Emily Clark, Advisor
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Gamma') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'emily.clark@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'ADVISOR')),
   -- Team Gamma, Jane Smith, Jury
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Gamma') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'jane.smith@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'JURY')),
   -- Team Gamma, Michael Johnson, Jury
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Gamma') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'michael.johnson@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'JURY')),
   -- Team Delta, Emily Clark, Advisor
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Delta') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'emily.clark@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'ADVISOR')),
   -- Team Delta, Michael Johnson, Jury
   ((SELECT id FROM team_reviewer WHERE team_id = (SELECT id FROM team WHERE name = 'Team Delta') AND reviewer_id = (SELECT id FROM reviewer WHERE email = 'michael.johnson@usj.edu.lb')),
    (SELECT id FROM reviewer_role WHERE name = 'JURY'));

INSERT INTO assessment (name, reviewer_role_id, weight) VALUES
    ('PROPOSAL_PRESENTATION',   (SELECT id FROM reviewer_role WHERE name = 'JURY'),     15),
    ('PROGRESS_REPORT',         (SELECT id FROM reviewer_role WHERE name = 'JURY'),     0),
    ('ADVISOR_ASSESSMENT',      (SELECT id FROM reviewer_role WHERE name = 'ADVISOR'),  25),
    ('FINAL_REPORT',            (SELECT id FROM reviewer_role WHERE name = 'JURY'),     30),
    ('FINAL_PRESENTATION',      (SELECT id FROM reviewer_role WHERE name = 'JURY'),     30);

INSERT INTO team_assessment (team_id, assessment_id, grade) VALUES
    ((SELECT id FROM team WHERE name = 'Team Alpha'), (SELECT id FROM assessment WHERE name = 'PROPOSAL_PRESENTATION'), 12),
    ((SELECT id FROM team WHERE name = 'Team Alpha'), (SELECT id FROM assessment WHERE name = 'ADVISOR_ASSESSMENT'), 24),
    ((SELECT id FROM team WHERE name = 'Team Alpha'), (SELECT id FROM assessment WHERE name = 'FINAL_REPORT'), 28),
    ((SELECT id FROM team WHERE name = 'Team Alpha'), (SELECT id FROM assessment WHERE name = 'FINAL_PRESENTATION'), 29),

    ((SELECT id FROM team WHERE name = 'Team Beta'), (SELECT id FROM assessment WHERE name = 'PROPOSAL_PRESENTATION'), 13),
    ((SELECT id FROM team WHERE name = 'Team Beta'), (SELECT id FROM assessment WHERE name = 'ADVISOR_ASSESSMENT'), 23),
    ((SELECT id FROM team WHERE name = 'Team Beta'), (SELECT id FROM assessment WHERE name = 'FINAL_REPORT'), 29),
    ((SELECT id FROM team WHERE name = 'Team Beta'), (SELECT id FROM assessment WHERE name = 'FINAL_PRESENTATION'), 28),

    ((SELECT id FROM team WHERE name = 'Team Gamma'), (SELECT id FROM assessment WHERE name = 'PROPOSAL_PRESENTATION'), 15),
    ((SELECT id FROM team WHERE name = 'Team Gamma'), (SELECT id FROM assessment WHERE name = 'ADVISOR_ASSESSMENT'), 25),
    ((SELECT id FROM team WHERE name = 'Team Gamma'), (SELECT id FROM assessment WHERE name = 'FINAL_REPORT'), 30),
    ((SELECT id FROM team WHERE name = 'Team Gamma'), (SELECT id FROM assessment WHERE name = 'FINAL_PRESENTATION'), 30),

    ((SELECT id FROM team WHERE name = 'Team Delta'), (SELECT id FROM assessment WHERE name = 'PROPOSAL_PRESENTATION'), 14),
    ((SELECT id FROM team WHERE name = 'Team Delta'), (SELECT id FROM assessment WHERE name = 'ADVISOR_ASSESSMENT'), 22),
    ((SELECT id FROM team WHERE name = 'Team Delta'), (SELECT id FROM assessment WHERE name = 'FINAL_REPORT'), 28),
    ((SELECT id FROM team WHERE name = 'Team Delta'), (SELECT id FROM assessment WHERE name = 'FINAL_PRESENTATION'), 30);

UPDATE team
SET final_grade = (
    SELECT SUM(grade)
    FROM team_assessment
    WHERE team_assessment.team_id = team.id
);

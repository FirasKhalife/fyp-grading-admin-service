INSERT INTO public.role (id, name)
VALUES (1, 'ADMIN'), (2, 'JURY_MEMBER'), (3, 'ADVISOR')
ON CONFLICT(name) DO NOTHING;
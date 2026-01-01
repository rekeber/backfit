-- V3__Insert_sample_data.sql
-- Insert sample data for testing

-- Insert sample exercises
INSERT INTO exercises (name, category, duration, calories_burned, difficulty, description, image_url) VALUES
('Cardio HIIT', 'Cardio', 30, 350, 'Intermedio', 'Entrenamiento de alta intensidad que combina ejercicios cardiovasculares', null),
('Flexiones', 'Fuerza', 15, 120, 'Principiante', 'Ejercicio básico para fortalecer pecho, hombros y tríceps', null),
('Yoga Matutino', 'Flexibilidad', 45, 180, 'Principiante', 'Rutina de yoga para comenzar el día con energía', null),
('Sentadillas', 'Fuerza', 20, 150, 'Principiante', 'Ejercicio fundamental para fortalecer piernas y glúteos', null),
('Correr', 'Cardio', 60, 500, 'Intermedio', 'Carrera continua para mejorar resistencia cardiovascular', null),
('Plancha', 'Core', 10, 80, 'Intermedio', 'Ejercicio isométrico para fortalecer el core', null),
('Burpees', 'Cardio', 20, 300, 'Avanzado', 'Ejercicio de cuerpo completo de alta intensidad', null),
('Dominadas', 'Fuerza', 15, 140, 'Intermedio', 'Ejercicio para fortalecer espalda y bíceps', null),
('Zancadas', 'Fuerza', 25, 180, 'Principiante', 'Ejercicio para piernas y glúteos con movimiento funcional', null),
('Abdominales', 'Core', 15, 100, 'Principiante', 'Ejercicio básico para fortalecer músculos abdominales', null);

-- Insert muscle groups for exercises
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
(1, 'Corazón'), (1, 'Piernas'), (1, 'Core'),
(2, 'Pecho'), (2, 'Hombros'), (2, 'Tríceps'),
(3, 'Todo el cuerpo'),
(4, 'Cuádriceps'), (4, 'Glúteos'), (4, 'Core'),
(5, 'Piernas'), (5, 'Corazón'),
(6, 'Core'), (6, 'Hombros'),
(7, 'Todo el cuerpo'),
(8, 'Espalda'), (8, 'Bíceps'),
(9, 'Cuádriceps'), (9, 'Glúteos'), (9, 'Pantorrillas'),
(10, 'Abdominales'), (10, 'Core');

-- Insert sample foods
INSERT INTO foods (name, description, calories_per_100g, protein_per_100g, carbs_per_100g, fat_per_100g, fiber_per_100g, serving_size, is_healthy, verification_status, is_active) VALUES
('Avena', 'Cereal integral rico en fibra y proteínas', 389, 16.9, 66.3, 6.9, 10.6, '100g', true, 'VERIFIED', true),
('Pollo a la plancha', 'Pechuga de pollo sin piel cocida a la plancha', 165, 31.0, 0.0, 3.6, 0.0, '100g', true, 'VERIFIED', true),
('Brócoli', 'Verdura crucífera rica en vitaminas y minerales', 34, 2.8, 7.0, 0.4, 2.6, '100g', true, 'VERIFIED', true),
('Arroz integral', 'Cereal integral con alto contenido de fibra', 111, 2.6, 23.0, 0.9, 1.8, '100g', true, 'VERIFIED', true),
('Salmón', 'Pescado graso rico en omega-3', 208, 25.4, 0.0, 12.4, 0.0, '100g', true, 'VERIFIED', true),
('Plátano', 'Fruta tropical rica en potasio', 89, 1.1, 22.8, 0.3, 2.6, '1 unidad mediana', true, 'VERIFIED', true),
('Almendras', 'Frutos secos ricos en grasas saludables', 579, 21.2, 21.6, 49.9, 12.5, '30g', true, 'VERIFIED', true),
('Yogur griego', 'Lácteo fermentado alto en proteínas', 59, 10.0, 3.6, 0.4, 0.0, '100g', true, 'VERIFIED', true),
('Espinacas', 'Verdura de hoja verde rica en hierro', 23, 2.9, 3.6, 0.4, 2.2, '100g', true, 'VERIFIED', true),
('Quinoa', 'Pseudocereal completo en aminoácidos', 368, 14.1, 64.2, 6.1, 7.0, '100g', true, 'VERIFIED', true);

-- Insert food categories
INSERT INTO food_categories (food_id, category) VALUES
(1, 'cereales'), (1, 'desayuno'),
(2, 'proteína'), (2, 'carne'),
(3, 'verduras'), (3, 'vegetales'),
(4, 'cereales'), (4, 'carbohidratos'),
(5, 'proteína'), (5, 'pescado'),
(6, 'frutas'),
(7, 'frutos secos'), (7, 'grasas saludables'),
(8, 'lácteos'), (8, 'proteína'),
(9, 'verduras'), (9, 'vegetales'),
(10, 'cereales'), (10, 'proteína');

-- Insert sample achievements
INSERT INTO achievements (title, description, category, type, required_value, points) VALUES
('Primera semana', 'Completaste 7 días seguidos registrando actividades', 'Constancia', 'STREAK', 7, 50),
('Guerrero del fitness', 'Realizaste 20 entrenamientos', 'Ejercicio', 'MILESTONE', 20, 100),
('Nutricionista', 'Registraste 50 comidas', 'Nutrición', 'MILESTONE', 50, 75),
('Social', 'Hiciste 10 amigos', 'Comunidad', 'SOCIAL', 10, 60),
('Madrugador', 'Entrenaste antes de las 7 AM', 'Ejercicio', 'CHALLENGE', 1, 30),
('Centurión', 'Alcanzaste 100 puntos', 'Puntos', 'MILESTONE', 100, 25),
('Guerrero', 'Alcanzaste 500 puntos', 'Puntos', 'MILESTONE', 500, 100),
('Leyenda', 'Alcanzaste 1000 puntos', 'Puntos', 'MILESTONE', 1000, 200),
('Primera publicación', 'Creaste tu primer post', 'Social', 'MILESTONE', 1, 10),
('Comunicador activo', 'Creaste 10 publicaciones', 'Social', 'MILESTONE', 10, 50),
('Influencer', 'Creaste 50 publicaciones', 'Social', 'MILESTONE', 50, 150),
('Primer seguidor', 'Conseguiste tu primer seguidor', 'Social', 'SOCIAL', 1, 20),
('Popular', 'Tienes 10 seguidores', 'Social', 'SOCIAL', 10, 75),
('Estrella social', 'Tienes 50 seguidores', 'Social', 'SOCIAL', 50, 200);

-- Note: Users will be created through the registration process
-- Sample workout sessions, food entries, posts, etc. will be created when users interact with the app
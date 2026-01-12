-- Agregar alimentos de muestra para testing
INSERT INTO foods (name, calories_per_100g, protein_per_100g, carbs_per_100g, fat_per_100g, fiber_per_100g, created_at, updated_at) VALUES
-- Frutas
('Manzana', 52, 0.3, 14, 0.2, 2.4, NOW(), NOW()),
('Plátano', 89, 1.1, 23, 0.3, 2.6, NOW(), NOW()),
('Naranja', 47, 0.9, 12, 0.1, 2.4, NOW(), NOW()),
('Fresa', 32, 0.7, 8, 0.3, 2.0, NOW(), NOW()),
('Uva', 62, 0.6, 16, 0.2, 0.9, NOW(), NOW()),

-- Verduras
('Brócoli', 34, 2.8, 7, 0.4, 2.6, NOW(), NOW()),
('Zanahoria', 41, 0.9, 10, 0.2, 2.8, NOW(), NOW()),
('Espinaca', 23, 2.9, 4, 0.4, 2.2, NOW(), NOW()),
('Tomate', 18, 0.9, 4, 0.2, 1.2, NOW(), NOW()),
('Lechuga', 15, 1.4, 3, 0.2, 1.3, NOW(), NOW()),

-- Proteínas
('Pollo (pechuga)', 165, 31, 0, 3.6, 0, NOW(), NOW()),
('Salmón', 208, 20, 0, 13, 0, NOW(), NOW()),
('Huevo', 155, 13, 1.1, 11, 0, NOW(), NOW()),
('Atún en agua', 116, 26, 0, 0.8, 0, NOW(), NOW()),
('Carne de res (magra)', 250, 26, 0, 15, 0, NOW(), NOW()),

-- Carbohidratos
('Arroz blanco (cocido)', 130, 2.7, 28, 0.3, 0.4, NOW(), NOW()),
('Pan integral', 247, 13, 41, 4.2, 7, NOW(), NOW()),
('Pasta (cocida)', 131, 5, 25, 1.1, 1.8, NOW(), NOW()),
('Avena', 389, 17, 66, 7, 10.6, NOW(), NOW()),
('Quinoa (cocida)', 120, 4.4, 22, 1.9, 2.8, NOW(), NOW()),

-- Lácteos
('Leche descremada', 34, 3.4, 5, 0.1, 0, NOW(), NOW()),
('Yogur natural', 59, 10, 4, 0.4, 0, NOW(), NOW()),
('Queso cottage', 98, 11, 3.4, 4.3, 0, NOW(), NOW()),
('Queso cheddar', 402, 25, 1.3, 33, 0, NOW(), NOW()),

-- Frutos secos y semillas
('Almendras', 579, 21, 22, 50, 12.5, NOW(), NOW()),
('Nueces', 654, 15, 14, 65, 6.7, NOW(), NOW()),
('Semillas de chía', 486, 17, 42, 31, 34.4, NOW(), NOW()),
('Aguacate', 160, 2, 9, 15, 6.7, NOW(), NOW()),

-- Legumbres
('Frijoles negros (cocidos)', 132, 8.9, 23, 0.5, 8.7, NOW(), NOW()),
('Lentejas (cocidas)', 116, 9, 20, 0.4, 7.9, NOW(), NOW()),
('Garbanzos (cocidos)', 164, 8.9, 27, 2.6, 7.6, NOW(), NOW()),

-- Bebidas y otros
('Agua', 0, 0, 0, 0, 0, NOW(), NOW()),
('Café negro', 2, 0.3, 0, 0, 0, NOW(), NOW()),
('Té verde', 1, 0, 0, 0, 0, NOW(), NOW()),
('Aceite de oliva', 884, 0, 0, 100, 0, NOW(), NOW()),

-- Comidas preparadas comunes
('Pizza margarita', 266, 11, 33, 10, 2.3, NOW(), NOW()),
('Hamburguesa simple', 295, 17, 31, 12, 2.2, NOW(), NOW()),
('Ensalada César', 158, 3, 13, 11, 2.6, NOW(), NOW()),
('Sopa de pollo', 75, 6, 8, 2.5, 1.0, NOW(), NOW()),
('Sandwich de jamón', 233, 13, 27, 8, 2.1, NOW(), NOW());
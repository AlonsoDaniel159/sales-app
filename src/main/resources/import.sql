-- =====================================================
-- IMPORT.SQL - Datos iniciales para sales-app
-- Cada INSERT debe estar en UNA SOLA LÍNEA
-- =====================================================

-- ROLES
INSERT INTO role (id_role, name, enabled) VALUES (1, 'ROLE_ADMIN', true);
INSERT INTO role (id_role, name, enabled) VALUES (2, 'ROLE_USER', true);
INSERT INTO role (id_role, name, enabled) VALUES (3, 'ROLE_EDITOR', true);

-- CATEGORÍAS
INSERT INTO category (name, description, enabled) VALUES ('Electrónica', 'Productos electrónicos y tecnología', true);
INSERT INTO category (name, description, enabled) VALUES ('Ropa', 'Vestimenta y accesorios', true);
INSERT INTO category (name, description, enabled) VALUES ('Alimentos', 'Productos alimenticios y bebidas', true);
INSERT INTO category (name, description, enabled) VALUES ('Hogar', 'Artículos para el hogar y decoración', true);
INSERT INTO category (name, description, enabled) VALUES ('Deportes', 'Equipamiento y ropa deportiva', true);

-- PRODUCTOS
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (1, 'Laptop HP 15', 'Laptop HP 15 pulgadas, 8GB RAM, 256GB SSD', 899.99, 15, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (1, 'Mouse Logitech', 'Mouse inalámbrico Logitech MX Master 3', 99.99, 50, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (1, 'Teclado Mecánico', 'Teclado mecánico RGB retroiluminado', 79.99, 30, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (1, 'Monitor LG 24"', 'Monitor LG 24 pulgadas Full HD', 199.99, 20, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (2, 'Camiseta Nike', 'Camiseta deportiva Nike Dri-Fit', 29.99, 100, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (2, 'Jeans Levis', 'Jeans Levis 501 azul clásico', 79.99, 60, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (2, 'Zapatillas Adidas', 'Zapatillas Adidas Ultraboost 22', 149.99, 40, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (3, 'Café Colombiano', 'Café molido 100% colombiano 500g', 12.99, 200, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (3, 'Aceite de Oliva', 'Aceite de oliva extra virgen 1L', 15.99, 80, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (3, 'Arroz Integral', 'Arroz integral 1kg', 3.99, 150, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (4, 'Lámpara LED', 'Lámpara de escritorio LED regulable', 34.99, 45, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (4, 'Juego de Sábanas', 'Juego de sábanas queen size 100% algodón', 49.99, 35, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (5, 'Pelota de Fútbol', 'Pelota de fútbol profesional Nike', 39.99, 70, true);
INSERT INTO product (id_category, name, description, price, stock, enabled) VALUES (5, 'Pesas 10kg', 'Par de pesas de 10kg cada una', 89.99, 25, true);

-- PROVEEDORES
INSERT INTO provider (name, address, enabled) VALUES ('TechSupply SA', 'Av. Tecnología 123, Lima', true);
INSERT INTO provider (name, address, enabled) VALUES ('Distribuidora Ropa Total', 'Jr. Comercio 456, Arequipa', true);
INSERT INTO provider (name, address, enabled) VALUES ('Alimentos Premium SAC', 'Av. Industrial 789, Trujillo', true);
INSERT INTO provider (name, address, enabled) VALUES ('Importaciones Hogar', 'Calle Mayorista 321, Cusco', true);
INSERT INTO provider (name, address, enabled) VALUES ('Deportes Globales', 'Av. Deportiva 654, Chiclayo', true);

-- USUARIOS
INSERT INTO user_data (id_role, username, password, enabled) VALUES (2, 'admin', '$2a$12$UDsdJckhfMsfsFh/tXB/Zu96HnZX66i3FoudQV9bsKshHdVoPXozi', true);
INSERT INTO user_data (id_role, username, password, enabled) VALUES (1, 'juan.perez', '$2a$12$UDsdJckhfMsfsFh/tXB/Zu96HnZX66i3FoudQV9bsKshHdVoPXozi', true);
INSERT INTO user_data (id_role, username, password, enabled) VALUES (1, 'maria.garcia', '$2a$12$UDsdJckhfMsfsFh/tXB/Zu96HnZX66i3FoudQV9bsKshHdVoPXozi', true);
INSERT INTO user_data (id_role, username, password, enabled) VALUES (3, 'carlos.editor', '$2a$12$UDsdJckhfMsfsFh/tXB/Zu96HnZX66i3FoudQV9bsKshHdVoPXozi', true);
INSERT INTO user_data (id_role, username, password, enabled) VALUES (1, 'ana.lopez', '$2a$12$UDsdJckhfMsfsFh/tXB/Zu96HnZX66i3FoudQV9bsKshHdVoPXozi', true);

-- CLIENTES
INSERT INTO client (first_name, last_name, card_id, phone_number, email, address) VALUES ('Roberto', 'Martínez', '12345678', '987654321', 'roberto.martinez@email.com', 'Av. Principal 100, Lima');
INSERT INTO client (first_name, last_name, card_id, phone_number, email, address) VALUES ('Lucía', 'Fernández', '23456789', '987654322', 'lucia.fernandez@email.com', 'Jr. Libertad 200, Arequipa');
INSERT INTO client (first_name, last_name, card_id, phone_number, email, address) VALUES ('Diego', 'Sánchez', '34567890', '987654323', 'diego.sanchez@email.com', 'Calle Real 300, Cusco');
INSERT INTO client (first_name, last_name, card_id, phone_number, email, address) VALUES ('Carmen', 'Torres', '45678901', '987654324', 'carmen.torres@email.com', 'Av. Central 400, Trujillo');
INSERT INTO client (first_name, last_name, card_id, phone_number, email, address) VALUES ('Miguel', 'Ramírez', '56789012', '987654325', 'miguel.ramirez@email.com', 'Jr. Comercio 500, Chiclayo');
INSERT INTO client (first_name, last_name, card_id, phone_number, email, address) VALUES ('Patricia', 'Flores', '67890123', '987654326', 'patricia.flores@email.com', 'Av. Los Pinos 600, Piura');
INSERT INTO client (first_name, last_name, card_id, phone_number, email, address) VALUES ('Javier', 'Vega', '78901234', '987654327', 'javier.vega@email.com', 'Calle Sol 700, Ica');
INSERT INTO client (first_name, last_name, card_id, phone_number, email, address) VALUES ('Andrea', 'Castro', '89012345', '987654328', 'andrea.castro@email.com', 'Av. Norte 800, Tacna');
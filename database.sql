-- 1. Crear la base de datos desde PostgreSQL/pgAdmin.
CREATE DATABASE tienda_javafx;

-- 2. Conectarse a tienda_javafx y ejecutar lo siguiente.
CREATE TABLE categoria (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE producto (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    categoria_id INTEGER NOT NULL,
    precio_venta NUMERIC(12,2) NOT NULL,
    existencia INTEGER NOT NULL DEFAULT 0,
    ruta_imagen VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categoria(id)
);

-- Datos iniciales para probar ComboBox y TableView.
INSERT INTO categoria (nombre, activa) VALUES
('Bebidas', TRUE),
('Alimentos', TRUE),
('Limpieza', TRUE)
ON CONFLICT DO NOTHING;

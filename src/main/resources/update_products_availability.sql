-- Script para actualizar productos existentes y marcarlos como disponibles
-- Ejecutar este script una sola vez después de implementar los cambios

UPDATE product 
SET available = true 
WHERE available IS NULL OR available = false; 
-- Modificar la tabla ad para cambiar el tipo de columna imageUrl a TEXT
ALTER TABLE ad MODIFY COLUMN image_url TEXT;

-- También modificar la columna description para que sea TEXT
ALTER TABLE ad MODIFY COLUMN description TEXT; 
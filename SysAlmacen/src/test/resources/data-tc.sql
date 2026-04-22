-- Datos mínimos para que los tests de integración funcionen.
-- Los roles son necesarios para el registro de usuarios (UsuarioServiceImp.register).
INSERT IGNORE INTO upeu_roles (id_rol, descripcion, nombre) VALUES
    (1, 'Administrador', 'ADMIN'),
    (2, 'Admin DBA', 'DBA'),
    (3, 'Usuario', 'USER');

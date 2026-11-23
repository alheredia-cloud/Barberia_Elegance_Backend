package cl.duoc.barberia.model;

/** Roles del sistema segun Anexo 1. */
public enum Rol {
    /** Acceso total al sistema. */
    ADMIN,
    /** Solo visualiza productos y ordenes (boletas). */
    VENDEDOR,
    /** Solo accede a la tienda. */
    CLIENTE
}

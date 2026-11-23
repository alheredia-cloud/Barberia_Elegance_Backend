package cl.duoc.barberia.exception;

import org.springframework.http.HttpStatus;

/** Violacion de una regla de negocio (stock insuficiente, email duplicado, hora ocupada, etc.). */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String mensaje) {
        this(mensaje, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

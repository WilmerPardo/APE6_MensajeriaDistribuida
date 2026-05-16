package ec.unl.flota.exception;

/**
 * Excepción lanzada cuando ocurre un error al procesar un mensaje de RabbitMQ.
 * Permite registrar el error sin detener la aplicación completa.
 */
public class MessageProcessingException extends RuntimeException {

    public MessageProcessingException(String message) {
        super(message);
    }

    public MessageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

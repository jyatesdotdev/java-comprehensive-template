package com.example.template.restfulapi.exception;

/** Thrown when a client operation fails for non-not-found reasons. */
public class ClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the given detail message.
     *
     * @param message description of the client error
     */
    public ClientException(String message) {
        super(message);
    }
}

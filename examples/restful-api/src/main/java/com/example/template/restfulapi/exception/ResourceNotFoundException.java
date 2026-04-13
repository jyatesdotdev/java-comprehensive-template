package com.example.template.restfulapi.exception;

/** Thrown when a requested resource is not found. */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the given detail message.
     *
     * @param message description of the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

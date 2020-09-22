package com.exception;

/**
 * @author Dominic
 *         <p>
 *         Website: www.dominicheal.com
 *         <p>
 *         Github: www.github.com/DomHeal
 * @since 23-Oct-16
 */
public class DuplicateUsernameException extends Exception {

    private static final long serialVersionUID = -5642763982171042219L;

    public DuplicateUsernameException(String message) {
        super(message);
    }
}

package com.github.myth.core.exception;

import com.github.myth.core.spi.CoordinatorRepository;

/**
 * @author tony
 * @date 2017-12-25 11:14:42
 */
public class RepositoryException extends RuntimeException {

    private CoordinatorRepository repository;

    public RepositoryException(String message, CoordinatorRepository repository) {
        super(message);
        this.repository = repository;
    }

    public RepositoryException(String message, Throwable cause, CoordinatorRepository repository) {
        super(message, cause);
        this.repository = repository;
    }

    public RepositoryException(Throwable cause, CoordinatorRepository repository) {
        super(cause);
        this.repository = repository;
    }
}

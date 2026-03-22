package com.gameguessr.game.application.rest;

import com.gameguessr.game.domain.exception.DuplicateGuessException;
import com.gameguessr.game.domain.exception.InvalidPhaseException;
import com.gameguessr.game.domain.exception.MatchAlreadyStartedException;
import com.gameguessr.game.domain.exception.MatchNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Global exception handler — maps domain exceptions to RFC 7807 ProblemDetail
 * responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MatchNotFoundException.class)
    public ProblemDetail handleMatchNotFound(MatchNotFoundException ex) {
        log.warn("Match not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://gameguessr.com/errors/match-not-found"));
        problem.setTitle("Match Not Found");
        return problem;
    }

    @ExceptionHandler(MatchAlreadyStartedException.class)
    public ProblemDetail handleMatchAlreadyStarted(MatchAlreadyStartedException ex) {
        log.warn("Match already started: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://gameguessr.com/errors/match-already-started"));
        problem.setTitle("Match Already Started");
        return problem;
    }

    @ExceptionHandler(DuplicateGuessException.class)
    public ProblemDetail handleDuplicateGuess(DuplicateGuessException ex) {
        log.warn("Duplicate guess: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://gameguessr.com/errors/duplicate-guess"));
        problem.setTitle("Duplicate Guess");
        return problem;
    }

    @ExceptionHandler(InvalidPhaseException.class)
    public ProblemDetail handleInvalidPhase(InvalidPhaseException ex) {
        log.warn("Invalid phase: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("https://gameguessr.com/errors/invalid-phase"));
        problem.setTitle("Invalid Phase");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(URI.create("https://gameguessr.com/errors/validation"));
        problem.setTitle("Validation Error");
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflict");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again.");
        problem.setTitle("Internal Server Error");
        return problem;
    }
}

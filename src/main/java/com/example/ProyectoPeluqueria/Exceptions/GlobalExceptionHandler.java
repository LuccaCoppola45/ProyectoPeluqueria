package com.example.ProyectoPeluqueria.Exceptions;

import com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.*;
import com.example.ProyectoPeluqueria.Exceptions.auth.*;
import com.example.ProyectoPeluqueria.Exceptions.cancelacion.CancelacionNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioAlreadyExistsException;
import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioInvalidDataException;
import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.horarioAtencion.*;
import com.example.ProyectoPeluqueria.Exceptions.servicio.ServicioNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.user.UserProfileNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.user.UnauthorizedUserAccessException;
import com.example.ProyectoPeluqueria.Exceptions.user.UserEmailAlreadyInUseException;
import com.example.ProyectoPeluqueria.Exceptions.user.UserHasAssociatedTurnosException;
import com.example.ProyectoPeluqueria.Exceptions.user.UserWithoutChangesException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fieldError
                    ? fieldError.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        return buildErrorResponse("La solicitud contiene datos inválidos", request,
                HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        return buildErrorResponse("La solicitud contiene datos inválidos", request,
                HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildErrorResponse("El cuerpo de la solicitud no tiene un formato válido",
                request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildErrorResponse("El parámetro '" + ex.getName() + "' no tiene un formato válido",
                request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        return buildErrorResponse("Credenciales inválidas", request, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return buildErrorResponse("No tienes permisos para realizar esta operación",
                request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TurnoDuplicadoException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleDuplicateTurnException(
            TurnoDuplicadoException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value()) // 409
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ComercioAlreadyExistsException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerComercioAlreadyExistsException(ComercioAlreadyExistsException ex, HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HorarioFueraDeAtencionException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerHorarioFueraDeAtencionException(
            HorarioFueraDeAtencionException ex,
            HttpServletRequest request)
    {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HorarioInvalidoException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerHorarioInvalidoException(
            HorarioInvalidoException ex,
            HttpServletRequest request)
    {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HorarioSuperpuestoException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerHorarioSuperpuestoException(
            HorarioSuperpuestoException ex,
            HttpServletRequest request)
    {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value()) // 409
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(HorarioFueraDeComercioException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerHorarioFueraDeComercioException(
            HorarioFueraDeComercioException ex,
            HttpServletRequest request)
    {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HorarioSinReferenciaException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerHorarioSinReferenciaException(
            HorarioSinReferenciaException ex,
            HttpServletRequest request)
    {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity <@NonNull ErrorResponse> handleAccountLockedException(AccountLockedException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotValidateException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleAccountNotValidateException(AccountNotValidateException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordMissMatchException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlePasswordMissMatchException(PasswordMissMatchException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerRefreshTokenExpiredException(RefreshTokenExpiredException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerRefreshTokenNotFoundException(RefreshTokenNotFoundException ex, HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VerificationTokenInvalidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerVerificationTokenInvalidException(VerificationTokenInvalidException ex, HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WrongCredentialsException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerWrongCredentialsException(WrongCredentialsException ex, HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ServicioNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerServicioNotFound(ServicioNotFoundException ex, HttpServletRequest request){
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ComercioInvalidDataException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerComercioInvalidDataException(ComercioInvalidDataException ex, HttpServletRequest requestH){
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(requestH.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ComercioNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerComercioNotFoundException(ComercioNotFoundException ex, HttpServletRequest requestH){
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(requestH.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TurnFinishedException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTurnFinishedException(
            TurnFinishedException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourcenotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handlerecourcenotfound(
            ResourcenotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CancelacionNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleCancelacionNotFoundException(CancelacionNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value()) // 404
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TurnoAlreadyCancelledException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTurnoAlreadyCancelledException(TurnoAlreadyCancelledException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TurnoNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTurnoNotFoundException(TurnoNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value()) // 404
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TurnoOutsideBusinessException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTurnoOutsideBusinessHoursException(TurnoOutsideBusinessException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TurnoPastDateException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleTurnoPastDateException(TurnoPastDateException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedTurnoAccessException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUnauthorizedTurnoAccessException(UnauthorizedTurnoAccessException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value()) // 403
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUserProfileNotFoundException(
            UserProfileNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserEmailAlreadyInUseException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUserEmailAlreadyInUseException(
            UserEmailAlreadyInUseException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserHasAssociatedTurnosException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUserHasAssociatedTurnosException(
            UserHasAssociatedTurnosException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedUserAccessException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUnauthorizedUserAccessException(
            UnauthorizedUserAccessException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserWithoutChangesException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleUserWithoutChangesException(
            UserWithoutChangesException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<@NonNull ErrorResponse> buildErrorResponse(
            String message,
            HttpServletRequest request,
            HttpStatus status) {
        return buildErrorResponse(message, request, status, null);
    }

    private ResponseEntity<@NonNull ErrorResponse> buildErrorResponse(
            String message,
            HttpServletRequest request,
            HttpStatus status,
            Map<String, String> errors) {
        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
        return new ResponseEntity<>(error, status);
    }



}

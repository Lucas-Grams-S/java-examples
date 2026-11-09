package dev.estudos.boot.web;

import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * SB04-12 · Erros de validação com os campos. Fica no perfil {@code campos}: ao registrar um
 * {@code ResponseEntityExceptionHandler} próprio, o Boot recua do seu {@code ProblemDetailsExceptionHandler}
 * (por isso o comportamento padrão de SB04-11 e este não convivem na mesma aplicação).
 * Documento: 04-web-mvc.md §5.3.
 *
 * <p>Saída real ({@code POST /orders {"customerId":"","total":-1}}, perfil {@code campos}):
 * <pre>
 * 400 application/problem+json
 * {"detail":"Dados inválidos","errors":{"customerId":"must not be blank","total":"must be greater than 0"},
 *  "instance":"/orders","status":400,"title":"Bad Request"}
 * </pre>
 */
@RestControllerAdvice
@Profile("campos")
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos");
        pd.setProperty("errors", ex.getFieldErrors().stream()
            .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a)));
        return handleExceptionInternal(ex, pd, headers, status, request);
    }
}

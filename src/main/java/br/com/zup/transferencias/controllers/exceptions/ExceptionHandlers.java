package br.com.zup.transferencias.controllers.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ExceptionHandlers {

    Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                          WebRequest webRequest) {
        HttpStatus status = BAD_REQUEST;
        ErroPadrao erroPadrao = getErroPadrao(status, webRequest);

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        fieldErrors.forEach(erroPadrao::adicionar);

        logger.error("MethodArgumentNotValidException: " + ex.getLocalizedMessage(), ex);
        return ResponseEntity.status(status).body(erroPadrao);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex,
                                                  WebRequest webRequest) {
        HttpStatus status = ex.getStatus();
        ErroPadrao erroPadrao = getErroPadrao(status, webRequest);

        erroPadrao.adicionar(ex.getReason());

        logger.error("ResponseStatusException: " + ex.getReason(), ex);
        return ResponseEntity.status(status).body(erroPadrao);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleObjectOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex,
                                                                  WebRequest webRequest) {
        HttpStatus status = CONFLICT;
        ErroPadrao erroPadrao = getErroPadrao(status, webRequest);

        String mensagem = "Ocorreu um erro e a operação não foi completada. Por favor, tente novamente.";
        erroPadrao.adicionar(mensagem);

        logger.error("ObjectOptimisticLockingFailureException: " + mensagem, ex);
        return ResponseEntity.status(status).body(erroPadrao);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                          WebRequest webRequest) {
        HttpStatus status = UNPROCESSABLE_ENTITY;
        ErroPadrao erroPadrao = getErroPadrao(status, webRequest);

        String exceptionName = "DataIntegrityViolationException";
        String mensagem = "Houve um erro de violação integridade dos dados.";
        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException) {
            exceptionName = "ConstraintViolationException";
            String constraintNameUpper = ((ConstraintViolationException) cause).getConstraintName()
                                                                               .toUpperCase();

            if (constraintNameUpper.contains("UK_CONTA_EMAIL")) {
                mensagem = "Já existe uma conta cadastrada com o email informado.";
            } else if (constraintNameUpper.contains("UK_CONTA_HASH_CPF")) {
                mensagem = "Já existe uma conta cadastrada com o CPF informado.";
            } else {
                mensagem = "Houve um erro de violação de restrição.";
            }
        }
        erroPadrao.adicionar(mensagem);

        logger.error(exceptionName + ": " + mensagem, ex);
        return ResponseEntity.status(status).body(erroPadrao);
    }

    private ErroPadrao getErroPadrao(HttpStatus status, WebRequest webRequest) {
        Integer codigoHttp = status.value();
        String mensagemHttp = status.getReasonPhrase();
        String caminho = webRequest.getDescription(false).replace("uri=", "");

        return new ErroPadrao(codigoHttp, mensagemHttp, caminho);
    }

}

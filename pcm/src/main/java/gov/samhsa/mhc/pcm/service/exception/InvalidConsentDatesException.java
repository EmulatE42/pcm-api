package gov.samhsa.mhc.pcm.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidConsentDatesException extends RuntimeException {
    public InvalidConsentDatesException(String message) {
        super(message);
    }

    public InvalidConsentDatesException(String message, Throwable cause) {
        super(message, cause);
    }
}

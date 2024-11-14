package com.chat.exception

import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus


@Component
class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options.including(ErrorAttributeOptions.Include.MESSAGE))

        Throwable error = getError(request)
        if (error instanceof WebExchangeBindException) {
            return handleValidationException((WebExchangeBindException) error)
        } else if (error instanceof ResponseStatusException) {
            return handleResponseStatusException((ResponseStatusException) error)
        } else {
            errorAttributes.remove("trace")
            errorAttributes.remove("requestId")
            errorAttributes.remove("path")
        }

        return errorAttributes
    }

    private static Map<String, Object> handleValidationException(WebExchangeBindException ex) {
        Map<String, Object> errorAttributes = new HashMap<>()
        errorAttributes.put("status", HttpStatus.BAD_REQUEST.value())
        errorAttributes.put("error", "Validation failure")

        List<Map<String, String>> fieldErrors = new ArrayList<>()
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            Map<String, String> errorDetail = new HashMap<>()
            errorDetail.put("field", error.getField())
            errorDetail.put("message", error.getDefaultMessage())
            fieldErrors.add(errorDetail)
        })
        errorAttributes.put("fieldErrors", fieldErrors)
        return errorAttributes
    }


    private static Map<String, Object> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> errorAttributes = new HashMap<>()
        errorAttributes.put("status", ex.getStatusCode().value())
        errorAttributes.put("message", ex.getReason())
        return errorAttributes
    }

}





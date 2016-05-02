package com.mulesoft.commons.connectors.rest.builder.handler;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class DefaultXMLResponseHandler<T> implements ResponseHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultXMLResponseHandler.class);

    @Override
    public T handleResponse(final Response response, final Class<T> responseType) {
        Status status = Status.fromStatusCode(response.getStatus());
        Family family = status.getFamily();
        logger.debug("Response Status is {}", status);
        logger.trace("Response body:\n{}", response.readEntity(String.class));
        if (Family.SUCCESSFUL != family) {
            try {
                ResponseStatusExceptionMapper.valueOf(status.name()).throwException(response);
            } catch (IllegalArgumentException unmappedStatusException) {
                try {
                    ResponseStatusExceptionMapper.valueOf(family.name()).throwException(response);
                } catch (IllegalArgumentException unmappedFamilyException) {
                    throw new WebApplicationException(unmappedFamilyException, response);
                }
            }
        }

        // Parsing the successful response if necessary.
        return Optional.fromNullable(responseType).transform(new Function<Class<T>, T>() {

            @Override
            public T apply(Class<T> input) {
                logger.debug("Parsing response to an instance of {}", input.getSimpleName());
                return response.readEntity(responseType);
            }
        }).orNull();
    }
}
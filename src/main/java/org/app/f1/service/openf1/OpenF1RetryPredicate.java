package org.app.f1.service.openf1;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * Retries transient failures only: 5xx responses, connection errors and raw IO
 * failures. 4xx responses (e.g. 404 for a session OpenF1 doesn't know) are
 * definitive answers — retrying them just burns the rate-limit budget.
 */
public class OpenF1RetryPredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable t) {
        if (t instanceof RestClientResponseException e) {
            return !e.getStatusCode().is4xxClientError();
        }
        return t instanceof ResourceAccessException || t instanceof IOException;
    }
}

package com.neueda.interview.urlshortener.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.net.MalformedURLException;

public class UrlUtilTest {

    @Test
    public void shouldThrowExceptionWhenMalformedUrlSuppliedWithoutProtocol() {
        Assertions.assertThrows(MalformedURLException.class, () -> {
            UrlUtil.getBaseUrl("malformed url dummy text");
        });
    }

    @Test
    public void shouldThrowExceptionWhenMalformedUrlSuppliedWithIllegalChars() {
        Assertions.assertThrows(MalformedURLException.class, () -> {
            UrlUtil.getBaseUrl("malformed://example.com/foo");
        });
    }

    @Test
    public void shouldReturnBaseUrlWhenValidUrlSuppliedWithoutPort() throws MalformedURLException {
        Assertions.assertEquals("http://example.com/", UrlUtil.getBaseUrl("http://example.com/foo"));
    }

    @Test
    public void shouldReturnBaseUrlWhenValidUrlSuppliedWithPort() throws MalformedURLException {
        Assertions.assertEquals("http://example.com:8080/", UrlUtil.getBaseUrl("http://example.com:8080/foo"));
    }
}
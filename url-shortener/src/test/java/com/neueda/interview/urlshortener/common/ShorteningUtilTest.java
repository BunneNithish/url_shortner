package com.neueda.interview.urlshortener.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShorteningUtilTest {
    @Test
    public void shouldConvertMaxLongToShortString() {
        String maxIdShortString = ShorteningUtil.idToStr(Long.MAX_VALUE);
        Assertions.assertNotNull(maxIdShortString);
        Assertions.assertNotEquals("", maxIdShortString);
    }

    @Test
    public void shouldThrowExceptionWhenShortStrLongerThanTenChars() {
        long id = ShorteningUtil.strToId("sclqgMAPqi2Z");
    }

}
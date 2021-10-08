package no.ssb.maskinporten.guardian;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import no.ks.fiks.maskinporten.Maskinportenklient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;
import org.mockito.Mock;

@MicronautTest
class MaskinportenGuardianTest {

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Mock
    Maskinportenklient maskinportenklient;


    @Test
    void testGetAccesstoken(){

    }

}

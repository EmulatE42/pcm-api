package gov.samhsa.mhc.pcm.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sadhana.chandra on 3/4/2016.
 */
@Configuration
public class FhirServiceConfig {

    @Value("${mhc.pcm.config.fhir.serverUrl}")
    String fhirServerUrl;

    @Value("${mhc.pcm.config.fhir.fhirClientSocketTimeoutInMs}")
    String fhirClientSocketTimeout;

    // Create a context
    FhirContext fhirContext = FhirContext.forDstu2();;

    @Bean
    public FhirContext fhirContext(){
        if(fhirContext == null)
            fhirContext = FhirContext.forDstu2();
        return fhirContext;
    }

    @Bean
    public IGenericClient fhirClient() {
        // Create a client
        fhirContext.getRestfulClientFactory().setSocketTimeout(Integer.parseInt(fhirClientSocketTimeout));
        IGenericClient fhirClient = fhirContext.newRestfulGenericClient(fhirServerUrl);
        return fhirClient;
    }

    @Bean
    public IParser fhirXmlParser(){
        IParser fhirXmlParser = fhirContext.newXmlParser();
        return fhirXmlParser;
    }

    @Bean
    public IParser fhirJsonParser(){
        IParser fhirJsonParser = fhirContext.newJsonParser();
        return fhirJsonParser;
    }

    @Bean
    public FhirValidator fhirValidator(){
        FhirValidator fhirValidator = fhirContext.newValidator();
        return fhirValidator;
    }

/*    @Bean
    public FhirPaResourceConverter fhirResourceConverter(){
        FhirResourceConverter fhirResourceConverter = new FhirResourceConverter();
        return fhirResourceConverter;
    }*/
}

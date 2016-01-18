package gov.samhsa.bhits.pcm.web.di;

import gov.samhsa.bhits.pcm.service.consentexport.ConsentExportMapper;
import gov.samhsa.bhits.pcm.service.consentexport.ConsentExportMapperImpl;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsentExportMapperConfig {

    @Bean
    public ConsentExportMapper consentExportMapper(ModelMapper modelMapper) {
        return new ConsentExportMapperImpl(modelMapper);
    }
}

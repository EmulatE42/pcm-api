package gov.samhsa.mhc.pcm.web;

import gov.samhsa.mhc.pcm.service.consent.ConsentService;
import gov.samhsa.mhc.pcm.service.dto.XacmlDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class XacmlRestController {

    @Autowired
    private ConsentService consentService;

    @RequestMapping("/xacml/{consentId}")
    public XacmlDto getXacmlDto(@PathVariable("consentId") Long consentId){
        return consentService.findXACMLForCCDByConsentId(consentId);
    }
}

package gov.samhsa.c2s.pcm.service.consent;

import gov.samhsa.c2s.pcm.config.PcmProperties;
import gov.samhsa.c2s.pcm.domain.consent.ConsentRepository;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import gov.samhsa.c2s.common.util.UniqueValueGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * The Class PolicyIdServiceImpl.
 */
@Service
public class PolicyIdServiceImpl implements PolicyIdService {

    /**
     * The Constant RANDOM_STRING_LENGTH.
     */
    private static final int RANDOM_STRING_LENGTH = 6;

    @Autowired
    private PcmProperties pcmProperties;

    /**
     * The consent repository.
     */
    @Autowired
    private ConsentRepository consentRepository;

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.samhsa.consent2share.service.consent.PolicyIdService#generatePolicyId
     * (gov.samhsa.consent2share.service.dto.ConsentDto, java.lang.String)
     */
    @Override
    public String generatePolicyId(ConsentDto consentDto, String mrn) {
        final short iterationLimit = 3;
        return UniqueValueGenerator
                .generateUniqueValue(() -> generateRandomPolicyId(consentDto, mrn),
                        generatedValue -> consentRepository.findAllByConsentReferenceId(generatedValue)
                                .size() == 0, iterationLimit);
    }

    /**
     * Generate random policy id.
     *
     * @param consentDto the consent dto
     * @param mrn        the mrn
     * @return the string
     */
    private String generateRandomPolicyId(ConsentDto consentDto, String mrn) {
        Assert.hasText(mrn, "The patient must have an local c2s identifier.");
        StringBuilder consentReferenceIdBuilder = new StringBuilder();
        consentReferenceIdBuilder.append(mrn);
        consentReferenceIdBuilder.append(":&");
        consentReferenceIdBuilder.append(this.pcmProperties.getPid().getDomain().getId());
        consentReferenceIdBuilder.append("&");
        consentReferenceIdBuilder.append(this.pcmProperties.getPid().getDomain().getType());

        consentReferenceIdBuilder.append(":");
        if (consentDto.getOrganizationalProvidersDisclosureIsMadeToNpi() != null) {
            if (!consentDto.getOrganizationalProvidersDisclosureIsMadeToNpi().isEmpty())
                consentReferenceIdBuilder
                        .append(consentDto
                                .getOrganizationalProvidersDisclosureIsMadeToNpi()
                                .toArray()[0]);
            else {
                consentReferenceIdBuilder.append(consentDto
                        .getProvidersDisclosureIsMadeToNpi().toArray()[0]);
            }
        } else {
            consentReferenceIdBuilder.append(consentDto
                    .getProvidersDisclosureIsMadeToNpi().toArray()[0]);
        }
        consentReferenceIdBuilder.append(":");
        if (consentDto.getOrganizationalProvidersPermittedToDiscloseNpi() != null) {
            if (!consentDto.getOrganizationalProvidersPermittedToDiscloseNpi().isEmpty()) {
                consentReferenceIdBuilder
                        .append(consentDto
                                .getOrganizationalProvidersPermittedToDiscloseNpi()
                                .toArray()[0]);
            } else {
                consentReferenceIdBuilder.append(consentDto
                        .getProvidersPermittedToDiscloseNpi().toArray()[0]);
            }
        } else {
            consentReferenceIdBuilder.append(consentDto
                    .getProvidersPermittedToDiscloseNpi().toArray()[0]);
        }
        consentReferenceIdBuilder.append(":");
        consentReferenceIdBuilder.append(RandomStringUtils
                .randomAlphanumeric((RANDOM_STRING_LENGTH)));
        return consentReferenceIdBuilder.toString().toUpperCase();
    }
}

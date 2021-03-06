package gov.samhsa.c2s.pcm.service.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.pcm.config.FHIRProperties;
import gov.samhsa.c2s.pcm.domain.consent.ConsentDoNotShareClinicalDocumentTypeCode;
import gov.samhsa.c2s.pcm.domain.consent.ConsentDoNotShareSensitivityPolicyCode;
import gov.samhsa.c2s.pcm.domain.consent.ConsentIndividualProviderDisclosureIsMadeTo;
import gov.samhsa.c2s.pcm.domain.consent.ConsentIndividualProviderPermittedToDisclose;
import gov.samhsa.c2s.pcm.domain.consent.ConsentOrganizationalProviderDisclosureIsMadeTo;
import gov.samhsa.c2s.pcm.domain.consent.ConsentOrganizationalProviderPermittedToDisclose;
import gov.samhsa.c2s.pcm.domain.consent.ConsentShareForPurposeOfUseCode;
import gov.samhsa.c2s.pcm.domain.provider.IndividualProvider;
import gov.samhsa.c2s.pcm.domain.provider.OrganizationalProvider;
import gov.samhsa.c2s.pcm.domain.reference.ClinicalConceptCode;
import gov.samhsa.c2s.pcm.domain.reference.PurposeOfUseCode;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import gov.samhsa.c2s.pcm.service.dto.SensitivePolicyCodeEnum;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.codesystems.V3ActReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
public class FhirConsentServiceImpl implements FhirConsentService {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FhirContext fhirContext;

    @Autowired
    private FhirValidator fhirValidator;

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private FhirPatientService fhirPatientService;

    @Autowired
    private UniqueOidProvider uniqueOidProvider;

    @Autowired
    private FHIRProperties fhirProperties;



    // FHIR resource identifiers for inline/embedded objects
    private String CONFIDENTIALITY_CODE_CODE_SYSTEM = "urn:oid:2.16.840.1.113883.5.25";
    private String CODE_SYSTEM_SET_OPERATOR = "http://hl7.org/fhir/v3/SetOperator";

    @Override
    public void publishFhirConsentToHie(Consent fhirConsent) {
        //validate the resource
        ValidationResult validationResult =  fhirValidator.validateWithResult(fhirConsent);

        logger.debug("validationResult.isSuccessful(): " + validationResult.isSuccessful());
        //throw format error if the validation is not successful
        if (!validationResult.isSuccessful()) {
            throw new FHIRFormatErrorException("Consent Validation is not successful" + validationResult.getMessages());
        }

        /*
        Use the client to store a new consent resource instance
        Invoke the server create method (and send pretty-printed JSON
        encoding to the server
        instead of the default which is non-pretty printed XML)
        invoke Consent service
        */
       fhirClient.create().resource(fhirConsent).execute();

    }

    @Override
    public void publishFhirConsentToHie(gov.samhsa.c2s.pcm.domain.consent.Consent consent, PatientDto patientDto) {
        publishFhirConsentToHie(createFhirConsent(consent, patientDto));
    }

    @Override
    public Consent createFhirConsent(gov.samhsa.c2s.pcm.domain.consent.Consent consent, PatientDto patientDto) {
        return createGranularConsent(consent, patientDto);
    }

    private Consent createBasicConsent(gov.samhsa.c2s.pcm.domain.consent.Consent c2sConsent, PatientDto patientDto) {


        Consent fhirConsent = new Consent();

        // set the id as a concatenated "OID.consentId"
        final String xdsDocumentEntryUniqueId = uniqueOidProvider.getOid();
        fhirConsent.setId(new IdType(xdsDocumentEntryUniqueId));

        // Set patient reference and add patient as contained resource
        Patient fhirPatient = fhirPatientService.createFhirPatient(patientDto);
        fhirConsent.getPatient().setReference("#" + patientDto.getMedicalRecordNumber());
        fhirConsent.getContained().add(fhirPatient);

        // Consent signature details
        Reference consentSignature = new Reference();
        consentSignature.setDisplay(fhirPatient.getNameFirstRep().getNameAsSingleString());
        consentSignature.setReference("#" + patientDto.getMedicalRecordNumber());
        fhirConsent.getConsentor().add(consentSignature);

        // consent status
        fhirConsent.setStatus(Consent.ConsentStatus.ACTIVE);

        // Specify Authors, the providers authorizes to disclose
        // Author :: Organizational Provider
        Organization sourceOrganizationResource = null;
        for (ConsentOrganizationalProviderPermittedToDisclose orgPermittedTo : c2sConsent.getOrganizationalProvidersPermittedToDisclose()) {
            Set<OrganizationalProvider> sourceOrgPermittedTo = new HashSet<>();
            sourceOrgPermittedTo.add(orgPermittedTo.getOrganizationalProvider());
            sourceOrganizationResource = setOrganizationProvider(sourceOrgPermittedTo);
        }

        if (null != sourceOrganizationResource) {
            fhirConsent.getContained().add(sourceOrganizationResource);
            fhirConsent.getOrganization().setReference("#" + sourceOrganizationResource.getId());
            // TODO :: Need to add source organization details to patient object
        } else {
            // Author :: Individual Provider
            Practitioner sourcePractitioner = null;
            for (ConsentIndividualProviderPermittedToDisclose indPermittedTo : c2sConsent.getProvidersPermittedToDisclose()) {
                Set<IndividualProvider> sourceindPermittedTo = new HashSet<>();
                sourceindPermittedTo.add(indPermittedTo.getIndividualProvider());
                sourcePractitioner = setPractitionerProvider(sourceindPermittedTo);
            }
            if (null != sourcePractitioner) {
                fhirConsent.getContained().add(sourcePractitioner);
                fhirConsent.getOrganization().setReference("#" + sourcePractitioner.getId());
                // TODO :: Need to add source organization details to patient object
            }
        }

        // Specify Policy - Reference the "default" OAuth2 policy that covers the related information
        fhirConsent.setPolicy(c2sConsent.getConsentReferenceId());

        // Specify Recipients, the providers disclosure is made to Recipient :: Organizational Provider
        Organization recipientOrganization = null;
        for (ConsentOrganizationalProviderDisclosureIsMadeTo orgMadeTo : c2sConsent.getOrganizationalProvidersDisclosureIsMadeTo()) {
            Set<OrganizationalProvider> recipientOrgMadeTo = new HashSet<>();
            recipientOrgMadeTo.add(orgMadeTo.getOrganizationalProvider());
            recipientOrganization = setOrganizationProvider(recipientOrgMadeTo);
        }
        if (null != recipientOrganization) {
            fhirConsent.getContained().add(recipientOrganization);
            fhirConsent.getRecipient().add(new Reference().setReference("#" + recipientOrganization.getId()));
        } else {
            // Recipient :: Individual Provider
            Practitioner recipientPractitioner = null;
            for (ConsentIndividualProviderDisclosureIsMadeTo indPermittedTo : c2sConsent.getProvidersDisclosureIsMadeTo()) {
                Set<IndividualProvider> recipientIndPermittedTo = new HashSet<>();
                recipientIndPermittedTo.add(indPermittedTo.getIndividualProvider());
                recipientPractitioner = setPractitionerProvider(recipientIndPermittedTo);
            }
            if(null != recipientPractitioner) {
                fhirConsent.getContained().add(recipientPractitioner);
                fhirConsent.getRecipient().add(new Reference().setReference("#" + recipientPractitioner.getId()));
            }
        }


        // set POU
        for (ConsentShareForPurposeOfUseCode pou : c2sConsent.getShareForPurposeOfUseCodes()) {
            String fhirPou = getPurposeOfUseCode.apply(pou.getPurposeOfUseCode());
            Coding coding = new Coding(fhirProperties.getPou().getSystem(), fhirPou, pou.getPurposeOfUseCode().getCode());
            fhirConsent.getPurpose().add(coding);
        }

        // set terms of consent and intended recipient(s)
        fhirConsent.getPeriod().setStart(c2sConsent.getStartDate());
        fhirConsent.getPeriod().setEnd(c2sConsent.getEndDate());
        // consent sign time
        fhirConsent.setDateTime(new Date());

         // set identifier for this consent
        fhirConsent.getIdentifier().setSystem(fhirProperties.getPid().getDomain().getSystem()).setValue(c2sConsent.getConsentReferenceId());

        //set category
        CodeableConcept categoryConcept = new CodeableConcept();

        //TODO need to replace DISL from enum value
        categoryConcept.addCoding(new Coding().setCode(fhirProperties.getConsentType().getCode())
                                              .setSystem(fhirProperties.getConsentType().getSystem())
                                              .setDisplay(fhirProperties.getConsentType().getLabel()));
        fhirConsent.getCategory().add(categoryConcept);

        return fhirConsent;
    }

    private Organization setOrganizationProvider(Set<OrganizationalProvider> orgProviders) {
        Organization sourceOrganizationResource = new Organization();

        orgProviders.forEach((OrganizationalProvider organizationalProvider) ->
        {
            sourceOrganizationResource.setId(new IdType(organizationalProvider.getNpi()));
            sourceOrganizationResource.addIdentifier().setSystem(fhirProperties.getNpi().getSystem()).setValue(organizationalProvider.getNpi());
            sourceOrganizationResource.setName(organizationalProvider.getOrgName());
            sourceOrganizationResource.addAddress().addLine(organizationalProvider.getFirstLinePracticeLocationAddress())
                    .setCity(organizationalProvider.getPracticeLocationAddressCityName())
                    .setState(organizationalProvider.getPracticeLocationAddressStateName())
                    .setPostalCode(organizationalProvider.getPracticeLocationAddressPostalCode());
        });
        return sourceOrganizationResource;
    }

    private Practitioner setPractitionerProvider(Set<IndividualProvider> individualProviders) {
        Practitioner sourcePractitionerResource = new Practitioner();

        individualProviders.forEach((IndividualProvider individualProvider) ->
        {
            sourcePractitionerResource.setId(new IdType(individualProvider.getNpi()));
            sourcePractitionerResource.addIdentifier().setSystem(fhirProperties.getNpi().getSystem()).setValue(individualProvider.getNpi());
            //setting the name element
            HumanName indName = new HumanName();
            indName.setFamily(individualProvider.getLastName());
            indName.addGiven(individualProvider.getFirstName());
            sourcePractitionerResource.addName(indName);
            //setting the address
            sourcePractitionerResource.addAddress().addLine(individualProvider.getFirstLinePracticeLocationAddress())
                    .setCity(individualProvider.getPracticeLocationAddressCityName())
                    .setState(individualProvider.getPracticeLocationAddressStateName())
                    .setPostalCode(individualProvider.getPracticeLocationAddressPostalCode());

        });

        return sourcePractitionerResource;
    }

    private Function<PurposeOfUseCode, String> getPurposeOfUseCode = new Function<PurposeOfUseCode, String>() {
         @Override
        public String apply(PurposeOfUseCode pou) {
            String codeString = pou.getCode();
            if (codeString != null && !"".equals(codeString) || codeString != null && !"".equals(codeString)) {
                if ("TREATMENT".equalsIgnoreCase(codeString)) {
                    return  V3ActReason.TREAT.toString();
                } else if ("PAYMENT".equalsIgnoreCase(codeString)) {
                    return  V3ActReason.HPAYMT.toString();
                } else if ("RESEARCH".equalsIgnoreCase(codeString)) {
                    return  V3ActReason.HRESCH.toString();
                } else {
                    throw new IllegalArgumentException("Unknown Purpose of Use code \'" + codeString + "\'");
                }
            } else {
                return "";
            }
        }
    };

    private Consent createGranularConsent(gov.samhsa.c2s.pcm.domain.consent.Consent c2sConsent, PatientDto patientDto) {
        // get basic consent details
        Consent fhirConsent = createBasicConsent(c2sConsent, patientDto);

        // get obligations from consent
        List<String> excludeCodes = getConsentObligations(c2sConsent);

        List<Coding> excludeCodingList = new ArrayList<>();
        List<Coding> includeCodingList = new ArrayList<>();
        // go over full list and add obligation as exclusions
        for (SensitivePolicyCodeEnum codesEnum : SensitivePolicyCodeEnum.values()) {
            if (excludeCodes.contains(codesEnum.getCode())) {
                // exclude it
                 excludeCodingList.add(new Coding(codesEnum.getCodeSystem(), codesEnum.getCode(), codesEnum.getDisplayName()));
              } else {
                // include it
                includeCodingList.add(new Coding(codesEnum.getCodeSystem(), codesEnum.getCode(), codesEnum.getDisplayName()));
             }
        }

        // add list to consent
        Consent.ExceptComponent exceptComponent = new Consent.ExceptComponent();

        if(fhirProperties.isKeepExcludeList()) {
            //List of Excluded Sensitive policy codes
            exceptComponent.setType(Consent.ConsentExceptType.DENY);
            exceptComponent.setSecurityLabel(excludeCodingList);
        } else {
            //List of included Sensitive policy codes
            exceptComponent.setSecurityLabel(includeCodingList);
            exceptComponent.setType(Consent.ConsentExceptType.PERMIT);
         }
        fhirConsent.setExcept(Collections.singletonList(exceptComponent));

        //logs FHIRConsent into json and xml format in debug mode
        logFHIRConsent(fhirConsent);

        return fhirConsent;

    }

    private List<String> getConsentObligations(gov.samhsa.c2s.pcm.domain.consent.Consent  consent) {
        final Set<String> obligationCodes = new HashSet<>();

        for (final ConsentDoNotShareClinicalDocumentTypeCode item : consent
                .getDoNotShareClinicalDocumentTypeCodes()) {
            obligationCodes.add(item
                    .getClinicalDocumentTypeCode().getCode());
        }

        for (final ConsentDoNotShareSensitivityPolicyCode item : consent
                .getDoNotShareSensitivityPolicyCodes()) {
            obligationCodes.add(item
                    .getValueSetCategory().getCode());
        }

        for (final ClinicalConceptCode item : consent
                .getDoNotShareClinicalConceptCodes()) {
            obligationCodes.add(item.getCode());
        }
        return new ArrayList<>(obligationCodes);
    }


    private void logFHIRConsent(Consent fhirConsent) {
        logger.debug(() -> fhirContext.newXmlParser().setPrettyPrint(true)
                .encodeResourceToString(fhirConsent));
        logger.debug(() -> fhirContext.newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(fhirConsent));
    }

}

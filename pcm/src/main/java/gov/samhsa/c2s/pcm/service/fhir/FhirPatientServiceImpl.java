package gov.samhsa.c2s.pcm.service.fhir;


import gov.samhsa.c2s.pcm.config.FHIRProperties;
import gov.samhsa.c2s.pcm.infrastructure.dto.PatientDto;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class FhirPatientServiceImpl implements FhirPatientService {

    @Autowired
    private FHIRProperties fhirProperties;



    @Override
    public Patient createFhirPatient(PatientDto patientDto) {
        return patientDtoToPatient.apply(patientDto);
    }

    Function<PatientDto, Patient> patientDtoToPatient = new Function<PatientDto, Patient>() {
        @Override
        public Patient apply(PatientDto patientDto) {
            // set patient information
            Patient fhirPatient = new Patient();

            //setting mandatory fields
            fhirPatient.setId(new IdType(patientDto.getMedicalRecordNumber()));
            fhirPatient.addName().setFamily(patientDto.getLastName()).addGiven(patientDto.getFirstName());
            fhirPatient.addTelecom().setValue(patientDto.getEmail()).setSystem(ContactPoint.ContactPointSystem.EMAIL);
            fhirPatient.setBirthDate(patientDto.getBirthDate());
            fhirPatient.setGender(getPatientGender.apply(patientDto.getGenderCode()));
            fhirPatient.setActive(true);

            //Add an Identifier
            setIdentifiers(fhirPatient, patientDto);

            //optional fields
            fhirPatient.addAddress().addLine(patientDto.getAddress()).setCity(patientDto.getCity()).setState(patientDto.getStateCode()).setPostalCode(patientDto.getZip());
            return fhirPatient;
        }
    };


    private void setIdentifiers(Patient patient, PatientDto patientDto) {

        //setting patient mrn
        patient.addIdentifier().setSystem(fhirProperties.getPid().getDomain().getSystem())
                .setUse(Identifier.IdentifierUse.OFFICIAL).setValue(patientDto.getMedicalRecordNumber());

        // setting ssn value
        if(null != patientDto.getSocialSecurityNumber() && ! patientDto.getSocialSecurityNumber().isEmpty())
            patient.addIdentifier().setSystem(fhirProperties.getSsn().getSystem())
                    .setValue(patientDto.getSocialSecurityNumber());

        if(null != patientDto.getTelephone() && ! patientDto.getTelephone().isEmpty())
            patient.addTelecom().setValue(patientDto.getTelephone()).setSystem(ContactPoint.ContactPointSystem.PHONE);



    }

    Function<String, Enumerations.AdministrativeGender> getPatientGender = new Function<String, AdministrativeGender>() {
        @Override
        public AdministrativeGender apply(String codeString) {
            if (codeString != null && !"".equals(codeString) || codeString != null && !"".equals(codeString)) {
                if ("male".equalsIgnoreCase(codeString) || "M".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.MALE;
                } else if ("female".equalsIgnoreCase(codeString) || "F".equalsIgnoreCase(codeString)) {
                    return Enumerations.AdministrativeGender.FEMALE;
                } else if ("other".equalsIgnoreCase(codeString) || "O".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.OTHER;
                } else if ("unknown".equalsIgnoreCase(codeString) || "UN".equalsIgnoreCase(codeString)) {
                    return AdministrativeGender.UNKNOWN;
                } else {
                    throw new IllegalArgumentException("Unknown AdministrativeGender code \'" + codeString + "\'");
                }
            } else {
                return null;
            }
        }
    };


}

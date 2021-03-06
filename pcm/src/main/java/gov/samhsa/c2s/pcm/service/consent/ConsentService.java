/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.c2s.pcm.service.consent;


import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.pcm.domain.consent.Consent;
import gov.samhsa.c2s.pcm.service.dto.AbstractPdfDto;
import gov.samhsa.c2s.pcm.service.dto.AttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentListDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentPdfDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentRevocationAttestationDto;
import gov.samhsa.c2s.pcm.service.dto.ConsentRevokationPdfDto;
import gov.samhsa.c2s.pcm.service.dto.XacmlDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The Interface ConsentService.
 */
public interface ConsentService {

    /**
     * Count all consents.
     *
     * @return the long
     */
    long countAllConsents();

    /**
     * Delete consent.
     *
     * @param consentId the consent id
     * @return the boolean true on success, false on failure
     */
    boolean deleteConsent(Long consentId);

    /**
     * Find consent.
     *
     * @param id the id
     * @return the consent
     */
    Consent findConsent(Long id);

    /**
     * Find all consents.
     *
     * @return the list
     */
    List<Consent> findAllConsents();

    /**
     * Find all consents dto by patient.
     *
     * @param patientId the patient id
     * @return the list
     */
    List<ConsentListDto> findAllConsentsDtoByPatient(Long patientId);

    /**
     * Find consent entries.
     *
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list
     */
    List<Consent> findConsentEntries(int firstResult, int maxResults);

    /**
     * Find consentPdfDto.
     *
     * @param consentId the consent id
     * @return the consent pdf dto
     */
    ConsentPdfDto findConsentPdfDto(Long consentId);

    /**
     * Gets the attested consent PDF Dto.
     *
     * @param consentId the consent id
     * @return the consent pdf as byte[]
     */
    byte[] getAttestedConsentPdf(Long consentId) throws ConsentGenException;

    /**
     * Gets the attested consent revoked PDF Dto.
     *
     * @param consentId the consent id
     * @return the consent pdf as byte[]
     */
    byte[] getAttestedConsentRevokedPdf(Long consentId) throws ConsentGenException;

    /**
     * Create the attested consent.
     *
     * @param attestationDto the consent id and attester's IP address
     */
    void attestConsent(AttestationDto attestationDto);


    /**
     * Create the attested consent revocation.
     *
     * @param attestationDto the consent id and attester's IP address
     */
    void attestConsentRevocation(AttestationDto attestationDto);


    /**
     * Find consentPdfDto.
     *
     * @param consentId the consent id
     * @return the consent pdf dto
     */
    AbstractPdfDto findConsentContentDto(Long consentId);

    /**
     * Make consentListDto Array.
     *
     * @return the array list
     */
    ArrayList<ConsentListDto> makeConsentListDtos();

    /**
     * Make consent revokation pdf dto.
     *
     * @return the consent revokation pdf dto
     */
    ConsentRevokationPdfDto makeConsentRevokationPdfDto();

    /**
     * Save consent.
     *
     * @param consent the consent
     */
    void saveConsent(Consent consent);

    /**
     * Save consent.
     *
     * @param consentDto the consent dto
     * @param patientId  the patient id
     * @return the object
     * @throws ConsentGenException the consent gen exception
     */
    Object saveConsent(ConsentDto consentDto, long patientId)
            throws ConsentGenException;

    /**
     * Update consent.
     *
     * @param consent the consent
     * @return the consent
     */
    Consent updateConsent(Consent consent);

    /**
     * Checks if this consent belong to this user.
     *
     * @param consentId the consent id
     * @param patient   the patient
     * @return true, if is consent belong to this user
     */
    boolean isConsentBelongToThisUser(Long consentId, Long patient);

    /**
     * Find consent by id.
     *
     * @param consentId the consent id
     * @return the consent dto
     */
    ConsentDto findConsentById(String username, Long consentId);

    List<String> findObligationsConsentById(String username, Long consentId);

    XacmlDto findXACMLForCCDByConsentId(Long consentId);

    /**
     * Validate consent date.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return true, if successful
     */
    boolean validateConsentDate(Date startDate, Date endDate);

    /**
     * Make ConsentPdfDto.
     *
     * @return the consent pdf dto
     */
    ConsentPdfDto makeConsentPdfDto();

    /**
     * Make consent.
     *
     * @return the consent
     */
    Consent makeConsent();

    /**
     * Make consent dto.
     *
     * @return the consent dto
     */
    ConsentDto makeConsentDto();

    /**
     * Are there duplicates in two sets.
     *
     * @param set1 the set1
     * @param set2 the set2
     * @return true, if successful
     */
    @SuppressWarnings("rawtypes")
    boolean areThereDuplicatesInTwoSets(Set set1, Set set2);

    /**
     * Find consent revokation pdf dto.
     *
     * @param consentId the consent id
     * @return the consent revokation pdf dto
     */
    ConsentRevokationPdfDto findConsentRevokationPdfDto(Long consentId);

    /**
     * Find all consents dto by user name.
     *
     * @param userName the user name
     * @return the list
     */
    List<ConsentListDto> findAllConsentsDtoByUserName(String userName);

    /**
     * Gets the consent signed stage.
     *
     * @param consentId the consent id
     * @return the consent signed stage
     */
    String getConsentStatus(Long consentId);


    /**
     * Gets the xacml ccd.
     *
     * @param consentId the consent id
     * @return the xacml ccd
     */
    byte[] getXacmlCcd(Long consentId);

    byte[] getConsentDirective(Long consentId);

    Map<String, Object> findAllConsentsDtoByPatientAndPage(Long patientId, String pageNumber);

    ConsentAttestationDto getConsentAttestationDto(String userName, Long consentId);

    /**
     * Gets the ConsentRevocationAttestationDto
     *
     * @param userName  the user/patient name for whom to search for the consent to be revoked
     * @param consentId the consent id for the consent to be revoked
     * @return ConsentRevocationAttestationDto
     */
    ConsentRevocationAttestationDto getConsentRevocationAttestationDto(String userName, Long consentId);
}

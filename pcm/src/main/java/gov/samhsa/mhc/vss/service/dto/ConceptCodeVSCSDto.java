package gov.samhsa.mhc.vss.service.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConceptCodeVSCSDto extends AbstractNodeDto {

    private ConceptCodeDto conceptCodeDto = new ConceptCodeDto();

    private Map<Long, String> valueSetsMap = new HashMap<Long, String>();

    private List<CSVersionsDto> csVersions = new ArrayList<CSVersionsDto>();

    public ConceptCodeDto getConceptCodeDto() {
        return conceptCodeDto;
    }

    public void setConceptCodeDto(ConceptCodeDto conceptCodeDto) {
        this.conceptCodeDto = conceptCodeDto;
    }

    public Map<Long, String> getValueSetsMap() {
        return valueSetsMap;
    }

    public void setValueSetsMap(Map<Long, String> valueSetsMap) {
        this.valueSetsMap = valueSetsMap;
    }

    public List<CSVersionsDto> getCsVersions() {
        return csVersions;
    }

    public void setCsVersions(List<CSVersionsDto> csVersions) {
        this.csVersions = csVersions;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

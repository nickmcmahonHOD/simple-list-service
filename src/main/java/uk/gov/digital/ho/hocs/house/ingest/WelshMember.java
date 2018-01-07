package uk.gov.digital.ho.hocs.house.ingest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WelshMember {

    @JacksonXmlProperty(localName = "fullusername")
    private String name;

    public String getName() {
        return name;
    }

}
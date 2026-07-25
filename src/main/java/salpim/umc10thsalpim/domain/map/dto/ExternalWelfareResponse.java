package salpim.umc10thsalpim.domain.map.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "wantedList")
public class ExternalWelfareResponse {

    @JacksonXmlProperty(localName = "resultCode")
    private String resultCode;

    @JacksonXmlProperty(localName = "resultMessage")
    private String resultMessage;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "servList")
    private List<ServList> servList;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServList{

        @JacksonXmlProperty(localName = "servId")
        private String servId;

        @JacksonXmlProperty(localName = "servNm")
        private String servNm;

        @JacksonXmlProperty(localName = "aplyMtdNm")
        private String aplyMtdNm;

        @JacksonXmlProperty(localName = "ctpvNm")
        private String ctpvNm;

        @JacksonXmlProperty(localName = "sggNm")
        private String sggNm;
    }
}

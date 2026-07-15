package salpim.umc10thsalpim.global.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class BokjiroApiDTO {

    @Getter
    @JacksonXmlRootElement(localName = "wantedList")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BenefitListRes {
        private int totalCount;
        private int pageNo;
        private int numOfRows;
        private String resultMessage;
        private String resultCode;

        @JacksonXmlProperty(localName = "servList")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<BenefitItem> benefitList = new ArrayList<>();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BenefitItem {
        private String servId;
        private String servNm;
    }

}

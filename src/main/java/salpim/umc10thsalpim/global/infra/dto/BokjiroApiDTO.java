package salpim.umc10thsalpim.global.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class BokjiroApiDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JacksonXmlRootElement(localName = "wantedList")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BenefitListRes {
        @JacksonXmlProperty(localName = "totalCount")
        private int maxTotalCount;
//        private int pageNo;
//        private int numOfRows;
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
        private String inqNum;
        private String ctpvNm;
        private String sggNm;
    }

}

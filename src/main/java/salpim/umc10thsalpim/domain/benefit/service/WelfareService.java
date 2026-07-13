package salpim.umc10thsalpim.domain.benefit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.List;

@Service
public class WelfareService {



    @Transactional
    public CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> getSearchResult(String searchKey, List<Integer> regionIds, List<Integer> categoryIds, String cursor, Integer pageSize, String sort) {



        return null;
    }
}

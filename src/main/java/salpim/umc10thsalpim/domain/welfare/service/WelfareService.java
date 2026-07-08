package salpim.umc10thsalpim.domain.welfare.service;

import org.springframework.stereotype.Service;
import salpim.umc10thsalpim.domain.welfare.dto.WelfareResDTO;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.List;

@Service
public class WelfareService {
    public CursorResDTO.Pagination<WelfareResDTO.WelfareSearchResultDTO> getSearchResult(String searchKey, List<Integer> regionIds, List<Integer> categoryIds, String cursor, Integer pageSize, String sort) {
        return null;
    }
}

package salpim.umc10thsalpim.domain.benefit.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.global.dto.CursorResDTO;

import java.util.List;

@Service
@AllArgsConstructor
public class BenefitService {

    private final WelfareBenefitRepository welfareBenefitRepository;

    @Transactional
    public CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> getSearchResult(String searchKey, List<Integer> regionIds, List<Integer> categoryIds, String cursor, Integer pageSize, String sort) {

        PageRequest pageRequest = PageRequest.of(0, pageSize);

        Slice<WelfareBenefit> benefits;
        String nextCursor = null;
        Long idCursor;

        String sortType=sort.toLowerCase();

        if (cursor.equals("-1")) {
            switch (sortType) {
                case "popular":

                    break;

                case "deadline":

                    break;
            }
        }else{
            switch (sortType) {
                case "popular":

                    break;

                case "deadline":

                    break;
            }
        }

        return null;
    }
}

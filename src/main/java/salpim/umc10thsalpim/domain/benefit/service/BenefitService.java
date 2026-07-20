package salpim.umc10thsalpim.domain.benefit.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.benefit.converter.BenefitConverter;
import salpim.umc10thsalpim.domain.benefit.dto.BenefitResDTO;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.global.apiPayload.exception.ProjectException;
import salpim.umc10thsalpim.global.apiPayload.exception.code.BenefitErrorCode;
import salpim.umc10thsalpim.global.dto.CursorResDTO;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;

import java.util.*;

@Service
@AllArgsConstructor
public class BenefitService {

    private final WelfareBenefitRepository welfareBenefitRepository;
    private final BokjiroApiClient bokjiroApiClient;

    private static final int API_MAX_SIZE=500;
    private static final int MAX_SERV_NUMBER=1001;
    private static final String SOURCE_NATIONAL = "NATIONAL";

    @Transactional(readOnly = true)
    public CursorResDTO.Pagination<BenefitResDTO.WelfareSearchResultDTO> getSearchResult(String searchKey, List<Long> regionIds, List<Long> categoryIds, String cursor, Integer pageSize, String sort) {

        PageRequest pageRequest = PageRequest.of(0, pageSize);

        String nextCursor = null;
        Integer totalCount=0;

        //복지로 api 이용해서 검색어에 맞는 혜택 id를 리스트에 넣기
        List<String> servIds = new ArrayList<>();
        int pageNumber = 1;
        while(servIds.size()<MAX_SERV_NUMBER){
            BokjiroApiDTO.BenefitListRes res =
                    bokjiroApiClient.searchNationalBenefits(pageNumber, API_MAX_SIZE, searchKey, null);
            res.getBenefitList().forEach(item -> servIds.add(item.getServId()));
            if (pageNumber*API_MAX_SIZE>=res.getTotalCount()){ break; }
            if (pageNumber==1){
                totalCount = res.getTotalCount();
            }
            pageNumber++;
        }

        //순서 저장하는 리스트 생성
        Map<String, Integer> orderIndex = new HashMap<>();
        int i = 0;
        for (String servId : servIds) {
            orderIndex.put(servId, i++);
        }

        //DB 매칭 & 카테고리 필터링
        List<WelfareBenefit> matched = servIds.isEmpty()
                ? List.of() : welfareBenefitRepository.findByExternalIdInAndSource(servIds, SOURCE_NATIONAL);

        List<WelfareBenefit> filtered = matched.stream()
                .filter(b -> categoryIds==null || categoryIds.isEmpty()||
                        categoryIds.contains(b.getWelfareCategory().getId()))
                .toList();

        //정렬
        List<WelfareBenefit> sortedBenefits = sortBenefits(filtered, sort, orderIndex);

        //페이징
        List<WelfareBenefit> afterCursor = applyCursor(sortedBenefits, cursor);
        List<WelfareBenefit> page = afterCursor.stream().limit(pageSize).toList();
        boolean hasNext = afterCursor.size()>pageSize;
        nextCursor = hasNext ? afterCursor.get(pageSize).getId().toString() : null;

        return BenefitConverter.toPagination(page, nextCursor, totalCount);
    }

    private List<WelfareBenefit> sortBenefits(List<WelfareBenefit> benefits,
                                             String sort, Map<String, Integer> orderIndex) {
        Comparator<WelfareBenefit> comparator = switch (sort.toLowerCase()) {
            case "popular" -> Comparator
                    .comparing((WelfareBenefit b) -> orderIndex.get(b.getExternalId()));
            case "deadline" -> Comparator
                    .comparing(WelfareBenefit::getApplicationEndDate,
                            Comparator.nullsLast(Comparator.naturalOrder())) // 마감 가까운 순, 비어있으면 뒤로
                    .thenComparing(WelfareBenefit::getId);
            default -> throw new ProjectException(BenefitErrorCode.INVALID_SORT_TYPE);
        };
        return benefits.stream().sorted(comparator).toList();
    }

    private List<WelfareBenefit> applyCursor(List<WelfareBenefit> benefits, String cursor) {
        if (cursor.equals("-1")) {
            return benefits;
        }else{
            for (int i=0; i<benefits.size(); i++) {
                if (benefits.get(i).getId().toString().equals(cursor)) {
                    return benefits.subList(i+1, benefits.size());
                }
            }
        }
        return Collections.emptyList();
    }
}

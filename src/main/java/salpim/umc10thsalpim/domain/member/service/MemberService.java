package salpim.umc10thsalpim.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.member.converter.MemberConverter;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public MemberResDTO.MyPageInfo getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if(member.getRegionId() == null) {
            throw new MemberException(MemberErrorCode.MEMBER_REGION_NOT_SET);
        }

        Region memberRegion = regionRepository.findById(member.getRegionId())
                .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));

        Region sido = findAncestorRegion(memberRegion, RegionLevel.SIDO);
        Region sigungu = findAncestorRegion(memberRegion, RegionLevel.SIGUNGU);

        return MemberConverter.toMyPageInfo(
                member,
                sido.getName(),
                sigungu.getName()
        );
    }

    private Region findAncestorRegion(Region region, RegionLevel targetLevel) {
        Region currentRegion = region;

        while (currentRegion.getRegionLevel() != targetLevel) {
            Long parentId = currentRegion.getParentId();

            if(parentId == null){
                throw new RegionException(RegionErrorCode.REGION_HIERARCHY_INVALID);
            }

            currentRegion = regionRepository.findById(parentId)
                    .orElseThrow(() -> new RegionException(RegionErrorCode.REGION_NOT_FOUND));
        }

        return currentRegion;
    }
}

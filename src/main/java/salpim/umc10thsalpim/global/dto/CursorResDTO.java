package salpim.umc10thsalpim.global.dto;

import lombok.Builder;

import java.util.List;

public class CursorResDTO {

    @Builder
    public record Pagination<T>(
        List<T> data,
        Boolean hasNext,
        String nextCursor,
        Integer pageSize,
        Integer totalCount
    ){}

}

package com.miany.psycheecho.dto.response;

import com.miany.psycheecho.content.EchoCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EchoCategoryResponse {

    private String name;
    private String icon;
    private Integer count;

    public static EchoCategoryResponse fromContent(EchoCategory category) {
        return EchoCategoryResponse.builder()
                .name(category.getMetadata().getName())
                .icon(category.getSpec().getIcon())
                .count(category.getSpec().getCount())
                .build();
    }
}

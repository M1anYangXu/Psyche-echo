package com.miany.psycheecho.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EchoNoteRequest {

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long categoryId;

    private String categoryName;

    private List<MediaItem> medias;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaItem {
        private String url;
        private String type;
        private String cover;
        private String displayName;
    }
}

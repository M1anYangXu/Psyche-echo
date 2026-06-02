package com.miany.psycheecho.dto.response;

import com.miany.psycheecho.content.EchoNote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EchoNoteResponse {

    private String name;
    private String content;
    private String time;
    private String categoryId;
    private String categoryName;
    private List<EchoNote.Media> medias;
    private Long visitCount;

    public static EchoNoteResponse fromContent(EchoNote echo) {
        return EchoNoteResponse.builder()
                .name(echo.getMetadata().getName())
                .content(echo.getSpec().getContent())
                .time(echo.getStatus().getTime())
                .categoryId(echo.getStatus().getCategoryId())
                .categoryName(echo.getSpec().getCategoryName())
                .medias(echo.getSpec().getMedias())
                .visitCount(echo.getStatus().getVisitCount())
                .build();
    }
}

package com.miany.psycheecho.content;

import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(
    group = "echo.miany.run",
    version = "v1alpha1",
    kind = "EchoNote",
    plural = "echonotes",
    singular = "echonote"
)
public class EchoNote extends AbstractExtension {
    
    private EchoNoteSpec spec;
    private EchoNoteStatus status;
    
    @Data
    public static class EchoNoteSpec {
        private String author;
        private String avatar;
        private String content;
        private List<Media> medias;
        private String categoryName;
    }
    
    @Data
    public static class Media {
        private String url;
        private String type;
        private String cover;
        private String displayName;
    }
    
    @Data
    public static class EchoNoteStatus {
        private String time;
        private Long visitCount;
        private String categoryId;
    }
}

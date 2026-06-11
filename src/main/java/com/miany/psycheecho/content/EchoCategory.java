package com.miany.psycheecho.content;

import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(
    group = "echo.miany.run",
    version = "v1alpha1",
    kind = "EchoCategory",
    plural = "echocategories",
    singular = "echocategory"
)
public class EchoCategory extends AbstractExtension {
    
    private EchoCategorySpec spec;
    
    @Data
    public static class EchoCategorySpec {
        private String name;
        private String icon;
        private Integer count;
        private Integer order;
    }
}

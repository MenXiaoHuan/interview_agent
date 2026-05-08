package com.multimodal.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FacialAnalysisRequest 数据传输对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "面部表情分析请求")
public class FacialAnalysisRequest {
    
    /**
     * 图像或视频URL
     */
    @NotBlank(message = "URL不能为空")
    @Schema(description = "图像或视频URL，指向需要分析的面部表情资源", example = "https://example.com/face.jpg", required = true)
    private String url;
    
    /**
     * 资源类型
     */
    @Schema(description = "资源类型，image或video", example = "image", defaultValue = "image")
    private String type;
    
    /**
     * 分析深度
     */
    @Schema(description = "分析深度，basic或advanced", example = "basic", defaultValue = "basic")
    private String depth;
}

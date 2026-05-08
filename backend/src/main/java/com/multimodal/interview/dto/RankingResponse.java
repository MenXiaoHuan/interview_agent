package com.multimodal.interview.dto;

import lombok.Data;

/**
 * RankingResponse 数据传输对象。
 */
@Data
public class RankingResponse {
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Double score;
    private Integer rank;
}

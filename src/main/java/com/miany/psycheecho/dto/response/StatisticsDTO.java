package com.miany.psycheecho.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {

    private long totalNotes;

    private long todayNotes;

    private long thisWeekNotes;

    private long thisMonthNotes;

    private Map<String, Long> categoryStats;

    private Map<String, Long> moodStats;

    private Map<String, Long> weatherStats;

    private Map<String, Long> environmentStats;

    private Map<String, Long> locationStats;

    private List<MonthlyStat> monthlyStats;

    private List<DailyStat> recentDaysStats;

    private String earliestDate;

    private String latestDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private String month;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStat {
        private String date;
        private long count;
    }
}
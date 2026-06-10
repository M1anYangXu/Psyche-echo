package com.miany.psycheecho.service.impl;

import com.miany.psycheecho.content.EchoCategory;
import com.miany.psycheecho.content.EchoNote;
import com.miany.psycheecho.dto.response.StatisticsDTO;
import com.miany.psycheecho.service.EchoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EchoServiceImpl implements EchoService {

    private final ExtensionClient extensionClient;

    @Override
    public Flux<EchoCategory> getAllCategories() {
        var categories = extensionClient.listAll(EchoCategory.class, ListOptions.builder().build(), null);
        
        if (categories.isEmpty()) {
            initializeDefaultCategories();
            categories = extensionClient.listAll(EchoCategory.class, ListOptions.builder().build(), null);
        }
        
        return Flux.fromIterable(categories)
                .sort(Comparator.comparing(c -> c.getMetadata().getName()));
    }

    @Override
    public Mono<EchoCategory> getCategoryByName(String name) {
        var categoryOpt = extensionClient.fetch(EchoCategory.class, name);
        return categoryOpt.<Mono<EchoCategory>>map(Mono::just)
                .orElse(Mono.error(new IllegalArgumentException("分类不存在: " + name)));
    }

    @Override
    public Mono<EchoCategory> createCategory(EchoCategory category) {
        Assert.notNull(category.getSpec(), "Spec must not be null");
        Assert.notNull(category.getSpec().getName(), "Name must not be null");

        String name = category.getSpec().getName();
        
        var existing = extensionClient.fetch(EchoCategory.class, name);
        if (existing.isPresent()) {
            return Mono.error(new IllegalArgumentException("分类已存在: " + name));
        }
        
        var metadata = new run.halo.app.extension.Metadata();
        metadata.setName(name);
        category.setMetadata(metadata);
        extensionClient.create(category);
        return Mono.just(category);
    }

    @Override
    public Mono<EchoCategory> updateCategory(String name, EchoCategory category) {
        if ("默认".equals(name)) {
            return Mono.error(new IllegalArgumentException("默认分类不能修改"));
        }
        
        var existingOpt = extensionClient.fetch(EchoCategory.class, name);
        if (existingOpt.isEmpty()) {
            return Mono.error(new IllegalArgumentException("分类不存在: " + name));
        }
        
        var existing = existingOpt.get();
        if (category.getSpec().getName() != null) {
            existing.getSpec().setName(category.getSpec().getName());
        }
        if (category.getSpec().getIcon() != null) {
            existing.getSpec().setIcon(category.getSpec().getIcon());
        }
        if (category.getSpec().getCount() != null) {
            existing.getSpec().setCount(category.getSpec().getCount());
        }
        extensionClient.update(existing);
        return Mono.just(existing);
    }

    @Override
    public Mono<Void> deleteCategory(String name) {
        if ("默认".equals(name)) {
            return Mono.error(new IllegalArgumentException("默认分类不能删除"));
        }
        
        return Mono.fromCallable(() -> extensionClient.fetch(EchoCategory.class, name))
                .flatMap(categoryOpt -> {
                    if (categoryOpt.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("分类不存在: " + name));
                    }
                    return Mono.fromRunnable(() -> extensionClient.delete(categoryOpt.get()));
                }).then();
    }

    @Override
    public Flux<EchoNote> getEchoes(String categoryId) {
        var allNotes = extensionClient.listAll(EchoNote.class, ListOptions.builder().build(), null);
        
        if (categoryId == null || categoryId.equals("1") || categoryId.equals("all") || categoryId.equals("全部")) {
            return Flux.fromIterable(allNotes)
                    .filter(note -> "默认".equals(note.getStatus().getCategoryId()))
                    .sort(Comparator.comparing(n -> n.getMetadata().getName()));
        }
        
        return Flux.fromIterable(allNotes)
                .filter(note -> categoryId.equals(note.getStatus().getCategoryId()))
                .sort(Comparator.comparing(n -> n.getMetadata().getName()));
    }

    @Override
    public Mono<EchoNote> getEchoByName(String name) {
        var noteOpt = extensionClient.fetch(EchoNote.class, name);
        return noteOpt.<Mono<EchoNote>>map(Mono::just)
                .orElse(Mono.error(new IllegalArgumentException("日记不存在: " + name)));
    }

    @Override
    public Mono<EchoNote> createEcho(EchoNote echo) {
        Assert.notNull(echo.getSpec(), "Spec must not be null");
        Assert.notNull(echo.getSpec().getContent(), "Content must not be null");

        String name = "echo-" + System.currentTimeMillis();
        var metadata = new run.halo.app.extension.Metadata();
        metadata.setName(name);
        echo.setMetadata(metadata);

        if (echo.getStatus() == null) {
            echo.setStatus(new EchoNote.EchoNoteStatus());
        }
        
        if (echo.getStatus().getTime() == null || echo.getStatus().getTime().isEmpty()) {
            echo.getStatus().setTime("刚刚");
        }
        
        if (echo.getStatus().getVisitCount() == null) {
            echo.getStatus().setVisitCount(0L);
        }
        
        if (echo.getStatus().getCreationTimestamp() == null || echo.getStatus().getCreationTimestamp().isEmpty()) {
            echo.getStatus().setCreationTimestamp(java.time.Instant.now().toString());
        }

        if (echo.getSpec().getCategoryName() == null) {
            echo.getSpec().setCategoryName("默认");
        }
        if (echo.getStatus().getCategoryId() == null) {
            echo.getStatus().setCategoryId("默认");
        }

        return Mono.fromRunnable(() -> {
                extensionClient.create(echo);
                
                String categoryId = echo.getStatus().getCategoryId();
                if (categoryId != null && !categoryId.equals("1") && !categoryId.equals("全部")) {
                    updateCategoryCount(categoryId, 1);
                }
            })
            .thenReturn(echo);
    }
    
    @Override
    public Mono<EchoNote> updateEcho(String name, EchoNote echo) {
        var existingOpt = extensionClient.fetch(EchoNote.class, name);
        if (existingOpt.isEmpty()) {
            return Mono.error(new IllegalArgumentException("日记不存在: " + name));
        }
        
        var existing = existingOpt.get();
        String oldCategoryId = existing.getStatus().getCategoryId();
        String newCategoryId = echo.getStatus() != null ? echo.getStatus().getCategoryId() : oldCategoryId;

        if (echo.getSpec().getContent() != null) {
            existing.getSpec().setContent(echo.getSpec().getContent());
        }
        if (echo.getSpec().getMedias() != null) {
            existing.getSpec().setMedias(echo.getSpec().getMedias());
        }
        if (echo.getSpec().getCategoryName() != null) {
            existing.getSpec().setCategoryName(echo.getSpec().getCategoryName());
        }
        if (newCategoryId != null) {
            existing.getStatus().setCategoryId(newCategoryId);
        }

        if (oldCategoryId != null && newCategoryId != null && !oldCategoryId.equals(newCategoryId)) {
            updateCategoryCount(oldCategoryId, -1);
            updateCategoryCount(newCategoryId, 1);
        }

        extensionClient.update(existing);
        return Mono.just(existing);
    }

    @Override
    public Mono<Void> deleteEcho(String name) {
        return Mono.fromCallable(() -> extensionClient.fetch(EchoNote.class, name))
                .flatMap(noteOpt -> {
                    if (noteOpt.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("日记不存在: " + name));
                    }
                    var note = noteOpt.get();
                    String categoryId = note.getStatus().getCategoryId();
                    return Mono.fromRunnable(() -> {
                        if (categoryId != null && !categoryId.equals("1") && !categoryId.equals("全部")) {
                            updateCategoryCount(categoryId, -1);
                        }
                        extensionClient.delete(note);
                    });
                }).then();
    }

    @Override
    public void initializeDefaultCategories() {
        List<String[]> defaultCategories = List.of(
                new String[]{"默认", "folder"},
                new String[]{"生活", "book"},
                new String[]{"工作", "computer"},
                new String[]{"心情", "motion"},
                new String[]{"旅行", "motion"}
        );

        for (String[] cat : defaultCategories) {
            String name = cat[0];
            var existing = extensionClient.fetch(EchoCategory.class, name);
            if (existing.isEmpty()) {
                EchoCategory category = new EchoCategory();
                var metadata = new run.halo.app.extension.Metadata();
                metadata.setName(name);
                category.setMetadata(metadata);
                EchoCategory.EchoCategorySpec spec = new EchoCategory.EchoCategorySpec();
                spec.setName(name);
                spec.setIcon(cat[1]);
                spec.setCount(0);
                category.setSpec(spec);
                extensionClient.create(category);
                log.info("创建默认分类: {}", name);
            } else {
                log.debug("分类已存在: {}", name);
            }
        }
    }

    private void updateCategoryCount(String categoryId, int delta) {
        if (categoryId == null || categoryId.equals("1") || categoryId.equals("全部") || categoryId.equals("默认")) {
            return;
        }

        var categoryOpt = extensionClient.fetch(EchoCategory.class, categoryId);
        categoryOpt.ifPresent(category -> {
            int currentCount = category.getSpec().getCount() != null ? category.getSpec().getCount() : 0;
            category.getSpec().setCount(Math.max(0, currentCount + delta));
            extensionClient.update(category);
        });
    }

    @Override
    public Mono<StatisticsDTO> getStatistics() {
        log.info("获取日记统计信息");
        return Mono.fromSupplier(() -> {
                var allNotes = extensionClient.listAll(EchoNote.class, ListOptions.builder().build(), null);
                
                LocalDate today = LocalDate.now();
                LocalDate weekStart = today.minusDays(7);
                LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
                
                long totalNotes = allNotes.size();
                
                long todayNotes = allNotes.stream()
                        .filter(note -> isSameDay(note, today))
                        .count();
                
                long thisWeekNotes = allNotes.stream()
                        .filter(note -> isInDateRange(note, weekStart, today))
                        .count();
                
                long thisMonthNotes = allNotes.stream()
                        .filter(note -> isInDateRange(note, monthStart, today))
                        .count();
                
                Map<String, Long> categoryStats = allNotes.stream()
                        .collect(Collectors.groupingBy(
                                note -> note.getSpec() != null && note.getSpec().getCategoryName() != null 
                                        ? note.getSpec().getCategoryName() 
                                        : "默认",
                                Collectors.counting()
                        ));
                        
                Map<String, Long> moodStats = allNotes.stream()
                        .filter(note -> note.getSpec() != null && note.getSpec().getMood() != null && !note.getSpec().getMood().isEmpty())
                        .collect(Collectors.groupingBy(
                                note -> note.getSpec().getMood(),
                                Collectors.counting()
                        ));
                        
                Map<String, Long> weatherStats = allNotes.stream()
                        .filter(note -> note.getSpec() != null && note.getSpec().getWeatherDay() != null && !note.getSpec().getWeatherDay().isEmpty())
                        .collect(Collectors.groupingBy(
                                note -> note.getSpec().getWeatherDay(),
                                Collectors.counting()
                        ));
                        
                Map<String, Long> environmentStats = allNotes.stream()
                        .filter(note -> note.getSpec() != null && note.getSpec().getEnvironment() != null && !note.getSpec().getEnvironment().isEmpty())
                        .collect(Collectors.groupingBy(
                                note -> note.getSpec().getEnvironment(),
                                Collectors.counting()
                        ));
                        
                Map<String, Long> locationStats = allNotes.stream()
                        .filter(note -> note.getSpec() != null && note.getSpec().getLocation() != null && !note.getSpec().getLocation().isEmpty())
                        .map(note -> {
                            String location = note.getSpec().getLocation();
                            String[] parts = location.split("：");
                            if (parts.length >= 2) {
                                return parts[0] + "：" + parts[1];
                            }
                            return location;
                        })
                        .collect(Collectors.groupingBy(
                                loc -> loc,
                                Collectors.counting()
                        ));
                        
                List<StatisticsDTO.MonthlyStat> monthlyStats = generateMonthlyStats(allNotes);
                
                List<StatisticsDTO.DailyStat> recentDaysStats = generateRecentDaysStats(allNotes, 14);
                
                String earliestDate = null;
                String latestDate = null;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                
                if (!allNotes.isEmpty()) {
                    Optional<EchoNote> earliest = allNotes.stream()
                            .min(Comparator.comparing(n -> n.getMetadata().getCreationTimestamp()));
                    Optional<EchoNote> latest = allNotes.stream()
                            .max(Comparator.comparing(n -> n.getMetadata().getCreationTimestamp()));
                    
                    earliestDate = earliest.map(n -> parseDate(n.getMetadata().getCreationTimestamp(), formatter)).orElse(null);
                    latestDate = latest.map(n -> parseDate(n.getMetadata().getCreationTimestamp(), formatter)).orElse(null);
                }
                
                return StatisticsDTO.builder()
                        .totalNotes(totalNotes)
                        .todayNotes(todayNotes)
                        .thisWeekNotes(thisWeekNotes)
                        .thisMonthNotes(thisMonthNotes)
                        .categoryStats(categoryStats)
                        .moodStats(moodStats)
                        .weatherStats(weatherStats)
                        .environmentStats(environmentStats)
                        .locationStats(locationStats)
                        .monthlyStats(monthlyStats)
                        .recentDaysStats(recentDaysStats)
                        .earliestDate(earliestDate)
                        .latestDate(latestDate)
                        .build();
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    private boolean isSameDay(EchoNote note, LocalDate date) {
        LocalDateTime timestamp = getNoteTimestamp(note);
        if (timestamp == null) {
            return false;
        }
        return timestamp.toLocalDate().equals(date);
    }
    
    private LocalDateTime getNoteTimestamp(EchoNote note) {
        try {
            String timestampStr = null;
            
            if (note.getStatus() != null && note.getStatus().getCreationTimestamp() != null && !note.getStatus().getCreationTimestamp().isEmpty()) {
                timestampStr = note.getStatus().getCreationTimestamp();
            } else if (note.getMetadata().getCreationTimestamp() != null) {
                timestampStr = note.getMetadata().getCreationTimestamp().toString();
            }
            
            if (timestampStr != null) {
                return Instant.parse(timestampStr)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private boolean isInDateRange(EchoNote note, LocalDate start, LocalDate end) {
        LocalDateTime timestamp = getNoteTimestamp(note);
        if (timestamp == null) {
            return false;
        }
        LocalDate noteDate = timestamp.toLocalDate();
        return !noteDate.isBefore(start) && !noteDate.isAfter(end);
    }

    private List<StatisticsDTO.MonthlyStat> generateMonthlyStats(List<EchoNote> notes) {
        Map<String, Long> monthlyCounts = notes.stream()
                .filter(note -> getNoteTimestamp(note) != null)
                .collect(Collectors.groupingBy(
                        note -> {
                            LocalDateTime timestamp = getNoteTimestamp(note);
                            if (timestamp == null) {
                                return "unknown";
                            }
                            return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        },
                        Collectors.counting()
                ));
        
        return monthlyCounts.entrySet().stream()
                .filter(entry -> !"unknown".equals(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> StatisticsDTO.MonthlyStat.builder()
                        .month(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<StatisticsDTO.DailyStat> generateRecentDaysStats(List<EchoNote> notes, int days) {
        LocalDate today = LocalDate.now();
        Map<String, Long> dailyCounts = notes.stream()
                .filter(note -> getNoteTimestamp(note) != null)
                .collect(Collectors.groupingBy(
                        note -> {
                            LocalDateTime timestamp = getNoteTimestamp(note);
                            if (timestamp == null) {
                                return "unknown";
                            }
                            return timestamp.toLocalDate().toString();
                        },
                        Collectors.counting()
                ));
        
        List<StatisticsDTO.DailyStat> stats = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            stats.add(StatisticsDTO.DailyStat.builder()
                    .date(dateStr)
                    .count(dailyCounts.getOrDefault(dateStr, 0L))
                    .build());
        }
        
        return stats;
    }

    private String parseDate(java.time.Instant timestamp, DateTimeFormatter formatter) {
        try {
            LocalDateTime dateTime = timestamp.atZone(ZoneId.systemDefault()).toLocalDateTime();
            return dateTime.toLocalDate().format(formatter);
        } catch (Exception e) {
            return null;
        }
    }
}
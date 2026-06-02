package com.miany.psycheecho.service.impl;

import com.miany.psycheecho.content.EchoCategory;
import com.miany.psycheecho.content.EchoNote;
import com.miany.psycheecho.service.EchoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;

import java.util.Comparator;
import java.util.List;

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
        echo.getStatus().setTime("刚刚");
        echo.getStatus().setVisitCount(0L);

        if (echo.getSpec().getCategoryName() == null) {
            echo.getSpec().setCategoryName("生活");
        }
        if (echo.getStatus().getCategoryId() == null) {
            echo.getStatus().setCategoryId("生活");
        }

        return getContextUser()
            .doOnNext(user -> {
                if (echo.getSpec().getAuthor() == null || echo.getSpec().getAuthor().isEmpty()) {
                    echo.getSpec().setAuthor(user.getSpec().getDisplayName());
                }
                if (echo.getSpec().getAvatar() == null || echo.getSpec().getAvatar().isEmpty()) {
                    var avatar = user.getSpec().getAvatar();
                    echo.getSpec().setAvatar(avatar != null ? avatar : "");
                }
            })
            .then(Mono.fromRunnable(() -> {
                extensionClient.create(echo);
                
                String categoryId = echo.getStatus().getCategoryId();
                if (categoryId != null && !categoryId.equals("1") && !categoryId.equals("全部")) {
                    updateCategoryCount(categoryId, 1);
                }
            }))
            .thenReturn(echo);
    }
    
    private Mono<User> getContextUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(auth -> {
                Object principal = auth.getPrincipal();
                if (principal instanceof User) {
                    return (User) principal;
                }
                var user = new User();
                var spec = new User.UserSpec();
                spec.setDisplayName("Administrator");
                user.setSpec(spec);
                return user;
            })
            .onErrorReturn(createDefaultUser());
    }
    
    private User createDefaultUser() {
        var user = new User();
        var metadata = new run.halo.app.extension.Metadata();
        metadata.setName("admin");
        user.setMetadata(metadata);
        var spec = new User.UserSpec();
        spec.setDisplayName("Administrator");
        user.setSpec(spec);
        return user;
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
                new String[]{"全部", "folder"},
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
        if (categoryId == null || categoryId.equals("1") || categoryId.equals("全部")) {
            return;
        }

        var categoryOpt = extensionClient.fetch(EchoCategory.class, categoryId);
        categoryOpt.ifPresent(category -> {
            int currentCount = category.getSpec().getCount() != null ? category.getSpec().getCount() : 0;
            category.getSpec().setCount(Math.max(0, currentCount + delta));
            extensionClient.update(category);
        });
    }
}
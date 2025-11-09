package com.gdg.jwtpractice.controller;

import com.gdg.jwtpractice.dto.post.PostInfoResponseDto;
import com.gdg.jwtpractice.dto.post.PostSaveRequestDto;
import com.gdg.jwtpractice.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostInfoResponseDto> createPost(
            @RequestBody PostSaveRequestDto requestDto,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(postService.createPost(requestDto, userId));
    }

    @GetMapping
    public ResponseEntity<List<PostInfoResponseDto>> getAllPosts() {
        return ResponseEntity.ok(postService.findAllPosts());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostInfoResponseDto> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.findPostById(postId));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<PostInfoResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestBody PostSaveRequestDto requestDto,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(postService.updatePost(postId, requestDto, userId));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }
}


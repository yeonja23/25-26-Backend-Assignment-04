package com.gdg.jwtpractice.service;

import com.gdg.jwtpractice.domain.Post;
import com.gdg.jwtpractice.domain.User;
import com.gdg.jwtpractice.dto.post.PostInfoResponseDto;
import com.gdg.jwtpractice.dto.post.PostSaveRequestDto;
import com.gdg.jwtpractice.repository.PostRepository;
import com.gdg.jwtpractice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostInfoResponseDto createPost(PostSaveRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .user(user)
                .build();

        postRepository.save(post);
        return PostInfoResponseDto.from(post);
    }

    @Transactional(readOnly = true)
    public List<PostInfoResponseDto> findAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(PostInfoResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostInfoResponseDto findPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return PostInfoResponseDto.from(post);
    }

    @Transactional
    public PostInfoResponseDto updatePost(Long postId, PostSaveRequestDto requestDto, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인 게시글만 수정할 수 있습니다.");
        }

        post.update(requestDto.title(), requestDto.content());
        return PostInfoResponseDto.from(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인 게시글만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }
}

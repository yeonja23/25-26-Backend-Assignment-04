package com.gdg.jwtpractice.repository;

import com.gdg.jwtpractice.domain.Post;
import com.gdg.jwtpractice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);
}


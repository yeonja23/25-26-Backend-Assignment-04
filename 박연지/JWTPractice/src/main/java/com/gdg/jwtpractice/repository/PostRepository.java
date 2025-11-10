package com.gdg.jwtpractice.repository;

import com.gdg.jwtpractice.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}


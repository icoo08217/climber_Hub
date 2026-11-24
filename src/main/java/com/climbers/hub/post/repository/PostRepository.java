package com.climbers.hub.post.repository;

import com.climbers.hub.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByGym_GymIdOrderByCreatedAtDesc(Long gymId);

    List<Post> findByGymIsNullOrderByCreatedAtDesc();
}

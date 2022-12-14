package com.ktk.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Subreddit {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;
    
    private String name;
    
    private String description;
    
    @OneToMany(mappedBy = "postId", fetch = LAZY)
    private List<Post> posts;
    
    private Instant createdDate;
    
    @ManyToOne(fetch = LAZY)
    private Member member;
}
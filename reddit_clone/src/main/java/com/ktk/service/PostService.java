package com.ktk.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ktk.domain.dto.PostRequest;
import com.ktk.domain.dto.PostResponse;
import com.ktk.domain.dto.VoteDto;
import com.ktk.domain.dto.VoteResponse;
import com.ktk.domain.entity.Member;
import com.ktk.domain.entity.Post;
import com.ktk.domain.entity.Subreddit;
import com.ktk.domain.entity.Vote;
import com.ktk.domain.entity.VoteType;
import com.ktk.exception.RedditException;
import com.ktk.exception.voteException;
import com.ktk.mapper.PostMapper;
import com.ktk.repository.MemberRepository;
import com.ktk.repository.PostRepository;
import com.ktk.repository.SubredditRepository;
import com.ktk.repository.VoteRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostService {
	private final PostMapper postMapper;
	private final JwtAuthService authService;
	private final PostRepository postRepository;
	private final VoteRepository voteRepository;
	private final MemberRepository memberRepository;
	private final SubredditRepository subredditRepository;
	
	@Transactional
	public List<PostResponse> getAllPost(){
		return postRepository.findALLByOrderByCreatedDateDesc().stream()
								.map(postMapper::mapToDto)
								.collect(Collectors.toList());
	}
	
	public PostResponse getPostById(Long id) {
		Post post = postRepository.findById(id)
                .orElseThrow(() -> new RedditException(id.toString()));
        return postMapper.mapToDto(post);
	}
	
	public void save(PostRequest postRequest) {
        Subreddit subreddit = subredditRepository.findByName(postRequest.getSubredditName())
                .orElseThrow(() -> new RedditException(postRequest.getSubredditName()));
        postRepository.save(postMapper.map(postRequest, subreddit, authService.getCurrentMember()));
    }

	@Transactional(readOnly = true)
    public List<PostResponse> getPostsBySubreddit(Long subredditId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new RedditException(subredditId.toString()));
        List<Post> posts = postRepository.findAllBySubreddit(subreddit);
        return posts.stream().map(postMapper::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUsername(String username) {
        Member member = memberRepository.findByName(username)
                .orElseThrow(() -> new RedditException(username));
        return postRepository.findByMemberOrderByCreatedDateDesc(member)
                .stream()
                .map(postMapper::mapToDto)
                .collect(Collectors.toList());
    }
    
    public VoteResponse vote(VoteDto voteDto) {
    	Post post = postRepository.findById(voteDto.getPostId())
				.orElseThrow(() -> new RedditException("Not Found post with post id - " + voteDto.getPostId()));
    	
    	Member member = authService.getCurrentMember();
    	
    	Optional<Vote> vote = voteRepository.findTopByPostAndMemberOrderByVoteIdDesc(post, member);
    	
    	if(vote.isPresent() && vote.get().getVoteType().equals(voteDto.getVoteType())) {
    		throw new voteException("You have already " + voteDto.getVoteType() + "'d for this post");
    	}
    	
    	Integer voteCount = post.getVoteCount() == null ? 0 : post.getVoteCount();
    	if(VoteType.UP.equals(voteDto.getVoteType())) {
    		post.setVoteCount(voteCount + 1);
    	}else {
    		post.setVoteCount(voteCount - 1);
    	}
    	
    	postRepository.save(post);
    	voteRepository.save(mapToVote(voteDto, post, member));
    	
    	return postMapper.mapToVoteResponse(post);
    }
    
    private Vote mapToVote(VoteDto voteDto, Post post, Member member) {
    	return Vote.builder()
    				.voteType(voteDto.getVoteType())
    				.member(member)
    				.post(post)
    				.build();
    }
}

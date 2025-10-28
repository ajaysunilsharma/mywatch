package com.goatwatches.repository;

import com.goatwatches.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, String> {
    Optional<Vote> findByWatchIdAndVoterToken(String watchId, String voterToken);
    long countByWatchIdAndVoteValue(String watchId, int voteValue);
}

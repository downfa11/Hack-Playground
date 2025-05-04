package com.ns.solve.repository;

import com.ns.solve.domain.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.creator WHERE c.problem.id = :problemId")
    List<Comment> findByProblemId(Long problemId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.creator WHERE c.board.id = :boardId")
    List<Comment> findByBoardId(Long boardId);
}

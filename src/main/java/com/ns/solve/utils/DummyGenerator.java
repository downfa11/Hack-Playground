package com.ns.solve.utils;

import com.ns.solve.domain.entity.*;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.problem.ProblemType;
import com.ns.solve.domain.entity.problem.WargameProblem;
import com.ns.solve.repository.CommentRepository;
import com.ns.solve.repository.SolvedRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.board.BoardRepository;
import com.ns.solve.repository.problem.ProblemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

@Slf4j
@Component
public class DummyGenerator {

    @Autowired private UserRepository userRepository;

    @Autowired private BoardRepository boardRepository;

    @Autowired private CommentRepository commentRepository;

    @Autowired private ProblemRepository problemRepository;

    @Autowired private SolvedRepository solvedRepository;

    private static final Random RANDOM = new Random();

    public void generateDummyData(int userCount, int boardCount, int boardCommentCount, int problemCount, int probelmCommentCount, int solvedCount) {
        generateDummyUsers(userCount);
        generateDummyBoardsAndComments(userCount, boardCount, boardCommentCount);
        generateDummyProblemsAndComments(userCount, problemCount, probelmCommentCount);
        generateDummySolvedData(userCount, problemCount, solvedCount);

        log.info("dummy create successfully.");
    }

    @Transactional
    public void generateDummyUsers(int userCount) {
        log.info("generateDummyUsers");

        for (int i = 1; i <= userCount; i++) {
            User user = new User();
            user.setNickname("user" + i);
            user.setRole(Role.ROLE_MEMBER);
            user.setAccount("account" + i);
            user.setPassword("password" + i);
            user.setScore(RANDOM.nextLong(0, 100));
            user.setCreated(LocalDateTime.now());
            user.setLastActived(LocalDateTime.now());

            userRepository.save(user);
        }
    }

    @Transactional
    public void generateDummyBoardsAndComments(int userCount, int problemCount, int commentCount) {
        log.info("generateDummyBoardsAndComments");

        for (int i = 1; i <= problemCount; i++) {
            Board board = new Board();
            board.setTitle("Board Title " + i);
            board.setType(i % 2 == 0 ? BoardType.ANNOUNCE : BoardType.FREE);

            User creator = userRepository.findById(RANDOM.nextLong(1, userCount)).orElseThrow();
            board.setCreator(creator);
            board.setContents("tset contents " + i);
            board.setCreatedAt(LocalDateTime.now());
            board.setUpdatedAt(LocalDateTime.now());
            boardRepository.save(board);

            for (int j = 1; j <= commentCount; j++) {
                Comment comment = new Comment();
                comment.setContent("Comment content for board " + i + " - " + j);
                comment.setType(CommentType.BOARD);
                comment.setCreator(creator);
                comment.setBoard(board);

                comment.setCreatedAt(LocalDateTime.now());
                comment.setUpdatedAt(LocalDateTime.now());
                commentRepository.save(comment);
            }
        }
    }

    @Transactional
    public void generateDummyProblemsAndComments(int userCount, int boardCount, int commentCount) {
        log.info("generateDummyProblemsAndComments");

        for (int i = 1; i <= boardCount; i++) {
            WargameProblem problem = new WargameProblem();
            problem.setTitle("Problem Title " + i);
            problem.setIsChecked(i % 2 == 0);
            problem.setType(ProblemType.WARGAME);
            problem.setCreator(userRepository.findById(RANDOM.nextLong(1, userCount)).orElseThrow());
            problem.setAttemptCount(0);
            problem.setEntireCount(0.0);
            problem.setCorrectCount(0.0);
            problem.setSource("source" + i);
            problem.setReviewer("reviewer" + i);
            problem.setTags(Arrays.asList("tag" + i, "tag2"));
            problem.setCreatedAt(LocalDateTime.now());
            problem.setUpdatedAt(LocalDateTime.now());

            WargameProblem savedProblem = problemRepository.save(problem);

            for (int j = 1; j <= commentCount; j++) {
                Comment comment = new Comment();
                comment.setContent("Comment content for problem " + i + " - " + j);
                comment.setType(CommentType.PROBLEM);
                comment.setCreator(userRepository.findById(RANDOM.nextLong(1, userCount)).orElseThrow());
                comment.setProblem(savedProblem);

                comment.setCreatedAt(LocalDateTime.now());
                comment.setUpdatedAt(LocalDateTime.now());
                commentRepository.save(comment);
            }
        }
    }


    @Transactional
    public void generateDummySolvedData(int userCount, int problemCount, int solvedCount) {
        log.info("generateDummySolvedData");

        for (int i = 1; i <= problemCount; i++) {
            Problem problem = problemRepository.findById((long) i).orElseThrow();

            for (int j = 1; j <= solvedCount; j++) {
                Solved solved = new Solved();
                solved.setSolve(i % 2 == 0);
                solved.setSolvedUser(userRepository.findById(RANDOM.nextLong(1, userCount)).orElseThrow());
                solved.setSolvedProblem(problem);
                solved.setSolvedTime(LocalDateTime.now());
                solvedRepository.save(solved);
            }
        }
    }

}

package com.company.app.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.company.app.domain.entity.Task;
import com.company.app.domain.valueobject.Priority;
import com.company.app.domain.valueobject.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Find tasks by board ID
    List<Task> findByBoardId(String boardId);

    // Find tasks assigned to user
    List<Task> findByAssignedTo(String userId);

    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);

    // Find overdue tasks (JPA query syntax)
    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != com.company.app.domain.valueobject.TaskStatus.DONE")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    // Find tasks by board and status
    List<Task> findByBoardIdAndStatus(String boardId, TaskStatus status);

    // Additional useful queries
    List<Task> findByBoardIdOrderByCreatedAtDesc(String boardId);

    List<Task> findByAssignedToAndStatus(String userId, TaskStatus status);

    long countByBoardIdAndStatus(String boardId, TaskStatus status);

    // Additional queries that might be useful
    List<Task> findByPriorityOrderByDueDateAsc(Priority priority);

    @Query("SELECT t FROM Task t WHERE t.boardId = :boardId AND t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findByBoardIdAndDueDateBetween(
            @Param("boardId") String boardId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :userId AND t.dueDate < CURRENT_TIMESTAMP AND t.status != 'DONE'")
    List<Task> findUserOverdueTasks(@Param("userId") String userId);

    // Find tasks with pagination support
    Page<Task> findByBoardId(String boardId, Pageable pageable);

    // Find tasks by multiple statuses
    @Query("SELECT t FROM Task t WHERE t.boardId = :boardId AND t.status IN :statuses")
    List<Task> findByBoardIdAndStatusIn(
            @Param("boardId") String boardId,
            @Param("statuses") List<TaskStatus> statuses);
}
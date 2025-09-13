package com.company.app.domain.repository;

import com.company.app.domain.entity.Task;
import com.company.app.domain.valueobject.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    
    // Find tasks by board ID
    List<Task> findByBoardId(String boardId);
    
    // Find tasks assigned to user
    List<Task> findByAssignedTo(String userId);
    
    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);
    
    // Find overdue tasks (MongoDB query syntax)
    @Query("{ 'dueDate' : { $lt: ?0 }, 'status' : { $ne: 'DONE' } }")
    List<Task> findOverdueTasks(LocalDateTime now);
    
    // Find tasks by board and status
    @Query("{ 'boardId' : ?0, 'status' : ?1 }")
    List<Task> findByBoardIdAndStatus(String boardId, TaskStatus status);
    
    // Additional useful queries
    List<Task> findByBoardIdOrderByCreatedAtDesc(String boardId);
    
    List<Task> findByAssignedToAndStatus(String userId, TaskStatus status);
    
    long countByBoardIdAndStatus(String boardId, TaskStatus status);
}
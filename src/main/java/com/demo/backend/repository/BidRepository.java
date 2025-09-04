package com.demo.backend.repository;

import com.demo.backend.model.Bid;
import com.demo.backend.model.Task;
import com.demo.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    @Query("SELECT b FROM Bid b JOIN FETCH b.freelancer JOIN FETCH b.task t WHERE t = :task")
    List<Bid> findByTask(@Param("task") Task task);

    @Query("SELECT b FROM Bid b JOIN FETCH b.task t JOIN FETCH t.createdBy WHERE b.id = :bidId")
    Optional<Bid> findByIdWithTaskAndCreator(@Param("bidId") Long bidId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Bid b SET b.status = 'REJECTED' WHERE b.task.id = :taskId AND b.id != :acceptedBidId AND b.status = 'PENDING'")
    void rejectOtherBids(@Param("taskId") Long taskId, @Param("acceptedBidId") Long acceptedBidId);

    Optional<Bid> findByTaskAndFreelancer(Task task, User freelancer);
}

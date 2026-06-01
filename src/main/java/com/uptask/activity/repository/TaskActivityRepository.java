package com.uptask.activity.repository;

import com.uptask.activity.entity.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {

    List<TaskActivity> findAllByTaskIdOrderByCreatedAtDesc(Long taskId);
}

package com.webank.wecube.core.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webank.wecube.core.domain.workflow.TaskNodeExecVariableEntity;

public interface TaskNodeExecVariableEntityRepository extends JpaRepository<TaskNodeExecVariableEntity, Integer> {

    @Query("select t from TaskNodeExecVariableEntity t  where t.taskNodeExecLog.id = :execLogId")
    List<TaskNodeExecVariableEntity> findEntitiesByExecLog(Integer execLogId);
}

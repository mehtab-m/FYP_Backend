package com.scd.fyp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByLeaderId(Long leaderId);

//    @Query("SELECT g FROM Group g JOIN g.members gm WHERE gm.studentId = :userId")
//    Group findByMemberId(@Param("userId") Long userId);


    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.id.studentId = :userId")
    Group findByMemberId(@Param("userId") Long userId);



}

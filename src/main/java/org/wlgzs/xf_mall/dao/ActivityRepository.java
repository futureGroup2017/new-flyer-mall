package org.wlgzs.xf_mall.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.wlgzs.xf_mall.entity.Activity;

import java.util.List;

/**
 * @Auther: 阿杰
 * @Date: 2018/5/10 09:06
 * @Description:
 */
public interface ActivityRepository extends JpaRepository<Activity, Long>,JpaSpecificationExecutor<Activity> {
    Activity findById(long activitySumId);

    @Query(value = "SELECT * FROM activity WHERE activity_name = ?",nativeQuery = true)
    Activity findByActivityName(String activity_name);

    @Query("FROM Activity a WHERE a.activity_name=?1")
    Activity selectActivity(String activity_name);

    @Query(value = "SELECT * FROM activity WHERE activity_name = ?",nativeQuery = true)
    List<Activity> findActivity(String activity_name);
}

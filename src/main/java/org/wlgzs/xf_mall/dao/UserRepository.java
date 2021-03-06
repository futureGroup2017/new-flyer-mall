package org.wlgzs.xf_mall.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import org.wlgzs.xf_mall.entity.User ;

/**
 * @Auther: 阿杰
 * @Date: 2018/4/15 13:16
 * @Description: 接口
 */
public interface UserRepository extends JpaRepository<User, Long>,JpaSpecificationExecutor<User>  {
    User findById(long userId);

    /**     
     * @author 胡亚星
     * @date 2018/5/1 10:10  
     * @param   
     * @return   
     *@Description:修改用户头像路径
     */
    @Query("UPDATE User u SET u.user_avatar=user_avatar WHERE u.userId=id")
    @Modifying
    @Transactional
    void ModifyAvatar(String user_avatar,long id);

    //修改密码：判断用户输入密码是否正确
    @Query("FROM User u WHERE u.user_password=?1 and u.userId=?2")
    User checkPassWord(String user_password,long id);

    @Query("UPDATE User u SET u.user_password=?1 WHERE u.userId=?2")
    @Modifying
    @Transactional
    void changePassword(String user_rePassword,long userId);

    @Query("UPDATE User u SET u.user_password=?1 WHERE u.user_mail=?2")
    @Modifying
    @Transactional
    void changePassword(String user_password,String user_mail);

    @Query("UPDATE User u SET u.user_mail=?1 WHERE u.userId=?2")
    @Modifying
    @Transactional
    void changeEmail(String user_mail,long userId);

    //批量删除用户
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.userId in :Ids")
    void deleteByIds(@Param(value = "Ids") long [] Ids);

    //修改手机号
    @Query("UPDATE User u SET u.user_phone=?1 WHERE u.userId=?2")
    @Modifying
    @Transactional
    void changePhone(String user_phone,long userId);
}
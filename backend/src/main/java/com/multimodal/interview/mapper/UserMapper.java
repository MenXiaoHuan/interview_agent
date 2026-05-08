package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * User 数据访问接口。
 */
@Mapper
public interface UserMapper {
/**
 * 根据用户名查询用户信息
 * @param username 用户名
 * @return 返回符合条件的用户对象
 */
    @Select("SELECT * FROM user WHERE username = #{username} AND status = 1")  // 使用MyBatis的@Select注解执行SQL查询，从user表中查询username字段匹配且status为1的用户记录
    User findByUsername(String username);  // 定义一个方法，接收username参数，返回User对象

/**
 * 根据用户ID查询用户信息
 * 该方法会从数据库中查询指定ID且状态为1的用户记录
 *
 * @param id 用户ID，用于精确匹配用户记录
 * @return 返回符合条件的User对象，如果未找到则可能返回null
 */
    @Select("SELECT * FROM user WHERE id = #{id} AND status = 1")
    User findById(Long id);

/**
 * 根据分页参数查询所有状态为1的用户
 * @param size 每页显示的记录数
 * @param offset 偏移量，用于计算分页的起始位置
 * @return 返回用户列表，包含查询到的所有符合条件的用户记录
 */
    @Select("SELECT * FROM user WHERE status = 1 LIMIT #{size} OFFSET #{offset}")
    List<User> findAll(@Param("size") int size, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM user WHERE status = 1")
    int countAllUsers();

    @Select("SELECT * FROM user WHERE email = #{email} AND status = 1")
    User findByEmail(String email);

    @Select("SELECT * FROM user WHERE phone = #{phone} AND status = 1")
    User findByPhone(String phone);

    @Insert("INSERT INTO user (username, password, email, phone, user_type, status, eye_care_mode, surprise_mode, created_at, updated_at) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, 1, 1, COALESCE(#{eyeCareMode}, 0), COALESCE(#{surpriseMode}, 0), " +
            "NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET last_login_at = NOW() WHERE id = #{id}")
    int updateLastLoginTime(Long id);

    @Update("UPDATE user SET eye_care_mode = #{eyeCareMode}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateEyeMode(User user);

    @Update("UPDATE user SET surprise_mode = #{surpriseMode}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateSurpriseMode(User user);

    @Update("UPDATE user SET nickname = #{nickname}, updated_at = NOW() WHERE id = #{id}")
    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    @Update("UPDATE user SET avatar_url = #{avatarUrl}, updated_at = NOW() WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);

    @Update("UPDATE user SET password = #{password}, updated_at = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE user SET email = #{email}, updated_at = NOW() WHERE id = #{id}")
    int updateEmail(@Param("id") Long id, @Param("email") String email);

    @Update("UPDATE user SET phone = #{phone}, updated_at = NOW() WHERE id = #{id}")
    int updatePhone(@Param("id") Long id, @Param("phone") String phone);

    @Update("UPDATE user SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    @Update("UPDATE user SET user_type = #{userType}, updated_at = NOW() WHERE id = #{id}")
    int updateUserType(@Param("id") Long id, @Param("userType") int userType);

    @Update("UPDATE user SET gender = #{gender}, updated_at = NOW() WHERE id = #{id}")
    int updateGender(@Param("id") Long id, @Param("gender") int gender);

    @Delete("DELETE FROM user WHERE username = #{username}")
    int deleteByUsername(String username);
}

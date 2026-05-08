package com.multimodal.interview.mapper;

import com.multimodal.interview.entity.PasswordResetToken;
import org.apache.ibatis.annotations.*;

/**
 * PasswordResetToken 数据访问接口。
 */
@Mapper
public interface PasswordResetTokenMapper {
    @Insert("INSERT INTO password_reset_token (user_id, channel, contact, code, expires_at, created_at) " +
            "VALUES (#{userId}, #{channel}, #{contact}, #{code}, #{expiresAt}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PasswordResetToken token);

    @Select("SELECT * FROM password_reset_token WHERE contact = #{contact} AND code = #{code} AND used_at IS NULL AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    PasswordResetToken findValid(String contact, String code);

    @Update("UPDATE password_reset_token SET used_at = NOW() WHERE id = #{id}")
    int markUsed(Long id);

    @Select("SELECT * FROM password_reset_token WHERE contact = #{contact} AND used_at IS NULL AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1")
    PasswordResetToken findLatestValidByContact(String contact);
}

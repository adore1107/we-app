package com.songjia.textile.service;

import com.songjia.textile.entity.User;
import com.songjia.textile.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserMapper userMapper;

    /**
     * 根据openid查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findByOpenid(String openid) {
        User user = userMapper.findByOpenid(openid);
        return Optional.ofNullable(user);
    }

    /**
     * 创建或更新用户（微信登录时使用）
     */
    public User createOrUpdateUser(String openid, String nickname, String avatarUrl) {
        User existingUser = userMapper.findByOpenid(openid);

        if (existingUser != null) {
            // 更新现有用户信息
            if (nickname != null) {
                existingUser.setNickname(nickname);
            }
            if (avatarUrl != null) {
                existingUser.setAvatarUrl(avatarUrl);
            }
            existingUser.setLastLoginAt(LocalDateTime.now());

            int result = userMapper.updateById(existingUser);
            if (result > 0) {
                log.info("更新用户信息: openid={}", openid);
                return existingUser;
            } else {
                log.error("更新用户信息失败: openid={}", openid);
                throw new RuntimeException("更新用户信息失败");
            }
        } else {
            // 创建新用户
            User newUser = User.builder()
                    .openid(openid)
                    .nickname(nickname)
                    .avatarUrl(avatarUrl)
                    .lastLoginAt(LocalDateTime.now())
                    .build();

            int result = userMapper.insert(newUser);
            if (result > 0) {
                log.info("创建新用户: openid={}, id={}", openid, newUser.getId());
                return newUser;
            } else {
                log.error("创建用户失败: openid={}", openid);
                throw new RuntimeException("创建用户失败");
            }
        }
    }

    /**
     * 创建或更新用户（支持完整用户信息，用于开发环境登录）
     */
    public User createOrUpdateUserWithFullInfo(String openid, String nickname, String avatarUrl,
                                                String companyName, String realName, String position,
                                                String industry, String city, String phone) {
        User existingUser = userMapper.findByOpenid(openid);

        if (existingUser != null) {
            // 更新现有用户信息
            if (nickname != null && !nickname.isEmpty()) {
                existingUser.setNickname(nickname);
            }
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                existingUser.setAvatarUrl(avatarUrl);
            }
            if (companyName != null && !companyName.isEmpty()) {
                existingUser.setCompanyName(companyName);
            }
            if (realName != null && !realName.isEmpty()) {
                existingUser.setRealName(realName);
            }
            if (position != null && !position.isEmpty()) {
                existingUser.setPosition(position);
            }
            if (industry != null && !industry.isEmpty()) {
                existingUser.setIndustry(industry);
            }
            if (city != null && !city.isEmpty()) {
                existingUser.setCity(city);
            }
            if (phone != null && !phone.isEmpty()) {
                existingUser.setPhone(phone);
            }
            existingUser.setLastLoginAt(LocalDateTime.now());

            int result = userMapper.updateById(existingUser);
            if (result > 0) {
                log.info("更新用户完整信息: openid={}, userId={}", openid, existingUser.getId());
                return existingUser;
            } else {
                log.error("更新用户完整信息失败: openid={}", openid);
                throw new RuntimeException("更新用户完整信息失败");
            }
        } else {
            // 创建新用户（包含完整信息）
            User newUser = User.builder()
                    .openid(openid)
                    .nickname(nickname != null ? nickname : "微信用户")
                    .avatarUrl(avatarUrl)
                    .companyName(companyName)
                    .realName(realName)
                    .position(position)
                    .industry(industry)
                    .city(city)
                    .phone(phone)
                    .lastLoginAt(LocalDateTime.now())
                    .build();

            int result = userMapper.insert(newUser);
            if (result > 0) {
                log.info("创建新用户（完整信息）: openid={}, id={}, companyName={}",
                         openid, newUser.getId(), companyName);
                return newUser;
            } else {
                log.error("创建用户失败: openid={}", openid);
                throw new RuntimeException("创建用户失败");
            }
        }
    }

    /**
     * 更新用户详细信息
     */
    public User updateUserProfile(Integer userId, String phone, String companyName,
                                  String realName, String position, String industry, String city) {
        User user = userMapper.findByIdAndDeleted(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (phone != null) {
            user.setPhone(phone);
        }
        if (companyName != null) {
            user.setCompanyName(companyName);
        }
        if (realName != null) {
            user.setRealName(realName);
        }
        if (position != null) {
            user.setPosition(position);
        }
        if (industry != null) {
            user.setIndustry(industry);
        }
        if (city != null) {
            user.setCity(city);
        }

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("更新用户详细信息: userId={}", userId);
            return user;
        } else {
            log.error("更新用户详细信息失败: userId={}", userId);
            throw new RuntimeException("更新用户详细信息失败");
        }
    }

    /**
     * 获取用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Integer userId) {
        User user = userMapper.findByIdAndDeleted(userId);
        return Optional.ofNullable(user);
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime(Integer userId) {
        int result = userMapper.updateLastLoginTime(userId);
        if (result > 0) {
            log.info("更新用户最后登录时间: userId={}", userId);
        } else {
            log.warn("更新用户最后登录时间失败: userId={}", userId);
        }
    }

    /**
     * 检查用户是否存在
     */
    @Transactional(readOnly = true)
    public boolean userExists(Integer userId) {
        User user = userMapper.findByIdAndDeleted(userId);
        return user != null;
    }

    /**
     * 根据手机号查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        User user = userMapper.findByPhone(phone);
        return Optional.ofNullable(user);
    }

    /**
     * 删除用户（逻辑删除）
     */
    public boolean deleteUser(Integer userId) {
        int result = userMapper.deleteById(userId);
        if (result > 0) {
            log.info("成功删除用户: userId={}", userId);
            return true;
        } else {
            log.error("删除用户失败: userId={}", userId);
            return false;
        }
    }
}
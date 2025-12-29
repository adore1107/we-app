package com.songjia.textile.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Inquiry;
import com.songjia.textile.mapper.InquiryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 询价服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryMapper inquiryMapper;

    /**
     * 创建询价记录
     */
    @Transactional
    public Inquiry createInquiry(Integer userId, Integer productId, Integer quantity, String message) {
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .message(message)
                .build();

        int result = inquiryMapper.insert(inquiry);
        if (result > 0) {
            log.info("创建询价记录: userId={}, productId={}, quantity={}", userId, productId, quantity);
            return inquiry;
        } else {
            log.error("创建询价记录失败: userId={}, productId={}", userId, productId);
            throw new RuntimeException("创建询价记录失败");
        }
    }

    /**
     * 获取用户询价记录
     */
    @Transactional(readOnly = true)
    public IPage<Inquiry> getUserInquiries(Integer userId, int page, int size) {
        Page<Inquiry> pageable = new Page<>(page + 1, size);
        IPage<Inquiry> result = inquiryMapper.findByUserId(userId, pageable);
        log.info("获取用户询价记录: userId={}, 第 {} 页, 每页 {} 个, 共 {} 条记录",
                userId, page, size, result.getTotal());
        return result;
    }

    /**
     * 获取用户询价记录（非分页）
     */
    @Transactional(readOnly = true)
    public List<Inquiry> getAllUserInquiries(Integer userId) {
        List<Inquiry> inquiries = inquiryMapper.findByUserId(userId);
        log.info("获取用户所有询价记录: userId={}, 数量={}", userId, inquiries.size());
        return inquiries;
    }

    /**
     * 获取所有询价记录（管理员用）
     */
    @Transactional(readOnly = true)
    public IPage<Inquiry> getAllInquiries(int page, int size) {
        Page<Inquiry> pageable = new Page<>(page + 1, size);
        IPage<Inquiry> result = inquiryMapper.findByStatus(0, pageable);
        log.info("获取所有待处理询价记录: 第 {} 页, 每页 {} 个, 共 {} 条记录",
                page, size, result.getTotal());
        return result;
    }

    /**
     * 获取所有询价记录（非分页）
     */
    @Transactional(readOnly = true)
    public List<Inquiry> getAllInquiriesByStatus(Integer status) {
        List<Inquiry> inquiries = inquiryMapper.findByStatus(status);
        log.info("获取询价记录: status={}, 数量={}", status, inquiries.size());
        return inquiries;
    }

    /**
     * 根据ID获取询价记录
     */
    @Transactional(readOnly = true)
    public Optional<Inquiry> getInquiryById(Integer id) {
        Inquiry inquiry = inquiryMapper.selectById(id);
        return Optional.ofNullable(inquiry);
    }

    /**
     * 更新询价状态（管理员回复）
     */
    @Transactional
    public Inquiry updateInquiryReply(Integer inquiryId, String reply) {
        Inquiry inquiry = inquiryMapper.selectById(inquiryId);
        if (inquiry == null) {
            throw new RuntimeException("询价记录不存在");
        }

        inquiry.setAdminReply(reply);
        inquiry.setStatus(1); // 已处理
        inquiry.setReplyTime(LocalDateTime.now());

        int result = inquiryMapper.updateById(inquiry);
        if (result > 0) {
            log.info("更新询价回复: inquiryId={}", inquiryId);
            return inquiry;
        } else {
            log.error("更新询价回复失败: inquiryId={}", inquiryId);
            throw new RuntimeException("更新询价回复失败");
        }
    }

    /**
     * 获取用户询价数量
     */
    @Transactional(readOnly = true)
    public int getUserInquiryCount(Integer userId) {
        int count = inquiryMapper.countByUserId(userId);
        log.info("用户询价数量: userId={}, count={}", userId, count);
        return count;
    }

    /**
     * 获取指定状态的询价数量
     */
    @Transactional(readOnly = true)
    public int getInquiryCountByStatus(Integer status) {
        int count = inquiryMapper.countByStatus(status);
        log.info("询价记录数量: status={}, count={}", status, count);
        return count;
    }

    /**
     * 更新询价记录
     */
    @Transactional
    public Inquiry updateInquiry(Inquiry inquiry) {
        int result = inquiryMapper.updateById(inquiry);
        if (result > 0) {
            log.info("更新询价记录: id={}", inquiry.getId());
            return inquiry;
        } else {
            log.error("更新询价记录失败: id={}", inquiry.getId());
            throw new RuntimeException("更新询价记录失败");
        }
    }

    /**
     * 删除询价记录（逻辑删除）
     */
    @Transactional
    public boolean deleteInquiry(Integer id) {
        int result = inquiryMapper.deleteById(id);
        if (result > 0) {
            log.info("删除询价记录: id={}", id);
            return true;
        } else {
            log.error("删除询价记录失败: id={}", id);
            return false;
        }
    }

    /**
     * 批量更新询价状态
     */
    @Transactional
    public int batchUpdateInquiryStatus(List<Integer> ids, Integer status) {
        int updatedCount = 0;
        for (Integer id : ids) {
            Inquiry inquiry = inquiryMapper.selectById(id);
            if (inquiry != null) {
                inquiry.setStatus(status);
                if (inquiryMapper.updateById(inquiry) > 0) {
                    updatedCount++;
                }
            }
        }
        log.info("批量更新询价状态: 更新数量={}, 状态={}", updatedCount, status);
        return updatedCount;
    }
}
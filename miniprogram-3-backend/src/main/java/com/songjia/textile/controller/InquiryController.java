package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Inquiry;
import com.songjia.textile.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 询价控制器
 */
@Slf4j
@RestController
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 提交询价
     */
    @PostMapping("/submit")
    public Result<Inquiry> submitInquiry(@RequestParam @NotNull Integer userId,
                                         @RequestParam @NotNull Integer productId,
                                         @RequestParam Integer quantity,
                                         @RequestParam(required = false) String message) {
        try {
            Inquiry inquiry = inquiryService.createInquiry(userId, productId, quantity, message);
            return Result.success("询价提交成功，我们会尽快联系您！", inquiry);
        } catch (Exception e) {
            log.error("提交询价失败: userId={}, productId={}", userId, productId, e);
            return Result.error("提交询价失败");
        }
    }

    /**
     * 获取用户询价记录
     */
    @GetMapping("/list/{userId}")
    public Result<IPage<Inquiry>> getUserInquiries(
            @PathVariable @NotNull Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Inquiry> inquiries = inquiryService.getUserInquiries(userId, page, size);
            return Result.success("获取成功", inquiries);
        } catch (Exception e) {
            log.error("获取用户询价记录失败: userId={}", userId, e);
            return Result.error("获取询价记录失败");
        }
    }

    /**
     * 获取询价详情
     */
    @GetMapping("/detail/{id}")
    public Result<Inquiry> getInquiryDetail(@PathVariable @NotNull Integer id) {
        try {
            return inquiryService.getInquiryById(id)
                    .map(inquiry -> Result.success("获取成功", inquiry))
                    .orElse(Result.error("询价记录不存在"));
        } catch (Exception e) {
            log.error("获取询价详情失败: id={}", id, e);
            return Result.error("获取询价详情失败");
        }
    }

    /**
     * 管理员回复询价
     */
    @PostMapping("/reply/{id}")
    public Result<Inquiry> replyInquiry(@PathVariable @NotNull Integer id,
                                       @RequestParam @NotBlank String reply) {
        try {
            Inquiry inquiry = inquiryService.updateInquiryReply(id, reply);
            return Result.success("回复成功", inquiry);
        } catch (RuntimeException e) {
            log.error("回复询价失败: id={}", id, e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("回复询价失败: id={}", id, e);
            return Result.error("回复失败");
        }
    }

    /**
     * 获取所有询价记录（管理员用）
     */
    @GetMapping("/admin/list")
    public Result<IPage<Inquiry>> getAllInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Inquiry> inquiries = inquiryService.getAllInquiries(page, size);
            return Result.success("获取成功", inquiries);
        } catch (Exception e) {
            log.error("获取询价记录失败", e);
            return Result.error("获取询价记录失败");
        }
    }

    /**
     * 获取用户询价数量
     */
    @GetMapping("/count/{userId}")
    public Result<Integer> getUserInquiryCount(@PathVariable @NotNull Integer userId) {
        try {
            int count = inquiryService.getUserInquiryCount(userId);
            return Result.success("获取成功", count);
        } catch (Exception e) {
            log.error("获取用户询价数量失败: userId={}", userId, e);
            return Result.error("获取数量失败");
        }
    }
}
package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.common.result.ResultCode;
import com.multimodal.interview.dto.InterviewResultResponse;
import com.multimodal.interview.dto.InterviewSubmitRequest;
import com.multimodal.interview.dto.InterviewSubmitResponse;
import com.multimodal.interview.entity.User;
import com.multimodal.interview.entity.choiceQuestion;
import com.multimodal.interview.entity.choiceQuestionRecord;
import com.multimodal.interview.mapper.UserMapper;
import com.multimodal.interview.service.ChoiceQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 试题作答接口控制器。
 */
@Tag(name = "试题作答", description = "试题作答相关接口")
@RestController
@RequestMapping("/api/interview")
public class ChoiceQuestionController {

    @Autowired
    private ChoiceQuestionService choiceQuestionService;

    @Autowired
    private UserMapper userMapper;
    /**
     * 获取试题。
     * @return 统一响应体
     */
    @Operation(summary = "获取试题",
            description = "获取所有选择试题")
    @GetMapping("/question/getAll")
    public ApiResponse<List<choiceQuestion>> getAllQuestions(){
        return ApiResponse.success(choiceQuestionService.getAllQuestions());
    }
    /**
     * 获取试题。
     *
     * @param jobId 岗位 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取试题",
            description = "根据岗位ID获取选择试题")
    @GetMapping("/question/{jobId}")
    public ApiResponse<List<choiceQuestion>> getQuestions(@PathVariable Long jobId){
        return ApiResponse.success(choiceQuestionService.getQuestions(jobId));
    }
    /**
     * 增加试题。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "增加试题",
            description = "增加选择试题")
    @PostMapping("/question/add")
    public ApiResponse<String> addQuestion(@RequestBody choiceQuestion request){
        choiceQuestionService.addQuestion(request);
        return ApiResponse.success("添加成功");
    }
    /**
     * 更新试题。
     *
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "更新试题",
            description = "更新选择试题")
    @PutMapping("/question/update")
    public ApiResponse<String> updateQuestion(@RequestBody choiceQuestion request){
        choiceQuestionService.updateQuestion(request);
        return ApiResponse.success("更新成功");
    }
    /**
     * 删除试题。
     *
     * @param id 主键 ID
     * @return 统一响应体
     */
    @Operation(summary = "删除试题",
            description = "删除选择试题")
    @PostMapping("/question/delete/{id}")
    public ApiResponse<String> deleteQuestion(@PathVariable("id") Long id){
        choiceQuestionService.deleteQuestion(id);
        return ApiResponse.success("删除成功");
    }
    /**
     * 提交面试答案。
     *
     * @param userDetails 当前登录用户信息
     * @param request 请求体参数
     * @return 统一响应体
     */
    @Operation(summary = "提交面试答案",
            description = "提交面试答案并获取结果")
    @PostMapping("/submit")
    public ApiResponse<InterviewSubmitResponse> submitAnswers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody InterviewSubmitRequest request){
        // 从UserDetails中获取用户ID
        User user = userMapper.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ApiResponse.error(ResultCode.UNAUTHORIZED.getCode(), "用户不存在");
        }
        return ApiResponse.success(choiceQuestionService.submitAnswers(user.getId(), request));
    }
    /**
     * 获取面试结果。
     *
     * @param interviewQuestionRecordId 面试记录 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取面试结果",
            description = "根据面试记录ID获取详细的面试结果")
    @GetMapping("/result/{interviewQuestionRecordId}")
    public ApiResponse<InterviewResultResponse> getResult(@PathVariable Long interviewQuestionRecordId){
        return ApiResponse.success(choiceQuestionService.getResult(interviewQuestionRecordId));
    }
    /**
     * 获取用户试题作答记录。
     *
     * @param userId 用户 ID
     * @param evaluationType 评估类型
     * @return HTTP 响应结果
     */
    @Operation(summary = "获取用户试题作答记录",
            description = "获取指定用户、指定类型的试题作答记录")
    @GetMapping("/question/history/{userId}")
    public ResponseEntity<?> getRecordByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "SPECIAL") String evaluationType){
        List<choiceQuestionRecord> records = choiceQuestionService.getRecordByUserId(userId, evaluationType);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
    /**
     * 获取用户岗位试题作答记录。
     *
     * @param userId 用户 ID
     * @param jobId 岗位 ID
     * @param evaluationType 评估类型
     * @return HTTP 响应结果
     */
    @Operation(summary = "获取用户岗位试题作答记录",
            description = "获取指定用户、指定类型的指定岗位的试题作答记录")
    @GetMapping("/question/history/{userId}/job/{jobId}")
    public ResponseEntity<?> getRecordByUserIdAndJob(
            @PathVariable Long userId,
            @PathVariable Long jobId,
            @RequestParam(required = false, defaultValue = "SPECIAL") String evaluationType){
        List<choiceQuestionRecord> records = choiceQuestionService.getRecordByUserIdAndJob(userId, jobId, evaluationType);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    // ==================== Agent Tools（供 ReactAgent 调用） ====================
    @Tool(
            name = "choice_question_get_all",
            description = "获取全部选择题题库（不区分岗位），用于快速构造练习题或抽题"
    )
    public List<choiceQuestion> toolGetAllQuestions() {
        return choiceQuestionService.getAllQuestions();
    }

    @Tool(
            name = "choice_question_get_by_job",
            description = "根据岗位ID获取选择题列表，用于岗位专项练习与评测"
    )
    public List<choiceQuestion> toolGetQuestionsByJob(
            @ToolParam(description = "岗位ID") Long jobId
    ) {
        return choiceQuestionService.getQuestions(jobId);
    }

    @Tool(
            name = "choice_question_get_result",
            description = "根据面试记录ID获取详细面试结果（题目、作答、得分、解析等）"
    )
    public InterviewResultResponse toolGetInterviewResult(
            @ToolParam(description = "面试记录ID") Long interviewQuestionRecordId
    ) {
        return choiceQuestionService.getResult(interviewQuestionRecordId);
    }
} 

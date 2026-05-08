package com.multimodal.interview.controller;

import com.multimodal.interview.common.result.ApiResponse;
import com.multimodal.interview.entity.ScenarioQuestion;
import com.multimodal.interview.service.ScenarioQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 场景问题接口控制器。
 */
@Tag(name = "场景问题", description = "场景问题相关接口")
@RestController
@RequestMapping("/api/scenario-question")
public class ScenarioQuestionController {
    @Autowired
    private ScenarioQuestionService scenarioQuestionService;
    /**
     * 获取所有场景问题。
     * @return 统一响应体
     */
    @Operation(summary = "获取所有场景问题")
    @GetMapping("/getAll")
    public ApiResponse<List<ScenarioQuestion>> getAll(){
        List<ScenarioQuestion> question = scenarioQuestionService.getAll();
        return ApiResponse.success("查找成功", question);
    }
    /**
     * 获取场景问题。
     *
     * @param jobId 岗位 ID
     * @return 统一响应体
     */
    @Operation(summary = "获取场景问题",
            description = "根据岗位ID获取场景问题")
    @GetMapping("/get/{jobId}")
    public ApiResponse<List<ScenarioQuestion>> getQuestion(@PathVariable("jobId") Long jobId){
        List<ScenarioQuestion> question = scenarioQuestionService.getQuestion(jobId);
        return ApiResponse.success("查找成功", question);
    }
    /**
     * 创建场景问题。
     *
     * @param scenarioQuestion 场景问题参数
     * @return 统一响应体
     */
    @Operation(summary = "创建场景问题",
            description = "创建场景问题")
    @PostMapping("/create")
    public ApiResponse<String> createScenarioQuestion(@RequestBody ScenarioQuestion scenarioQuestion){
        scenarioQuestionService.createQuestion(scenarioQuestion);
        return ApiResponse.success("创建成功");
    }
    /**
     * 更新场景问题。
     *
     * @param scenarioQuestion 场景问题参数
     * @return 统一响应体
     */
    @Operation(summary = "更新场景问题",
            description = "更新场景问题")
    @PostMapping("/update")
    public ApiResponse<String> updateQuestion(@RequestBody ScenarioQuestion scenarioQuestion){
        scenarioQuestionService.updateQuestion(scenarioQuestion);
        return ApiResponse.success("更新成功");
    }
    /**
     * 删除场景问题。
     *
     * @param id 主键 ID
     * @return 统一响应体
     */
    @Operation(summary = "删除场景问题",
            description = "删除场景问题")
    @PostMapping("/delete/{id}")
    public ApiResponse<String> deleteScenarioQuestion(@PathVariable("id") Long id){
        scenarioQuestionService.deleteQuestion(id);
        return ApiResponse.success("删除成功");
    }
}

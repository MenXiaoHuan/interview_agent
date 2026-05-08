package com.multimodal.interview.reactagent.skill;

import com.alibaba.cloud.ai.graph.skills.SkillMetadata;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 单技能视图的 SkillRegistry。
 *
 * <p>为什么需要它：</p>
 * <p>
 * - 官方 skills 机制通过 {@code SkillRegistry} + {@code SkillsAgentHook} 提供 read_skill，并把 skills 列表注入系统提示
 * </p>
 * <p>
 * - 但本项目要求“每个 agent 只有自己的独有 skill”，不能一次性注册/注入所有 skill
 * </p>
 * <p>
 * 因此我们用本类把一个“全量 registry”包一层，只暴露 {@code allowedSkillName} 这一个 skill。
 * 这样每个 ReactAgent 仍然使用官方 hook 与 read_skill，同时满足渐进式披露与隔离要求。
 * </p>
 */
public final class SingleSkillRegistry implements SkillRegistry {

    private final SkillRegistry delegate;
    private final String allowedSkillName;

    /**
     * @param delegate 全量技能注册表（通常为 ClasspathSkillRegistry）
     * @param allowedSkillName 允许暴露的 skill name（与 agentKey 一致）
     */
    public SingleSkillRegistry(SkillRegistry delegate, String allowedSkillName) {
        this.delegate = delegate;
        this.allowedSkillName = allowedSkillName;
    }

    @Override
    public Optional<SkillMetadata> get(String name) {
        if (!allowedSkillName.equals(name)) {
            return Optional.empty();
        }
        return delegate.get(name);
    }

    @Override
    public List<SkillMetadata> listAll() {
        return get(allowedSkillName).map(List::of).orElseGet(List::of);
    }

    @Override
    public boolean contains(String name) {
        return allowedSkillName.equals(name) && delegate.contains(name);
    }

    @Override
    public int size() {
        return contains(allowedSkillName) ? 1 : 0;
    }

    @Override
    public void reload() {
        // reload 不做隔离：底层刷新后，本视图仍只暴露 allowedSkillName
        delegate.reload();
    }

    @Override
    public String readSkillContent(String skillName) throws IOException {
        if (!allowedSkillName.equals(skillName)) {
            throw new IOException("Skill not allowed: " + skillName);
        }
        return delegate.readSkillContent(skillName);
    }

    @Override
    public String getSkillLoadInstructions() {
        return delegate.getSkillLoadInstructions();
    }

    @Override
    public String getRegistryType() {
        return delegate.getRegistryType();
    }

    @Override
    public SystemPromptTemplate getSystemPromptTemplate() {
        // 模板里包含 placeholders；SkillsAgentHook 会结合本 registry 的 listAll() 注入技能列表并生成系统提示。
        return delegate.getSystemPromptTemplate();
    }
}

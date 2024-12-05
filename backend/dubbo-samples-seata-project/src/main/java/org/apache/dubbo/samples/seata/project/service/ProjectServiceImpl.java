package org.apache.dubbo.samples.seata.project.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.dto.*;
import org.apache.dubbo.samples.seata.project.entity.Member;
import org.apache.dubbo.samples.seata.project.entity.Project;
import org.apache.dubbo.samples.seata.project.repository.MemberRepository;
import org.apache.dubbo.samples.seata.project.repository.ProjectRepository;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@DubboService
@Service
public class ProjectServiceImpl implements ProjectService {

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private MemberRepository memberRepository;

    @DubboReference(check = false)
    private UserService userService;

    private void validateMembership(Integer projectId, Integer userId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
            
        if (!memberRepository.findByUserIdAndProjectsContaining(userId, project).isPresent()) {
            throw new RuntimeException("Access denied: user is not a member of this project");
        }
    }

    @Override
    @GlobalTransactional
    public ProjectDTO createProject(Integer ownerId, ProjectCreateBody createBody) {
        // 验证创建者是否存在
        UserDTO user = userService.getUserById(ownerId);
        Integer userId = user.getUserId();
        
        // 创建项目
        Project project = new Project();
        BeanUtils.copyProperties(createBody, project);

        project.setOwnerId(userId);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus("IN_PROGRESS");
        project = projectRepository.save(project);
        
        // 创建或获取 owner 成员
        Member ownerMember = memberRepository.findByUserId(userId)
            .orElseGet(() -> {
                Member newMember = new Member();
                newMember.setUserId(userId);
                newMember.setJoinedAt(LocalDateTime.now());
                return memberRepository.save(newMember);
            });
        
        // 初始化集合并添加 owner 作为成员
        //project.setMembers(new HashSet<>(Collections.singletonList(ownerMember)));
        project.getMembers().add(ownerMember);
        project = projectRepository.save(project);
        
        // 确保 owner 的 projects 集合也被更新
        ownerMember.getProjects().add(project);
        memberRepository.save(ownerMember);
        
        return convertToProjectDTO(project);
    }

    @Override
    @GlobalTransactional
    public ProjectDTO updateProject(Integer memberId, Integer projectId, ProjectUpdateBody updateBody) {
        // 验证项目是否存在
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证用户是否是项目成员
        validateMembership(projectId, memberId);

        BeanCopyUtils.copyNonNullProperties(updateBody, project);
        return convertToProjectDTO(projectRepository.save(project));
    }

    @Override
    @GlobalTransactional
    public void deleteProject(Integer memberId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证用户是否是项目所有者
        if (!project.getOwnerId().equals(memberId)) {
            throw new RuntimeException("Only project owner can delete the project");
        }
        
        projectRepository.deleteById(projectId);
    }

    @Override
    public List<ProjectDTO> getProjectByOwnerId(Integer ownerId) {
        // 获取该用户作为owner的所有项目
        return projectRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getAllProjects(Integer memberId) {
        // 直接返回所有项目
        return projectRepository.findAll()
                .stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

    @Override
    @GlobalTransactional
    public ProjectDTO addMember(Integer ownerId, Integer projectId, Integer newUserId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证是否是项目所有者
        if (!project.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only project owner can add members");
        }
        
        // 验证新成员是否存在
        userService.getUserById(newUserId);
        
        // 检查成员是否已存在
        if (memberRepository.findByUserIdAndProjectsContaining(newUserId, project).isPresent()) {
            throw new RuntimeException("User is already a member of this project");
        }
        
        // 创建或获取成员
        Member member = memberRepository.findByUserId(newUserId)
            .orElseGet(() -> {
                Member newMember = new Member();
                newMember.setUserId(newUserId);
                newMember.setJoinedAt(LocalDateTime.now());
                return memberRepository.save(newMember);
            });
        
        // 建立双向关联
        project.getMembers().add(member);
        member.getProjects().add(project);
        
        // 保存更改
        project = projectRepository.save(project);
        memberRepository.save(member);
        
        // 返回更新后的项目信息
        return convertToProjectDTO(project);
    }

    @Override
    @GlobalTransactional
    public void removeMember(Integer ownerId, Integer projectId, Integer memberId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        // 验证是否是项目所有者
        if (!project.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only project owner can remove members");
        }
        
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        if (member.getUserId().equals(project.getOwnerId())) {
            throw new RuntimeException("Cannot remove project owner");
        }
        
        project.getMembers().remove(member);
        projectRepository.save(project);
    }

    @Override
    public List<MemberDTO> getProjectMembers(Integer projectId) {
        return memberRepository.findByProjects_Id(projectId)
            .stream()
            .map(this::convertToMemberDTO)
            .collect(Collectors.toList());
    }

    private ProjectDTO convertToProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        BeanUtils.copyProperties(project, dto);
        
        // 添加成员信息
        dto.setMembers(project.getMembers().stream()
            .map(this::convertToMemberDTO)
            .collect(Collectors.toList()));
            
        return dto;
    }

    private MemberDTO convertToMemberDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.setUserId(member.getUserId());
        dto.setJoinedAt(member.getJoinedAt());
        return dto;
    }
}
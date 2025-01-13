import React, { useState, useEffect } from 'react';
import { MemberManagement } from './add-member';
import axios from 'axios';

interface ProjectMember {
    id: number;
    name: string;
    email: string;
    profilePhoto: string | null;
}

interface ProjectMemberRelation {
    userId: number;
    joinedAt: string;
    deleted: boolean;
}

interface UserInfo {
    id: number;
    name: string;
    email: string;
    profilePhoto: string | null;
}

interface ProjectMembersProps {
    projectId: string;
    render?: (members: UserInfo[]) => React.ReactNode;
    displayManagement?: boolean;
    selectedUserId?: number;
}

export const ProjectMembers: React.FC<ProjectMembersProps> = ({
    projectId,
    render,
    selectedUserId,
    displayManagement = true
}) => {
    const [members, setMembers] = useState<UserInfo[]>([]);
    const [memberRelations, setMemberRelations] = useState<ProjectMemberRelation[]>([]);

    // 筛选显示的成员，考虑 deleted 状态
    const displayMembers = members.filter(member => {
        // 如果指定了 selectedUserId，只显示该用户
        if (selectedUserId) {
            return member.id === selectedUserId;
        }

        // 检查成员是否未被删除
        const relation = memberRelations.find(rel => rel.userId === member.id);
        return relation && !relation.deleted;
    });

    const fetchMembers = async () => {
        try {
            // 1. 获取项目成员关系
            const memberResponse = await axios.get<ProjectMemberRelation[]>(
                `/api/projects/${projectId}/members`,
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                        'Content-Type': 'application/json',
                    },
                }
            );

            setMemberRelations(memberResponse.data);

            // 2. 获取所有用户数据
            const usersResponse = await axios.get<UserInfo[]>('/api/users', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                }
            });

            // 3. 匹配并筛选出项目成员的详细信息
            const memberDetails = memberResponse.data
                .filter(member => !member.deleted)  // 只保留未删除的成员
                .map(member => {
                    const userInfo = usersResponse.data.find(user => user.id === member.userId);
                    return userInfo;
                })
                .filter((userInfo): userInfo is UserInfo => userInfo !== undefined);

            setMembers(memberDetails);
        } catch (error) {
            console.error('Error in fetchMembers function:', error);
            setMembers([]);
            setMemberRelations([]);
        }
    };

    useEffect(() => {
        if (projectId) {
            fetchMembers();
        }
    }, [projectId]);

    if (render) {
        return <>{render(displayMembers)}</>;
    }

    return displayManagement ? (
        <MemberManagement
            members={displayMembers
                .filter(member => member && member.id)
                .map(member => ({
                    id: String(member.id),
                    name: member.name || '',
                    avatar: member.profilePhoto || undefined
                })) || []}
            onAddMember={fetchMembers}
        />
    ) : null;
}
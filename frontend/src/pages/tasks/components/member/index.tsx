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

    // 筛选显示的成员
    const displayMembers = selectedUserId
        ? members.filter(member => member.id === selectedUserId)
        : members;

    const fetchMembers = async () => {
        try {
            // 1. 先获取项目成员关系
            const memberResponse = await axios.get<ProjectMemberRelation[]>(
                `/api/projects/${projectId}/members`,
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                        'Content-Type': 'application/json',
                    },
                }
            ).catch(error => {
                console.error('Error fetching members:', error);
                console.error('Error details:', {
                    status: error.response?.status,
                    data: error.response?.data,
                    config: error.config
                });
                throw error;
            });

            console.log('Members Response:', memberResponse.data);

            // 2. 获取所有用户数据
            const usersResponse = await axios.get<UserInfo[]>('/api/users', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Content-Type': 'application/json'
                }
            }).catch(error => {
                console.error('Error fetching users:', error);
                console.error('Error details:', error);
                throw error;
            });

            console.log('Users Response:', usersResponse.data);

            // 3. 匹配并筛选出项目成员的详细信息
            const memberDetails = memberResponse.data
                .filter(member => !member.deleted)
                .map(member => {
                    const userInfo = usersResponse.data.find(user => user.id === member.userId);
                    return userInfo;
                })
                .filter((userInfo): userInfo is UserInfo => userInfo !== undefined);

            console.log('Member details:', memberDetails);
            setMembers(memberDetails);
        } catch (error) {
            console.error('Error in fetchMembers function:', error);
            setMembers([]);
        }
    };

    useEffect(() => {
        if (projectId) {
            fetchMembers();
        }
    }, [projectId]);

    if (render) {
        return <>{render(displayMembers)}</>;  // 使用 displayMembers 而不是 members
    }

    return displayManagement ? (
        <MemberManagement
            members={displayMembers?.filter(member => member && member.id)  // 使用 displayMembers
                .map(member => ({
                    id: String(member.id),
                    name: member.name || '',
                    avatar: member.profilePhoto || undefined
                })) || []}
            onAddMember={fetchMembers}
        />
    ) : null;
};
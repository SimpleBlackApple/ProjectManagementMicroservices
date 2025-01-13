import React, { useState } from 'react';
import { Button, Modal, Tooltip, Space, Form, Input, message } from 'antd';
import { UserAddOutlined } from '@ant-design/icons';
import { CustomAvatar } from './avatar';
import axios from 'axios';
import { useParams } from 'react-router-dom';

interface User {
    id: number;
    name: string;
    email: string;
    profilePhoto: string | null;
}

interface Member {
    id: string;
    name: string;
    avatar?: string;
}

interface ProjectMemberRelation {
    userId: number;
    joinedAt: string;
    deleted: boolean;
}

interface MemberManagementProps {
    members: Member[];
    onAddMember?: (member: Member) => void;
}

export const MemberManagement: React.FC<MemberManagementProps> = ({ members, onAddMember }) => {
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const { id: projectId } = useParams();

    const handleAddMember = async (values: { email: string }) => {
        try {
            setLoading(true);

            // 1. 获取所有用户信息
            const usersResponse = await axios.get('/api/users', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                }
            });

            // 查找匹配的用户
            const user = usersResponse.data.find((user: User) => user.email === values.email);

            if (!user) {
                message.error('This user not registered');
                return;
            }

            // 2. 获取项目成员关系以检查成员状态
            const membersResponse = await axios.get<ProjectMemberRelation[]>(
                `/api/projects/${projectId}/members`,
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    }
                }
            );

            // 检查用户是否已是活跃成员（未被删除）
            const existingMember = membersResponse.data.find(
                member => member.userId === user.id && !member.deleted
            );

            if (existingMember) {
                message.error('User is already a member of this project');
                return;
            }

            // 3. 添加项目成员
            await axios.post(`/api/projects/${projectId}/members/${user.id}`, {}, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                }
            });

            message.success('Team member added successfully');
            form.resetFields();
            setIsModalVisible(false);

            // 如果存在回调，调用它
            if (onAddMember) {
                onAddMember({
                    id: user.id.toString(),
                    name: user.name,
                    avatar: user.profilePhoto || undefined,
                });
            }

        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                message.error(error.response.data || 'Failed to add team member');
            } else {
                message.error('Failed to add team member');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex items-center gap-2">
            <Space size={-4} className="flex items-center">
                {members.map(member => (
                    <Tooltip key={member.id} title={member.name}>
                        <CustomAvatar
                            name={member.name}
                            src={member.avatar}
                            style={{
                                marginLeft: '4px',
                                cursor: 'pointer'
                            }}
                        />
                    </Tooltip>
                ))}
                <Button
                    type="dashed"
                    shape="circle"
                    icon={<UserAddOutlined />}
                    onClick={() => setIsModalVisible(true)}
                    style={{ marginLeft: '8px' }}
                />
            </Space>

            <Modal
                title="Add Team Member"
                open={isModalVisible}
                onCancel={() => {
                    setIsModalVisible(false);
                    form.resetFields();
                }}
                footer={null}
            >
                <Form
                    form={form}
                    onFinish={handleAddMember}
                    layout="vertical"
                >
                    <Form.Item
                        name="email"
                        label="Email"
                        rules={[
                            { required: true, message: 'Please input email!' },
                            { type: 'email', message: 'Please input a valid email!' }
                        ]}
                    >
                        <Input placeholder="Enter member email" />
                    </Form.Item>

                    <Form.Item>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                            <Button onClick={() => {
                                setIsModalVisible(false);
                                form.resetFields();
                            }}>
                                Cancel
                            </Button>
                            <Button type="primary" htmlType="submit" loading={loading}>
                                Add
                            </Button>
                        </div>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
}
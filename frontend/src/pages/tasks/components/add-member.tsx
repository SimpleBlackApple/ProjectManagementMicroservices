import React, { useState } from 'react';
import { Button, Modal, Tooltip, Space } from 'antd';
import { UserAddOutlined } from '@ant-design/icons';
import { CustomAvatar } from '@/components';

interface Member {
    id: string;
    name: string;
    avatar?: string;
}

interface MemberManagementProps {
    members: Member[];
    onAddMember?: (member: Member) => void;
}

export const MemberManagement: React.FC<MemberManagementProps> = ({ members, onAddMember }) => {
    const [isModalVisible, setIsModalVisible] = useState(false);

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
                onCancel={() => setIsModalVisible(false)}
                footer={null}
            >
                {/* Add member form will be implemented here */}
            </Modal>
        </div>
    );
};
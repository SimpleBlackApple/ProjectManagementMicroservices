export interface Project {
  id: number;
  name: string;
  description: string;
  members: Array<{
    id: number;
    name: string;
    avatarUrl?: string;
  }>;
  // 可以根据需要添加更多字段
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  type: 'user_story' | 'bug' | 'task';
  status: 'UNASSIGNED' | 'TO_DO' | 'IN_PROGRESS' | 'DONE';
  projectId: number;
  sprintId?: number | null;
  managerId: number;
  storyPoints: number;
  startDate?: string;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

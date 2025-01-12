declare module 'react-gantt-timeline' {
  import { Component } from 'react';

  export interface TimelineTask {
    id: number | string;
    name: string;
    start: Date;
    end: Date;
    color?: string;
    [key: string]: any;
  }

  export interface TimelineProps {
    data: TimelineTask[];
    links?: any[];
    mode?: 'day' | 'week' | 'month' | 'year';
    itemHeight?: number;
    selectedItem?: number | string;
    onSelectItem?: (item: TimelineTask) => void;
    onUpdateTask?: (item: TimelineTask, start: Date, end: Date) => void;
    onCreateLink?: (item: any) => void;
    onUpdateLink?: (item: any, start: any, end: any) => void;
  }

  export default class TimeLine extends Component<TimelineProps> {}
}
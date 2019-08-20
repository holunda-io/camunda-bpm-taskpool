import {TaskActions, TaskActionTypes} from './task.actions';
import {TaskWithDataEntries} from 'tasklist/models';

export enum SortDirection {
  ASC = '+',
  DESC = '-'
}

export interface Field {
  fieldName: string;
  direction: SortDirection;
}

export interface TaskState {
  sortingColumn: Field;
  page: number;
  taskCount: number;
  tasks: TaskWithDataEntries[];
}

const initialState: TaskState = {
  sortingColumn: {fieldName: 'task.dueDate', direction: SortDirection.DESC },
  page: 0,
  taskCount: 0,
  tasks: []
};

export function taskReducer(state: TaskState = initialState, action: TaskActions): TaskState {
  switch (action.type) {

    case TaskActionTypes.TasksLoaded:
      return {
        ...state,
        tasks: action.payload.tasks,
        taskCount: action.payload.totalCount
      };

    case TaskActionTypes.PageSelected:
      return {
        ...state,
        page: action.payload
      };

    case TaskActionTypes.UpdateSortingColumn:
      return {
        ...state,
        sortingColumn: action.payload
      };

    default:
      return state;
  }
}

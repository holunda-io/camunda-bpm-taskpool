import {TaskActions, TaskActionTypes} from './task.actions';
import {Field} from 'app/services/filter.service';
import {TaskWithDataEntries} from 'tasklist/models';

export interface TaskState {
  sortingColumn: Field;
  taskCount: number;
  tasks: TaskWithDataEntries[];
}

const initialState: TaskState = {
  sortingColumn: null,
  taskCount: 0,
  tasks: []
};

export function taskReducer(state: TaskState = initialState, action: TaskActions): TaskState {
  switch (action.type) {

    case TaskActionTypes.TasksLoaded:
      return {
        ...state,
        tasks: action.payload
      };

    default:
      return state;
  }
}

import {createSelector} from '@ngrx/store';
import {TaskState} from 'app/task/state/task.reducer';
import {TaskWithDataEntries} from 'tasklist/models/task-with-data-entries';

export interface StateWithTasks {
  task: TaskState;
}

const selectFeature = (state: StateWithTasks) => state.task;

export const getTasks = createSelector(
  selectFeature,
  (state: TaskState): TaskWithDataEntries[] => state.tasks
);

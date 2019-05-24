import {Action} from '@ngrx/store';
import {TaskWithDataEntries} from 'tasklist/models/task-with-data-entries';
import {Task} from 'tasklist/models/task';

export enum TaskActionTypes {
  LoadTasks = '[Task] Load tasks',
  TasksLoaded = '[Task] Tasks loaded',
  ClaimTask = '[Task] Claim task',
  TaskClaimed = '[Task] Task claimed',
  UnclaimTask = '[Task] Unclaim task',
  TaskUnclaimed = '[Task] Task unclaimed'
}

export class LoadTasksAction implements Action {
  readonly type = TaskActionTypes.LoadTasks;

  constructor() {
  }
}

export class TasksLoadedAction implements Action {
  readonly type = TaskActionTypes.TasksLoaded;

  constructor(public payload: TaskWithDataEntries[]) {
  }
}

export class ClaimTaskAction implements Action {
  readonly type = TaskActionTypes.ClaimTask;

  constructor(public payload: Task) {}
}

export class TaskClaimedAction implements Action {
  readonly type = TaskActionTypes.TaskClaimed;
}

export class UnclaimTaskAction implements Action {
  readonly type = TaskActionTypes.UnclaimTask;

  constructor(public payload: Task) {}
}

export class TaskUnclaimedAction implements Action {
  readonly type = TaskActionTypes.TaskUnclaimed;
}

export type TaskActions =
  | LoadTasksAction
  | TasksLoadedAction;

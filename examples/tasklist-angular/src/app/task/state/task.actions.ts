import {Action} from '@ngrx/store';
import {TaskWithDataEntries} from 'tasklist/models/task-with-data-entries';
import {Task} from 'tasklist/models/task';
import {Field} from 'app/task/state/task.reducer';

export enum TaskActionTypes {
  LoadTasks = '[Task] Load tasks',
  TasksLoaded = '[Task] Tasks loaded',
  UpdateSortingColumn = '[Task] Update sorting column',
  SelectPage = '[Task] Select page',
  PageSelected = '[Task] Page Selected',
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

export interface PaginatedTasks {
  tasks: TaskWithDataEntries[];
  totalCount: number;
}

export class TasksLoadedAction implements Action {
  readonly type = TaskActionTypes.TasksLoaded;

  constructor(public payload: PaginatedTasks) {
  }
}

export class UpdateSortingColumnAction implements Action {
  readonly type = TaskActionTypes.UpdateSortingColumn;

  constructor(public payload: Field) {
  }
}

export class SelectPageAction implements Action {
  readonly type = TaskActionTypes.SelectPage;

  constructor(public payload: number) {
  }
}

export class PageSelectedAction implements Action {
  readonly type = TaskActionTypes.PageSelected;

  constructor(public payload: number) {
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
  | TasksLoadedAction
  | UpdateSortingColumnAction
  | SelectPageAction
  | PageSelectedAction
  | ClaimTaskAction
  | TaskClaimedAction
  | UnclaimTaskAction
  | TaskUnclaimedAction;

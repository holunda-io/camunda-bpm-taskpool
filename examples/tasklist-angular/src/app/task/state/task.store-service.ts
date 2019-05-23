import {Injectable} from '@angular/core';
import {Dispatch, StoreService} from '@ngxp/store-service';
import {ClaimTaskAction, LoadTasksAction, UnclaimTaskAction} from './task.actions';
import {TaskState} from 'app/task/state/task.reducer';
import {Task} from 'tasklist/models';

@Injectable()
export class TaskStoreService extends StoreService<TaskState> {

  @Dispatch(LoadTasksAction)
  loadTasks: () => void;

  @Dispatch(ClaimTaskAction)
  claimTask: (task: Task) => void;

  @Dispatch(UnclaimTaskAction)
  unclaimTask: (task: Task) => void;
}

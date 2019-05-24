import {Injectable} from '@angular/core';
import {Dispatch, Select, StoreService} from '@ngxp/store-service';
import {ClaimTaskAction, LoadTasksAction, UnclaimTaskAction} from './task.actions';
import {TaskState} from 'app/task/state/task.reducer';
import {Task, TaskWithDataEntries} from 'tasklist/models';
import {getTasks} from 'app/task/state/task.selectors';
import {Observable} from 'rxjs';

@Injectable()
export class TaskStoreService extends StoreService<TaskState> {

  @Dispatch(LoadTasksAction)
  loadTasks: () => void;

  @Dispatch(ClaimTaskAction)
  claimTask: (task: Task) => void;

  @Dispatch(UnclaimTaskAction)
  unclaimTask: (task: Task) => void;

  @Select(getTasks)
  tasks: () => Observable<TaskWithDataEntries[]>;
}

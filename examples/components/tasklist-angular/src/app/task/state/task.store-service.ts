import {Injectable} from '@angular/core';
import {Dispatch, Select, StoreService} from '@ngxp/store-service';
import {ClaimTaskAction, LoadTasksAction, SelectPageAction, UnclaimTaskAction, UpdateSortingColumnAction} from './task.actions';
import {Field, TaskState} from 'app/task/state/task.reducer';
import {Task, TaskWithDataEntries} from 'tasklist/models';
import {getCount, getSelectedPage, getSortingColumn, getTasks} from 'app/task/state/task.selectors';
import {Observable} from 'rxjs';

@Injectable()
export class TaskStoreService extends StoreService<TaskState> {

  @Dispatch(LoadTasksAction)
  loadTasks: () => void;

  @Dispatch(UpdateSortingColumnAction)
  updateSortingColumn: (field: Field) => void;

  @Dispatch(SelectPageAction)
  selectPage: (page: number) => void;

  @Dispatch(ClaimTaskAction)
  claim: (task: Task) => void;

  @Dispatch(UnclaimTaskAction)
  unclaim: (task: Task) => void;

  @Select(getTasks)
  tasks: () => Observable<TaskWithDataEntries[]>;

  @Select(getSortingColumn)
  sortingColumn$: () => Observable<Field>;

  @Select(getCount)
  taskCount$: () => Observable<number>;

  @Select(getSelectedPage)
  selectedPage$: () => Observable<number>;
}

import {Injectable} from '@angular/core';
import {TaskService} from 'tasklist/services';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {UserStoreService} from 'app/user/state/user.store-service';
import {
  ClaimTaskAction,
  LoadTasksAction,
  PageSelectedAction,
  SelectPageAction,
  TaskActionTypes, TaskClaimedAction,
  TasksLoadedAction, TaskUnclaimedAction, UnclaimTaskAction
} from 'app/task/state/task.actions';
import {catchError, filter, flatMap, map, withLatestFrom} from 'rxjs/operators';
import {SelectUserAction, UserActionTypes} from 'app/user/state/user.actions';
import {of} from 'rxjs';
import {TaskStoreService} from 'app/task/state/task.store-service';

@Injectable()
export class TaskEffects {
  public constructor(
    private taskService: TaskService,
    private userStore: UserStoreService,
    private taskStore: TaskStoreService,
    private actions$: Actions) {
  }

  @Effect()
  loadTasksOnUserSelect$ = this.actions$.pipe(
    ofType<SelectUserAction>(UserActionTypes.SelectUser),
    filter((action) => !!action.payload),
    map(() => new LoadTasksAction())
  );

  @Effect()
  loadTasksOnSortingChange = this.actions$.pipe(
    ofType(TaskActionTypes.UpdateSortingColumn),
    map(() => new LoadTasksAction())
  );

  @Effect()
  loadTasks$ = this.actions$.pipe(
    ofType(TaskActionTypes.LoadTasks),
    withLatestFrom(this.userStore.userId$()),
    flatMap(([_, userId]) =>
      this.taskService.getTasks$Response({
        'X-Current-User-ID': userId
      })),
    map((response) => new TasksLoadedAction({
      tasks: response.body,
      totalCount: Number(response.headers.get('X-ElementCount'))
    })),
    catchError(err => {
      console.log('Error loading tasks:', err);
      return of();
    })
  );

  @Effect()
  selectPage$ = this.actions$.pipe(
    ofType<SelectPageAction>(TaskActionTypes.SelectPage),
    map(action => action.payload),
    withLatestFrom(this.taskStore.selectedPage$()),
    filter(([newPage, currentPage]) => newPage !== currentPage),
    map(([newPage, _]) => new PageSelectedAction(newPage))
  );

  @Effect()
  claimTask$ = this.actions$.pipe(
    ofType<ClaimTaskAction>(TaskActionTypes.ClaimTask),
    map(action => action.payload),
    withLatestFrom(this.userStore.userId$()),
    flatMap(([task, userId]) => this.taskService.claim({id: task.id, 'X-Current-User-ID': userId})),
    map(() => new TaskClaimedAction()),
    catchError(err => {
      console.log('Error while claiming task', err);
      return of();
    })
  );

  @Effect()
  unclaimTask$ = this.actions$.pipe(
    ofType<UnclaimTaskAction>(TaskActionTypes.UnclaimTask),
    map(action => action.payload),
    withLatestFrom(this.userStore.userId$()),
    flatMap(([task, userId]) => this.taskService.unclaim({id: task.id, 'X-Current-User-ID': userId})),
    map(() => new TaskUnclaimedAction()),
    catchError(err => {
      console.log('Error while unclaiming task', err);
      return of();
    })
  );

  @Effect()
  reloadTasks$ = this.actions$.pipe(
    ofType(TaskActionTypes.TaskClaimed, TaskActionTypes.TaskUnclaimed),
    map(() => new LoadTasksAction())
  );
}

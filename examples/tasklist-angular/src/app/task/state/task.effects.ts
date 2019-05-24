import {Injectable} from '@angular/core';
import {TaskService} from 'tasklist/services';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {UserStoreService} from 'app/user/state/user.store-service';
import {LoadTasksAction, PageSelectedAction, SelectPageAction, TaskActionTypes, TasksLoadedAction} from 'app/task/state/task.actions';
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
    flatMap(([_,userId]) =>
      this.taskService.getTasksResponse({
        filter: [''],
        page: 0,
        size: 10,
        sort: '',
        XCurrentUserID: userId
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
    filter(([newPage,currentPage]) => newPage !== currentPage),
    map(([newPage,_]) => new PageSelectedAction(newPage))
  );
}

import {Injectable} from '@angular/core';
import {TaskService} from 'tasklist/services';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {UserStoreService} from 'app/user/state/user.store-service';
import {LoadTasksAction, TaskActionTypes, TasksLoadedAction} from 'app/task/state/task.actions';
import {catchError, filter, flatMap, map, withLatestFrom} from 'rxjs/operators';
import {SelectUserAction, UserActionTypes} from 'app/user/state/user.actions';
import {of} from 'rxjs';

@Injectable()
export class TaskEffects {

  public constructor(
    private taskService: TaskService,
    private userStore: UserStoreService,
    private actions$: Actions) {
  }

  @Effect()
  loadTasksOnUserSelect$ = this.actions$.pipe(
    ofType<SelectUserAction>(UserActionTypes.SelectUser),
    filter((action) => !!action.payload),
    map(() => new LoadTasksAction())
  );

  @Effect()
  loadTasks$ = this.actions$.pipe(
    ofType(TaskActionTypes.LoadTasks),
    withLatestFrom(this.userStore.userId$()),
    flatMap(([_,userId]) =>
      this.taskService.getTasks({
        filter: [''],
        page: 0,
        size: 10,
        sort: '',
        XCurrentUserID: userId
      })),
    map(tasks => new TasksLoadedAction(tasks)),
    catchError(err => {
      console.log('Error loading tasks:', err);
      return of();
    })
  )
}

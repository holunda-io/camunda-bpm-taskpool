import {Injectable} from '@angular/core';
import {TaskService} from 'tasklist/services';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {UserStoreService} from 'app/user/state/user.store-service';
import {TaskActionTypes, TasksLoadedAction} from 'app/task/state/task.actions';
import {filter, flatMap, map, withLatestFrom} from 'rxjs/operators';

@Injectable()
export class TaskEffects {

  public constructor(
    private taskService: TaskService,
    private userStore: UserStoreService,
    private actions$: Actions) {
  }

  @Effect()
  loadTasks$ = this.actions$.pipe(
    ofType(TaskActionTypes.LoadTasks),
    withLatestFrom(this.userStore.userId$()),
    filter(([_, userId]) => !!userId),
    flatMap(([_, userId]) => {
      console.log('test');
      return this.taskService.getTasks({
        filter: [],
        page: 0,
        size: 10,
        sort: '',
        XCurrentUserID: userId
      });
    }),
    map(tasks => {
      console.log('foo');
      return new TasksLoadedAction(tasks);
    })
  );
}

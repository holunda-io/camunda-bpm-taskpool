import {TaskEffects} from './task.effects';
import {Action} from '@ngrx/store';
import {of} from 'rxjs';
import {Actions} from '@ngrx/effects';
import {TaskService} from 'tasklist/services';
import {UserStoreService} from 'app/user/state/user.store-service';
import {createStoreServiceMock} from '@ngxp/store-service/testing';
import {LoadTasksAction, TasksLoadedAction} from 'app/task/state/task.actions';
import {SelectUserAction} from 'app/user/state/user.actions';

describe('TaskEffects', () => {

  let taskService: TaskService;
  let userStore: UserStoreService;

  beforeEach(() => {
    taskService = new TaskService(null, null);
    // default user store to be overridden in test if needed.
    userStore = createStoreServiceMock(UserStoreService,
      {userId$: 'kermit'});
  });

  function effectsFor(action: Action): TaskEffects {
    return new TaskEffects(taskService, userStore, new Actions(of(action)));
  }

  it('should trigger loading tasks on user select', (done) => {
    // given:
    const action = new SelectUserAction('kermit');

    // when:
    effectsFor(action).loadTasksOnUserSelect$.subscribe((newAction) => {
      expect(newAction).toEqual(new LoadTasksAction());
      done();
    });
  });

  it('should load tasks', (done) => {
    // given:
    const action = new LoadTasksAction();
    const spy = spyOn(taskService, 'getTasks').and.returnValue(of([]));

    // when:
    effectsFor(action).loadTasks$.subscribe(newAction => {
      expect(newAction).toEqual(new TasksLoadedAction([]));
      expect(spy).toHaveBeenCalled();
      done();
    });
  });
});

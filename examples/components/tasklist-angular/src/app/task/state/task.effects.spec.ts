import {TaskEffects} from './task.effects';
import {Action} from '@ngrx/store';
import {of} from 'rxjs';
import {Actions} from '@ngrx/effects';
import {TaskService} from 'tasklist/services';
import {UserStoreService} from 'app/user/state/user.store-service';
import {createStoreServiceMock} from '@ngxp/store-service/testing';
import {LoadTasksAction, PageSelectedAction, SelectPageAction, TasksLoadedAction} from 'app/task/state/task.actions';
import {SelectUserAction} from 'app/user/state/user.actions';
import {TaskStoreService} from 'app/task/state/task.store-service';

describe('TaskEffects', () => {

  let taskService: TaskService;
  let userStore: UserStoreService;
  let taskStore: TaskStoreService;

  beforeEach(() => {
    taskService = new TaskService(null, null);
    // default user store to be overridden in test if needed.
    userStore = createStoreServiceMock(UserStoreService,
      {userId$: 'kermit'});
    taskStore = createStoreServiceMock(TaskStoreService);
  });

  function effectsFor(action: Action): TaskEffects {
    return new TaskEffects(taskService, userStore, taskStore, new Actions(of(action)));
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
    const spy = spyOn(taskService, 'getTasks$Response').and.returnValue(of({body: [],
    headers: { get: (field: string) => '0'}} as any));

    // when:
    effectsFor(action).loadTasks$.subscribe(newAction => {
      expect(newAction).toEqual(new TasksLoadedAction({tasks: [], totalCount: 0}));
      expect(spy).toHaveBeenCalled();
      done();
    });
  });

  it('should update selected page', (done) => {
    // given:
    const action = new SelectPageAction(1);
    taskStore = createStoreServiceMock(TaskStoreService, {
      selectedPage$: 0
    });

    effectsFor(action).selectPage$.subscribe((newAction) => {
      expect(newAction).toEqual(new PageSelectedAction(1));
      done();
    });
  });
});

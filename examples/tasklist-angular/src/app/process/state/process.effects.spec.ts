import {ProcessEffects} from './process.effects';
import {Action} from '@ngrx/store';
import {of} from 'rxjs';
import {Actions} from '@ngrx/effects';
import {ProcessService} from 'tasklist/services';
import {UserStoreService} from 'app/user/state/user.store-service';
import {createStoreServiceMock} from '@ngxp/store-service/testing';
import {LoadStartableProcessDefinitions} from 'app/process/state/process.actions';

describe('ProcessEffects', () => {

  let processService: ProcessService;
  let userStore: UserStoreService;

  beforeEach(() => {
    processService = new ProcessService(null, null);
    // default user store to be overridden in test if needed.
    userStore = createStoreServiceMock(UserStoreService,
      {userId$: 'kermit'});
  });

  function effectsFor(action: Action): ProcessEffects {
    return new ProcessEffects(processService, userStore, new Actions(of(action)));
  }

  it('should load available users', (done) => {
    // given:
    const action = new LoadStartableProcessDefinitions();
    const procDtos = [
      {processName: 'foo', description: '', url: ''},
      {processName: 'bar', description: '', url: ''}
    ];
    const serviceSpy = spyOn(processService, 'getStartableProcesses').and.returnValue(of(procDtos));

    // when:
    effectsFor(action).loadStartableProcesses$.subscribe((newAction) => {
      expect(newAction.payload).toEqual([
        {name: 'foo', description: '', url: ''},
        {name: 'bar', description: '', url: ''}
      ]);
      done();
    });
  });
});

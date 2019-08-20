import {Injectable} from '@angular/core';
import {Dispatch, Select, StoreService} from '@ngxp/store-service';
import {ProcessDefinition, ProcessState} from './process.reducer';
import {startableProcesses} from './process.selectors';
import {Observable} from 'rxjs';
import {LoadStartableProcessDefinitions} from './process.actions';

@Injectable()
export class ProcessStoreService extends StoreService<ProcessState> {

  @Select(startableProcesses)
  startableProcesses$: () => Observable<ProcessDefinition[]>;

  @Dispatch(LoadStartableProcessDefinitions)
  loadStartableProcessDefinitions: () => void;
}

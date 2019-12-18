import {Injectable} from '@angular/core';
import {Dispatch, Select, StoreService} from '@ngxp/store-service';
import {DataEntry, DataEntryState} from './dataentry.reducer';
import {dataEntries} from './dataentry.selectors';
import {Observable} from 'rxjs';
import {LoadDataEntries} from './dataentry.actions';

@Injectable()
export class DataentryStoreService extends StoreService<DataEntryState> {

  @Select(dataEntries)
  dataEntries$: () => Observable<DataEntry[]>;

  @Dispatch(LoadDataEntries)
  loadDataEntries: () => void;
}

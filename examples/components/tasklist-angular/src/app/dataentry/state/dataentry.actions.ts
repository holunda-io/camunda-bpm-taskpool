import {Action} from '@ngrx/store';
import {DataEntry} from 'app/dataentry/state/dataentry.reducer';

export enum DataEntryActionTypes {
  LoadDataEntries = '[Data Entry] Load data entries',
  DataEntriesLoaded = '[Data Entry] Data entries loaded',
}

export class LoadDataEntries implements Action {
  readonly type = DataEntryActionTypes.LoadDataEntries;

  constructor() {
  }
}

export class DataEntriesLoaded implements Action {
  readonly type = DataEntryActionTypes.DataEntriesLoaded;

  constructor(public dataEntries: DataEntry[]) {
  }
}

export type DataEntryActions =
  | LoadDataEntries
  | DataEntriesLoaded;

import {DataEntryActions, DataEntryActionTypes, LoadDataEntries} from './dataentry.actions';
import {ProcessState} from 'app/process/state/process.reducer';
import {ProcessActionTypes} from 'app/process/state/process.actions';

export interface DataEntry {
  name: string;
  description?: string;
  type: string;
  payload: {};
  url: string;
  state: string;
  stateType: string;
  protocol: ProtocolEntry[];
}

export interface ProtocolEntry {
  timestamp?: string;
  user?: string;
  log?: string;
  logDetails?: string;
}

export interface DataEntryState {
  dataEntries: DataEntry[];
}

const initialState: DataEntryState = {
  dataEntries: []
};

export function dataentryReducer(state: DataEntryState = initialState, action: DataEntryActions): DataEntryState {
  switch (action.type) {

    case DataEntryActionTypes.DataEntriesLoaded:
      return {
        ...state,
        dataEntries: action.dataEntries
      };

    default:
      return state;
  }
}

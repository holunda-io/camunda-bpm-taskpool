import {DataEntryActions, DataEntryActionTypes, LoadDataEntries} from './dataentry.actions';

export interface DataEntry {
  name: string;
  description?: string;
  type: string;
  payload: {};
  url: string;
  currentState: string;
  currentStateType: string;
  protocol: ProtocolEntry[];
}

export interface ProtocolEntry {
  timestamp?: string;
  user?: string;
  state?: string;
  stateType?: string;
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

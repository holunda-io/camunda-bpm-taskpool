import {ProcessActions, ProcessActionTypes} from './process.actions';

export interface ProcessDefinition {
  name: string;
  description: string;
  url: string;
}

export interface ProcessState {
  startableProcesses: ProcessDefinition[];
}

const initialState: ProcessState = {
  startableProcesses: []
};

export function processReducer(state: ProcessState = initialState, action: ProcessActions): ProcessState {
  switch (action.type) {

    case ProcessActionTypes.StartableProcessesLoaded:
      return {
        ...state,
        startableProcesses: action.payload
      };

    default:
      return state;
  }
}

import {Action} from '@ngrx/store';
import {ProcessDefinition} from 'app/process/state/process.reducer';

export enum ProcessActionTypes {
  LoadStartableProcesses = '[Process] Load startable processes',
  StartableProcessesLoaded = '[Process] Startable processes loaded',
}

export class LoadStartableProcessDefinitions implements Action {
  readonly type = ProcessActionTypes.LoadStartableProcesses;

  constructor() {
  }
}

export class StartableProcessDefinitionsLoaded implements Action {
  readonly type = ProcessActionTypes.StartableProcessesLoaded;

  constructor(public payload: ProcessDefinition[]) {
  }
}

export type ProcessActions =
  | LoadStartableProcessDefinitions
  | StartableProcessDefinitionsLoaded;
